package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 投票选项数据bean
 */
class VoteOptionBean: BaseBean() {
    val id: Int = 0
    // 选项标题
    var option: String = ""
    // 该选项对应的票数
    var count: Int = 0
}