package com.cqcsy.lgsp.video.danmaku

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.ui.DanmakuDisplayer
import com.kuaishou.akdanmaku.utils.Size
import kotlin.math.roundToInt

/**
 * 通用的渲染器
 */
open class CommonRenderer(context: Context) : SimpleRenderer(context) {
    val likeUnSelectedDrawable: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_24_danmu_zan)
    val likeSelectedDrawable: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_24_danmu_zan_selected)
    val likeImageSize = SizeUtils.dp2px(12f)

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 6f
    }

    val likeImagePaint: Paint by lazy {
        TextPaint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    val likeTextPaint: Paint by lazy {
        TextPaint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    val likeStrokeTextPaint: Paint by lazy {
        TextPaint().apply {
            textSize = likeTextPaint.textSize
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.FILL_AND_STROKE
            isAntiAlias = true
        }
    }

    override fun updatePaint(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ) {
        super.updatePaint(item, displayer, config)
        val danmakuItemData = item.data as BaseDanmakuItemData
        likeTextPaint.color = if (danmakuItemData.barrageBean.isLike) {
            ColorUtils.getColor(R.color.blue)
        } else {
            ColorUtils.getColor(R.color.white)
        }
        likeTextPaint.textSize = smallSize * config.textSizeScale
        likeTextPaint.typeface = if (config.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        likeStrokeTextPaint.textSize = likeTextPaint.textSize
        likeStrokeTextPaint.typeface = likeTextPaint.typeface
        likeStrokeTextPaint.color =
            if (likeTextPaint.color == DEFAULT_DARK_COLOR) Color.WHITE else Color.BLACK
    }

    override fun measure(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ): Size {
        updatePaint(item, displayer, config)
        val itemData = item.data as BaseDanmakuItemData
        val danmakuItemData = item.data
        var textWidth =
            textPaint.measureText(danmakuItemData.content, 0, danmakuItemData.content.length)
        val textHeight = getCacheHeight(textPaint)
        if (itemData.barrageBean.good > 0) {
            val likeCount = itemData.barrageBean.good.toString()
            textWidth += SizeUtils.dp2px(10f) * config.textSizeScale
            textWidth += likeImageSize * config.textSizeScale
            textWidth += SizeUtils.dp2px(4f) * config.textSizeScale
            textWidth += likeTextPaint.measureText(likeCount, 0, likeCount.length)
        }
        return Size(
            textWidth.roundToInt() + CANVAS_PADDING,
            textHeight.roundToInt() + CANVAS_PADDING
        )
    }

    override fun draw(
        item: DanmakuItem,
        canvas: Canvas,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ) {
        val itemData = item.data as BaseDanmakuItemData
        updatePaint(item, displayer, config)
        val textBaseLine = CANVAS_PADDING * 0.5f - textPaint.ascent() // 开始绘制位置
        var left = 0f
        val x = CANVAS_PADDING * 0.5f
        val y = CANVAS_PADDING * 0.5f - textPaint.ascent()
        canvas.drawText(itemData.content, 0, itemData.content.length, x, y, strokePaint)
        canvas.drawText(itemData.content, 0, itemData.content.length, x, y, textPaint)
        left += textPaint.measureText(itemData.content, 0, itemData.content.length)
        if (itemData.barrageBean.good > 0) {
            left += SizeUtils.dp2px(10f) * config.textSizeScale
            val drawable = if (itemData.barrageBean.isLike) {
                likeSelectedDrawable
            } else {
                likeUnSelectedDrawable
            }
            val likeImageMatrix = Matrix()
            likeImageMatrix.postScale(config.textSizeScale, config.textSizeScale)
            val height = SizeUtils.dp2px(12f) * config.textSizeScale
            likeImageMatrix.postTranslate(left, textBaseLine - height)
            canvas.drawBitmap(drawable, likeImageMatrix, likeImagePaint)
            left += SizeUtils.dp2px(12f + 4f) * config.textSizeScale

            val likeCount = itemData.barrageBean.good.toString()
            canvas.drawText(
                likeCount,
                0,
                likeCount.length,
                left,
                textBaseLine,
                likeStrokeTextPaint
            )
            canvas.drawText(likeCount, left, textBaseLine, likeTextPaint)
        }
        if (itemData.danmakuStyle == DanmakuItemData.DANMAKU_STYLE_SELF_SEND) {
            canvas.drawRect(
                0f,
                0f,
                canvas.width.toFloat(),
                canvas.height.toFloat(),
                borderPaint
            )
        }
    }

    companion object {
        private val smallSize = SizeUtils.sp2px(12f)
    }
}