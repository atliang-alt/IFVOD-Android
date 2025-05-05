package com.cqcsy.lgsp.medialoader

/**
 * 作者：wangjianxiong
 * 创建时间：2021/9/2
 *
 *
 */
object MimeTypeUtil {
    private const val MIME_TYPE_PREFIX_IMAGE = "image"
    private const val MIME_TYPE_PREFIX_VIDEO = "video"
    private const val MIME_TYPE_PREFIX_AUDIO = "audio"

    fun isImage(mineType: String?): Boolean {
        return !mineType.isNullOrEmpty() && mineType.startsWith(MIME_TYPE_PREFIX_IMAGE)
    }

    fun isVideo(mineType: String?): Boolean {
        return !mineType.isNullOrEmpty() && mineType.startsWith(MIME_TYPE_PREFIX_VIDEO)
    }

    fun isAudio(mineType: String?): Boolean {
        return !mineType.isNullOrEmpty() && mineType.startsWith(MIME_TYPE_PREFIX_AUDIO)
    }
}