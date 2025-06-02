package com.cqcsy.lgsp.database.bean

import com.cqcsy.library.base.BaseBean

class SearchKeywordBean: BaseBean() {
    // 搜索关键词
    var keyword: String = ""
    // 存储时间
    var time: String = ""
    // 用户ID，没有登录可为空
    var uid: String = ""
}