package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 自定义设置滑动viewpager
 */
class CustomViewpager : ViewPager {

    private var isSliding: Boolean = false

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (isSliding) {
            false
        } else {
            super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (isSliding) {
            false
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }

    fun setIsSlide(isSlide: Boolean) {
        this.isSliding = isSlide
    }
}