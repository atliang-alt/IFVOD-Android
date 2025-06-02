package com.cqcsy.library.pay.card

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.pay.model.VipPayBean

/**
 * 信用卡支付须知
 */
class CreditCardNoticeActivity : NormalActivity() {

    override fun getContainerView(): Int {
        return R.layout.activity_credit_card_notice
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vipPayBean = intent.getSerializableExtra("vipPayBean") as VipPayBean
        setHeaderTitle(StringUtils.getString(R.string.pay_type, (vipPayBean?.title ?: "")))
    }

    fun nextClick(view: View) {
        val i = intent
        i.setClass(this@CreditCardNoticeActivity, SelectBillAddressActivity::class.java)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}