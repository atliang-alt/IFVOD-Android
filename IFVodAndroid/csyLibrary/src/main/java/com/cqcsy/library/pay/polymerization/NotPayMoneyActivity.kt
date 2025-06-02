package com.cqcsy.library.pay.polymerization

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.pay.PayUrls
import com.cqcsy.library.utils.JumpUtils
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_not_pay_money.*
import org.json.JSONObject

/**
 * 我未付款页
 */
class NotPayMoneyActivity : NormalActivity() {
    override fun getContainerView(): Int {
        return R.layout.activity_not_pay_money
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val payType = intent.getStringExtra("payType") ?: ""
        setHeaderTitle(StringUtils.getString(R.string.pay_type, payType))
    }

    fun cancelOrder(view: View) {
        cancelOrderHttp()
    }

    private fun cancelOrderHttp() {
        showProgressDialog()
        val param = HttpParams()
        param.put("order", intent.getStringExtra("order") ?: "")
        param.put("status", 1)
        HttpRequest.post(PayUrls.SURE_CANCEL_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                cancelLayout.visibility = View.GONE
                backLayout.visibility = View.VISIBLE
                dismissProgressDialog()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                dismissProgressDialog()
            }
        }, param, this)
    }

    fun backClick(view: View) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("position", 2)
//        startActivity(intent)
        JumpUtils.jumpAnyUtils(this, JumpUtils.appendJumpParam("com.cqcsy.lgsp.main.MainActivity", mutableMapOf("position" to 2)))
        finish()
    }
}