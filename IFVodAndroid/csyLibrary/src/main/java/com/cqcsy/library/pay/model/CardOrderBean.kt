package com.cqcsy.library.pay.model

import com.cqcsy.library.base.BaseBean

/**
 * 信用卡支付创建订单bean
 */
class CardOrderBean: BaseBean() {
    var orderID: Int = 0
    var amount: Double = 0.0
    var accountName: String = ""
    var addressID: Int = 0
    var options: Option = Option()

    inner class Option:BaseBean() {
        var addressList: MutableList<BillAddressBean> = ArrayList()
        var monthList: MutableList<BillAddressBean> = ArrayList()
        var yearList: MutableList<BillAddressBean> = ArrayList()
    }
}