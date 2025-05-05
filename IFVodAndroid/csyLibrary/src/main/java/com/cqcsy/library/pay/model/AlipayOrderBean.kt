package com.cqcsy.library.pay.model

import com.cqcsy.library.base.BaseBean

/**
 * 聚合支付创建订单bean
 */
class AlipayOrderBean : BaseBean() {
    var payType: Int = 0
    var money: Double = 0.0
    var order: String = ""
    var tgcBalance: Double = 0.0
    // 0 正常 1 等待放币（用户目前需要等待，如果长时间还处于等待状态则可以进行申诉） 2 卖家申述 3. 第一次警告 4.永久禁止
    var u_status: Int = 0
    var status: Int = 0
    var account: String = ""
    var accountName: String = ""
    var image: String = ""
    var payPrice: Double = 0.0
}