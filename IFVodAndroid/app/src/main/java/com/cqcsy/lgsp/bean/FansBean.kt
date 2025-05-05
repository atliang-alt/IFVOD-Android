package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 粉丝数据bean
 */
class FansBean: BaseBean() {
    // 头像
    val avatar: String = ""
    // ID
    var userId: Int = 0
    // 昵称
    var nickName: String = ""
    // 内容
    var context: String = ""
    // 关注时间
    var updateTime: String = ""
    // 关注状态
    var focusStatus: Boolean = true
    // 是否是大V
    var bigV: Boolean = false
    // VIP等级
    var vipLevel: Int = 0
    // 性别
    var sex: Int = -1
    var isBlackList: Boolean = false
}