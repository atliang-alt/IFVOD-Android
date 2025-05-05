package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 广告
 */

class AdvertBean : BaseBean() {
    var bannerId: Int = 0
    var linkURL: String? = null //广告点击跳转地址
    var mediaItem: VideoBaseBean? = null
    var regionID: Int = 0
    var resourceType: Int = 0   // 1: 广告
    var showURL: String = ""    // 广告地址
    var title: String? = null
    var piDuration: Float = 0f
    var viewURL: String? = null // 广告展示回调
    var appParam: Any? = null // 广告跳转的参数
    var endtime: String? = null // 过期时间
    var playtime: String? = null // 开始时间

    /**
     * 是否领取弹幕广告金币
     */
    var isDrawCoin: Boolean = false
}