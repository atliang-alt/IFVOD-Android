package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean

/**
 * 消息未读数量bean
 */
class MessageCountsBean: BaseBean() {
    // 粉丝未读数量
    var fansMsgCount: Int =  0
    // 评论未读数量
    var commentMsgCount: Int =  0
    // 赞未读数量
    var zanMsgCount: Int =  0
    // 系统消息未读数量
    var systeMsgCount: Int =  0
    // 私信未读数量
    var privateMsgCount: Int =  0
    // 私信未读消息文本内容
    var systemMessageContext: String = ""
    // 私信未读消息时间
    var systemMessageDate: String? = null
    // 私信发送着头像
    var privateMessageNickName: String = ""
    // 私信发送的时间
    var privateMessageDate: String? = null
}