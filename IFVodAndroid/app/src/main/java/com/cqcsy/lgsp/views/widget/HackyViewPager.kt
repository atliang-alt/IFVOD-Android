package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 解决photoview和viewpager冲突
 */
class HackyViewPager : ViewPager {

    private var isLocked = false

    constructor(context: Context) : super(context) {
        isLocked = false
    }

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    {
        isLocked = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (!isLocked) {
            try {
                super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                false
            }
        } else false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return !isLocked && super.onTouchEvent(event)
    }

    fun toggleLock() {
        isLocked = !isLocked
    }

    fun setLocked(isLocked: Boolean) {
        this.isLocked = isLocked
    }

    fun isLocked(): Boolean {
        return isLocked
    }
}