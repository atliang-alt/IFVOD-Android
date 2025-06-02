package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 谷歌nearbySearch接口搜索结果
 */
class NearbySearchResultBean: BaseBean() {
    var business_status: String = ""
    // 详细地址
    var formatted_address: String = ""
    var geometry: GeoCodeResultBean.Geometry ?= null
    var icon: String = ""
    var icon_background_color: String = ""
    var icon_mask_base_uri: String = ""
    // 显示地址
    var name: String = ""
    var place_id: String = ""
    var rating: String = ""
    var reference: String = ""
    var types: MutableList<String> ?= null
    var user_ratings_total: String = ""
}