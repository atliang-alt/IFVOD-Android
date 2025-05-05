package com.cqcsy.lgsp.vip.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.utils.GlobalValue
import kotlinx.android.synthetic.main.layout_vip_info_dialog.*

/**
 * Vip详情页dialog
 */
class VipInfoDetail(context: Context) : Dialog(context, R.style.dialog_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vip_info_dialog)
        if (GlobalValue.userInfoBean?.vipLevel in 1..6)
            vipClassifyImage.setImageResource(VipGradeImageUtil.getVipImage(GlobalValue.userInfoBean?.vipLevel!!))
        val vipName = GlobalValue.userInfoBean?.vipTypeName ?: ""
        vipClassifyName.text = vipName
        vipDate.text = StringUtils.getString(
            R.string.vipDate, TimeUtils.date2String(
                TimesUtils.formatDate(GlobalValue.userInfoBean?.eDate!!), "yyyy-MM-dd"
            )
        )
        vipTitle.text =
            StringUtils.getString(R.string.vipInfoTitle, vipName.replace("VIP", ""))
        vipClose.setOnClickListener {
            dismiss()
        }
    }
}