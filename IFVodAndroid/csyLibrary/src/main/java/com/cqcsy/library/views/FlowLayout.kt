package com.cqcsy.library.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * 自定义流式布局
 * 使用历史搜索
 */
class FlowLayout : ViewGroup {
    //存储所有子View
    private val mAllChildViews: MutableList<MutableList<View>> =
        ArrayList()
    //每一行的高度
    private val mLineHeight: MutableList<Int> = ArrayList()

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //父控件传进来的宽度和高度以及对应的测量模式
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        //如果当前ViewGroup的宽高为wrap_content的情况
        var width = 0 //自己测量的 宽度
        var height = 0 //自己测量的高度
        //记录每一行的宽度和高度
        var lineWidth = 0
        var lineHeight = 0
        //获取子view的个数
        val childCount = childCount
        for (i in 0 until childCount) {
            val child: View = getChildAt(i)
            //测量子View的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            //得到LayoutParams
            val lp = child.layoutParams as MarginLayoutParams
            //子View占据的宽度
            val childWidth: Int = child.measuredWidth + lp.leftMargin + lp.rightMargin
            //子View占据的高度
            val childHeight: Int = child.measuredHeight + lp.topMargin + lp.bottomMargin
            //换行时候
            if (lineWidth + childWidth > sizeWidth) {
                //对比得到最大的宽度
                width = Math.max(width, lineWidth)
                //重置lineWidth
                lineWidth = childWidth
                //记录行高
                height += lineHeight
                lineHeight = childHeight
            } else { //不换行情况 叠加行宽
                lineWidth += childWidth
                //得到最大行高
                lineHeight = Math.max(lineHeight, childHeight)
            }
            //处理最后一个子View的情况
            if (i == childCount - 1) {
                width = Math.max(width, lineWidth)
                height += lineHeight
            }
        }
        //wrap_content
        setMeasuredDimension(
            if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else width,
            if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else height
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        mAllChildViews.clear()
        mLineHeight.clear()
        //获取当前ViewGroup的宽度
        val width = width

        var lineWidth = 0
        var lineHeight = 0
        //记录当前行的view
        var lineViews: MutableList<View> =
            ArrayList()
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as MarginLayoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            //如果需要换行
            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width) { //记录LineHeight
                mLineHeight.add(lineHeight)
                //记录当前行的Views
                mAllChildViews.add(lineViews)
                //重置行的宽高
                lineWidth = 0
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin
                //重置view的集合
                lineViews = ArrayList()
            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin
            lineHeight =
                Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin)
            lineViews.add(child)
        }
        //处理最后一行
        mLineHeight.add(lineHeight)
        mAllChildViews.add(lineViews)

        //设置子View的位置
        var left = 0
        var top = 0
        //获取行数
        val lineCount: Int = mAllChildViews.size
        for (i in 0 until lineCount) {
            //当前行的views和高度
            lineViews = mAllChildViews[i]
            lineHeight = mLineHeight[i]
            for (j in lineViews.indices) {
                val child = lineViews[j]
                //判断是否显示
                if (child.visibility == View.GONE) {
                    continue
                }
                val lp = child.layoutParams as MarginLayoutParams
                val cLeft = left + lp.leftMargin
                val cTop = top + lp.topMargin
                val cRight = cLeft + child.measuredWidth
                val cBottom = cTop + child.measuredHeight
                //进行子View进行布局
                child.layout(cLeft, cTop, cRight, cBottom)
                left += child.measuredWidth + lp.leftMargin + lp.rightMargin
            }
            left = 0
            top += lineHeight
        }
    }

    /**
     * 与当前ViewGroup对应的LayoutParams
     */
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? {
        return MarginLayoutParams(context, attrs)
    }
}