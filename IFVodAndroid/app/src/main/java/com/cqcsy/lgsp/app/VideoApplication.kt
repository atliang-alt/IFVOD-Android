package com.cqcsy.lgsp.app

import android.app.Activity
import android.app.Application
import cn.jpush.android.api.JPushInterface
import com.base.library.net.HttpManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.BuildConfig
import com.cqcsy.lgsp.R
import com.cqcsy.library.database.DBManger
import com.cqcsy.lgsp.event.AppStatusChange
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.lgsp.views.RefreshFooter
import com.cqcsy.lgsp.views.RefreshHeader
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.android.exoplayer2.database.DefaultDatabaseProvider
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.tencent.bugly.crashreport.CrashReport
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit


class VideoApplication : Application(), Utils.OnAppStatusChangedListener {
    var isAppBackground = false

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        Location.instance(this).destory()

        SPUtils.getInstance("SpConfig")
        GlobalValue.initStaticValue()
        HttpRequest.initOkGo(this)
        initRefresh()
        initHttp()

        JPushInterface.setDebugMode(isDebug()) // 设置开启日志,发布时请关闭日志
        JPushInterface.init(this)

        CrashReport.initCrashReport(this, BuildConfig.BUGLY_ID, isDebug())
        CrashReport.setIsDevelopmentDevice(this, isDebug())

//        initUmeng()

        GlobalValue.computeScaleSize()
        initPlayer()
        AppUtils.registerAppStatusChangedListener(this)
    }

    private fun initHttp() {
        val builder = OkHttpClient.Builder()
        val loggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
        if (BuildConfig.BUILD_TYPE != "release")
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY //log打印级别，决定了log显示的详细程度
        builder.addInterceptor(loggingInterceptor)
        builder.readTimeout(20_000, TimeUnit.MILLISECONDS) //全局的读取超时时间
        builder.writeTimeout(20_000, TimeUnit.MILLISECONDS) //全局的写入超时时间
        builder.connectTimeout(10_000, TimeUnit.MILLISECONDS) //全局的连接超时时间
        builder.retryOnConnectionFailure(false)
        HttpManager.init(builder.build())
    }

//    private fun initUmeng() {
//        UMConfigure.init(this, BuildConfig.UMENG_KEY,
//            "Umeng", UMConfigure.DEVICE_TYPE_PHONE, "")
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
//    }

    private fun initRefresh() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { _, _ ->
            RefreshHeader(this).setEnableLastTime(false)
                .setProgressResource(R.mipmap.icon_little_progress)
                .setArrowResource(R.mipmap.icon_little_progress)
        }

        SmartRefreshLayout.setDefaultRefreshFooterCreator { _, _ ->
            RefreshFooter(this).setProgressResource(R.mipmap.icon_little_progress)
                .setArrowResource(R.mipmap.icon_little_progress)
        }
    }

    private fun initPlayer() {
        if (isDebug()) {
            Debuger.enable()
        } else {
            Debuger.disable()
        }
        //自定义playerManager，主要处理缓存key问题
        PlayerFactory.setPlayManager(ExoPlayerManager::class.java)
        CacheFactory.setCacheManager(ExoPlayerCacheManager::class.java)
        ExoPlayerSourceManager.setSkipSSLChain(true)
        ExoPlayerSourceManager.setDatabaseProvider(DefaultDatabaseProvider(DBManger.instance.getDBHelper()))
        /*GSYVideoType.enableMediaCodec()
        GSYVideoType.enableMediaCodecTexture()*/
        GSYVideoManager.instance().setTimeOut(15 * 1000, false)
        //dkVideoPlayer-下个版本替换gsy再解开
        /*VideoViewManager.setConfig(
            VideoViewConfig.newBuilder().setLogEnabled(BuildConfig.DEBUG)
                .setPlayerFactory(ExoMediaPlayerFactory.create())
                .setRenderViewFactory(TextureRenderViewFactory.create())
                .build()
        )
        ExoMediaSourceHelper.getInstance(this).setCache(
            SimpleCache(
                File(externalCacheDir, "exo"),  //缓存目录
                LeastRecentlyUsedCacheEvictor(512 * 1024 * 1024),  //缓存大小，默认512M，使用LRU算法实现
                DefaultDatabaseProvider(DBManger.instance.getDBHelper())
            )
        )*/
    }

    private fun isDebug(): Boolean {
        return BuildConfig.BUILD_TYPE != "release"
    }

    override fun onForeground(activity: Activity?) {
        checkBackgroundChange(false)
    }

    override fun onBackground(activity: Activity?) {
        checkBackgroundChange(true)
    }

    private fun checkBackgroundChange(isBackground: Boolean) {
        if (isAppBackground != isBackground) {
            isAppBackground = isBackground
            val event = AppStatusChange(isBackground)
            EventBus.getDefault().post(event)
        }
    }

}