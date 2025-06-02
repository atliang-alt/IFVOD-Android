package com.cqcsy.lgsp.event

import com.cqcsy.lgsp.bean.CommentBean

/**
 * 发表评论成功
 */
class CommentEvent {
    var commentBean: CommentBean? = null
    var replyId: Int = 0
    var replyUserID: Int = 0
    var mediaKey: String = ""
}