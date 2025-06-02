package com.cqcsy.library.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest


/**
 * 2024.01.09
 * des: 解决center_crop图片圆角
 */
class CornerBitmapTransform(val radius: Int) : BitmapTransformation() {
    private val ID = "com.cqcsy.library.utils.CornerBitmapTransform"
    private val ID_BYTES = ID.toByteArray(CHARSET)

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight)
        return if (radius == 0) {
            bitmap
        } else {
            TransformationUtils.roundedCorners(pool, bitmap, radius)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is CornerBitmapTransform
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }
}