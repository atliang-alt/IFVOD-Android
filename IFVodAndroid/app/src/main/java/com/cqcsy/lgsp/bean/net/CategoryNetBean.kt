package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean
import com.cqcsy.lgsp.bean.CategoryBean

/**
 * 网络请求导航频道数据Bean
 */
class CategoryNetBean : BaseBean() {
    var categoryId: String = ""
    var name: String = ""
    // 类型 1：剧集的、2：小视频的
    var type: Int = 0
    var list: MutableList<CategoryBean>? = null
}