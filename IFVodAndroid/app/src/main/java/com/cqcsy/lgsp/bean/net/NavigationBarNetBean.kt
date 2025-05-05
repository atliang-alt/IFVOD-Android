package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean
import com.cqcsy.lgsp.bean.NavigationBarBean

/**
 * 网络请求首页导航栏分类数据Bean
 */
class NavigationBarNetBean: BaseBean() {
    // 版本号
    var versionNo: String = ""
    // 分类集合
    var list: List<NavigationBarBean> = ArrayList()
}