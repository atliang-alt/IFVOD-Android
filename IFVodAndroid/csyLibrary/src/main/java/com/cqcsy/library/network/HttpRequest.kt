package com.cqcsy.library.network

import android.app.Application
import android.webkit.URLUtil
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.library.BuildConfig
import com.cqcsy.library.R
import com.cqcsy.library.event.ReloginEvent
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.SPCookieStore
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * 网络请求
 */

object HttpRequest {

    fun initOkGo(application: Application) {
        val headers = HttpHeaders()
        headers.put("BundleId", AppUtils.getAppPackageName())
        headers.put("AppVersion", AppUtils.getAppVersionName())
        headers.put("System", "Android")
        headers.put("SystemVersion", DeviceUtils.getSDKVersionName())
        headers.put("DeviceInfo", DeviceUtils.getManufacturer() + " " + DeviceUtils.getModel())
        headers.put("Lat", GlobalValue.lat.toString())
        headers.put("Lng", GlobalValue.lng.toString())
        headers.put("DeviceId", DeviceUtils.getUniqueDeviceId())
        headers.put("Version", "V3")
        val params = HttpParams()
//        params.put("commonParamsKey1", "commonParamsValue1") //param支持中文,直接传,不要自己编码
        //----------------------------------------------------------------------------------------//
        val builder = OkHttpClient.Builder()
        //log相关
        builder.addInterceptor(HeaderInterceptor())
        val loggingInterceptor = HttpLoggingInterceptor("LiteVideoHttp")
        if (BuildConfig.BUILD_TYPE != "release")
            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY) //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO) //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor) //添加OkGo默认debug日志
        //第三方的开源库，使用通知显示当前请求的log，不过在做文件下载的时候，这个库好像有问题，对文件判断不准确
        //builder.addInterceptor(new ChuckInterceptor(this));
        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS / 3, TimeUnit.MILLISECONDS) //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS / 3, TimeUnit.MILLISECONDS) //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS / 6, TimeUnit.MILLISECONDS) //全局的连接超时时间
        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        builder.cookieJar(CookieJarImpl(SPCookieStore(application)))            //使用sp保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(CookieJarImpl(DBCookieStore(this))) //使用数据库保持cookie，如果cookie不过期，则一直有效
//        builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失
        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
//        val sslParams1 = HttpsUtils.getSslSocketFactory()
//        方法二：自定义信任规则，校验服务端证书
//        val sslParams2: HttpsUtils.SSLParams =
//            HttpsUtils.getSslSocketFactory(SafeTrustManager())
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
//HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
//方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
//HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
//        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//        builder.hostnameVerifier(SafeHostnameVerifier())
        // 其他统一的配置

        OkGo.getInstance().init(application) //必须调用初始化
            .setOkHttpClient(builder.build()) //建议设置OkHttpClient，不设置会使用默认的
//            .setCacheMode(CacheMode.REQUEST_FAILED_READ_CACHE) //全局统一缓存模式，默认不使用缓存，可以不传
//            .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE) //全局统一缓存时间，默认永不过期，可以不传
            .setRetryCount(0) //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
            .addCommonHeaders(headers) //全局公共头
            .addCommonParams(params) //全局公共参数
    }

    /**
     * 普通get请求，数据格式json
     */
    fun <T> get(
        url: String,
        callBack: HttpCallBack<T>?,
        params: HttpParams = HttpParams(),
        tag: Any = url
    ) {
        if (!URLUtil.isValidUrl(url)) {
            callBack?.onError("", StringUtils.getString(R.string.url_error))
            return
        }
        val request = OkGo.get<String>(url).params(params).tag(tag)
        request.execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                try {
                    parseResponse(response, callBack)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callBack?.onError(response?.body(), StringUtils.getString(R.string.parse_error))
                }
            }

            override fun onError(response: Response<String>?) {
                callBack?.onError(response?.body(), StringUtils.getString(R.string.http_error))
            }

            override fun onCacheSuccess(response: Response<String>?) {
                parseResponse(response, callBack)
            }

            override fun uploadProgress(progress: Progress) {
                super.uploadProgress(progress)
                callBack?.onProgress(progress)
            }
        })
    }

    fun cancelRequest(tag: Any) {
        OkGo.getInstance().cancelTag(tag)
    }

    /**
     * 普通post请求，数据格式json，如果params中含有文件，则上传文件方式请求
     */
    fun <T> post(
        url: String,
        callBack: HttpCallBack<T>? = null,
        params: HttpParams = HttpParams(),
        tag: Any = url
    ) {
        if (!URLUtil.isValidUrl(url)) {
            callBack?.onError("", StringUtils.getString(R.string.url_error))
            return
        }
        val request = OkGo.post<String>(url).tag(tag)
        if (params.fileParamsMap.size == 0) {
            val json = JSONObject()
            for (key in params.urlParamsMap.keys) {
                val value: MutableList<String> = params.urlParamsMap[key] ?: break
                if (value.size == 0) {
                    break
                }
                if (value.size == 1) {
                    json.put(key, params.urlParamsMap[key]?.get(0))
                } else {
                    json.put(key, JSONArray(Gson().toJson(value)))
                }
            }
            if (json.length() > 0)
                request.upJson(json)
        } else {
            request.params(params)
        }
        request.execute(object : StringCallback() {
            override fun onSuccess(response: Response<String>?) {
                try {
                    parseResponse(response, callBack)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callBack?.onError(response?.body(), StringUtils.getString(R.string.parse_error))
                }
            }

            override fun onError(response: Response<String>?) {
                callBack?.onError(response?.body(), StringUtils.getString(R.string.http_error))
            }

            override fun onCacheSuccess(response: Response<String>?) {
                parseResponse(response, callBack)
            }

            override fun uploadProgress(progress: Progress) {
                super.uploadProgress(progress)
                callBack?.onProgress(progress)
            }
        })
    }

    fun <T> parseResponse(response: Response<String>?, callBack: HttpCallBack<T>?) {
        if (response?.body() == null) {
            callBack?.onError(response?.body(), StringUtils.getString(R.string.empty_body))
            return
        }
        val body = response.body().toString()
        try {
            val json = JSONObject(body)
            val ret = json.optInt("ret")
            if (ret == 200) {
                val data = json.opt("data")
                if (data != null && data.toString().isNotEmpty()) {
                    callBack?.onSuccess(data as T)
                } else {
                    callBack?.onSuccess(null)
                }
            } else if (ret == 201 || ret == 301 || ret == 1000) {  // 201 登录过期     301 冻结   1000 必须登录或者登录过期，需要直接跳转到登录页面
                callBack?.onError(body, json.optString("msg"))
                EventBus.getDefault().post(ReloginEvent(ret == 1000))
            } else {
                callBack?.onError(body, json.optString("msg"))
            }
        } catch (e: Exception) {
            callBack?.onSuccess(body as T)
        }
    }
}