package com.cqcsy.lgsp.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * 作者：wangjianxiong
 * 创建时间：2023/4/17
 *
 *
 */
class MaxHeightRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RecyclerView(context, attrs, defStyleAttr) {
    var maxHeight = 0
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (maxHeight in 1 until measuredHeight) {
            setMeasuredDimension(measuredWidth, maxHeight)
        }
    }
}