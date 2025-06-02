package com.cqcsy.lgsp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation


/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/15
 *
 *
 */
class TiktokLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    private var animSet: AnimationSet? = null

    private fun initAnim() {
        val scale = ScaleAnimation(
            0.1f, 1f, 1f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        val alpha = AlphaAnimation(0.8f, 0.2f)
        scale.repeatCount = Animation.INFINITE
        alpha.repeatCount = Animation.INFINITE
        animSet = AnimationSet(true).apply {
            addAnimation(scale)
            addAnimation(alpha)
            duration = 500
        }
        animation = animSet
    }

    fun start() {
        if (animSet == null) {
            initAnim()
        }
        startAnimation(animSet)
    }

    fun stop() {
        animSet?.cancel()
        animation = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (changedView is TiktokLoadingView) {
            if (visibility == VISIBLE) {
                start()
            } else {
                stop()
            }
        }
    }
}