package com.cqcsy.library.network

import com.blankj.utilcode.util.SPUtils
import com.cqcsy.library.BuildConfig
import com.cqcsy.library.utils.Constant

/**
 * 相关服务地址
 */
object BaseUrl {
    // H5
    var H5_HOST = BuildConfig.H5_HOST

    // 接口服务器
    var BASE_URL = BuildConfig.BASE_URL

    // socket服务器
    var SOCKET_URL = BuildConfig.SERVICE_URL

    // 图片上传
    var PICTURE_UPLOAD = BASE_URL + "api/Album/UploadFile"

    init {
        if (BuildConfig.BUILD_TYPE != "release") {
            val type = SPUtils.getInstance().getInt("hostType", 2)
//           1 正式 2 预发布 3 测试
            BASE_URL = when (type) {
                1 -> "https://live-api.${BuildConfig.HOST}/"
                3 -> "https://test-api.${BuildConfig.HOST}/"
                else -> "https://pre-api.${BuildConfig.HOST}/"
            }
            PICTURE_UPLOAD = BASE_URL + "api/Album/UploadFile"
        } else {
            BASE_URL = SPUtils.getInstance().getString(Constant.KEY_RELEASE_BASE_URL)
            SOCKET_URL = SPUtils.getInstance().getString(Constant.KEY_RELEASE_SOCKET_URL)
            H5_HOST = SPUtils.getInstance().getString(Constant.KEY_RELEASE_H5_URL)
            PICTURE_UPLOAD = BASE_URL + "api/Album/UploadFile"
        }
    }
}