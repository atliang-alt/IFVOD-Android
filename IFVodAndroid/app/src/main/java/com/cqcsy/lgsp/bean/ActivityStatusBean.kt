package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 砍价活动状态
 */
data class ActivityStatusBean(
    var categoryId: Int?,
    var endTime: Long?,
    var packageId: Int?,
    var price: Float,
    var remark: String?,
    var key: String?, // 放弃或者免费领取时使用
    var type: Int, // 0 未开始 1 进行中 2 待支付
    var url: String? // 进入活动地址
) : BaseBean() {

    fun reset() {
        categoryId = 0
        endTime = 0
        packageId = 0
        price = 0f
        remark = null
        key = null
        type = 0
    }
}