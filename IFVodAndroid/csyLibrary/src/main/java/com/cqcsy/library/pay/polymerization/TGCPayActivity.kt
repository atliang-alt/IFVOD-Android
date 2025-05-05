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
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.JumpUtils
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_tgc_pay.*
import org.json.JSONObject

/**
 * 聚合 -- TGC支付
 */
class TGCPayActivity : NormalActivity() {
    var vipPayBean: VipPayBean? = null
    var vipClassifyBean: VipClassifyBean? = null
    var toUid: Int = 0
    var isPaySuccess = false
    var pathInfo = ""

    override fun getContainerView(): Int {
        return R.layout.activity_tgc_pay
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vipPayBean = intent.getSerializableExtra("vipPayBean") as VipPayBean
        vipClassifyBean = intent.getSerializableExtra("vipClassifyBean") as VipClassifyBean
        toUid = intent.getIntExtra("toUid", 0)
        pathInfo = intent.getStringExtra("pathInfo") ?: ""
        setHeaderTitle(StringUtils.getString(R.string.pay_type, vipPayBean?.title))
        setRightImage(R.mipmap.icon_chat_service)
        getTGCBalance()
    }

    override fun onRightClick(view: View) {
//        val intent = Intent(this, ChatActivity::class.java)
//        intent.putExtra(ChatActivity.CHAT_TYPE, ChatActivity.TYPE_SERVER_TGC)
//        startActivity(intent)
        val params = JumpUtils.appendJumpParam(
            "com.cqcsy.lgsp.upper.chat.ChatActivity",
            mutableMapOf("chatType" to 2),
            true
        )
        JumpUtils.jumpAnyUtils(this, params)
    }

    /**
     * 获取TGC余额
     */
    private fun getTGCBalance() {
        showProgress()
        HttpRequest.post(PayUrls.GET_TGC_BALANCE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val tgcBalance = response?.optDouble("tgcBalance") ?: 0.0
                val scale = response?.optDouble("scale") ?: 0.0
                initView(tgcBalance, scale)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                showFailed {
                    getTGCBalance()
                }
            }

        }, tag = this)
    }

    private fun initView(balance: Double, scale: Double) {
        val price = if (!vipClassifyBean?.disprice.isNullOrEmpty()) {
            vipClassifyBean?.disprice
        } else {
            vipClassifyBean?.price
        }
        if (!price.isNullOrEmpty()) {
            val tgc = (price.toDouble()) * scale
            if (tgc > balance) {
                tgcBalanceTips.text = StringUtils.getString(R.string.tgc_balance_tips, vipPayBean?.title)
                notBalanceLayout.visibility = View.VISIBLE
                payLayout.visibility = View.GONE
                paySuccess.visibility = View.GONE
            } else {
                tgcBalance.text = StringUtils.getString(R.string.tgc_balance, balance.toString())
                money.text = tgc.toString()
                notBalanceLayout.visibility = View.GONE
                payLayout.visibility = View.VISIBLE
                paySuccess.visibility = View.GONE
            }
        }
    }

    fun surePay(view: View) {
        showProgressDialog()
        val params = HttpParams()
        val promotions = vipClassifyBean?.promotions
        if (!promotions.isNullOrEmpty()) {
            params.put("Promotions", promotions.joinToString(separator = "|") {
                it.promotionCode
            })
        }
        params.put("ProductID", vipClassifyBean?.packageId ?: 0)
        params.put("SysName", vipPayBean?.payType ?: -1)
        if (toUid == 0) {
            toUid = GlobalValue.userInfoBean?.id ?: 0
        }
        params.put("ToUID", toUid)
        params.put("refer", pathInfo)
        HttpRequest.post(PayUrls.CREATE_TGC_ORDER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                isPaySuccess = true
                notBalanceLayout.visibility = View.GONE
                payLayout.visibility = View.GONE
                paySuccess.visibility = View.VISIBLE
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                dismissProgressDialog()
            }

        }, params, tag = this)
    }

    override fun onBackPressed() {
        if (isPaySuccess) {
            setResult(RESULT_OK)
        }
        finish()
    }

    fun backClick(view: View) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("position", 2)
//        startActivity(intent)
        JumpUtils.jumpAnyUtils(this, JumpUtils.appendJumpParam("com.cqcsy.lgsp.main.MainActivity", mutableMapOf("position" to 2)))
        finish()
    }
}