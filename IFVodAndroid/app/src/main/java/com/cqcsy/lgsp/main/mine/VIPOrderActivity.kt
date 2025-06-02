package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshListActivity
import com.cqcsy.lgsp.bean.OrderRecordBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import org.json.JSONArray

/**
 * VIP订单列表
 */
class VIPOrderActivity : RefreshListActivity<OrderRecordBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.orders)
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(this).setSpacing(10f).build())
    }

    override fun getUrl(): String {
        return RequestUrls.ORDERS
    }

    override fun getItemLayout(): Int {
        return R.layout.layout_order_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<OrderRecordBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<OrderRecordBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: OrderRecordBean, position: Int) {
        val vipName = holder.getView<TextView>(R.id.vip_name)
        if (item.vipType in 1..4) {
            val drawable = getDrawable(VipGradeImageUtil.getVipImage(item.vipType))
            val size = SizeUtils.dp2px(19f)
            drawable?.setBounds(0, 0, size, size)
            vipName.setCompoundDrawables(
                drawable,
                null,
                null,
                null
            )
        } else {
            vipName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
        }
        holder.setText(R.id.vip_name, item.typeName)
        val stringBuffer = StringBuffer()
        stringBuffer.append(getString(R.string.normal_time, item.effectiveTime))
        if (item.presentTime > 0) {
            stringBuffer.append(getString(R.string.present_time, item.presentTime))
        }
        if (item.useNum > 1) {
            stringBuffer.append(getString(R.string.user_num, item.useNum))
        }
        holder.setText(R.id.order_name, stringBuffer.toString())
        holder.setText(R.id.order_time, getString(R.string.termOfValidity, item.about))
        holder.setText(R.id.pay_type, getString(R.string.payType, item.payType))
        if (!item.remark.isNullOrEmpty()) {
            holder.setText(R.id.order_desc, item.remark)
        } else if (!item.senderAccount.isNullOrEmpty()) {
            holder.setText(R.id.order_desc, getString(R.string.send_user, item.senderAccount))
        } else {
            holder.setText(R.id.order_desc, "")
        }
    }

    override fun showEmpty() {
        super.showEmpty()
        emptyLargeTip.setText(R.string.no_vip_order)
        emptyLittleTip.setText(R.string.no_vip_order_tip)
        emptyLittleTip.visibility = View.VISIBLE
    }
}