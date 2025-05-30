package com.cqcsy.lgsp.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory;
import com.google.android.exoplayer2.upstream.cache.CacheSpan;
import com.google.android.exoplayer2.upstream.cache.ContentMetadata;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Ascii;

import java.io.File;
import java.util.Map;
import java.util.NavigableSet;

import tv.danmaku.ijk.media.exo2.ExoMediaSourceInterceptListener;


/**
 * Created by guoshuyu on 2018/5/18.
 */

public class ExoPlayerSourceManager {

    private static final String TAG = "ExoSourceManager";

    private static final long DEFAULT_MAX_SIZE = 512 * 1024 * 1024;

    public static final int TYPE_RTMP = 14;

    private static Cache mCache;
    /**
     * 忽律Https证书校验
     *
     * @deprecated 如果需要忽略证书，请直接使用 ExoMediaSourceInterceptListener 的 getHttpDataSourceFactory
     */
    @Deprecated
    private static boolean sSkipSSLChain = false;

    private static int sHttpReadTimeout = -1;

    private static int sHttpConnectTimeout = -1;

    private static boolean isForceRtspTcp = true;

    private Context mAppContext;

    private Map<String, String> mMapHeadData;

    private String mDataSource;

    private static ExoMediaSourceInterceptListener sExoMediaSourceInterceptListener;
    private static DatabaseProvider sDatabaseProvider;

    private boolean isCached = false;

    public static ExoPlayerSourceManager newInstance(Context context, @Nullable Map<String, String> mapHeadData) {
        return new ExoPlayerSourceManager(context, mapHeadData);
    }

    private ExoPlayerSourceManager(Context context, Map<String, String> mapHeadData) {
        mAppContext = context.getApplicationContext();
        mMapHeadData = mapHeadData;
    }

