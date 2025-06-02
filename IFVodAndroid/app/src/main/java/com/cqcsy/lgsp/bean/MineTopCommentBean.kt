package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 我的 -- 评论/回复顶部楼主数据bean
 */
class MineTopCommentBean: BaseBean() {
    // 评论ID
    var replyID: Int = 0
    // 评论文本内容
    var contxt: String = ""
    // 发布时间
    var postTime: String = ""
    // 点赞数
    var likesNumber: Int = 0
    // 点赞状态
    var likeStatus: Boolean = false
    // 回复数量
    var repliesNumber: Int = 0
    // 昵称
    var nickName: String = ""
    // 用户id
    var uid: Int = 0
    // vip等级
    var vipLevel: Int = 0
    // 头像
    var avatar: String = ""
    // 是否已删除
    var deleteState: Boolean = false
    // vip表情
    var vipexpression: String = ""
    // 是否是加入黑名单
    var isForbidden: Boolean = false
    // 是否是大V
    var bigV: Boolean = false
}