package com.cqcsy.lgsp.main

import android.content.Intent
import android.media.SoundPool
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.bean.UserStatusChangeBean
import com.cqcsy.lgsp.event.BarrageEvent
import com.cqcsy.lgsp.event.BarrageType
import com.cqcsy.lgsp.login.AccountHelper
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.network.BaseUrl
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * socket处理
 */
class SocketClient {

    companion object {
        private const val joinVideoRoom = "joinVideoRoom"     // 加入视频聊天页面（弹幕）
        private const val leaveVideoRoom = "leaveVideoRoom"   // 离开视频聊天页面（退出）
        private var mSocket: Socket? = null
        private var roomId: String? = null
        private var socketClient: SocketClient? = null

        private const val socketVersion = "1.0.3"

        private const val initUser = "conn"       // 初始化用户状态监听

        /**
         * 进入观看视频页面调用
         */
        fun joinVideoRoom(roomId: String) {
            if (mSocket == null) {
                this.roomId = roomId
                socketClient = SocketClient()
                socketClient?.connect()
            } else {
                println("SocketService==joinVideoRoom==$roomId")
                val jsonObject = JSONObject()
                jsonObject.put("uniqueKey", roomId)
                mSocket?.emit(joinVideoRoom, jsonObject)
                this.roomId = null
            }
        }

        /**
         * 退出观看\切换视频页面调用
         */
        fun leaveVideoRoom(roomId: String) {
            if (mSocket != null) {
                println("SocketService==leaveVideoRoom==$roomId")
                val jsonObject = JSONObject()
                jsonObject.put("uniqueKey", roomId)
                mSocket?.emit(leaveVideoRoom, jsonObject)
            }
        }

        fun userLogin() {
            if (!GlobalValue.isLogin()) {
                return
            }
            if (mSocket == null) {
                socketClient = SocketClient()
                socketClient?.connect()
                return
            }
            val jsonObject = JSONObject(Gson().toJson(GlobalValue.userInfoBean!!.token))
            jsonObject.put("version", socketVersion)
            mSocket?.emit(initUser, jsonObject)
        }

        fun disconnect() {
            socketClient?.disconnect()
        }

    }

    private val userStatusMethod = "userLiveTest"   // 多端登录监听
    private val userStatusChangeMethod = "userStateChange"   // 用户状态监听
    private val barrageReceiverMethod = "sendBarrage"   // 接收弹幕监听
    private val privateMessageMethod = "privateMessage"   // 私信监听

    private fun connect() {
        initSocket()
    }

    private fun disconnect() {
        mSocket?.disconnect()
        mSocket = null
        roomId = null
    }

    private fun initSocket() {
        val option = IO.Options()
        option.transports = arrayOf("websocket")
        option.query = "uid=${GlobalValue.userInfoBean?.id}"
        mSocket = IO.socket(BaseUrl.SOCKET_URL, option)
        mSocket?.on(Socket.EVENT_CONNECT) {
            println("SocketService==EVENT_CONNECT")
            if (!roomId.isNullOrEmpty()) {
                joinVideoRoom(roomId!!)
            }
            userLogin()
        }
        mSocket?.on(Socket.EVENT_DISCONNECT) {
            println("SocketService==EVENT_DISCONNECT")
            disconnect()
        }
//        mSocket?.on(Socket.EVENT_PING) {
//            println("SocketService==EVENT_PING")
//        }
        mSocket?.on(Socket.EVENT_CONNECT_ERROR) {
            println("SocketService==EVENT_CONNECT_ERROR")
            println(it[0].toString())
        }
//        mSocket?.on(Socket.EVENT_CONNECT_TIMEOUT) {
//            println("SocketService==EVENT_CONNECT_TIMEOUT")
//        }
        // 初始化用户
        mSocket?.on(initUser) {
            println("SocketService==initUser")
            println(it[0].toString())
        }
        // 用户登录状态监听
        mSocket?.on(userStatusMethod) {
            println("SocketService==userStatusMethod")
            println(it[0].toString())
            returnToHome()
            showLoginOutTips()
            mSocket?.disconnect()
            AccountHelper.logout()
            disconnect()
        }
        // 用户状态监听
        mSocket?.on(userStatusChangeMethod) {
            println("SocketService==userStatusChangeMethod")
            if (!it.isNullOrEmpty()) {
                println(it[0].toString())
                val bean = GsonUtils.fromJson(it[0].toString(), UserStatusChangeBean::class.java)
                when (bean.action) {
                    0 -> {
                        EventBus.getDefault().post(bean)
                    }
                    1 -> {
                        ToastUtils.showShort(bean.context)
                        AccountHelper.logout()
                        disconnect()
                    }
                    2 -> {

                    }
                }
            }
        }
        // 私信监听
        mSocket?.on(privateMessageMethod) {
            Log.e("privateMessageMethod1", "${ThreadUtils.isMainThread()}")
            println("SocketService==privateMessageMethod")
            println(it[0].toString())
            val value = it[0].toString()
            val chatMessageBean = Gson().fromJson<ChatMessageBean>(
                value,
                object : TypeToken<ChatMessageBean>() {}.type
            )
            EventBus.getDefault().post(chatMessageBean)
            if (chatMessageBean.isRemind == 1) {
                playMessageRingtone()
            }
        }
        // 弹幕监听
        mSocket?.on(barrageReceiverMethod) {
            println("SocketService==barrageReceiverMethod")
            println(it[0].toString())
            try {
                val json = JSONObject(it[0].toString())
                val event = BarrageEvent()
                event.eventType =
                    if (json.optInt("type") == 1) BarrageType.EVENT_CHAT else BarrageType.EVENT_BARRAGE
                event.message = Gson().fromJson<BarrageBean>(
                    it[0].toString(),
                    object : TypeToken<BarrageBean>() {}.type
                )
                EventBus.getDefault().post(event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 加入房间返回数据监听
        mSocket?.on(joinVideoRoom) {
            println("SocketService==joinVideoRoom===${it[0]}")
            chatCountChange(it[0].toString())
        }
        // 离开房间返回数据监听
        mSocket?.on(leaveVideoRoom) {
            println("SocketService==leaveVideoRoom===${it[0]}")
            chatCountChange(it[0].toString())
        }
        mSocket?.connect()
        println("SocketService==create")
    }

    private fun showLoginOutTips() {
        val intent = Intent(Utils.getApp(), LoginOutTipActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Utils.getApp().startActivity(intent)
    }

    private fun returnToHome() {
        val intent = Intent(Utils.getApp(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        intent.putExtra("position", 0)
        Utils.getApp().startActivity(intent)
    }

    private fun chatCountChange(response: String) {
        try {
            val json = JSONObject(response)
            val event = BarrageEvent()
            event.eventType = BarrageType.EVENT_ONLINE
            event.onlineNumber = json.optInt("onlineCount")
            EventBus.getDefault().post(event)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playMessageRingtone() {
        val soundPool = SoundPool.Builder().build()
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {
                soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
            }
        }
        soundPool.load(Utils.getApp(), R.raw.message_tip, 0)
    }
}