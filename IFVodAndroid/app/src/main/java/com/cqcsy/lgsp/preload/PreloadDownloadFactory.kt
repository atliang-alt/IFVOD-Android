package com.cqcsy.lgsp.preload

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.Downloader
import com.google.android.exoplayer2.offline.DownloaderFactory
import com.google.android.exoplayer2.offline.ProgressiveDownloader
import com.google.android.exoplayer2.source.dash.offline.DashDownloader
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloader
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.util.Util
import java.util.concurrent.Executor

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/21
 *
 *
 */
class PreloadDownloadFactory(
    private val cacheDataSourceFactory: CacheDataSource.Factory,
    private val executor: Executor
) : DownloaderFactory {

    override fun createDownloader(request: DownloadRequest): Downloader {
        @C.ContentType val contentType =
            Util.inferContentTypeForUriAndMimeType(request.uri, request.mimeType)
        return when (contentType) {
            C.TYPE_DASH, C.TYPE_HLS, C.TYPE_SS -> createDownloader(request, contentType)
            C.TYPE_OTHER -> ProgressiveDownloader(
                MediaItem.Builder()
                    .setUri(request.uri)
                    .setCustomCacheKey(request.customCacheKey)
                    .build(),
                cacheDataSourceFactory,
                executor
            )
            else -> throw IllegalArgumentException("Unsupported type: $contentType")
        }
    }

    private fun createDownloader(
        request: DownloadRequest,
        @C.ContentType contentType: Int
    ): Downloader {
        val mediaItem = MediaItem.Builder()
            .setUri(request.uri)
            .setStreamKeys(request.streamKeys)
            .setCustomCacheKey(request.customCacheKey)
            .build()
        return when (contentType) {
            C.TYPE_DASH -> DashDownloader(mediaItem, cacheDataSourceFactory, executor)
            C.TYPE_HLS -> HlsPreloadDownloader(mediaItem, cacheDataSourceFactory, executor)
            C.TYPE_SS -> SsDownloader(mediaItem, cacheDataSourceFactory, executor)
            else -> throw IllegalArgumentException("Unsupported type: $contentType")
        }
    }

}