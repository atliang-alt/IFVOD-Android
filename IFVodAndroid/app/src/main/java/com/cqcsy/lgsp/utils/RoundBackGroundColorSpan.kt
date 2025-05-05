package com.cqcsy.lgsp.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

import android.text.style.ReplacementSpan
import com.blankj.utilcode.util.SizeUtils
import kotlin.math.abs

/**
 * spannable string 背景圆角
 */
class RoundBackGroundColorSpan(private val bgColor: Int, private val textColor: Int) :
    ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt() + 10
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val backColor: Int = paint.color
        val backSize: Float = paint.textSize
        paint.color = bgColor
        canvas.drawRoundRect(
            RectF(
                x,
                top + 3f,
                x + (paint.measureText(text, start, end).toInt() + 5),
                bottom - 3f
            ), 4f, 4f, paint
        )
        paint.color = textColor
        if (text != null) {
            paint.textSize = SizeUtils.sp2px(10f).toFloat()
            canvas.drawText(text, start, end, x + 15, y - abs(paint.textSize - backSize) / 2, paint)
        }
        paint.color = backColor
        paint.textSize = backSize
    }
}