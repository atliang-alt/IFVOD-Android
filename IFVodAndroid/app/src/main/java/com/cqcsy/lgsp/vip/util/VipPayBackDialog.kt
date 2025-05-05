package com.cqcsy.lgsp.vip.util

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.cqcsy.lgsp.R
import kotlinx.android.synthetic.main.layout_vip_pay_back_dialog.*

/**
 * Vip充值返回dialog
 */
class VipPayBackDialog(var mContext: Context, var listener: OnClickListener) :
    Dialog(mContext, R.style.dialog_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vip_pay_back_dialog)
        vipBackBtn.setOnClickListener {
            dismiss()
            listener.onBack()
        }
        vipBuyBtn.setOnClickListener {
            dismiss()
            listener.onBuy()
        }
    }

    /**
     * 点击事件接口
     */
    interface OnClickListener {
        fun onBuy()
        fun onBack()
    }
}