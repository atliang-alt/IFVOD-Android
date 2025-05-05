package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 弹幕数据Bean
 */
class BarrageBean : BaseBean() {
    // 弹幕ID
    var guid: String = ""

    // 弹幕颜色
    var color: String = ""

    // 弹幕内容
    var contxt: String = ""

    // 头像
    var avatar: String? = ""

    // 昵称
    var nickName: String? = ""

    // 地区
    var country: String? = ""

    // 弹幕位置
    var position: Int = 0

    // 用户ID
    var uid: Int = 0

    // 弹幕时间位置
    var second: Double = 0.00

    // 用户等级
    var level: Int = 0

    // 消息类型
    var type: Int = 0   // 0 弹幕 1 直播聊天 2 弹幕广告

    // 弹幕显示内容
    var prefix: Int = 0 // 0 所有打开 1 昵称和头像 2 国家 3 关闭所有

    // 大V
    var bigV: Boolean = false

    var good: Int = 0

    // VIP等级
    var gid: Int = 0

    var isLike: Boolean = false

    //自定义字段
    var isLive: Boolean = false
    var isAdvert: Boolean = false
    var advertButtonDesc: String = ""

    //是否领取金币
    var isDrawCoin: Boolean = false
    var advertId: Int = 0
    var coin: Int = 0
    var advertCallback: String? = null
    var advertLinkUrl: String? = null
}