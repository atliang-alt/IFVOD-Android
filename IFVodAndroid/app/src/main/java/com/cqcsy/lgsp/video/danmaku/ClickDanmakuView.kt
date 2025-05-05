package com.cqcsy.lgsp.video.danmaku

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.kuaishou.akdanmaku.ui.DanmakuView

/**
 ** 2022/9/1
 ** des：可点击弹幕
 **/

class ClickDanmakuView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : DanmakuView(context, attrs) {
    var onDanmakuListener: OnDanmakuListener? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val clickItems = touchHitDanmaku(event.x, event.y)
        if (!clickItems.isNullOrEmpty()) {
            onDanmakuListener?.onDanmakuClickListener(event.x, event.y, clickItems)
            return false
        }
        return super.onTouchEvent(event)
    }

    fun touchHitDanmaku(x: Float, y: Float): MutableList<DanmakuItem>? {
        return danmakuPlayer?.getDanmakusAtPoint(Point(x.toInt(), y.toInt()))?.toMutableList()
    }
}