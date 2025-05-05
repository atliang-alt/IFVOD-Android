package com.cqcsy.lgsp.upper.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.main.mine.AccountSettingActivity
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.utils.EmotionItemClickUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.lgsp.views.emotion.MessageEmotionFragment
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.LoadingRecyclerView
import com.cqcsy.library.views.MessageToast
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import com.scwang.smartrefresh.layout.constant.RefreshState
import kotlinx.android.synthetic.main.activity_chat.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Exception

/**
 * 私聊
 */
class ChatActivity : NormalActivity(), EmotionItemClickUtils.OnEmotionChange {
    companion object {
        // chatType=0时，必传参数
        const val NICK_NAME = "nickName"
        const val USER_IMAGE = "userImage"
        const val USER_ID = "userId"

        // chatType!=0时，可选参数
        const val SEND_MODEL_MESSAGE = "sendModelMessage"
        const val CHAT_TYPE = "chatType"

        // 后端对应1-0-影院客服，2-1-tgc客服，3-2-充值客服
        const val TYPE_NORMAL_CHAT = 0      // 普通聊天
        const val TYPE_SERVER_NORMAL = 1    // 影院客服/普通客服
        const val TYPE_SERVER_TGC = 2       // TGC客服/聚合支付
        const val TYPE_SERVER_CHARGE = 3    // 充值客服/人工客服
    }

    var userId = ""
    var nickName = ""
    var userImage = ""
    var modelMessage = ""
    var chatType = TYPE_NORMAL_CHAT    // 0-普通聊天  1-普通客服  2-tgc客服   3-充值客服。
    var messageList: MutableList<ChatMessageBean> = ArrayList()
    var page = 1
    val pageSize = 20
    var startId = 0
    private var emotionFragment: MessageEmotionFragment? = null
//    private var serviceTemple: String = ""   // 客服消息模版

    private var hasMoreRecord = true

    override fun onCreate(savedInstanceState: Bundle?) {
        getIntentData()
        super.onCreate(savedInstanceState)
    }

    override fun getContainerView(): Int {
        return R.layout.activity_chat
    }

    override fun onViewCreate() {
        super.onViewCreate()

        setRefresh()
        setEmoji()
        setListener()
        if (chatType == TYPE_NORMAL_CHAT) {
            setHeaderTitle(nickName)
            setRightImage(R.mipmap.icon_setting)
            getChatMessage()
        } else {
            setViewEnable(false)
            getCustomerService()
        }
    }


    private fun getIntentData() {
        if (intent.getStringExtra(NICK_NAME) != null) {
            nickName = intent.getStringExtra(NICK_NAME)!!
        }
        if (intent.getStringExtra(USER_IMAGE) != null) {
            userImage = intent.getStringExtra(USER_IMAGE)!!
        }
        if (intent.getStringExtra(USER_ID) != null) {
            userId = intent.getStringExtra(USER_ID)!!
            PictureUploadManager.removeTaskBackgroundRequest(userId)
        }
        if (intent.getStringExtra(SEND_MODEL_MESSAGE) != null) {
            modelMessage = intent.getStringExtra(SEND_MODEL_MESSAGE)!!
        }
        chatType = intent.getIntExtra(CHAT_TYPE, TYPE_NORMAL_CHAT)
    }

    private fun setListener() {
        inputMsg.setOnClickListener {
            hideFragment()
            KeyboardUtils.showSoftInput(inputMsg)
        }
        inputMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val tag = try {
                    sendMsg.tag.toString().toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
                val isEnable = !s.isNullOrEmpty() || tag > 0
                sendMsg.visibility = if (isEnable) View.VISIBLE else View.GONE
                sendMsg.isEnabled = isEnable
                sendImage.visibility = if (isEnable) View.GONE else View.VISIBLE
//                if (s != null)
//                    inputMsg.setSelection(s.length)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        messageRecord.setOnTouchListener { v, event ->
            KeyboardUtils.hideSoftInput(inputMsg)
            hideFragment()
            false
        }
        setSoftListener()
    }

