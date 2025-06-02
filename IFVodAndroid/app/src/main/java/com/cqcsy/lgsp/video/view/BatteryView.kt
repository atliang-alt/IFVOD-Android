package com.cqcsy.lgsp.video.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.util.AttributeSet
import android.view.View
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import java.util.*

/**
 * 电量时间显示view
 */
class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    View(context, attrs, defStyleAttr, defStyleRes) {
    private var mPaint: Paint? = null //文字的画笔
    private val textSize: Float = SizeUtils.sp2px(12f).toFloat()

    private var mBatteryPaint: Paint? = null //电池画笔边框
    private var mBatteryRect: RectF? = null //电池矩形
    private val mBatteryStroke = 2f //电池框宽度

    private var mPowerPaint: Paint? = null //电量画笔
    private var mPowerRect: RectF? = null //电量矩形
    private val batteryColor: Int = ColorUtils.getColor(R.color.white_40) //电池框颜色
    private val powerColor: Int = ColorUtils.getColor(R.color.white) //电量颜色
    private val lowPowerColor: Int = ColorUtils.getColor(R.color.white) //低电颜色
    private var power = 50 //当前电量（满电100

    private val batteryHeight = SizeUtils.dp2px(7f)
    private val batteryWidth = SizeUtils.dp2px(18f)

    private val batteryBorderPadding = 2f
    private val borderCorner = SizeUtils.dp2px(3f).toFloat()

    private var mCapPaint: Paint? = null //电池画笔边框
    private val mCapWidth: Float = 4f  // 电池盖宽度
    private val mCapHeight: Float = SizeUtils.dp2px(4f).toFloat()  // 电池盖高度
    private var mCapRect: RectF? = null //电池盖矩形
    private var time = 0L

    private val totalWidth =
        batteryWidth + batteryBorderPadding * 3 + mBatteryStroke * 2 + mCapWidth
    private val totalHeight =
        batteryHeight + batteryBorderPadding * 2 + mBatteryStroke * 2
    private var textWidth = (textSize * 3).toInt()

    init {
        initPaint()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            textWidth + marginLeft + marginRight + paddingLeft + paddingRight,
            (totalHeight + batteryBorderPadding + textSize).toInt()
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        time = System.currentTimeMillis()
        val timeFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        context.registerReceiver(timeReceiver, timeFilter)
        getBattery()
    }

    override fun onDetachedFromWindow() {
        context.unregisterReceiver(timeReceiver)
        super.onDetachedFromWindow()
    }

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            time = System.currentTimeMillis()
            getBattery()
        }

    }

    private fun getBattery() {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent = context.registerReceiver(null, batteryFilter)
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
        if (level == null || scale == null || level == 0 || scale == 0) {
            invalidate()
        } else {
            setPro(level * 100 / scale)
        }
    }

    private fun setPro(power: Int) {
        var power = power
        if (power < 0) {
            power = 0
        } else if (power > 100) {
            power = 100
        }
        this.power = power
        invalidate()
    }

    private fun initPaint() {
        /**
         * 设置电池画笔
         */
        mBatteryPaint = Paint()
        mBatteryPaint!!.color = batteryColor
        mBatteryPaint!!.isAntiAlias = true
        mBatteryPaint!!.style = Paint.Style.STROKE
        mBatteryPaint!!.strokeWidth = mBatteryStroke
        /**
         * 设置电量画笔
         */
        mPowerPaint = Paint()
        mPowerPaint!!.isAntiAlias = true
        mPowerPaint!!.style = Paint.Style.FILL
        /**
         * 设置电池盖画笔
         */
        mCapPaint = Paint()
        mCapPaint!!.isAntiAlias = true
        mCapPaint!!.style = Paint.Style.FILL
        mCapPaint!!.color = batteryColor
        /**
         * 设置文字画笔
         */
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.textSize = textSize
        mPaint!!.color = powerColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (power <= 20) {
            mPowerPaint!!.color = lowPowerColor
        } else {
            mPowerPaint!!.color = powerColor
        }
        val textString = TimeUtils.date2String(Date(time), "HH:mm")
        val textRect = Rect()
        mPaint!!.getTextBounds(textString, 0, textString.length, textRect)
        textWidth = textRect.width()
        val fontMetrics = mPaint!!.fontMetrics
        val top = fontMetrics.top //为基线到字体上边框的距离
        val bottom = fontMetrics.bottom //为基线到字体下边框的距离
        val baseLineY =
            (totalHeight + batteryBorderPadding * 2 + textSize / 2 - top / 2 - bottom / 2).toInt() //基线中间点的y轴计算公式
        canvas.drawText(
            textString,
            0f,
            baseLineY.toFloat(),
            mPaint!!
        )

        val batteryStart = textWidth - totalWidth
        /**
         * 设置电池矩形边框
         */
        mBatteryRect = RectF(
            batteryStart,
            0f,
            batteryStart + batteryBorderPadding * 2 + batteryWidth + mBatteryStroke * 2,
            batteryBorderPadding * 2 + batteryHeight + mBatteryStroke * 2
        )
        /**
         * 设置电池盖矩形
         */
        mCapRect = RectF(
            mBatteryRect!!.right + batteryBorderPadding,
            mBatteryRect!!.height() / 2 - mCapHeight / 2,
            mBatteryRect!!.right + batteryBorderPadding + mCapWidth,
            mBatteryRect!!.height() / 2 + mCapHeight / 2
        )
        /**
         * 设置电量矩形
         */
        val start = mBatteryRect!!.left + batteryBorderPadding + mBatteryStroke
        mPowerRect = RectF(
            start,
            mBatteryRect!!.top + batteryBorderPadding + mBatteryStroke,
            start + batteryWidth * power / 100,
            mBatteryRect!!.bottom - batteryBorderPadding - mBatteryStroke
        )
        canvas.drawRoundRect(mBatteryRect!!, borderCorner, borderCorner, mBatteryPaint!!)
        canvas.drawRoundRect(mCapRect!!, borderCorner, borderCorner, mCapPaint!!) // 画电池盖
        canvas.drawRoundRect(
            mPowerRect!!,
            batteryBorderPadding,
            batteryBorderPadding,
            mPowerPaint!!
        ) // 画电量

    }
}