package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 谷歌nearbySearch接口搜索结果
 */
class AutocompleteResultBean: BaseBean() {
    var description: String = ""
    var lat: Double = 0.00
    var lng: Double = 0.00
    var terms: MutableList<TermsBean> = ArrayList()

    inner class TermsBean : BaseBean() {
        var offset: Int = 0
        var value: String = ""
    }
}