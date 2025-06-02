package com.cqcsy.lgsp.utils

import android.graphics.*
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest

/**
 * Glide圆角处理
 */
class GlideRoundTransform : BitmapTransformation {

    private var radius = 0f

    constructor(dp: Int) : super() {
        this.radius = SizeUtils.dp2px(dp.toFloat()).toFloat()
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap? {
        //变换的时候裁切
        val bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
        return roundCrop(pool, bitmap)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {

    }

    private fun roundCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
        if (source == null) {
            return null
        }
        var result: Bitmap? = pool.get(source.width, source.height, Bitmap.Config.ARGB_8888)
        if (result == null) {
            result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(result!!)
        val paint = Paint()
        paint.shader = BitmapShader(
            source,
            Shader.TileMode.CLAMP,
            Shader.TileMode.CLAMP
        )
        paint.isAntiAlias = true
        val rectF = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        canvas.drawRoundRect(rectF, radius, radius, paint)
        // 下面两行只保留上面两个圆角
        val rectRound = RectF(
            0f, 100f,
            source.width.toFloat(),
            source.height.toFloat()
        )
        canvas.drawRect(rectRound, paint)
        return result
    }
}