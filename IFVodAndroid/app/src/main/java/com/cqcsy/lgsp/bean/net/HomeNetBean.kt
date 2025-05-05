package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.MovieModuleBean


/**
 * 网络请求 首页数据bean对象
 */
class HomeNetBean : BaseBean() {
    // 数据类型 1:热门 3:电影 4:电视剧 5:综艺 6:动漫 7:纪录片 8:体育
    var type: Int = 0
    // 模块名称
    var name: String = ""
    // 对应二级分类ID
    var subID: String = ""
    // 是否需要换一换
    var needRefresh: Boolean = false
    // 是否显示查看更多
    var isMore: Boolean = false
    // 是否可以不断加载数据
    var pullUp: Boolean = false
    var pageCount: Int = 0
    // 数据
    var list: MutableList<MovieModuleBean>? = null
    // Banner数据
    var bannerList: MutableList<AdvertBean>? = null
    // 筛选
    var filterList: MutableList<PageFilterBean>? = null
}