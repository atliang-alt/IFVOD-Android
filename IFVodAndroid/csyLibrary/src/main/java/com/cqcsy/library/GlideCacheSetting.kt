package com.cqcsy.library

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.cqcsy.library.utils.GlobalValue
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit


@Excludes(com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule::class)
@GlideModule
class GlideCacheSetting : AppGlideModule() {
    private val diskCacheSizeBytes = 1024 * 1024 * 200L  // 文件缓存大小

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        if (!FileUtils.isFileExists(GlobalValue.IMAGE_CACHE_PATH)) {
            FileUtils.createOrExistsDir(GlobalValue.IMAGE_CACHE_PATH)
        }
        builder.setDiskCache(DiskLruCacheFactory(GlobalValue.IMAGE_CACHE_PATH, diskCacheSizeBytes))
        builder.setLogLevel(Log.ERROR)
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        val factory = OkHttpUrlLoader.Factory(client)
        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }
}