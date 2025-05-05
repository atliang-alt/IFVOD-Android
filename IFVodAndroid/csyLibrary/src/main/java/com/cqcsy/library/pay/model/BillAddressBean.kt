package com.cqcsy.library.pay.model

import com.cqcsy.library.base.BaseBean

/**
 * 账单地址bean
 */
class BillAddressBean : BaseBean() {
    // 姓
    var lastname: String = ""

    // 名字
    var firstname: String = ""

    // 全名
    var fullName: String = ""

    // 地址
    var address: String = ""

    // 省份代码
    var state: String = ""

    var stateName: String = ""

    // 国家
    var countryName: String = ""

    // 国家代码
    var country: String = ""

    // 城市
    var city: String = ""

    // 邮编
    var zipCode: String = ""

    // 手机号
    var phone: String = ""

    // 邮箱
    var email: String = ""

    // 用户id
    var uid: Int = 0

    // id
    var id: Int = 0

    // 是否删除
    var isDeleted: Boolean = false
}