    /**
     * @param dataSource  链接
     * @param preview     是否带上header，默认有header自动设置为true
     * @param cacheEnable 是否需要缓存
     * @param isLooping   是否循环
     * @param cacheDir    自定义缓存目录
     */
    public MediaSource getMediaSource(String dataSource, boolean preview, boolean cacheEnable, boolean isLooping, File cacheDir, @Nullable String overrideExtension) {
        MediaSource mediaSource = null;
        if (sExoMediaSourceInterceptListener != null) {
            mediaSource = sExoMediaSourceInterceptListener.getMediaSource(dataSource, preview, cacheEnable, isLooping, cacheDir);
        }
        if (mediaSource != null) {
            return mediaSource;
        }
        mDataSource = dataSource;
        Uri contentUri = Uri.parse(dataSource);
        MediaItem mediaItem = MediaItem.fromUri(contentUri);
        int contentType = inferContentType(dataSource, overrideExtension);

        String uerAgent = null;
        if (mMapHeadData != null) {
            uerAgent = mMapHeadData.get("User-Agent");
        }
        if ("android.resource".equals(contentUri.getScheme())) {
            DataSpec dataSpec = new DataSpec(contentUri);
            final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(mAppContext);
            try {
                rawResourceDataSource.open(dataSpec);
            } catch (RawResourceDataSource.RawResourceDataSourceException e) {
                e.printStackTrace();
            }
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return rawResourceDataSource;
                }
            };
            return new ProgressiveMediaSource.Factory(
                    factory).createMediaSource(mediaItem);

        } else if ("assets".equals(contentUri.getScheme())) {
            DataSpec dataSpec = new DataSpec(contentUri);
            final AssetDataSource rawResourceDataSource = new AssetDataSource(mAppContext);
            try {
                rawResourceDataSource.open(dataSpec);
            } catch (Exception e) {
                e.printStackTrace();
            }
            DataSource.Factory factory = new DataSource.Factory() {
                @Override
                public DataSource createDataSource() {
                    return rawResourceDataSource;
                }
            };
            return new ProgressiveMediaSource.Factory(
                    factory).createMediaSource(mediaItem);
        }

        switch (contentType) {
            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir, uerAgent)),
                        new DefaultDataSource.Factory(mAppContext,
                                getHttpDataSourceFactory(mAppContext, preview, uerAgent))).createMediaSource(mediaItem);
                break;

            case C.TYPE_RTSP:
                RtspMediaSource.Factory rtspFactory = new RtspMediaSource.Factory();
                if (uerAgent != null) {
                    rtspFactory.setUserAgent(uerAgent);
                }
                if (sHttpConnectTimeout > 0) {
                    rtspFactory.setTimeoutMs(sHttpConnectTimeout);
                }
                rtspFactory.setForceUseRtpTcp(isForceRtspTcp);
                mediaSource = rtspFactory.createMediaSource(mediaItem);
                break;

            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir, uerAgent)),
                        new DefaultDataSource.Factory(mAppContext,
                                getHttpDataSourceFactory(mAppContext, preview, uerAgent))).createMediaSource(mediaItem);
                break;
            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable, preview, cacheDir, uerAgent))
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(mediaItem);
                break;
            case TYPE_RTMP:
                RtmpDataSource.Factory rtmpDataSourceFactory = new RtmpDataSource.Factory();
                mediaSource = new ProgressiveMediaSource.Factory(rtmpDataSourceFactory,
                        new DefaultExtractorsFactory())
                        .createMediaSource(mediaItem);
                break;
            case C.TYPE_OTHER:
            default:
                mediaSource = new ProgressiveMediaSource.Factory(getDataSourceFactoryCache(mAppContext, cacheEnable,
                        preview, cacheDir, uerAgent), new DefaultExtractorsFactory())
                        .createMediaSource(mediaItem);
                break;
        }
        return mediaSource;
    }


    /**
     * 设置ExoPlayer 的 MediaSource 创建拦截
     */
    public static void setExoMediaSourceInterceptListener(ExoMediaSourceInterceptListener exoMediaSourceInterceptListener) {
        sExoMediaSourceInterceptListener = exoMediaSourceInterceptListener;
    }

    public static void resetExoMediaSourceInterceptListener() {
        sExoMediaSourceInterceptListener = null;
    }

    public static ExoMediaSourceInterceptListener getExoMediaSourceInterceptListener() {
        return sExoMediaSourceInterceptListener;
    }


    @SuppressLint("WrongConstant")
    @C.ContentType
    public static int inferContentType(String fileName, @Nullable String overrideExtension) {
        fileName = Ascii.toLowerCase(fileName);
        if (fileName.startsWith("rtmp:")) {
            return TYPE_RTMP;
        } else {
            return inferContentType(Uri.parse(fileName), overrideExtension);
        }
    }

    @C.ContentType
    public static int inferContentType(Uri uri, @Nullable String overrideExtension) {
        return Util.inferContentType(uri, overrideExtension);
    }

    /**
     * 本地缓存目录
     */
    public static synchronized Cache getCacheSingleInstance(Context context, File cacheDir) {
        String dirs = context.getCacheDir().getAbsolutePath();
        if (cacheDir != null) {
            dirs = cacheDir.getAbsolutePath();
        }
        if (mCache == null) {
            String path = dirs + File.separator + "exo";
            boolean isLocked = SimpleCache.isCacheFolderLocked(new File(path));
            if (!isLocked) {
                mCache = new SimpleCache(new File(path), new LeastRecentlyUsedCacheEvictor(DEFAULT_MAX_SIZE), sDatabaseProvider);
            }
        }
        return mCache;
    }

    public void release() {
        isCached = false;
        if (mCache != null) {
            try {
                mCache.release();
                mCache = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cache需要release之后才能clear
     */
    public static void clearCache(Context context, File cacheDir, String url) {
        try {
            Cache cache = getCacheSingleInstance(context, cacheDir);
            if (!TextUtils.isEmpty(url)) {
                if (cache != null) {
                    removeCache(cache, url);
                }
            } else {
                if (cache != null) {
                    for (String key : cache.getKeys()) {
                        removeCache(cache, key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void removeCache(Cache cache, String url) {
        NavigableSet<CacheSpan> cachedSpans = cache.getCachedSpans(buildCacheKey(url));
        for (CacheSpan cachedSpan : cachedSpans) {
            try {
                cache.removeSpan(cachedSpan);
            } catch (Exception e) {
                // Do nothing.
            }
        }
    }

    public static String buildCacheKey(DataSpec dataSpec) {
        Uri uri = dataSpec.uri;
        String lb = uri.getQueryParameter("lb");
        if (lb != null) {
            return uri.getPath() + "-" + lb;
        } else {
            return CacheKeyFactory.DEFAULT.buildCacheKey(dataSpec);
        }
    }

    public static String buildCacheKey(String url) {
        return buildCacheKey(new DataSpec(Uri.parse(url)));
    }

    public static boolean cachePreView(Context context, File cacheDir, String url) {
        return resolveCacheState(getCacheSingleInstance(context, cacheDir), url);
    }

    public boolean hadCached() {
        return isCached;
    }

    /**
     * 忽律Https证书校验
     *
     * @deprecated 如果需要忽略证书，请直接使用 ExoMediaSourceInterceptListener 的 getHttpDataSourceFactory
     */
    @Deprecated
    public static boolean isSkipSSLChain() {
        return sSkipSSLChain;
    }

    /**
     * 设置https忽略证书
     *
     * @param skipSSLChain true时是hulve
     * @deprecated 如果需要忽略证书，请直接使用 ExoMediaSourceInterceptListener 的 getHttpDataSourceFactory
     */
    @Deprecated
    public static void setSkipSSLChain(boolean skipSSLChain) {
        sSkipSSLChain = skipSSLChain;
    }


    public static int getHttpReadTimeout() {
        return sHttpReadTimeout;
    }

    /**
     * 如果设置小于 0 就使用默认 8000 MILLIS
     */
    public static void setHttpReadTimeout(int httpReadTimeout) {
        ExoPlayerSourceManager.sHttpReadTimeout = httpReadTimeout;
    }

    public static int getHttpConnectTimeout() {
        return sHttpConnectTimeout;
    }

    /**
     * 如果设置小于 0 就使用默认 8000 MILLIS
     */
    public static void setHttpConnectTimeout(int httpConnectTimeout) {
        ExoPlayerSourceManager.sHttpConnectTimeout = httpConnectTimeout;
    }

    public static DatabaseProvider getDatabaseProvider() {
        return sDatabaseProvider;
    }

    public static void setDatabaseProvider(DatabaseProvider databaseProvider) {
        ExoPlayerSourceManager.sDatabaseProvider = databaseProvider;
    }

    public static boolean isForceRtspTcp() {
        return isForceRtspTcp;
    }

    public static void setForceRtspTcp(boolean isForceRtspTcp) {
        ExoPlayerSourceManager.isForceRtspTcp = isForceRtspTcp;
    }

    /**
     * 获取SourceFactory，是否带Cache
     */
    private DataSource.Factory getDataSourceFactoryCache(Context context, boolean cacheEnable, boolean preview, File cacheDir, String uerAgent) {
        if (cacheEnable) {
            Cache cache = getCacheSingleInstance(context, cacheDir);
            if (cache != null) {
                isCached = resolveCacheState(cache, mDataSource);
                CacheDataSource.Factory factory = new CacheDataSource.Factory();
                return factory.setCache(cache)
                        .setCacheReadDataSourceFactory(getDataSourceFactory(context, preview, uerAgent))
                        .setCacheKeyFactory(new CacheKeyFactory() {
                            @Override
                            public String buildCacheKey(DataSpec dataSpec) {
                                Uri uri = dataSpec.uri;
                                return uri.getPath() + "-" + uri.getQueryParameter("lb");
                            }
                        })
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                        .setUpstreamDataSourceFactory(getHttpDataSourceFactory(context, preview, uerAgent));
            }
        }
        return getDataSourceFactory(context, preview, uerAgent);
    }

    /**
     * 获取SourceFactory
     */
    private DataSource.Factory getDataSourceFactory(Context context, boolean preview, String uerAgent) {
        DefaultDataSource.Factory factory = new DefaultDataSource.Factory(context,
                getHttpDataSourceFactory(context, preview, uerAgent));
        if (preview) {
            factory.setTransferListener(new DefaultBandwidthMeter.Builder(context).build());
        }
        return factory;
    }

    private DataSource.Factory getHttpDataSourceFactory(Context context, boolean preview, String uerAgent) {
        if (uerAgent == null) {
            uerAgent = Util.getUserAgent(context, TAG);
        }
        int connectTimeout = DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS;
        int readTimeout = DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS;
        if (sHttpConnectTimeout > 0) {
            connectTimeout = sHttpConnectTimeout;
        }
        if (sHttpReadTimeout > 0) {
            readTimeout = sHttpReadTimeout;
        }
        boolean allowCrossProtocolRedirects = false;
        if (mMapHeadData != null && mMapHeadData.size() > 0) {
            allowCrossProtocolRedirects = "true".equals(mMapHeadData.get("allowCrossProtocolRedirects"));
        }
        DataSource.Factory dataSourceFactory = null;
        if (sExoMediaSourceInterceptListener != null) {
            dataSourceFactory = sExoMediaSourceInterceptListener.getHttpDataSourceFactory(uerAgent, preview ? null : new DefaultBandwidthMeter.Builder(mAppContext).build(),
                    connectTimeout,
                    readTimeout, mMapHeadData, allowCrossProtocolRedirects);
        }
        if (dataSourceFactory == null) {
            dataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(allowCrossProtocolRedirects)
                    .setConnectTimeoutMs(connectTimeout)
                    .setReadTimeoutMs(readTimeout)
                    .setTransferListener(preview ? null : new DefaultBandwidthMeter.Builder(mAppContext).build());
            if (mMapHeadData != null && mMapHeadData.size() > 0) {
                ((DefaultHttpDataSource.Factory) dataSourceFactory).setDefaultRequestProperties(mMapHeadData);
            }
        }
        return dataSourceFactory;
    }

    /**
     * 根据缓存块判断是否缓存成功
     */
    private static boolean resolveCacheState(Cache cache, String url) {
        boolean isCache = true;
        if (!TextUtils.isEmpty(url)) {
            String key = buildCacheKey(url);
            if (!TextUtils.isEmpty(key)) {
                NavigableSet<CacheSpan> cachedSpans = cache.getCachedSpans(key);
                if (cachedSpans.size() == 0) {
                    isCache = false;
                } else {
                    long contentLength = cache.getContentMetadata(key).get(ContentMetadata.KEY_CONTENT_LENGTH, C.LENGTH_UNSET);
                    long currentLength = 0;
                    for (CacheSpan cachedSpan : cachedSpans) {
                        currentLength += cache.getCachedLength(key, cachedSpan.position, cachedSpan.length);
                    }
                    isCache = currentLength >= contentLength;
                }
            } else {
                isCache = false;
            }
        }
        return isCache;
    }
}
