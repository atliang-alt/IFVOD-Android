package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 谷歌GeoCode接口搜索返回数据bean
 */
class GeoCodeResultBean : BaseBean() {
    var address_components: MutableList<AddressComponents> = ArrayList()
    // 详细地址
    var formatted_address: String = ""
    var geometry: Geometry? = null
    var place_id: String = ""
    var types: MutableList<String> ?= null

    inner class AddressComponents : BaseBean() {
        var long_name: String = ""
        var short_name: String = ""
        var types: MutableList<String> = ArrayList()
    }

    inner class Geometry : BaseBean() {
        var location: LatLng? = null
        var location_type: String = ""
        var viewport: Viewport? = null
    }

    inner class Viewport : BaseBean() {
        var northeast: LatLng? = null
        var southwest: LatLng? = null
    }

    inner class LatLng : BaseBean() {
        var lat: Double = 0.00
        var lng: Double = 0.00
    }
}