package com.cqcsy.lgsp.login

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_reset_password.errorTips
import org.json.JSONObject

/**
 * 重置密码
 */
class ResetPassword : NormalActivity() {
    private var accountName = ""
    private var areaCode = ""
    private var type = 0
    private var formId = 0
    private var validateCode = ""

    override fun getContainerView(): Int {
        return R.layout.activity_reset_password
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(resources.getString(R.string.resetPassword))
        initData()
        initView()
    }

    private fun initData() {
        formId = intent.getIntExtra("formId", 0)
        if (formId == 1) {
            setHeaderTitle(resources.getString(R.string.rebuildPassword))
        } else {
            setHeaderTitle(resources.getString(R.string.resetPassword))
        }
        accountName = intent.getStringExtra("AccountName") ?: ""
        areaCode = intent.getStringExtra("AreaCode") ?: ""
        validateCode = intent.getStringExtra("ValidateCode") ?: ""
        type = intent.getIntExtra("RegType", 0)
    }

    private fun initView() {
        editOne.addTextChangedListener { text ->
            resetPassword.isEnabled = !TextUtils.isEmpty(editTwo.text) && text!!.isNotEmpty()
        }
        editTwo.addTextChangedListener { text ->
            resetPassword.isEnabled = !TextUtils.isEmpty(editOne.text) && text!!.isNotEmpty()
        }
    }

    /**
     * 重置密码请求
     */
    fun resetPassword(view: View) {
        if (!resetPassword.isEnabled) {
            return
        }
        if (editOne.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.passwordTips)
            return
        }
        if (editTwo.text.isEmpty()) {
            errorTips.text = resources.getString(R.string.passwordTips)
            return
        }
        if (editOne.text.toString() != editTwo.text.toString()) {
            errorTips.text = resources.getString(R.string.passwordNotSame)
            return
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("RegType", type)
        params.put("AccountName", accountName)
        params.put("AreaCode", areaCode)
        params.put("UserPwd", editTwo.text.toString())
        params.put("ValidateCode", validateCode)
        params.put("VerifyAccount", true)
        HttpRequest.post(RequestUrls.RESET_PASSWORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                ToastUtils.showLong(R.string.updateSuccess)
                setResult(Activity.RESULT_OK)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                if (response != null) {
                    val ret = JSONObject(response).optInt("ret")
                    if (ret == 7004) {
                        ToastUtils.showLong(errorMsg)
                        setResult(RESULT_CANCELED)
                        finish()
                    } else {
                        errorTips.text = errorMsg
                    }
                } else {
                    errorTips.text = errorMsg
                }
            }
        }, params, this)
    }

    /**
     * 第一个输入框密码是否可见
     */
    fun lookPasswordOne(view: View) {
        if (lookPasswordOne.isSelected) {
            // 密码可见
            editOne.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPasswordOne.isSelected = false
        } else {
            // 不可见
            editOne.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPasswordOne.isSelected = true
        }
        editOne.setSelection(editOne.text.length)
    }

    /**
     * 第二个输入框密码是否可见
     */
    fun lookPasswordTwo(view: View) {
        if (lookPasswordTwo.isSelected) {
            // 密码可见
            editTwo.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPasswordTwo.isSelected = false
        } else {
            // 不可见
            editTwo.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPasswordTwo.isSelected = true
        }
        editTwo.setSelection(editTwo.text.length)
    }
}