package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 消息列表数据bean
 * 系统消息列表、私信列表
 */
class MessageListBean: BaseBean() {
    // 消息id
    var messageId: Int = 0
    // 昵称
    var nickName: String = ""
    // 发送时间
    var updateTime: String = ""
    // 发送内容
    var content: String = ""
    // 发送者头像
    var avatar: String = ""
    // 用户ID
    var userId: Int = 0
    // 是否已读
    var isRead: Boolean = false
    // 是否是大V
    var bigV: Boolean = false
    // VIP等级
    var vipLevel: Int = 0
    // 消息类型 0：普通消息  1：客服消息
    var type: Int = 0
}