    private fun setEmoji() {
        // 点击表情的全局监听管理类 绑定EditText
        EmotionItemClickUtils.instance.attachToEditText(inputMsg)
        // 点击VIP表情的全局监听管理类 绑定LinearLayout
        EmotionItemClickUtils.instance.attachToEditText(null, ArrayList(), object : CommentEditDialog.SendCommentListener {
            override fun sendComment(
                type: Int,
                inputText: String,
                vipList: MutableList<String>
            ) {
                if (type == 1) {
                    val openVipDialog = OpenVipDialog()
                    val bundle = Bundle()
                    bundle.putString("pathInfo", this.javaClass.simpleName)
                    openVipDialog.arguments = bundle
                    openVipDialog.show(supportFragmentManager, "openVipDialog")
                    return
                }
                sendMessage(inputText)
            }
        }, this)
        emojiImg.setOnClickListener {
            KeyboardUtils.hideSoftInput(inputMsg)
            addEmojiFragment()
        }
    }

    private fun setSoftListener() {
        KeyboardUtils.registerSoftInputChangedListener(this) { height ->
            if (height > 0) {
                scrollToLast()
            }
        }
    }

    private fun setMessageRead(fromUid: Int) {
        val params = HttpParams()
        params.put("FromUid", fromUid)
        params.put("MessageType", 1)
        HttpRequest.post(RequestUrls.CHAT_READ_STATUS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params, this)
    }

    override fun onDestroy() {
        KeyboardUtils.unregisterSoftInputChangedListener(window)
        super.onDestroy()
    }

    override fun onBackPressed() {
        val params = HttpParams()
        params.put("imageKey", "Code")
        params.put("ToUid", userId)
        params.put("MessageType", 8)
        PictureUploadManager.setTaskBackgroundRequest(userId, RequestUrls.CHAT_MESSAGE_SEND, params)
        super.onBackPressed()
    }

    private fun setRefresh() {
        refreshLayout.setDisableContentWhenRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setOnRefreshListener {
            page++
            getChatMessage()
        }
        messageRecord.setScrollListener(object : LoadingRecyclerView.OnScrollerListener {

            override fun onScroll(dx: Int, dy: Int) {
            }

            override fun onScrollStop() {
                if (hasMoreRecord && refreshLayout.state == RefreshState.None && isVisibleFirst()) {
                    refreshLayout.autoRefresh()
                }
            }

        })
    }

