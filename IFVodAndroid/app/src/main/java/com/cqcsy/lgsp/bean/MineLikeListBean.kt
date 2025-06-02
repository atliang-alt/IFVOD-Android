package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 我的点赞数据bean
 */
class MineLikeListBean: BaseBean(){
    // 对应的资源ID
    var mediaKey: String = ""
    // 头像
    var avatar: String = ""
    // 昵称
    var nickName: String = ""
    // vip等级
    var vipLevel: Int = 0
    // 内容
    var context: String = ""
    // 用户ID
    var userId: Int = 0
    // 点赞时间
    var updateTime: String = ""
    // 回复的ID
    var commentID: Int = 0
    // 当前评论id
    var originCommentID: Int = 0
    // 原评论id V2
    var parentCommentID: Int = 0
    // 是否是大V
    var bigV: Boolean = false
    // 是否失效
    var isUnAvailable: Boolean = false
    // 消息类型 确定点击跳转 0 剧集 1 小视频 2 动态 3 相册 4 回复的评论
    var businessType: Int = 4
    // 评论数量
    var comments: Int = 0
    // 资源类型 1 剧集  3  小视频  7 相册  8  动态
    var videoType: Int = 0
}