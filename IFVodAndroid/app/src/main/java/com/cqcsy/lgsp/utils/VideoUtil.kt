package com.cqcsy.lgsp.utils

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.Base64
import com.blankj.utilcode.util.ImageUtils
import com.cqcsy.library.utils.CachePathUtils
import java.io.File

class VideoUtil(filePath: String) {
    private val mMetadataRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
    private var duration: Long
    private var filePathMd5: String

    init {
        mMetadataRetriever.setDataSource(filePath)
        filePathMd5 = Base64.encode(filePath.toByteArray(), Base64.NO_WRAP).toString()
        duration = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLong() ?: 0
    }

    fun extractPics(numbers: Int): MutableList<String> {
        val result = ArrayList<String>()
        var position = duration / numbers
        var numOfPicture = numbers
//        if (position < 4_000) {  // 保证4s以上截图一张
//            numOfPicture = (duration / 4_000).toInt()
//            position = duration / numOfPicture
//        }
        for (i in 0 until numOfPicture) {
            val path = extract(position * i * 1000, i)
            if (!path.isNullOrEmpty()) {
                result.add(path)
            }
        }
        return result
    }

    fun getFirstFrame(): String? {
        return extract(0, 0)
    }

    fun getVideoDuration(): Int {
        var duration = 0
        try {
            duration = Integer.parseInt(mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return duration
    }

    fun getVideoRatio(): IntArray {
        val orientation = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0
        val width = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
        val height = mMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
        return if (orientation == 0 || orientation == 180) {
            intArrayOf(width, height)
        } else {
            intArrayOf(height, width)
        }
    }

    private fun extract(timeUs: Long, position: Int): String? {
        var bitmap = mMetadataRetriever.getFrameAtTime(
            timeUs,
            MediaMetadataRetriever.OPTION_CLOSEST
        )
        var filePath: String? = cacheBitmap(bitmap, position)
        if (filePath.isNullOrEmpty()) {
            var time = timeUs
            while (filePath.isNullOrEmpty()) {
                time += 100_000
                if (time > duration) break
                bitmap = mMetadataRetriever.getFrameAtTime(
                    time,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )
                filePath = cacheBitmap(bitmap, position)
            }
        }
        return filePath
    }

    private fun cacheBitmap(bitmap: Bitmap?, position: Int): String? {
        if (bitmap == null) {
            return null
        }
        val filePath = getFilePath(position)
        ImageUtils.save(bitmap, filePath, Bitmap.CompressFormat.JPEG)
        return filePath
    }

    /**
     * 保存获取视频封面图片路径
     */
    private fun getFilePath(position: Int): String {
        return CachePathUtils.getVideoFrameImageCacheDir() + File.separator + filePathMd5 + "_" + position + ".jpg"
    }

    fun release() {
        mMetadataRetriever.release()
    }

}