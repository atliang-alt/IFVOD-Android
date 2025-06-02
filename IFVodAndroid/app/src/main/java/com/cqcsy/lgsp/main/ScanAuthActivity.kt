package com.cqcsy.lgsp.main

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_scan_auth.*
import org.json.JSONObject

/**
 ** 2022/5/6
 ** des：扫码授权登录
 **/

class ScanAuthActivity : NormalActivity() {
    private var mAuthKey = ""

    override fun getContainerView(): Int {
        return R.layout.activity_scan_auth
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.scan_auth)
        mAuthKey = intent.getStringExtra("authKey") ?: ""
    }

    override fun onResume() {
        super.onResume()
        if (GlobalValue.isLogin()) {
            topGroup.visibility = View.VISIBLE
            ImageUtil.loadCircleImage(this, GlobalValue.userInfoBean?.avatar, userLogo)
            userAccount.text = GlobalValue.userInfoBean?.nickName
            loginConfirm.setText(R.string.login_confirm)
            loginConfirmTip.isVisible = false
            loginButton.setText(R.string.login_confirm_button)
        } else {
            topGroup.visibility = View.GONE
            loginConfirmTip.isVisible = true
            loginConfirm.setText(R.string.not_login)
            loginConfirmTip.setText(R.string.not_login_tip)
            loginButton.setText(R.string.to_login)
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.loginButton -> {
                if (GlobalValue.isLogin()) {
                    authLogin()
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
            R.id.cancelButton -> {
                finish()
            }
        }
    }

    private fun authLogin() {
        if (mAuthKey.isEmpty()) {
            ToastUtils.showShort(R.string.auth_key_error)
            return
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("key", mAuthKey)
        HttpRequest.get(RequestUrls.CONFIRM_AUTH_LOGIN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }
}