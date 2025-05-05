package com.cqcsy.library.bean

import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.pay.model.ProvinceBean

class AreaGroupBean : BaseBean() {
    var letter = ""
    var countries: MutableList<AreaBean>? = null
    var provinces: MutableList<ProvinceBean>? = null
}