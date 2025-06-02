package com.cqcsy.library.utils

import android.content.Intent
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.event.LoginEvent
import com.cqcsy.library.event.ReloginEvent
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import org.greenrobot.eventbus.EventBus

/**
 * 全局静态变量
 */

object GlobalValue {
    var downloadH5Address = ""

    var raffleAddress = ""

    var scaleImageSize = 1.0

    var lat = 0.0

    var lng = 0.0
        set(value) {
            field = value
            resetLocation()
        }
    const val LOGIN_RECEIVER = "com.cqcsy.login_status"

    private const val SCALE_MOBILE_STANDARD = 1.0

    private const val SCALE_WIFI_STANDARD = 2.0

    /** 文件缓存路径  **/
    var APP_CACHE_PATH = "/sdcard"
    var VIDEO_DOWNLOAD_PATH = "/videoDownload"
    var IMAGE_CACHE_PATH = "/imageCache"
    var IMAGE_SCREEN_SHORT = "/screenShot"
    var VIDEO_IMAGE_CLIP = "/videoImageClip/"
    var UPLOAD_CUT_FILE = "/uploadCut/"
    var DOWNLOAD_IMAGE = "/downloadImage/"


    // 登录信息
    var userInfoBean: UserInfoBean? = null
        set(value) {
            field = value
            field?.let { resetHttpParam(it) }
        }

    fun initStaticValue() {
        val login = SPUtils.getInstance().getString(Constant.KEY_USER_INFO)
        if (!login.isNullOrEmpty()) {
            userInfoBean = Gson().fromJson(String(EncodeUtils.base64Decode(login)), UserInfoBean::class.java)
        }
        initPath()
    }

    fun initPath() {
        APP_CACHE_PATH = PathUtils.getCachePathExternalFirst()
        FileUtils.createOrExistsDir(APP_CACHE_PATH)
        VIDEO_DOWNLOAD_PATH = APP_CACHE_PATH + VIDEO_DOWNLOAD_PATH
        IMAGE_CACHE_PATH = APP_CACHE_PATH + IMAGE_CACHE_PATH
        IMAGE_SCREEN_SHORT = APP_CACHE_PATH + IMAGE_SCREEN_SHORT
        VIDEO_IMAGE_CLIP = APP_CACHE_PATH + VIDEO_IMAGE_CLIP
        UPLOAD_CUT_FILE = APP_CACHE_PATH + UPLOAD_CUT_FILE
        DOWNLOAD_IMAGE = APP_CACHE_PATH + DOWNLOAD_IMAGE
        MUSIC_DOWNLOAD = APP_CACHE_PATH + MUSIC_DOWNLOAD
        MUSIC_LRC = APP_CACHE_PATH + MUSIC_LRC
        FileUtils.createOrExistsDir(VIDEO_DOWNLOAD_PATH)
        FileUtils.createOrExistsDir(IMAGE_CACHE_PATH)
        FileUtils.createOrExistsDir(IMAGE_SCREEN_SHORT)
        FileUtils.createOrExistsDir(VIDEO_IMAGE_CLIP)
        FileUtils.createOrExistsDir(UPLOAD_CUT_FILE)
        FileUtils.createOrExistsDir(DOWNLOAD_IMAGE)
        FileUtils.createOrExistsDir(MUSIC_DOWNLOAD)
        FileUtils.createOrExistsDir(MUSIC_LRC)
    }

    /**
     * 判断是否VIP，true，是，false不是
     */
    fun isVipUser(): Boolean {
        if (userInfoBean == null || userInfoBean!!.vipLevel <= 0) {
            return false
        }
        return true
    }

    /**
     * 判断是否是bigV
     */
    fun isBigV(): Boolean {
        return userInfoBean?.bigV == true
    }

    /**
     * 判断是否登陆，true，不是，false是
     */
    fun isLogin(): Boolean {
        return userInfoBean?.token?.token?.isNotEmpty() == true
    }

    /**
     * 推出登录，重置数据
     */
    fun loginOut() {
        userInfoBean = null
        SPUtils.getInstance().remove(Constant.KEY_USER_INFO)
        OkGo.getInstance().commonHeaders.remove("expire")
        OkGo.getInstance().commonHeaders.remove("gid")
        OkGo.getInstance().commonHeaders.remove("sign")
        OkGo.getInstance().commonHeaders.remove("token")
        OkGo.getInstance().commonHeaders.remove("uid")
        val event = LoginEvent()
        event.status = false
        EventBus.getDefault().post(event)
        val intent = Intent(LOGIN_RECEIVER)
        intent.setPackage(AppUtils.getAppPackageName())
        Utils.getApp().sendBroadcast(intent)
    }

    /**
     * 登陆完后重新设置通用参数，添加用户信息
     */
    fun resetHttpParam(userInfoBean: UserInfoBean) {
        val headers = HttpHeaders()
        headers.put("expire", userInfoBean.token.expire)
        headers.put("gid", userInfoBean.token.gid.toString())
        headers.put("sign", userInfoBean.token.sign)
        headers.put("token", userInfoBean.token.token)
        headers.put("uid", userInfoBean.token.uid.toString())
        OkGo.getInstance().addCommonHeaders(headers)
    }

    fun computeScaleSize() {
        scaleImageSize = if (NetworkUtils.isMobileData()) {
            ScreenUtils.getScreenDensity() / SCALE_MOBILE_STANDARD
        } else {
            ScreenUtils.getScreenDensity() / SCALE_WIFI_STANDARD
        }
    }

    fun resetLocation() {
        val headers = HttpHeaders()
        headers.put("Lat", lat.toString())
        headers.put("Lng", lng.toString())
        OkGo.getInstance().addCommonHeaders(headers)
    }

    fun isEnable(level: Int): Boolean {
        return isVipUser() || (userInfoBean?.userExtension?.currentLevel ?: 0) >= level
    }

    /**
     * 检查是否登陆，如果没有登陆，直接发送登陆事件
     */
    fun checkLogin(): Boolean {
        if (!isLogin()) {
            EventBus.getDefault().post(ReloginEvent(true))
            return false
        }
        return true
    }
}