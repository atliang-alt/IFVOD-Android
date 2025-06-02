package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_write_invite_code.*
import org.json.JSONObject
import java.util.*

/**
 * 填写邀请码
 */
class WriteInviteCodeActivity : NormalActivity() {

    private var isInvited = false

    override fun getContainerView(): Int {
        return R.layout.activity_write_invite_code
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.write_invite)
        isInvited = intent.getBooleanExtra("isInvited", false)
        if (isInvited) {
            noWriteLayout.visibility = View.VISIBLE
        } else {
            writeLayout.visibility = View.VISIBLE
            inviteCode.addTextChangedListener { text ->
                sureClick.isEnabled = text?.isNotEmpty() == true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isInvited) {
            inviteCode.requestFocus()
        }
    }

    fun sureClick(view: View) {
        showProgressDialog()
        val inputCode = inviteCode.text.toString().trim()
        val params = HttpParams()
        params.put("InviteCode", inputCode.uppercase(Locale.getDefault()))
        HttpRequest.post(RequestUrls.WRITE_INVITE_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                showTips(errorMsg)
            }
        }, params, this)
    }

    private fun showTips(errorMsg: String?) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.submitFail)
        tipsDialog.setMsg(errorMsg ?: getString(R.string.submitFail))
        tipsDialog.setRightListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }
}