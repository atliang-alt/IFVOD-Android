package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.blankj.utilcode.util.ScreenUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.CategoryBean

/**
 * 横向滑动筛选
 */
class HorizontalFilterView : HorizontalScrollView {
    var selectListener: OnItemSelectListener? = null

    interface OnItemSelectListener {
        fun onItemSelect(categoryBean: CategoryBean)
    }

    fun setOnItemSelectListener(listener: OnItemSelectListener) {
        selectListener = listener
    }

    constructor(context: Context) : super(context) {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
    }

    fun setView(data: MutableList<CategoryBean>) {
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        for (bean in data) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.layout_filter_item, null)
            val filterName = view.findViewById<TextView>(R.id.filter_name)
            filterName.text = bean.classifyName
            view.setOnClickListener {
                performClick(bean)
                selectListener?.onItemSelect(bean)
            }
            view.tag = bean
            linearLayout.addView(view)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val padding = resources.getDimensionPixelOffset(R.dimen.dp_4)
        linearLayout.setPadding(0, padding, 0, padding)
        addView(linearLayout, params)
    }

    fun performClick(bean: CategoryBean) {
        postDelayed({
            var selectPosition = 0
            var selectIndex = 0
            var isFindTag = false
            for (view in (getChildAt(0) as LinearLayout).children) {
                val filterView = view.findViewById<TextView>(R.id.filter_name)
                if(!isFindTag) {
                    selectPosition += view.measuredWidth
                    selectIndex++
                }
                if (bean == view.tag) {
                    filterView.setTypeface(Typeface.DEFAULT, Typeface.BOLD)
                    filterView.isSelected = true
                    isFindTag = true
                } else {
                    filterView.setTypeface(Typeface.DEFAULT, Typeface.NORMAL)
                    filterView.isSelected = false
                }
            }
            if (selectIndex > 1) {
                smoothScrollTo(selectPosition - ScreenUtils.getScreenWidth() / 2, 0)
            }
        }, 100)
    }
}