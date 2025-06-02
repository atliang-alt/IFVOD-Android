package com.cqcsy.lgsp.main.vip

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue

/**
 * VIP介绍activity，和VIPFragment一样
 */
class VIPIntroActivity : NormalActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.vipTitle)
    }

    override fun getContainerView(): Int {
        return R.layout.activity_vip_intro
    }

    fun startBuySelf(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, OpenVipActivity::class.java)
            intent.putExtra("pathInfo", this.javaClass.simpleName)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun startReceiveVip(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, SPUtils.getInstance().getString(Constant.KEY_BIG_V_URL))
            intent.putExtra(WebViewActivity.titleKey, getString(R.string.authentication))
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun exchangeVip(view: View) {
        if (GlobalValue.isLogin()) {
            startActivity(Intent(this, ExchangeVipActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}