    override fun onRightClick(view: View) {
        if (chatType != TYPE_NORMAL_CHAT) {
            return
        }
        val intent = Intent(this, ChatSettingActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("nickName", nickName)
        intent.putExtra("userImage", userImage)
        startActivityForResult(intent, 1000)
    }

    fun selectImage(view: View) {
        val intent = Intent(this, SelectLocalImageActivity::class.java)
        intent.putExtra(SelectLocalImageActivity.maxCountKey, 18)
        intent.putExtra(SelectLocalImageActivity.buttonText, StringUtils.getString(R.string.send))
        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    messageList.clear()
                    setResult(RESULT_OK)
                    (messageRecord.adapter as MessageAdapter).notifyDataSetChanged()
                }

                1001 -> {
                    if (data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) != null) {
                        val list = data.getSerializableExtra(SelectLocalImageActivity.imagePathList) as MutableList<LocalMediaBean>
                        if (list.size > 0) {
                            addLocalPictureMsg(list)
                            compressImage(list)
                        }
                    }
                }
            }
        }
    }

    private fun compressImage(data: MutableList<LocalMediaBean>) {
        val task: MutableList<PictureUploadTask> = ArrayList()
        data.forEach {
            val bean = PictureUploadTask()
            bean.localPath = it.path
            bean.userId = GlobalValue.userInfoBean!!.id
            bean.taskTag = userId
            task.add(bean)
        }
        PictureUploadManager.uploadImage(task)
    }

    private fun addLocalPictureMsg(data: MutableList<LocalMediaBean>) {
        val msgList: MutableList<ChatMessageBean> = ArrayList()
        data.forEach {
            val bean = ChatMessageBean()
            bean.fromUid = GlobalValue.userInfoBean!!.id
            bean.avatar = GlobalValue.userInfoBean!!.avatar ?: ""
            bean.toUid = userId.toInt()
            bean.messageType = 8
            bean.localPath = it.path
            bean.sendTime = TimesUtils.getUTCTime()
            msgList.add(bean)
        }
        messageList.addAll(msgList)
        if (messageRecord.adapter != null) {
            (messageRecord.adapter as MessageAdapter).notifyItemRangeInserted(messageList.size - msgList.size, msgList.size)
            scrollToLast()
        } else {
            setChatRecord()
        }
    }

    private fun getChatMessage() {
        val params = HttpParams()
        params.put("uid", userId)
        params.put("Size", pageSize)
        params.put("Page", page)
        params.put("id", startId)
        HttpRequest.post(RequestUrls.CHAT_MESSAGE_RECORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                refreshLayout.finishRefresh()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list: MutableList<ChatMessageBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<ChatMessageBean>>() {}.type
                    )
                    list.reverse()
                    if (page == 1) {
                        messageList.clear()
                        (messageList as ArrayList<ChatMessageBean>).addAll(list)
                    } else {
                        messageList.addAll(0, list)
                    }
                    if (messageList.size > 0) {
                        startId = messageList[0].id
                    }
                    if (messageRecord.adapter == null) {
                        setChatRecord()
                    } else {
                        (messageRecord.adapter as MessageAdapter).notifyDataSetChanged()
                        if (page == 1) scrollToLast()
                        else (messageRecord.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                            list.size - 1,
                            0
                        )
                    }
                    if (list.size < pageSize) {
                        hasMoreRecord = false
                        refreshLayout.setEnableRefresh(false)
                    } else {
                        hasMoreRecord = true
                    }
                } else {
                    refreshLayout.setEnableRefresh(false)
                    setChatRecord()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                refreshLayout.finishRefresh()
                setChatRecord()
            }

        }, params, this)
    }

    private fun setChatRecord() {
        val manager = LinearLayoutManager(this)
        messageRecord.layoutManager = manager
        messageRecord.addItemDecoration(XLinearBuilder(this).setSpacing(30f).build())
//        manager.stackFromEnd = true
//        if (chatType != TYPE_NORMAL_CHAT && serviceTemple.isNotEmpty()) {
//            val chatMessageBean = ChatMessageBean()
//            chatMessageBean.avatar = userImage
//            chatMessageBean.context = serviceTemple
//            chatMessageBean.fromUid = userId.toInt()
//            chatMessageBean.toUid = GlobalValue.userInfoBean!!.id
//            chatMessageBean.messageType = 1
//            chatMessageBean.sendTime = TimesUtils.getUTCTime()
//            messageList.add(chatMessageBean)
//            serviceTemple = ""
//        }
        val adapter = MessageAdapter(messageList)
        adapter.fromUserImage = userImage
        if (messageList.isEmpty()) {
            val header = TextView(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.rightMargin = SizeUtils.dp2px(54f)
            params.leftMargin = SizeUtils.dp2px(54f)
            params.topMargin = SizeUtils.dp2px(20f)
            params.bottomMargin = SizeUtils.dp2px(20f)

            header.layoutParams = params
            header.setTextColor(ColorUtils.getColor(R.color.grey_2))
            header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            header.setText(R.string.chat_warning)
            adapter.addHeaderView(header)
        }
        messageRecord.adapter = adapter

        sendMessageModel()

        setMessageRead(userId.toInt())
    }

    private fun sendMessageModel() {
        if (modelMessage.isNotEmpty()) {
            sendMessage(modelMessage, isModel = true)
            modelMessage = ""
        }
        scrollToLast()
    }

    private fun setViewEnable(enable: Boolean) {
        emojiImg.isEnabled = enable
        sendImage.isEnabled = enable
        inputMsg.isEnabled = enable
    }

    /**
     * 获取客服信息
     */
    private fun getCustomerService() {
        if (chatType == TYPE_NORMAL_CHAT) {
            return
        }
        val params = HttpParams()
        params.put("templateType", chatType - 1)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.get(RequestUrls.GET_PAY_SERVICE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    userId = response.optInt("id").toString()
                    userImage = response.optString("avatar")
                    nickName = response.optString("nickName")
//                    serviceTemple = response.optString("msgTemple")

                    setHeaderTitle(nickName)
                    setViewEnable(true)
                    getChatMessage()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }

        }, tag = this, params = params)
    }

    fun sendMessage(view: View) {
        val message = inputMsg.text.toString()
        if (message.isEmpty()) {
            return
        }
        inputMsg.setText("")
        sendMessage(message)
    }

    /**
     * messageType=8时必传
     */
    private fun sendMessage(message: String, messageType: Int = 1, task: PictureUploadTask? = null, isModel: Boolean = false) {
        val params = HttpParams()
        if (messageType == 8) {
            params.put("Code", message)
        } else {
            params.put("Context", message)
        }
        if (isModel) {
            params.put("templateType", chatType - 1)
        }
        params.put("ToUid", userId)
        params.put("MessageType", messageType)
        HttpRequest.post(RequestUrls.CHAT_MESSAGE_SEND, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val chatMessageBean = Gson().fromJson<ChatMessageBean>(
                    response.toString(),
                    object : TypeToken<ChatMessageBean>() {}.type
                )
                if (messageType == 8) {
                    setItemStatus(task!!)
                } else {
                    EventBus.getDefault().post(chatMessageBean)
                    addMessage(chatMessageBean)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (response != null) {
                    val ret = JSONObject(response).optInt("ret")
                    if (ret == 401) {
                        Handler().post { tipDialogShow(errorMsg ?: "") }
                    } else if (ret == 1001) {

                    } else {
                        ToastUtils.showLong(errorMsg)
                    }
                }
                if (messageType == 8) {
                    messageList.removeAll { !it.localPath.isNullOrEmpty() && it.pictureStatus != PictureUploadStatus.FINISH }
                    (messageRecord.adapter as MessageAdapter).notifyDataSetChanged()
                }
            }

        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPictureSend(task: PictureUploadTask) {
        if (task.taskTag != userId) {
            return
        }
        if (task.status == PictureUploadStatus.FINISH) {
            if (task.imageUrl.isNullOrEmpty()) {
                task.status = PictureUploadStatus.ERROR
                setItemStatus(task)
            } else {
                sendMessage(task.imageUrl!!, 8, task)
            }
        } else if (task.status == PictureUploadStatus.ERROR) {
            setItemStatus(task)
        }
    }

    private fun setItemStatus(task: PictureUploadTask) {
        val tempList = messageList.filter {
            PictureUploadManager.getAbsolutePath(it.localPath) == task.localPath && it.pictureStatus != task.status
        }
        if (tempList.size == 1) {
            val bean = tempList[0]
            bean.context = task.imageUrl!!
            bean.pictureStatus = task.status
            val adapter = messageRecord.adapter as MessageAdapter
            val position = adapter.getItemPosition(bean)
            adapter.notifyItemChanged(position)
            scrollToLast()
        } else {
            page = 1
            getChatMessage()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onNewMessage(message: ChatMessageBean) {
        if (message.fromUid.toString() == userId) {
            addMessage(message)
            setMessageRead(message.fromUid)
        } else if (!isPaused && message.isRemind == 1) {
            MessageToast.showMessage(this, message)
        }
    }

    private fun tipDialogShow(msg: String) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(msg)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.bindPhoneNumber) {
            val intent = Intent(this, AccountSettingActivity::class.java)
            intent.putExtra("formId", 0)
            startActivity(intent)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    private fun scrollToLast() {
        Handler().postDelayed({
//            if (!isVisibleLast()) {
//                messageRecord.scrollToPosition(messageRecord.adapter!!.itemCount - 1)
//            }
            messageRecord.scrollToBottom()
        }, 100)
    }

    private fun isVisibleFirst(): Boolean {
        if (messageRecord.layoutManager == null) {
            return true
        }
        val manager = messageRecord.layoutManager as LinearLayoutManager
        val firstItemPosition = manager.findFirstVisibleItemPosition()
        val visibleCount = manager.childCount
        return visibleCount > 0 && firstItemPosition == 0
    }

    private fun isVisibleLast(): Boolean {
        if (messageRecord.layoutManager == null) {
            return true
        }
        val manager = messageRecord.layoutManager as LinearLayoutManager
        val lastItemPosition = manager.findLastVisibleItemPosition()
        val visibleCount = manager.childCount
        val totalCount = manager.itemCount
        val state = messageRecord.scrollState
        return visibleCount > 0 && lastItemPosition == totalCount - 1 && state == RecyclerView.SCROLL_STATE_IDLE
    }

    private fun addMessage(chatMessageBean: ChatMessageBean) {
        messageList.add(chatMessageBean)
        if (messageRecord.adapter == null) {
            setChatRecord()
        } else {
            (messageRecord.adapter as MessageAdapter).notifyDataSetChanged()
            scrollToLast()
        }
    }

    private fun addEmojiFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        if (emotionFragment == null) {
            emotionFragment = MessageEmotionFragment()
            transaction.add(R.id.emojiContentLayout, emotionFragment!!)
        } else {
            transaction.show(emotionFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment() {
        if (emotionFragment != null) {
            supportFragmentManager.beginTransaction().hide(emotionFragment!!).commitAllowingStateLoss()
        }
    }

    override fun onChange(emotionSize: Int) {
        sendMsg.tag = emotionSize
        sendMsg.isEnabled = emotionSize > 0 || !inputMsg.text.isNullOrEmpty()
    }
}