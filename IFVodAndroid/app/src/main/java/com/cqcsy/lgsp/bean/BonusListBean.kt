package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

class BonusListBean: BaseBean() {
    // 第几天
    var day: Int = 0
    // 0表示送金币 1表示送会员
    var type: Int = 0
    // 签到获得的经验
    var reward: Int = 0
    // 签到获得的金币
    var coin: Int = 0
    // 赠送vip天数
    var vipDay: Int = 0
}