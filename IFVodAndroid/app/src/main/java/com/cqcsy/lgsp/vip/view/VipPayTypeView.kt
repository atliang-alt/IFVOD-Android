package com.cqcsy.lgsp.vip.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.children
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.library.network.H5Address
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.item_vip_pay.view.*
import kotlinx.android.synthetic.main.layout_vip_pay_type.view.*

/**
 * 创建时间：2023/4/12
 *
 */
class VipPayTypeView @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attributes, defStyleAttr) {
    var itemListener: OnItemListener? = null
    private var vipPayList: MutableList<VipPayBean>? = null
    private var selectPayType: VipPayBean? = null
    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.layout_vip_pay_type, this, true)
        vipWebAgreement.setOnClickListener {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, H5Address.VIP_AGREEMENT)
            intent.putExtra(WebViewActivity.titleKey, StringUtils.getString(R.string.vip_rule))
            context.startActivity(intent)
        }
    }

    fun setData(payList: MutableList<VipPayBean>?) {
        vipPayList = payList
        vipPayList?.let {
            addVipPayTypeView(it)
        }
    }

    /**
     * 添加vip支付类型
     */
    private fun addVipPayTypeView(list: MutableList<VipPayBean>) {
        vipPayLayout.removeAllViews()
        for (i in list.indices) {
            val item = list[i]
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_vip_pay, null)
            ImageUtil.loadImage(this, item.img, view.payImage, 0, defaultImage = 0)
            view.payName.text = item.title
            if (i == 0) {
                view.checkbox.isSelected = true
                itemListener?.onSelectPay(item)
                selectPayType = item
            }
            view.payLayout.setOnClickListener {
                performClick(item)
            }
            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(60f)
            )
            view.tag = item
            vipPayLayout.addView(view, params)
        }
    }

    private fun performClick(bean: VipPayBean) {
        for (view in vipPayLayout.children) {
            if (bean == view.tag) {
                view.checkbox.isSelected = true
                itemListener?.onSelectPay(bean)
                selectPayType = bean
            } else {
                view.checkbox.isSelected = false
            }
        }
    }

    fun getSelectedPayType(): VipPayBean? {
        return selectPayType
    }

    /**
     * 支付方式点击回调
     */
    interface OnItemListener {
        fun onSelectPay(payBean: VipPayBean)
    }
}