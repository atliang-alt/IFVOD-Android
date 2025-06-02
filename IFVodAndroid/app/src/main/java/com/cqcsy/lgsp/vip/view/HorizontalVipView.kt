package com.cqcsy.lgsp.vip.view

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.lgsp.vip.VipCategory
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import kotlinx.android.synthetic.main.item_vip_classify.view.*

/**
 * 横向滑动Vip套餐
 */
class HorizontalVipView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet?,
    def: Int = 0
) : HorizontalScrollView(context, attributeSet, def) {
    var selectListener: OnItemSelectListener? = null
    private lateinit var vipCategory: VipCategory
    private var selectId: String? = null

    interface OnItemSelectListener {
        fun onItemSelect(vipClassifyBean: VipClassifyBean)
    }

    fun setOnItemSelectListener(listener: OnItemSelectListener) {
        selectListener = listener
    }

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    fun setView(
        vipCategory: VipCategory,
        data: MutableList<VipClassifyBean>,
        selectId: String? = null
    ) {
        removeAllViews()
        this.vipCategory = vipCategory
        this.selectId = selectId
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        var selectedPosition = 0
        for (i in data.indices) {
            val item = data[i]
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_vip_classify, null)
            if (item.giftDays > 0) {
                view.vipGiftDays.text = StringUtils.getString(R.string.giftDays, item.giftDays)
                view.vipGiftDays.visibility = View.VISIBLE
            } else {
                view.vipGiftDays.visibility = View.GONE
            }
            setItemStyle(view)
            view.vipDays.text = StringUtils.getString(R.string.days, item.validityDays)
            view.vipClassifyName.text = item.name
            view.vipPriceSign.text = item.priceSymbol
            val promotions = item.promotions
            if (!item.disprice.isNullOrEmpty() && !item.disrmb.isNullOrEmpty()) {
                view.vipDisPrice.text = item.disprice
                view.vipRmb.text = StringUtils.getString(R.string.rmbSign, item.disrmb)

                view.vipPrice.visibility = View.VISIBLE
                view.vipPrice.text =
                    StringUtils.getString(R.string.old_price, (item.priceSymbol + item.price))
                view.vipPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
                view.vipPrice.paint.isAntiAlias = true
            } else {
                view.vipPrice.visibility = View.INVISIBLE
                view.vipDisPrice.text = item.price
                view.vipRmb.text = StringUtils.getString(R.string.rmbSign, item.rmb)
            }

            val categoryName = item.categoryName
            if (!promotions.isNullOrEmpty() && promotions[0].title.isNotEmpty()) {
                view.vipTag.setBackgroundResource(R.mipmap.icon_first_discount)
                view.vipTag.visibility = View.VISIBLE
                view.vipTag.text = promotions[0].title
                view.vipTag.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else if (!categoryName.isNullOrEmpty()) {
                view.vipTag.visibility = View.VISIBLE
                view.vipTag.setBackgroundResource(R.mipmap.img_vip_tag_bg)
                view.vipTag.setTextColor(ContextCompat.getColor(context, R.color.black))
                view.vipTag.text = categoryName
            } else {
                view.vipTag.visibility = View.GONE
            }
            when (item.menberCount) {
                2 -> {
                    view.vipNumbTag.setImageResource(R.mipmap.icon_vip_two_tag)
                    view.vipNumbTag.visibility = View.VISIBLE
                }

                3 -> {
                    view.vipNumbTag.setImageResource(R.mipmap.icon_vip_three_tag)
                    view.vipNumbTag.visibility = View.VISIBLE
                }

                else -> {
                    if (item.bargain) {
                        view.vipNumbTag.visibility = View.VISIBLE
                        view.vipNumbTag.setImageResource(R.mipmap.icon_vip_bargain)
                    } else {
                        view.vipNumbTag.visibility = View.GONE
                    }
                }
            }
            if (selectId != null) {
                if (item.packageId.toString() == selectId) {
                    selectedPosition = i
                    setSelectedStyle(view, item.type)
                    setSelectPosition(view)
                } else {
                    setNormalStyle(view)
                }
            } else {
                if (i == 0) {
                    this.selectId = item.packageId.toString()
                    setSelectedStyle(view, item.type)
                    setSelectPosition(view)
                } else {
                    setNormalStyle(view)
                }
            }
            view.setOnClickListener {
                this.selectId = item.packageId.toString()
                performClick(item)
                selectListener?.onItemSelect(item)
            }
            view.tag = item
            when (i) {
                0 -> {
                    view.setPadding(
                        SizeUtils.dp2px(12f),
                        SizeUtils.dp2px(22f),
                        SizeUtils.dp2px(5f),
                        0
                    )
                }

                (data.size - 1) -> {
                    view.setPadding(
                        SizeUtils.dp2px(5f),
                        SizeUtils.dp2px(22f),
                        SizeUtils.dp2px(12f),
                        0
                    )
                }

                else -> {
                    view.setPadding(
                        SizeUtils.dp2px(5f),
                        SizeUtils.dp2px(22f),
                        SizeUtils.dp2px(5f),
                        0
                    )
                }
            }
            linearLayout.addView(view)
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        addView(linearLayout, params)
        if (data.isNotEmpty()) {
            val classifyBean = data[selectedPosition]
            this.selectId = classifyBean.packageId.toString()
            performClick(classifyBean)
//            linearLayout.getChildAt(0)?.performClick()
        }
    }

    fun getSelectedClassify(): VipClassifyBean? {
        for (view in (getChildAt(0) as LinearLayout).children) {
            val item = view.tag as VipClassifyBean
            if (selectId == item.packageId.toString()) {
                return item
            }
        }
        return null
    }

    private fun performClick(bean: VipClassifyBean) {
        for (view in (getChildAt(0) as LinearLayout).children) {
            if (bean == view.tag) {
                setSelectedStyle(view, bean.type)
                setSelectPosition(view)
            } else {
                setNormalStyle(view)
            }
        }
    }

    private fun setSelectPosition(view: View) {
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                smoothScrollTo(view.x.toInt(), 0)
                return false
            }

        })
    }

    private fun setItemStyle(view: View) {
        if (vipCategory == VipCategory.LOCAL_VIP) {
            view.itemVipLayout.setBackgroundResource(R.drawable.vip_classify_item_selector)
            view.vipDays.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.vip_classify_days_text_color_selector
                )
            )
            view.vipGiftDays.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.vip_classify_days_text_color_selector
                )
            )
        } else {
            view.vipDays.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.international_vip_classify_days_text_color_selector
                )
            )
            view.vipGiftDays.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.international_vip_classify_days_text_color_selector
                )
            )
            view.itemVipLayout.setBackgroundResource(R.drawable.international_vip_classify_item_selector)
        }
    }

    /**
     * vip套餐
     * 设置没有选中状态样式
     */
    private fun setNormalStyle(view: View) {
        view.itemVipLayout.isSelected = false
        view.vipGradeImage.setImageResource(R.mipmap.icon_vip_level_0)
        view.vipDays.isSelected = false
        view.vipClassifyName.isSelected = false
        view.vipPriceSign.isSelected = false
        view.vipPrice.isSelected = false
        view.vipDisPrice.isSelected = false
        view.vipGiftDays.isSelected = false
    }

    /**
     * vip套餐
     * 设置选中状态样式
     */
    private fun setSelectedStyle(view: View, grade: Int) {
        view.itemVipLayout.isSelected = true
        view.vipDays.isSelected = true
        view.vipClassifyName.isSelected = true
        view.vipPriceSign.isSelected = true
        view.vipPrice.isSelected = true
        view.vipDisPrice.isSelected = true
        view.vipGiftDays.isSelected = true
        view.vipGradeImage.setImageResource(VipGradeImageUtil.getVipImage(grade))
    }
}