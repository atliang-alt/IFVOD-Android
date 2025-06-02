package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean

/**
 * 热搜网络请求数据Bean
 */
class HotSearchNetBean : BaseBean() {
    // 热搜id
    var itemId: Int = 0
    // 热搜标题
    var title: String = ""
    // 热搜跳转方式 搜索结果页、详情页
    var type: Int = 0
}