package com.cqcsy.lgsp.vip

/**
 * 创建时间：2022/8/11
 *
 */
enum class VipCategory(val styleType: Int) {

    /**
     * 直接购买，可以是原价\有折扣等
     */
    ORIGINAL_PRICE(1),

    /**
     * 本国vip
     */
    LOCAL_VIP(2),

    /**
     * 分享折扣
     */
    DISCOUNT_VIP(3),

    /**
     * 邀请注册砍价活动
     */
    INVITE_OFFSALE(4);

    companion object {
        @JvmStatic
        fun creator(value: Int): VipCategory {
            val found = values().find { it.styleType == value }
            return found ?: throw IllegalArgumentException("not found VipCategory with $value")
        }
    }
}