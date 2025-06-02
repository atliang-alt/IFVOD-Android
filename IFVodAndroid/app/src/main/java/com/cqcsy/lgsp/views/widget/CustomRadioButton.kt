package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatRadioButton
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R


/**
 * 自定义右上角显示红点RadioButton
 */
class CustomRadioButton : AppCompatRadioButton {
    private var isShowDot = false
    private val color = ColorUtils.getColor(R.color.red)
    private val radius = SizeUtils.dp2px(2.5f)
    private val marginTop = SizeUtils.dp2px(8f)
    private val marginRight = SizeUtils.dp2px(3f)

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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isShowDot) {
            var cx: Float = (width - marginRight - radius).toFloat()
            val cy: Float = (marginTop + radius).toFloat()
            val drawableTop = compoundDrawables[1]
            if (drawableTop != null) {
                val drawableTopWidth = drawableTop.intrinsicWidth
                if (drawableTopWidth > 0) {
                    val dotLeft = width / 2 + drawableTopWidth / 2
                    cx = dotLeft.toFloat()
                }
            }
            val paint: Paint = paint
            //save
            val tempColor = paint.color
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(cx, cy, radius.toFloat(), paint)
            //restore
            paint.color = tempColor
        }
    }

    /**
     * 设置是否显示小圆点
     */
    fun setShowSmallDot(isShowDot: Boolean) {
        this.isShowDot = isShowDot
        invalidate()
    }
}