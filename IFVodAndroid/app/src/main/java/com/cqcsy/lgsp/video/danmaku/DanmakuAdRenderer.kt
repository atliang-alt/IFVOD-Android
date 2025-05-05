package com.cqcsy.lgsp.video.danmaku

import android.content.Context
import android.graphics.*
import android.graphics.Shader.TileMode
import android.text.TextPaint
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.ui.DanmakuDisplayer
import com.kuaishou.akdanmaku.utils.Size
import kotlin.math.roundToInt

/**
 * 弹幕广告渲染器
 */
class DanmakuAdRenderer(context: Context) : SimpleRenderer(context) {

    private val receivedCoinBitmap: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_danmaku_ad_received_coid)
    private val notReceivedCoinBitmap: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_danmaku_ad_coin)

    private val coinImagePaint: Paint by lazy {
        TextPaint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    // 昵称
    private val namePaint = TextPaint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val nameStrokePaint = TextPaint().apply {
        textSize = namePaint.textSize
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    private val buttonPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val buttonStrokeBgPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 2f
    }

    // 头像
    private val imagePaint = TextPaint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val buttonTextPaint = TextPaint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun updatePaint(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ) {
        super.updatePaint(item, displayer, config)
        // update textPaint
        namePaint.color = ColorUtils.getColor(R.color.yellow)
        namePaint.textSize = nameTextSize * config.textSizeScale
        namePaint.typeface = if (config.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        // update strokePaint
        nameStrokePaint.textSize = namePaint.textSize
        nameStrokePaint.typeface = namePaint.typeface
        nameStrokePaint.color =
            if (namePaint.color == DEFAULT_DARK_COLOR) Color.WHITE else Color.BLACK
        buttonTextPaint.color = Color.WHITE
        buttonTextPaint.textSize = buttonTextSize * config.textSizeScale
    }

    override fun measure(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ): Size {
        updatePaint(item, displayer, config)
        val itemData = item.data as BaseDanmakuItemData
        if (itemData is AdvertModel) {
            var width = CANVAS_PADDING * config.textSizeScale + textPaint.measureText(
                itemData.content,
                0,
                itemData.content.length
            )
            var height = getCacheHeight(textPaint)
            width += notReceivedCoinBitmap.width * config.textSizeScale
            width += SizeUtils.dp2px(6f) * config.textSizeScale

            if (itemData.userImage != null) {
                width += avatarSize * config.textSizeScale
                //文字头像间距
                width += SizeUtils.dp2px(3f) * config.textSizeScale
                height = avatarSize * config.textSizeScale
            }
            if (!itemData.nickName.isNullOrEmpty()) {
                width += namePaint.measureText(itemData.nickName).roundToInt()
                width += SizeUtils.dp2px(5f) * config.textSizeScale
            }
            width += SizeUtils.dp2px(5f)
            val text = itemData.buttonDesc
            width += buttonTextPaint.measureText(text) + SizeUtils.dp2px(8f) * 2 * config.textSizeScale
            height += CANVAS_PADDING
            return Size(width.roundToInt(), height.roundToInt())
        } else {
            return super.measure(item, displayer, config)
        }
    }

    override fun draw(
        item: DanmakuItem,
        canvas: Canvas,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ) {
        val itemData = item.data as BaseDanmakuItemData
        updatePaint(item, displayer, config)
        if (itemData is AdvertModel) {
            val textBaseLine = CANVAS_PADDING * 0.5f - textPaint.ascent() // 开始绘制位置
            var left = 0f
            //绘制金币
            val coinMatrix = Matrix()
            coinMatrix.postScale(config.textSizeScale, config.textSizeScale)
            val bitmap = if (itemData.isReceived) {
                receivedCoinBitmap
            } else {
                notReceivedCoinBitmap
            }
            canvas.drawBitmap(bitmap, coinMatrix, coinImagePaint)
            left += bitmap.width * config.textSizeScale
            left += SizeUtils.dp2px(6f)

            if (itemData.userImage != null) {
                val matrix = Matrix()
                matrix.postScale(config.textSizeScale, config.textSizeScale)
                matrix.postTranslate(left, 0f)
                canvas.drawBitmap(itemData.userImage!!, matrix, imagePaint)
                left += avatarSize * config.textSizeScale
                left += SizeUtils.dp2px(3f) * config.textSizeScale
            }
            if (!itemData.nickName.isNullOrEmpty()) {
                canvas.drawText(
                    itemData.nickName!!,
                    0,
                    itemData.nickName!!.length,
                    left,
                    textBaseLine,
                    nameStrokePaint
                )
                canvas.drawText(
                    itemData.nickName!!,
                    0,
                    itemData.nickName!!.length,
                    left,
                    textBaseLine,
                    namePaint
                )
                left += namePaint.measureText(itemData.nickName) + SizeUtils.dp2px(5f) * config.textSizeScale
            }
            //绘制文字黑边
            canvas.drawText(
                itemData.content,
                0,
                itemData.content.length,
                left,
                textBaseLine,
                strokePaint
            )
            canvas.drawText(
                itemData.content,
                0,
                itemData.content.length,
                left,
                textBaseLine,
                textPaint
            )
            left += textPaint.measureText(
                itemData.content,
                0,
                itemData.content.length
            )
            left += SizeUtils.dp2px(5f)
            val buttonDesc = itemData.buttonDesc
            val width =
                buttonTextPaint.measureText(buttonDesc) + SizeUtils.dp2px(8f) * 2 * config.textSizeScale
            val actionHeight = SizeUtils.dp2px(18f)
            val height =
                (textBaseLine + textPaint.descent() - (textPaint.descent() - textPaint.ascent()) / 2) * 2
            val right = left + width
            val top = (height - actionHeight) / 2f
            val bottom = actionHeight + top

            buttonPaint.shader = LinearGradient(
                left, height / 2f, left + width, height / 2f,
                intArrayOf(
                    ColorUtils.getColor(R.color.color_80f176ff),
                    ColorUtils.getColor(R.color.color_803779ff)
                ), null, TileMode.CLAMP
            )
            canvas.drawRoundRect(left, top, right, bottom, 26f, 26f, buttonPaint)

            buttonStrokeBgPaint.shader = LinearGradient(
                left, height / 2f, left + width, height / 2f,
                intArrayOf(
                    ColorUtils.getColor(R.color.color_f176ff),
                    ColorUtils.getColor(R.color.color_3779ff)
                ), null, TileMode.CLAMP
            )
            canvas.drawRoundRect(left, top, right, bottom, 26f, 26f, buttonStrokeBgPaint)
            val baseLine =
                height / 2 + (buttonTextPaint.descent() - buttonTextPaint.ascent()) / 2 - buttonTextPaint.descent()
            left += SizeUtils.dp2px(8f) * config.textSizeScale
            canvas.drawText(
                buttonDesc,
                0,
                buttonDesc.length,
                left,
                baseLine,
                buttonTextPaint
            )
        } else {
            super.draw(item, canvas, displayer, config)
        }
    }

    companion object {
        private val nameTextSize = SizeUtils.sp2px(12f)
        private val buttonTextSize = SizeUtils.sp2px(12f)
        private val avatarSize = SizeUtils.dp2px(24f)

        fun getCoinArea(textSizeScale: Float, position: RectF): FloatArray {
            val start = position.left
            val end = position.left + avatarSize * textSizeScale
            return floatArrayOf(start, end)
        }

        /**
         * 获取查看详情点击区域
         */
        fun getCheckDetailArea(
            textSizeScale: Float,
            position: RectF,
            data: AdvertModel
        ): FloatArray {
            val paint = Paint().apply {
                textSize = textSizeScale * nameTextSize
            }
            val start =
                position.left + avatarSize * textSizeScale * 2 + SizeUtils.dp2px(6f) * textSizeScale + paint.measureText(
                    data.nickName
                ) * textSizeScale + SizeUtils.dp2px(
                    5f
                ) * textSizeScale
            val end = position.right
            return floatArrayOf(start, end)
        }
    }
}