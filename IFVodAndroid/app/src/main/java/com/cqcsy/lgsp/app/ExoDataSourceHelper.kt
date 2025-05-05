package com.cqcsy.lgsp.app

import android.content.Context
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/17
 *
 *
 */
object ExoDataSourceHelper {
    private const val DEFAULT_MAX_SIZE = 512 * 1024 * 1024L

    @Synchronized
    fun getSimpleCache(cachePath: String, databaseProvider: DatabaseProvider?): SimpleCache {
        val path = cachePath + File.separator + "exo"
        return if (databaseProvider != null) {
            SimpleCache(
                File(path),
                LeastRecentlyUsedCacheEvictor(DEFAULT_MAX_SIZE),
                databaseProvider
            )
        } else {
            SimpleCache(
                File(path),
                LeastRecentlyUsedCacheEvictor(DEFAULT_MAX_SIZE)
            )
        }
    }

    fun getCacheDataSourceFactory(cache: SimpleCache): CacheDataSource.Factory {
        return CacheDataSource.Factory().setCache(cache)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setCacheKeyFactory {
                return@setCacheKeyFactory ExoPlayerSourceManager.buildCacheKey(it)
            }
            .setUpstreamDataSourceFactory(getHttpDataSourceFactory())
    }

    fun getCacheDataSourceFactory(context: Context, cacheDir: String?): CacheDataSource.Factory {
        val cacheFile: File? = if (cacheDir.isNullOrEmpty()) {
            null
        } else {
            File(cacheDir)
        }
        val cache = ExoPlayerSourceManager.getCacheSingleInstance(context, cacheFile)
        return CacheDataSource.Factory().setCache(cache)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setCacheKeyFactory {
                return@setCacheKeyFactory ExoPlayerSourceManager.buildCacheKey(it)
            }
            .setUpstreamDataSourceFactory(getHttpDataSourceFactory())
    }

    /**
     * 获取SourceFactory
     */
    fun getDataSourceFactory(
        context: Context,
        preview: Boolean,
    ): DataSource.Factory {
        val factory = DefaultDataSource.Factory(
            context,
            getHttpDataSourceFactory()
        )
        if (preview) {
            factory.setTransferListener(DefaultBandwidthMeter.Builder(context).build())
        }
        return factory
    }

    private fun getHttpDataSourceFactory(): HttpDataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }
}