package com.cqcsy.lgsp.push

import android.content.Context
import android.content.Intent
import android.util.Log
import cn.jpush.android.api.*
import cn.jpush.android.service.JPushMessageReceiver

/**
 * 自定义推送消息接受处理
 */
class PushReceiver: JPushMessageReceiver() {

    private val TAG = "PushMessageReceiver"

    override fun onMessage(
        context: Context,
        customMessage: CustomMessage
    ) {
        Log.e(TAG, "[onMessage] $customMessage")
        processCustomMessage(context, customMessage)
    }

    override fun onNotifyMessageOpened(
        context: Context,
        message: NotificationMessage
    ) {
        Log.e(TAG, "[onNotifyMessageOpened] $message")
//        try { //打开自定义的Activity
//            val i = Intent(context, TestActivity::class.java)
//            val bundle = Bundle()
//            bundle.putString(JPushInterface.EXTRA_NOTIFICATION_TITLE, message.notificationTitle)
//            bundle.putString(JPushInterface.EXTRA_ALERT, message.notificationContent)
//            i.putExtras(bundle)
//            //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            context.startActivity(i)
//        } catch (throwable: Throwable) {
//        }
    }

    override fun onMultiActionClicked(context: Context?, intent: Intent) {
        Log.e(TAG, "[onMultiActionClicked] 用户点击了通知栏按钮")
        val nActionExtra =
            intent.extras!!.getString(JPushInterface.EXTRA_NOTIFICATION_ACTION_EXTRA)
        //开发者根据不同 Action 携带的 extra 字段来分配不同的动作。
        if (nActionExtra == null) {
            Log.d(TAG, "ACTION_NOTIFICATION_CLICK_ACTION nActionExtra is null")
            return
        }
        if (nActionExtra == "my_extra1") {
            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮一")
        } else if (nActionExtra == "my_extra2") {
            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮二")
        } else if (nActionExtra == "my_extra3") {
            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮三")
        } else {
            Log.e(TAG, "[onMultiActionClicked] 用户点击通知栏按钮未定义")
        }
    }

    override fun onNotifyMessageArrived(
        context: Context?,
        message: NotificationMessage
    ) {
        Log.e(TAG, "[onNotifyMessageArrived] $message")
    }

    override fun onNotifyMessageDismiss(
        context: Context?,
        message: NotificationMessage
    ) {
        Log.e(TAG, "[onNotifyMessageDismiss] $message")
    }

    override fun onRegister(
        context: Context?,
        registrationId: String
    ) {
        Log.e(TAG, "[onRegister] $registrationId")
    }

    override fun onConnected(
        context: Context?,
        isConnected: Boolean
    ) {
        Log.e(TAG, "[onConnected] $isConnected")
    }

    override fun onCommandResult(
        context: Context?,
        cmdMessage: CmdMessage
    ) {
        Log.e(TAG, "[onCommandResult] $cmdMessage")
    }

    override fun onTagOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        TagAliasOperatorHelper.instance?.onTagOperatorResult(context, jPushMessage)
        super.onTagOperatorResult(context, jPushMessage)
    }

    override fun onCheckTagOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        TagAliasOperatorHelper.instance?.onCheckTagOperatorResult(context, jPushMessage)
        super.onCheckTagOperatorResult(context, jPushMessage)
    }

    override fun onAliasOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        TagAliasOperatorHelper.instance?.onAliasOperatorResult(context, jPushMessage)
        super.onAliasOperatorResult(context, jPushMessage)
    }

    override fun onMobileNumberOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        TagAliasOperatorHelper.instance?.onMobileNumberOperatorResult(context, jPushMessage)
        super.onMobileNumberOperatorResult(context, jPushMessage)
    }

    //send msg to MainActivity
    private fun processCustomMessage(
        context: Context,
        customMessage: CustomMessage
    ) {
//        if (MainActivity.isForeground) {
//            val message = customMessage.message
//            val extras = customMessage.extra
//            val msgIntent = Intent(MainActivity.MESSAGE_RECEIVED_ACTION)
//            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message)
//            if (!ExampleUtil.isEmpty(extras)) {
//                try {
//                    val extraJson = JSONObject(extras)
//                    if (extraJson.length() > 0) {
//                        msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras)
//                    }
//                } catch (e: JSONException) {
//                }
//            }
//            LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent)
//        }
    }

    override fun onNotificationSettingsCheck(
        context: Context?,
        isOn: Boolean,
        source: Int
    ) {
        super.onNotificationSettingsCheck(context, isOn, source)
        Log.e(TAG, "[onNotificationSettingsCheck] isOn:$isOn,source:$source")
    }
}