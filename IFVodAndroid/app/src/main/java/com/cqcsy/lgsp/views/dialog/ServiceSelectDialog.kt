package com.cqcsy.lgsp.views.dialog

import android.content.Context
import android.os.Bundle
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ServiceItemBean
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.library.views.BottomBaseDialog
import com.littlejerk.rvdivider.builder.XLinearBuilder
import kotlinx.android.synthetic.main.layout_service_select.*

/**
 ** 2023/12/22
 ** des：选择咨询客服
 **/

class ServiceSelectDialog(context: Context, val serviceList: MutableList<ServiceItemBean>) : BottomBaseDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_service_select)
        setServiceList()

        cancel.setOnClickListener { dismiss() }
    }

    private fun setServiceList() {
        service_list.addItemDecoration(XLinearBuilder(context).setColorRes(R.color.divider_color).setSpacing(1f).build())
        val serviceAdapter = object : BaseQuickAdapter<ServiceItemBean, BaseViewHolder>(R.layout.layout_service_item, serviceList) {
            override fun convert(holder: BaseViewHolder, item: ServiceItemBean) {
                ImageUtil.loadImage(context, item.img, holder.getView(R.id.service_image))
                holder.setText(R.id.service_name, item.title)
                holder.setText(R.id.service_des, item.remark)
            }

        }
        serviceAdapter.setOnItemClickListener { adapter, _, position ->
            dismiss()
            val item = adapter.getItem(position) as ServiceItemBean
            if (item.appParam != null) {
                JumpUtils.jumpAnyUtils(context, item.appParam)
            }
        }
        service_list.adapter = serviceAdapter
    }
}