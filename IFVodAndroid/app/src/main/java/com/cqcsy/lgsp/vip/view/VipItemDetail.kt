package com.cqcsy.lgsp.vip.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VipIntroBean
import kotlinx.android.synthetic.main.layout_vip_item_dialog.*

/**
 * VIP特权详情
 */
class VipItemDetail(context: Context) : Dialog(context, R.style.dialog_style) {
    var vipIntroBean: VipIntroBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vip_item_dialog)
        itemImage.setImageResource(vipIntroBean?.detailIcon!!)
        itemName.text = vipIntroBean?.itemName
        itemSub.text = vipIntroBean?.itemSubDes
        detailImage.setImageResource(vipIntroBean?.detailImage!!)
        detailDes.text = vipIntroBean?.itemDetail
        detailEnsure.setOnClickListener { dismiss() }
    }
}