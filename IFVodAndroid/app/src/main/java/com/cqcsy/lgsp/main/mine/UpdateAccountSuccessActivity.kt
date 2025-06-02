package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.GlobalValue
import kotlinx.android.synthetic.main.activity_update_account_success.*

/**
 * 更改手机号、邮箱成功页
 */
class UpdateAccountSuccessActivity : NormalActivity() {
    // 0: 手机号设置  1: 邮箱设置
    private var formId = 0

    override fun getContainerView(): Int {
        return R.layout.activity_update_account_success
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        formId = intent.getIntExtra("formId", 0)
        if (formId == 1) {
            setHeaderTitle(
                StringUtils.getString(
                    R.string.updateAccountSuccessTitle,
                    StringUtils.getString(R.string.email)
                )
            )
            updateAccount.text = StringUtils.getString(
                R.string.updateEmailAccount, GlobalValue.userInfoBean!!.email
            )
            updateAccountSuccess.text = StringUtils.getString(
                R.string.updateAccountSuccess, getString(R.string.email)
            )
        } else {
            setHeaderTitle(
                StringUtils.getString(
                    R.string.updateAccountSuccessTitle,
                    StringUtils.getString(R.string.phoneNumber)
                )
            )
            updateAccount.text = StringUtils.getString(
                R.string.updatePhoneAccount,
                GlobalValue.userInfoBean!!.areacode + " " + GlobalValue.userInfoBean!!.phone
            )
            updateAccountSuccess.text = StringUtils.getString(
                R.string.updateAccountSuccess, getString(R.string.phoneNumber)
            )
        }
    }

    fun sure(view: View) {
        finish()
    }
}