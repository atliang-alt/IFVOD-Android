package com.cqcsy.lgsp.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

/**
 * 动画工具类
 */
class AnimationUtil {

    companion object {
        private var mInstance: AnimationUtil? = null

        fun with(): AnimationUtil? {
            if (mInstance == null) {
                synchronized(AnimationUtil::class.java) {
                    if (mInstance == null) {
                        mInstance = AnimationUtil()
                    }
                }
            }
            return mInstance
        }
    }

    /**
     * 从控件所在位置移动到控件的底部
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun moveToViewBottom(v: View, duration: Long, hideView: View) {
        if (v.visibility != View.VISIBLE) return
        val mHiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 1.0f
        )
        mHiddenAction.duration = duration
        v.clearAnimation()
        v.animation = mHiddenAction
        mHiddenAction.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                v.visibility = View.INVISIBLE
                hideView.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    /**
     * 从控件的底部移动到控件所在位置
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun bottomMoveToViewLocation(v: View, duration: Long) {
        if (v.visibility == View.VISIBLE) return
        v.visibility = View.VISIBLE
        val mShowAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        mShowAction.duration = duration
        v.clearAnimation()
        v.animation = mShowAction
    }

    /**
     * 从控件所在位置移动到控件的顶部
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun moveToViewTop(v: View, duration: Long) {
        if (v.visibility != View.VISIBLE) return
        val mHiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, -1.0f
        )
        mHiddenAction.duration = duration
        v.clearAnimation()
        v.animation = mHiddenAction
        mHiddenAction.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                v.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    /**
     * 从控件的顶部移动到控件所在位置
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun topMoveToViewLocation(v: View, duration: Long) {
        if (v.visibility == View.VISIBLE) return
        v.visibility = View.VISIBLE
        val mShowAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        mShowAction.duration = duration
        v.clearAnimation()
        v.animation = mShowAction
    }

    /**
     * 从控件所在位置移动到控件的右侧
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun moveToViewRight(v: View, duration: Long) {
        if (v.visibility != View.VISIBLE) return
        val mHiddenAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        mHiddenAction.duration = duration
        v.clearAnimation()
        v.animation = mHiddenAction
        mHiddenAction.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                v.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
    }

    /**
     * 从控件的右侧移动到控件所在位置
     *
     * @param v
     * @param duration 动画时间
     */
    @Synchronized
    fun rightMoveToViewLocation(v: View, duration: Long) {
        if (v.visibility == View.VISIBLE) return
        v.visibility = View.VISIBLE
        val mShowAction = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 0.0f
        )
        mShowAction.duration = duration
        v.clearAnimation()
        v.animation = mShowAction
    }
}