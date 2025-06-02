package com.cqcsy.lgsp.bean

/**
 * 搜索结果数据bean
 */
class SearchResultBean : VideoBaseBean(){
    // 上映时间
    var postTime: String = ""
    // 主演
    var actor: String = ""
    // 更新时间
    var updateDate: String = ""
    // 导演
    var director: String = ""
    // 更新数量
    var updateCount: Int = 0
    // 集数或者期数
    var episodes: MutableList<VideoBaseBean>? = null

    // 数据类型: 电影、电视剧等
    var mediaType: String = ""
}