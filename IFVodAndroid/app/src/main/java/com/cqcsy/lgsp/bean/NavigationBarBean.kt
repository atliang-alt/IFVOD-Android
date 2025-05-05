package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 首页导航栏分类数据Bean
 */
class NavigationBarBean : BaseBean() {
    // 分类ID
    var categoryId: String = ""

    // 分类名称
    var name: String = ""

    // 分类类型
    var type: Int = 0

    // 页面展示样式判断，0：分标题的模块显示  1：不分标题，直接是小视频播放列表 2：推荐 3：关注 4：动态/相册
    var styleType: Int = 0

    // 请求地址
    var url: String = ""
}