package com.cqcsy.lgsp.bean

/**
 * 短视频数据Bean
 */
class ShortVideoBean : VideoBaseBean() {
    // 短视频对应头像
    var headImg: String = ""

    // 短视频播放量
    var playCount: Int = 0
    // 短视频评论量
    var comments: Int = 0
    // 短视频更新时间
    var date: String = ""
    // 是否已关注
    var focusStatus: Boolean = false
    // 是否已点赞
    var likeStatus: Boolean = false
    // 点赞数量
    var likeCount: Int = 0
    // 收藏
    var favorites: VideoLikeBean = VideoLikeBean()
    // 是否失效
    var isUnAvailable: Boolean = false
    // 上传审核状态
    var status: String = ""
    // 上传审核状态id
    var statusId: String = ""
    // 审核不通过原因
    var returnMsg: String = ""
    // 分类
    var category: String = ""
    // 简介
    var introduce: String = ""
    // 是否是大V
    var bigV: Boolean = false
    var isBlackList: Boolean = false

    // VIP等级
    var vipLevel: Int = 0
}