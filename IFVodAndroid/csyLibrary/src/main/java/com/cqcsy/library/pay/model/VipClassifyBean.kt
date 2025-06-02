package com.cqcsy.library.pay.model

import com.cqcsy.library.base.BaseBean

/**
 * VIP套餐数据bean
 */
class VipClassifyBean : BaseBean() {
    // 套餐id
    var packageId: Int = 1

    // 1：黄金会员 2：至尊会员 3：情侣会员
    var type: Int = 0

    // 套餐名字
    var name: String = ""

    // 有效期时长 天数
    var validityDays: Int = 0

    // 赠送天数
    var giftDays: Int = 0

    // 货币符号
    var priceSymbol: String = ""

    // 价格
    var price: String = ""

    // 折扣价格
    var disprice: String? = ""

    // 折扣人民币价格
    var disrmb: String? = ""

    // 人民币价格
    var rmb: String = ""

    var img: String? = ""

    var topLeftTitle: String? = ""

    // 是否超划算
    var bargain: Boolean = false

    // 套餐允许同时使用人数
    var menberCount: Int = 1

    // 首充折扣
    var promotions: MutableList<Promotions>? = null

    var categoryName: String? = ""

    inner class Promotions : BaseBean() {
        var promotionCode: String = ""
        var extendDays: Int = 0
        var discountRate: Double = 0.0
        var minusPrice: Double = 0.0
        var title: String = ""
        var isNeedBindTel: Boolean = false
    }
}