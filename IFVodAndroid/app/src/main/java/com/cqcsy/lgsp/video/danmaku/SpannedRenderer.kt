package com.cqcsy.lgsp.video.danmaku

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.kuaishou.akdanmaku.ui.DanmakuDisplayer
import com.kuaishou.akdanmaku.utils.Size
import kotlin.math.roundToInt

/**
 * 图文混排
 */
class SpannedRenderer(context: Context) : CommonRenderer(context) {
    private val locationDrawable: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_danmaku_location)
    private val bigVDrawable: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.mipmap.icon_big_v_small)
    private val avatarSize = SizeUtils.dp2px(24f)
    private val bigVSize = SizeUtils.dp2px(12f)
    private val locationSize = SizeUtils.dp2px(10f)

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

    // 地区
    private val areaPaint = TextPaint().apply {
        color = ColorUtils.getColor(R.color.white_80)
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val areaStrokePaint = TextPaint().apply {
        textSize = areaPaint.textSize
        color = Color.BLACK
        strokeWidth = 3f
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 6f
    }

    // 头像
    private val imagePaint = TextPaint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // 大V\地区图标
    private val locationPaint = TextPaint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun updatePaint(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ) {
        super.updatePaint(item, displayer, config)
        val danmakuItemData = item.data as BaseDanmakuItemData
        if (danmakuItemData is SpannedModel) {
            // update textPaint
            namePaint.color =
                ColorUtils.getColor(if (danmakuItemData.isBigV) R.color.blue else R.color.yellow)
            namePaint.textSize = smallSize * config.textSizeScale
            namePaint.typeface = if (config.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            // update strokePaint
            nameStrokePaint.textSize = namePaint.textSize
            nameStrokePaint.typeface = namePaint.typeface
            nameStrokePaint.color =
                if (namePaint.color == DEFAULT_DARK_COLOR) Color.WHITE else Color.BLACK

            areaPaint.textSize = smallSize * config.textSizeScale
            areaPaint.typeface = if (config.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            areaStrokePaint.textSize = areaPaint.textSize
            areaStrokePaint.typeface = areaPaint.typeface
        }
    }

    override fun measure(
        item: DanmakuItem,
        displayer: DanmakuDisplayer,
        config: DanmakuConfig
    ): Size {
        updatePaint(item, displayer, config)
        val itemData = item.data as BaseDanmakuItemData
        if (itemData is SpannedModel) {
            var width = CANVAS_PADDING * config.textSizeScale + textPaint.measureText(
                itemData.content,
                0,
                itemData.content.length
            )
            var height = getCacheHeight(textPaint)
            if (itemData.userImage != null) {
                width += avatarSize * config.textSizeScale
                //文字头像间距
                width += SizeUtils.dp2px(3f) * config.textSizeScale
                height = avatarSize * config.textSizeScale
            }
            if (itemData.isBigV) {
                width += bigVSize * config.textSizeScale
                width += SizeUtils.dp2px(3f) * config.textSizeScale
            }
            if (!itemData.nickName.isNullOrEmpty()) {
                width += namePaint.measureText(itemData.nickName).roundToInt()
                width += SizeUtils.dp2px(5f) * config.textSizeScale
            }
            if (!itemData.location.isNullOrEmpty()) {
                width += locationSize * config.textSizeScale
                width += SizeUtils.dp2px(2f) * config.textSizeScale
                width += areaPaint.measureText(
                    itemData.location,
                    0,
                    itemData.location!!.length
                )
            }
            if (itemData.barrageBean.good > 0) {
                val likeCount = itemData.barrageBean.good.toString()
                width += SizeUtils.dp2px(10f) * config.textSizeScale
                width += likeImageSize * config.textSizeScale
                width += SizeUtils.dp2px(4f) * config.textSizeScale
                width += likeTextPaint.measureText(likeCount, 0, likeCount.length)
            }
            height += CANVAS_PADDING
            return Size(width.roundToInt(), height.roundToInt())
        }
        return super.measure(item, displayer, config)
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
        if (itemData is SpannedModel) {
            if (itemData.userImage != null) {
                val matrix = Matrix()
                matrix.postScale(config.textSizeScale, config.textSizeScale)
                canvas.drawBitmap(itemData.userImage!!, matrix, imagePaint)
                left += avatarSize * config.textSizeScale
                left += SizeUtils.dp2px(3f) * config.textSizeScale
            }
            if (itemData.isBigV) {
                val matrix = Matrix()
                matrix.postScale(config.textSizeScale, config.textSizeScale)
                val height = bigVSize * config.textSizeScale
                matrix.postTranslate(left, textBaseLine - height)
                canvas.drawBitmap(bigVDrawable, matrix, locationPaint)
                left += bigVSize * config.textSizeScale
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
            if (!itemData.location.isNullOrEmpty()) {
                val locationMatrix = Matrix()
                locationMatrix.postScale(config.textSizeScale, config.textSizeScale)
                val height = locationSize * config.textSizeScale
                locationMatrix.postTranslate(left, textBaseLine - height)
                canvas.drawBitmap(locationDrawable, locationMatrix, locationPaint)
                left += locationSize * config.textSizeScale
                left += SizeUtils.dp2px(2f) * config.textSizeScale

                canvas.drawText(
                    itemData.location!!,
                    0,
                    itemData.location!!.length,
                    left,
                    textBaseLine,
                    areaStrokePaint
                )
                canvas.drawText(itemData.location!!, left, textBaseLine, areaPaint)
                left += areaPaint.measureText(itemData.location, 0, itemData.location!!.length)
            }
            if (itemData.barrageBean.good > 0) {
                val likeCount = itemData.barrageBean.good.toString()
                left += SizeUtils.dp2px(10f) * config.textSizeScale
                val drawable = if (itemData.barrageBean.isLike) {
                    likeSelectedDrawable
                } else {
                    likeUnSelectedDrawable
                }
                val likeImageMatrix = Matrix()
                likeImageMatrix.postScale(config.textSizeScale, config.textSizeScale)
                val height = likeImageSize * config.textSizeScale
                likeImageMatrix.postTranslate(left, textBaseLine - height)
                canvas.drawBitmap(drawable, likeImageMatrix, likeImagePaint)
                left += likeImageSize * config.textSizeScale
                left += SizeUtils.dp2px(4f) * config.textSizeScale

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
        } else {
            super.draw(item, canvas, displayer, config)
        }
    }

    companion object {
        private val smallSize = SizeUtils.sp2px(12f)

        fun getAvatarArea(textSizeScale: Float, position: RectF, data: SpannedModel): FloatArray {
            val paint = Paint().apply {
                textSize = textSizeScale * smallSize
            }
            val start = position.left
            val end = start + SizeUtils.dp2px(24f + 3f) * textSizeScale
            +paint.measureText(data.nickName) * textSizeScale
            return floatArrayOf(start, end)
        }

        fun getLikeArea(
            textSizeScale: Float,
            position: RectF,
            data: BaseDanmakuItemData
        ): FloatArray {
            val paint = Paint().apply {
                textSize = textSizeScale * smallSize
            }
            val start = position.right - (SizeUtils.dp2px(12f + 4f) * textSizeScale) -
                    paint.measureText(data.barrageBean.good.toString())
            val end = position.right
            return floatArrayOf(start, end)
        }
    }
}