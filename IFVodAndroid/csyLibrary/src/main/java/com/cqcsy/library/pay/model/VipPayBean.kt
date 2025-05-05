package com.cqcsy.library.pay.model

import com.cqcsy.library.base.BaseBean

/**
 * VIP支付类型bean
 */
class VipPayBean : BaseBean() {
    // 支付方式ID
    var channelId: Int = 0

    // 支付类型  0：支付宝 1：微信 2：TGC 3：人工 4：信用卡 5：支付宝国际网页
    var payType: Int = -1

    // 支付方式名称
    var title: String = ""

    // 图标
    var img: String = ""

    // 描述、须知等
    var description: String = ""

    // 点击跳转支付地址
    var linkUrl: String = ""

    var isDefault: Boolean = false
}