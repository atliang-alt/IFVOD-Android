package com.cqcsy.lgsp.vip

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VipCategoryBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.lgsp.vip.view.HorizontalVipView
import com.cqcsy.lgsp.vip.view.VipPayTypeView
import com.cqcsy.library.views.TipsDialog

/**
 * 创建时间：2022/8/11
 *
 */
class VipOptionAdapter(private val selectId: String?) : BaseQuickAdapter<VipCategoryBean, BaseViewHolder>(R.layout.item_vip_discount_option) {

    private var itemClickListener: OnPayItemClickListener? = null
    var title: String? = null
    var desc: String? = null
    var shareText: String? = null
    var isShare: Boolean = false
    var isCopy: Boolean = false
    var shareFinish: Boolean = false

    override fun convert(holder: BaseViewHolder, item: VipCategoryBean, payloads: List<Any>) {
        val vipPayTypeView = holder.getView<VipPayTypeView>(R.id.vip_pay_type)
        val shareContainer = holder.getView<LinearLayout>(R.id.share_container)
        val shareSuccessContainer = holder.getView<LinearLayout>(R.id.share_success_container)
        if (item.styleType == VipCategory.DISCOUNT_VIP.styleType && data.size > 1) {
            holder.setText(R.id.share_text, shareText)
                .setText(R.id.tv_vip_discount, title)
                .setText(R.id.tv_vip_discount_desc, desc)
                .setText(R.id.purchase, context.getString(R.string.discount_purchase, title))
            val confirmShare = holder.getView<TextView>(R.id.confirm_share)
            val purchase = holder.getView<TextView>(R.id.purchase)
            val copy = holder.getView<TextView>(R.id.copy)
            if (shareFinish) {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = false
                vipPayTypeView.isVisible = true
            }
            confirmShare.isEnabled = isShare
            confirmShare.setOnClickListener {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = true
            }
            purchase.setOnClickListener {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = false
                vipPayTypeView.isVisible = true
                shareFinish = true
//                itemClickListener?.onClickPurchase()
            }
            copy.setOnClickListener {
                if (!shareText.isNullOrEmpty()) {
                    showCopySuccessDialog()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(context.packageName, shareText))
                }
            }
        } else {
            shareContainer.isVisible = false
            shareSuccessContainer.isVisible = false
            vipPayTypeView.isVisible = true
        }
    }

    override fun convert(holder: BaseViewHolder, item: VipCategoryBean) {
        val vipClassifyView = holder.getView<HorizontalVipView>(R.id.vip_classify)
        val vipPayTypeView = holder.getView<VipPayTypeView>(R.id.vip_pay_type)
        val vipClassifyBeans = item.data
        if (vipClassifyBeans != null) {
            vipClassifyView.setView(VipCategory.creator(item.id), vipClassifyBeans, selectId)
        }
        vipClassifyView.setOnItemSelectListener(object : HorizontalVipView.OnItemSelectListener {
            override fun onItemSelect(vipClassifyBean: VipClassifyBean) {
                itemClickListener?.onSelectClassify(vipClassifyBean)
            }
        })
        vipPayTypeView.itemListener = object : VipPayTypeView.OnItemListener {
            override fun onSelectPay(payBean: VipPayBean) {
//                itemClickListener?.onSelectPay(item.id, payBean)
            }
        }
        vipPayTypeView.setData(item.payTypes)
        val shareContainer = holder.getView<LinearLayout>(R.id.share_container)
        val shareSuccessContainer = holder.getView<LinearLayout>(R.id.share_success_container)
        if (item.styleType == VipCategory.DISCOUNT_VIP.styleType && data.size > 1) {
            holder.setText(R.id.share_text, shareText)
                .setText(R.id.tv_vip_discount, title)
                .setText(R.id.tv_vip_discount_desc, desc)
                .setText(R.id.purchase, context.getString(R.string.discount_purchase, title))
            val confirmShare = holder.getView<TextView>(R.id.confirm_share)
            val purchase = holder.getView<TextView>(R.id.purchase)
            val copy = holder.getView<TextView>(R.id.copy)
            if (shareFinish) {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = false
                vipPayTypeView.isVisible = true
            }
            confirmShare.isEnabled = isShare
            confirmShare.setOnClickListener {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = true
            }
            purchase.setOnClickListener {
                shareContainer.isVisible = false
                shareSuccessContainer.isVisible = false
                vipPayTypeView.isVisible = true
                shareFinish = true
//                itemClickListener?.onClickPurchase()
            }
            copy.setOnClickListener {
                if (!shareText.isNullOrEmpty()) {
                    showCopySuccessDialog()
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(context.packageName, shareText))
                }
            }
        } else {
            shareContainer.isVisible = false
            shareSuccessContainer.isVisible = false
            vipPayTypeView.isVisible = true
        }
    }

    fun getSelectedClassify(position: Int): VipClassifyBean? {
        val vipClassifyView = getViewByPosition(position, R.id.vip_classify) as HorizontalVipView
        return vipClassifyView.getSelectedClassify()
    }

    fun getSelectedPayType(position: Int): VipPayBean? {
        val vipClassifyView = getViewByPosition(position, R.id.vip_pay_type) as VipPayTypeView
        return vipClassifyView.getSelectedPayType()
    }

    fun setPayItemClickListener(itemClickListener: OnPayItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    fun clear() {
        if (data.size > 0) {
            val size = data.size - 1
            data.clear()
            notifyItemMoved(0, size)
        }
    }

    private fun showCopySuccessDialog() {
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.copySuccess)
        dialog.setMsg(R.string.copy_success_share_vip_tip)
        dialog.setRightListener(R.string.known) {
            dialog.dismiss()
            isCopy = true
        }
        dialog.show()
    }
}