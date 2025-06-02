package com.cqcsy.lgsp.vip.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.cqcsy.lgsp.R
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.lgsp.vip.VipCategory
import kotlinx.android.synthetic.main.layout_vip_classify.view.*

/**
 * Vip选择套餐、支付方式选择 View
 */
class VipClassifyModeView : LinearLayout {
    private var vipClassifyList: MutableList<VipClassifyBean> = ArrayList()
    private var itemListener: OnItemListener? = null
    private lateinit var vipCategory: VipCategory;

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.layout_vip_classify, this, true)
        orientation = VERTICAL
    }

    fun setData(
        id: Int, remark: String?,
        classifyList: MutableList<VipClassifyBean>?,
        selectId: String? = null
    ) {
        vipClassifyList.clear()
        vipCategory = VipCategory.creator(id)
        if (remark.isNullOrEmpty()) {
            vipRemark.isVisible = false
        } else {
            vipRemark.isVisible = true
            vipRemark.text = remark
        }
        if (!classifyList.isNullOrEmpty()) {
            vipClassifyList.addAll(classifyList)
            addVipClassifyView(vipClassifyList, selectId)
        }

    }

    fun setOnItemClickListener(listener: OnItemListener) {
        itemListener = listener
    }

    /**
     * 添加vip套餐view
     */
    private fun addVipClassifyView(list: MutableList<VipClassifyBean>, selectId: String? = null) {
        vipOptions.removeAllViews()
        vipOptions.setView(vipCategory, list, selectId)
        vipOptions.setOnItemSelectListener(object : HorizontalVipView.OnItemSelectListener {
            override fun onItemSelect(vipClassifyBean: VipClassifyBean) {
                itemListener?.onSelectClassify(vipClassifyBean)
            }
        })
    }

    fun getSelectedClassify(): VipClassifyBean? {
        return vipOptions.getSelectedClassify()
    }


    /**
     * 支付套餐点击回调
     */
    interface OnItemListener {
        fun onSelectClassify(vipClassifyBean: VipClassifyBean)
    }
}