package com.cqcsy.library.pay.card

import android.os.Bundle
import android.view.View
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.JumpUtils

/**
 * 信用卡支付成功
 */
class CardPaySuccessActivity: NormalActivity() {
    override fun getContainerView(): Int {
        return R.layout.activity_card_pay_success
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.credit_card_pay)
    }

    fun backClick(view: View) {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("position", 2)
//        startActivity(intent)
        JumpUtils.jumpAnyUtils(this, JumpUtils.appendJumpParam("com.cqcsy.lgsp.main.MainActivity", mutableMapOf("position" to 2)))
        finish()
    }
}