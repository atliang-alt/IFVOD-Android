package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

class OrderRecordBean : BaseBean() {
    var typeName = ""   // vip名称，如黄金VIP
    var vipType = 0     // VIP等级，0、1、2
    var effectiveTime = 0   // 购买会员时长，单位天
    var presentTime = 0     // 赠送会员时长，单位天
    var useNum = 0          // 可供多少人使用
    var payType = ""        // 支付方式
    var about = ""          // 有效期：年月日--年月日
    var postTime = ""       // 支付时间
    var senderAccount: String? = null   // 赠送者账号
    var remark: String? = null   // VIP获得描述文字
}