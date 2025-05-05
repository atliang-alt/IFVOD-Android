package com.cqcsy.lgsp.upper.chat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_chat_setting.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 聊天消息设置
 */
class ChatSettingActivity : NormalActivity() {
    var userId = 0
    var status: Boolean = false
    var userImage = ""
    var nickName = ""

    override fun getContainerView(): Int {
        return R.layout.activity_chat_setting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.chat_setting)

        val id = intent.getStringExtra("userId")
        userImage = intent.getStringExtra("userImage") ?: ""
        nickName = intent.getStringExtra("nickName") ?: ""
        userId = id?.toInt() ?:0
        getForbiddenStatus(userId)
        ImageUtil.loadCircleImage(this, userImage, upperLogo)
        upperName.text = nickName
        forbiddenMessage.setOnClickListener { view ->
            forbidden(userId)
        }
    }

    private fun getForbiddenStatus(uid: Int) {
        val params = HttpParams()
        params.put("uid", uid)
        HttpRequest.get(RequestUrls.CHAT_FORBIDDEN_STATUS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    status = response.optBoolean("status")
                    forbiddenMessage.isChecked = status
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }

        }, params, this)
    }

    private fun forbidden(uid: Int) {
        val params = HttpParams()
        params.put("uid", uid)
        params.put("status", status)
        HttpRequest.post(RequestUrls.CHAT_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    status = response.optBoolean("status")
                    forbiddenMessage.isChecked = status
                    EventBus.getDefault().post(BlackListEvent(uid, status))
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }

        }, params, this)
    }

    fun clearRecord(view: View) {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.clear_chat_message)
        dialog.setMsg(R.string.clear_record_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.ensure) {
            dialog.dismiss()
            clear(userId)
        }
        dialog.show()
    }

    /**
     * 投诉
     */
    fun complaintClick(view: View) {
        val intent = Intent(this, ComplaintActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("userImage", userImage)
        intent.putExtra("nickName", nickName)
        startActivity(intent)
    }

    fun clear(uid: Int) {
        val params = HttpParams()
        params.put("uid", uid)
        HttpRequest.post(RequestUrls.CLEAR_CHAT_MESSAGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                setResult(Activity.RESULT_OK)
                ToastUtils.showLong(R.string.clear_chat_message_success)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }
}