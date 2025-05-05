package com.cqcsy.lgsp.bean

/**
 * 视频详情页的Bean
 */
class VideoDetailsBean : VideoBaseBean() {
    // 视频类型名字 电影、电视剧等
    var typeName: String = ""

    // 视频上映时间
    var postTime: String = ""

    // 小视频发布时间
    var publishTime: String = ""

    // 主演
    var actor: String = ""

    // 导演
    var director: String = ""

    // 视频描述
    var introduce: String = ""

    // 视频评论总数
    var comments: Int = 0

    // 视频播放量
    var playCount: Int = 0

    // 评分数据
//    var score: String = "0"
    // 分享数量
    var shareCount: Int = 0

    // 剧集更新时间
    var updateMsg: String = ""

    // 跳过广告需要的金币数量
    var adGold: Int = 0

    // 评论模块是否显示 0显示发布 1不显示不能发布
    var commentStatus: Int = 0
}