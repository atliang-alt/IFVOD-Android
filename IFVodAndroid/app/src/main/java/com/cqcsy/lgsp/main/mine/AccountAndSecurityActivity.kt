package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.login.AccountVerificationActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.utils.GlobalValue
import kotlinx.android.synthetic.main.layout_forget_password_popup.view.*

/**
 * 设置 -- 账号与安全
 */
class AccountAndSecurityActivity : NormalActivity() {
    private val setPasswordCode = 1001

    override fun getContainerView(): Int {
        return R.layout.activity_account_security
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.account_setting)
    }

    fun passwordSetting(view: View) {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_forget_password_popup, null)
        contentView.phoneRetrieve.visibility = View.GONE
        contentView.emailRetrieve.visibility = View.GONE
        contentView.title.text = StringUtils.getString(R.string.rebuildPassword)
        contentView.phoneRetrieveLayout.setOnClickListener {
            if (GlobalValue.userInfoBean?.phone.isNullOrEmpty()) {
                ToastUtils.showLong(R.string.noPhoneTips)
                dialog.dismiss()
                return@setOnClickListener
            }
            // 跳转到手机号验证
            val intent = Intent(this, AccountVerificationActivity::class.java)
            intent.putExtra("formId", 1)
            intent.putExtra("type", Constant.MOBIL_TYPE)
            intent.putExtra("areaCode", GlobalValue.userInfoBean?.areacode)
            intent.putExtra("accountName", GlobalValue.userInfoBean?.phone)
            startActivityForResult(intent, setPasswordCode)
            dialog.dismiss()
        }
        contentView.emailRetrieveLayout.setOnClickListener {
            if (GlobalValue.userInfoBean?.email.isNullOrEmpty()) {
                ToastUtils.showLong(R.string.noEmailTips)
                dialog.dismiss()
                return@setOnClickListener
            }
            // 跳转到邮箱验证
            val intent = Intent(this, AccountVerificationActivity::class.java)
            intent.putExtra("formId", 1)
            intent.putExtra("type", Constant.EMAIL_TYPE)
            intent.putExtra("accountName", GlobalValue.userInfoBean?.email)
            startActivityForResult(intent, setPasswordCode)
            dialog.dismiss()
        }
        contentView.cancelRetrievePassword.setOnClickListener {
            // 取消
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun emailSetting(view: View) {
        val intent = Intent(this, AccountSettingActivity::class.java)
        intent.putExtra("formId", 1)
        startActivity(intent)
    }

    fun phoneSetting(view: View) {
        val intent = Intent(this, AccountSettingActivity::class.java)
        intent.putExtra("formId", 0)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == setPasswordCode) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}