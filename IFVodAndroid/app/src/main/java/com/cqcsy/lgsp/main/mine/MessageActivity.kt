package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.net.MessageCountsBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_message.*
import org.json.JSONObject

/**
 * 消息页
 */
class MessageActivity : NormalActivity() {
    override fun getContainerView(): Int {
        return R.layout.activity_message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.message)
    }

    override fun onResume() {
        super.onResume()
        getMessageCount()
    }

    private fun getMessageCount() {
        HttpRequest.post(RequestUrls.GET_MESSAGE_COUNT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val messageCountsBean = Gson().fromJson<MessageCountsBean>(
                    response.toString(),
                    object : TypeToken<MessageCountsBean>() {}.type
                )
                // 粉丝
                if (messageCountsBean.fansMsgCount > 0) {
                    fansCount.text =
                        if (messageCountsBean.fansMsgCount > 99) "99+" else messageCountsBean.fansMsgCount.toString()
                    fansCount.visibility = View.VISIBLE
                } else {
                    fansCount.visibility = View.GONE
                }
                // 评论/回复
                if (messageCountsBean.commentMsgCount > 0) {
                    commentCount.text =
                        if (messageCountsBean.commentMsgCount > 99) "99+" else messageCountsBean.commentMsgCount.toString()
                    commentCount.visibility = View.VISIBLE
                } else {
                    commentCount.visibility = View.GONE
                }
                // 赞
                if (messageCountsBean.zanMsgCount > 0) {
                    fabulousCount.text =
                        if (messageCountsBean.zanMsgCount > 99) "99+" else messageCountsBean.zanMsgCount.toString()
                    fabulousCount.visibility = View.VISIBLE
                } else {
                    fabulousCount.visibility = View.GONE
                }
                // 系统消息
                if (messageCountsBean.systemMessageContext.isNotEmpty()) {
                    messageTxt.text = Html.fromHtml(messageCountsBean.systemMessageContext)
                } else {
                    messageTxt.text = StringUtils.getString(R.string.noMessage)
                }
                if (messageCountsBean.systeMsgCount > 0) {
                    systemCount.text =
                        if (messageCountsBean.systeMsgCount > 99) "99+" else messageCountsBean.systeMsgCount.toString()
                    systemCount.visibility = View.VISIBLE
                    if (messageCountsBean.systemMessageDate != null) {
                        messageTime.text =
                            TimesUtils.friendDate(messageCountsBean.systemMessageDate!!)
                        messageTime.visibility = View.VISIBLE
                    }
                } else {
                    systemCount.visibility = View.GONE
                    messageTime.visibility = View.GONE
                }
                // 私信消息
                if (messageCountsBean.privateMessageNickName.isNotEmpty()) {
                    privateTxt.text = StringUtils.getString(
                        R.string.privateMessageTxt,
                        messageCountsBean.privateMessageNickName
                    )
                } else {
                    privateTxt.text = StringUtils.getString(R.string.noPrivate)
                }
                if (messageCountsBean.privateMsgCount > 0) {
                    privateCount.text =
                        if (messageCountsBean.privateMsgCount > 99) "99+" else messageCountsBean.privateMsgCount.toString()
                    privateCount.visibility = View.VISIBLE
                    if (messageCountsBean.privateMessageDate != null) {
                        privateTime.text =
                            TimesUtils.friendDate(messageCountsBean.privateMessageDate!!)
                        privateTime.visibility = View.VISIBLE
                    }
                } else {
                    privateCount.visibility = View.GONE
                    privateTime.visibility = View.GONE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, tag = this)
    }

    fun fansClick(view: View) {
        fabulousCount.visibility = View.GONE
        startActivity(Intent(this, MineFansActivity::class.java))
    }

    fun commentClick(view: View) {
        commentCount.visibility = View.GONE
        startActivity(Intent(this, CommentAndReplyActivity::class.java))
    }

    fun fabulousClick(view: View) {
        fabulousCount.visibility = View.GONE
        startActivity(Intent(this, ReceivedFabulousActivity::class.java))
    }

    fun systemMessage(view: View) {
        systemCount.visibility = View.GONE
        startActivity(Intent(this, SystemMessageActivity::class.java))
    }

    fun privateClick(view: View) {
        privateCount.visibility = View.GONE
        startActivity(Intent(this, PrivateMessageActivity::class.java))
    }
}