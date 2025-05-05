package com.cqcsy.lgsp.preload

import android.net.Uri
import android.text.TextUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.app.ExoDataSourceHelper
import com.cqcsy.lgsp.app.ExoPlayerSourceManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.Downloader
import com.google.android.exoplayer2.offline.ProgressiveDownloader
import com.google.android.exoplayer2.offline.StreamKey
import com.google.android.exoplayer2.source.dash.offline.DashDownloader
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader
import com.google.android.exoplayer2.upstream.cache.ContentMetadata
import com.google.android.exoplayer2.util.Util
import java.util.*
import java.util.concurrent.Executors

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/16
 *
 * 只适用于Exo
 */
object PreloadManager {


    /**
     * 单线程池，按照添加顺序依次执行[PreloadTask]
     */
    private val executorService = Executors.newSingleThreadExecutor()

    /**
     * 保存正在预加载的[PreloadTask]
     */
    private val preloadTasks: LinkedHashMap<String, PreloadTask> = LinkedHashMap()

    /**
     * 标识是否需要预加载
     */
    private var startPreload = true
    fun addPreloadTask(position: Int, url: String) {
        val preloaded = isPreloaded(url)
        if (preloaded) {
            return
        }
        val task = PreloadTask(position, url, createDownloader(url))
        preloadTasks[url] = task
        if (startPreload) {
            //开始预加载
            task.executeOn(executorService)
        }
    }

    fun removePreloadTask(url: String) {
        val task = preloadTasks[url]
        if (task != null) {
            task.cancel()
            preloadTasks.remove(url)
        }
    }

    fun resumePreload(position: Int, isReverseScroll: Boolean) {
        startPreload = true
        for ((_, task) in preloadTasks.entries) {
            val preloaded = isPreloaded(task.url)
            if (isReverseScroll) {
                if (task.position < position && !task.isLoadFinished && !preloaded) {
                    task.executeOn(executorService)
                }
            } else {
                if (task.position > position && !task.isLoadFinished && !preloaded) {
                    task.executeOn(executorService)
                }
            }
        }
    }

    fun pausePreload(position: Int, isReverseScroll: Boolean) {
        startPreload = false
        for ((_, task) in preloadTasks.entries) {
            if (isReverseScroll) {
                if (task.position >= position) {
                    task.cancel()
                }
            } else {
                if (task.position <= position) {
                    task.cancel()
                }
            }
        }
    }

    fun clear() {
        for ((_, task) in preloadTasks.entries) {
            task.cancel()
        }
        preloadTasks.clear()
    }

    private fun createDownloader(url: String): Downloader {
        val uri = Uri.parse(url)
        @C.ContentType val contentType = Util.inferContentTypeForUriAndMimeType(uri, null)
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(url))
            .setStreamKeys(
                Collections.singletonList(
                    StreamKey(HlsMasterPlaylist.GROUP_INDEX_VARIANT, 0)
                )
            ).build()
        val cacheDataSourceFactory =
            ExoDataSourceHelper.getCacheDataSourceFactory(Utils.getApp(), null)
        return when (contentType) {
            C.TYPE_DASH -> DashDownloader(mediaItem, cacheDataSourceFactory)
            C.TYPE_HLS -> HlsPreloadDownloader(mediaItem, cacheDataSourceFactory)
            C.TYPE_SS -> SsDownloader(mediaItem, cacheDataSourceFactory)
            else -> ProgressiveDownloader(
                mediaItem,
                cacheDataSourceFactory,
            )
        }
    }

    private fun isPreloaded(url: String): Boolean {
        var isCache = true
        val cache =
            ExoPlayerSourceManager.getCacheSingleInstance(Utils.getApp(), null)
        if (!TextUtils.isEmpty(url)) {
            val key = ExoPlayerSourceManager.buildCacheKey(url)
            if (!TextUtils.isEmpty(key)) {
                val cachedSpans = cache.getCachedSpans(key)
                if (cachedSpans.size == 0) {
                    isCache = false
                } else {
                    val contentLength =
                        cache.getContentMetadata(key)[ContentMetadata.KEY_CONTENT_LENGTH, C.LENGTH_UNSET.toLong()]
                    var currentLength: Long = 0
                    for (cachedSpan in cachedSpans) {
                        currentLength += cache.getCachedLength(
                            key,
                            cachedSpan.position,
                            cachedSpan.length
                        )
                    }
                    isCache = currentLength >= contentLength
                }
            } else {
                isCache = false
            }
        }
        return isCache
    }
}