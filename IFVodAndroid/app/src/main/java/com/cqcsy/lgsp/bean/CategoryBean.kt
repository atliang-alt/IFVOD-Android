package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

class CategoryBean : BaseBean() {
    var classifyName: String = ""
    var classifyId: String = ""

    // 用于判断跳转下一个页面显示格式，1：剧集的、2：小视频的
    var classType: Int = 0
    var index: Int = 0
    var subID: String? = ""
}