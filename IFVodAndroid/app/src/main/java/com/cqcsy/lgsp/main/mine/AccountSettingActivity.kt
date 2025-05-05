package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.login.AccountVerificationActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_phone_setting.*
import org.json.JSONObject

/**
 * 设置 -- 账号设置
 */
class AccountSettingActivity : NormalActivity() {
    // 0: 手机号设置  1: 邮箱设置
    private var formId = 0
    private val updateAccountCode = 1001
    private val accountVerification = 1002

    override fun getContainerView(): Int {
        return R.layout.activity_phone_setting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        formId = intent.getIntExtra("formId", 0)
        initData()
        initView()
    }

    private fun initData() {
        if (formId == 1) {
            setHeaderTitle(R.string.emailSetting)
            if (GlobalValue.userInfoBean?.email.isNullOrEmpty()) {
                // 没有邮箱安全认证
                securityLayout.visibility = View.VISIBLE
                modifyLayout.visibility = View.GONE
                bindAccountTips.text = StringUtils.getString(
                    R.string.bindAccountTips, StringUtils.getString(R.string.email)
                )
                sure.text = StringUtils.getString(
                    R.string.toBindAccount, StringUtils.getString(R.string.email)
                )
            } else {
                // 修改绑定
                securityLayout.visibility = View.GONE
                modifyLayout.visibility = View.VISIBLE
                image.setImageResource(R.mipmap.icon_email)
                accountName.text = NormalUtil.formatEmail(GlobalValue.userInfoBean?.email!!)
                currentAccountTips.text = StringUtils.getString(
                    R.string.currentAccount, StringUtils.getString(R.string.email)
                )
                /*modifySure.text = StringUtils.getString(
                    R.string.modifyAccount, StringUtils.getString(R.string.email)
                )*/
                // 暂时不支持修改
                modifySure.visibility = View.GONE
            }
        } else {
            setHeaderTitle(R.string.phoneNumberSetting)
            if (GlobalValue.userInfoBean?.phone.isNullOrEmpty()) {
                // 没有手机号安全认证
                securityLayout.visibility = View.VISIBLE
                modifyLayout.visibility = View.GONE
                bindAccountTips.text = StringUtils.getString(
                    R.string.bindAccountTips, StringUtils.getString(R.string.phoneNumber)
                )
                sure.text = StringUtils.getString(
                    R.string.toBindAccount, StringUtils.getString(R.string.phoneNumber)
                )
            } else {
                // 修改绑定
                securityLayout.visibility = View.GONE
                modifyLayout.visibility = View.VISIBLE
                accountName.text =
                    NormalUtil.formatPhoneNumber(GlobalValue.userInfoBean?.phone!!)
                currentAccountTips.text = StringUtils.getString(
                    R.string.currentAccount, StringUtils.getString(R.string.phoneNumber)
                )
                /*modifySure.text = StringUtils.getString(
                    R.string.modifyAccount, StringUtils.getString(R.string.phoneNumber)
                )*/
                // 暂时不支持修改
                modifySure.visibility = View.GONE
            }
        }
    }

    private fun initView() {
        editPassword.addTextChangedListener { text ->
            sure.isEnabled = text!!.isNotEmpty()
        }
    }

    /**
     * 密码确认
     */
    fun surePassword(view: View) {
        if (!sure.isEnabled) {
            ToastUtils.showLong(R.string.passwordTips)
            return
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("UserPwd", editPassword.text.toString())
        HttpRequest.post(RequestUrls.VALIDATE_PASSWORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                val intent = Intent(this@AccountSettingActivity, UpdateAccountActivity::class.java)
                if (formId == 1) {
                    intent.putExtra("RegType", Constant.EMAIL_TYPE)
                } else {
                    intent.putExtra("RegType", Constant.MOBIL_TYPE)
                }
                startActivityForResult(intent, updateAccountCode)
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 修改账号
     */
    fun modifyAccount(view: View) {
        // 跳转到账号验证
        val intent = Intent(this, AccountVerificationActivity::class.java)
        intent.putExtra("formId", 2)
        if (formId == 1) {
            intent.putExtra("type", Constant.EMAIL_TYPE)
            intent.putExtra("accountName", GlobalValue.userInfoBean?.email)
        } else {
            intent.putExtra("type", Constant.MOBIL_TYPE)
            intent.putExtra("areaCode", GlobalValue.userInfoBean?.areacode)
            intent.putExtra("accountName", GlobalValue.userInfoBean?.phone)
        }
        startActivityForResult(intent, accountVerification)
    }

    /**
     * 输入框密码是否可见
     */
    fun lookPassword(view: View) {
        if (lookPassword.isSelected) {
            // 密码可见
            editPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            lookPassword.isSelected = false
        } else {
            // 不可见
            editPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            lookPassword.isSelected = true
        }
        editPassword.setSelection(editPassword.text.length)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val intent = Intent(this, UpdateAccountSuccessActivity::class.java)
            intent.putExtra("formId", formId)
            startActivity(intent)
            finish()
        }
    }
}