package com.cqcsy.lgsp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 数据Bean对象
 * 适用：电影、动漫、综艺、纪录片、电视剧、热门等模块
 */
class MovieModuleBean : VideoBaseBean(), MultiItemEntity {
    // 对应导航栏的id
    var type: Int = 0

    // 播放量
    var playCount: Int = 0

    // 是否显示推荐图标
    var isRecommend: Boolean = false

    // 最近更新数,最近的更新集数或期数,为0则不显示更新标签
    var updateCount: Int = 0

    // 语言
    var language: String? = ""

    // 发布时间
    var publishTime: String = ""

    // 播放记录日期
    var date: String = ""

    // 观看至XXXX (正在追需要)
    var seriesWatchProgress: String = ""

    // 是否失效
    var isUnAvailable: Boolean = false

    // 评分数据
    var score: String = ""

    override var itemType: Int = 1

    // 是否大图
    var isFull = false
}