package com.cqcsy.lgsp.views.emotion

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import java.util.*

/**
 * 自定义表情底部指示器
 */
class EmotionIndicatorView : LinearLayout {
    private var mImageViews //所有指示器集合
            : ArrayList<View>? = null
    private val size = 6f
    private val marginSize = 4f
    // 指示器的大小
    private val pointSize: Int
    // 间距
    private val marginLeft: Int

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
        pointSize = SizeUtils.dp2px(size)
        marginLeft = SizeUtils.dp2px(marginSize)
    }

    /**
     * 初始化指示器
     * @param count 指示器的数量
     */
    fun initIndicator(count: Int) {
        mImageViews = ArrayList()
        removeAllViews()
        var lp: LayoutParams
        for (i in 0 until count) {
            val v = View(context)
            lp = LayoutParams(pointSize, SizeUtils.dp2px(2f))
            if (i != 0) lp.leftMargin = marginLeft
            v.layoutParams = lp
            v.setBackgroundResource(R.drawable.emoji_indicator_point_selector)
            v.isSelected = i == 0
            mImageViews!!.add(v)
            this.addView(v)
        }
    }

    /**
     * 页面移动时切换指示器
     */
    fun playByStartPointToNext(startPosition: Int, nextPosition: Int) {
        var startPosition = startPosition
        var nextPosition = nextPosition
        if (startPosition < 0 || nextPosition < 0 || nextPosition == startPosition) {
            nextPosition = 0
            startPosition = nextPosition
        }
        val viewStart = mImageViews!![startPosition]
        val viewNext = mImageViews!![nextPosition]
        viewNext.isSelected = true
        viewStart.isSelected = false
    }

}