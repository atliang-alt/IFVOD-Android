package com.cqcsy.library.utils

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import java.io.File

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/27
 *
 *
 */
object CachePathUtils {

    const val CACHE_VIDEO_PATH = "/videoCache"
    const val CACHE_IMAGE_PATH = "/imageCache"
    const val CACHE_VIDEO_FRAME_IMAGE_PATH = "$CACHE_IMAGE_PATH/videoFrameImageCache"
    const val CACHE_DYNAMIC_VIDEO_PRELOAD_PATH = "$CACHE_VIDEO_PATH/dynamicVideoPreloadCache"

    fun getImageCachePath(): String {
        val dir = PathUtils.getExternalAppCachePath() + CACHE_IMAGE_PATH
        FileUtils.createOrExistsDir(dir)
        return dir
    }

    /**
     * 获取视频帧图的目录地址
     */
    fun getVideoFrameImageCacheDir(): String {
        val dir = PathUtils.getExternalAppCachePath() + CACHE_VIDEO_FRAME_IMAGE_PATH
        FileUtils.createOrExistsDir(dir)
        return dir
    }

    /**
     * 获取动态小视频预缓存的目录路径
     */
    fun getDynamicVideoPreloadCachePath(): String {
        return PathUtils.getExternalAppCachePath() + CACHE_DYNAMIC_VIDEO_PRELOAD_PATH
    }

    /**
     * 获取视频缓存根路径
     */
    fun getVideoCacheRootPath(): String {
        return PathUtils.getExternalAppCachePath() + CACHE_VIDEO_PATH
    }

    /**
     * 获取动态视频压缩后的路径
     */
    fun getDynamicCompressVideoPath(fileName: String): String {
        val dir = getVideoCacheRootPath()
        FileUtils.createOrExistsDir(dir)
        return dir + File.separator + fileName
    }
}