package com.cqcsy.lgsp.video.bean

import com.cqcsy.library.base.BaseBean

/**
 * 左侧菜单item对象
 */
class VideoItemBean : BaseBean() {
    var id: Int = 0
    var text: String? = ""
    var enableTitle: String? = ""   // 可选title
    var enableTip: String? = ""   // 可选提示
    var disableTip: String? = ""   // 待解锁提示
    var goldOpenNumber: Int = 0     // 金币解锁数量
    var isVip: Boolean = false  // VIP标签
    var enbale: Boolean = true  // 是否可选
    var isCurrent: Boolean = false  // 是否当前
    var isNew: Boolean = false
    var isNormalMenu: Boolean = false   // 普通选择，不带播放中
    var isLive: Boolean = false   // 直播
    var isLiveError: Boolean = false   // 直播错误状态
}