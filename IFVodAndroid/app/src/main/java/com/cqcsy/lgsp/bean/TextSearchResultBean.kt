package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 谷歌搜索地址结果bean
 */
class TextSearchResultBean: BaseBean() {
    // 详细地址
    var formattedAddress: String = ""
    var lat: Double = 0.00
    var lng: Double = 0.00
    // 显示地址
    var name: String = ""
}