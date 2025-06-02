package com.cqcsy.lgsp.main.vip

import android.content.Intent
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.main.mine.AccountSettingActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_exchange_vip.*
import org.json.JSONObject

/**
 ** 2022/12/14
 ** des：兑换VIP
 **/

class ExchangeVipActivity : NormalActivity() {

    override fun getContainerView(): Int {
        return R.layout.activity_exchange_vip
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.vip_exchange)
        exchangeCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    exchangeCode.letterSpacing = 0f
                    exchangeButton.isEnabled = false
                } else {
                    exchangeCode.letterSpacing = 0.3f
                    exchangeButton.isEnabled = true
                }
            }

        })
    }

    fun exchangeVipSubmit(view: View) {
        val input = exchangeCode.text
        if (input.isEmpty()) {
            ToastUtils.showLong(R.string.input_exchange_code)
            return
        }
        showProgressDialog(cancelAble = false)
        val params = HttpParams()
        params.put("code", input.toString())
        HttpRequest.get(RequestUrls.EXCHANGE_VIP, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                ToastUtils.showLong(R.string.exchange_success)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                if (response != null) {
                    val ret = JSONObject(response).optInt("ret")
                    if (ret == 401) {
                        Handler().post { tipDialogShow(errorMsg ?: "") }
                    } else {
                        ToastUtils.showLong(errorMsg)
                    }
                }
            }
        }, params, this)
    }

    private fun tipDialogShow(msg: String) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(msg)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.bindPhoneNumber) {
            val intent = Intent(this, AccountSettingActivity::class.java)
            intent.putExtra("formId", 0)
            startActivity(intent)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }
}