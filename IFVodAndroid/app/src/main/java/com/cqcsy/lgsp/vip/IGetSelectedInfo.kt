package com.cqcsy.lgsp.vip

import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean

/**
 ** 2023/7/6
 ** des：获取选中的套餐、支付方式
 **/

interface IGetSelectedInfo {
    var listener: OnPayItemClickListener?

    /**
     * 获取选中的套餐
     */
    fun getSelectVip(): VipClassifyBean?

    /**
     * 获取选中支付方式
     */
    fun getSelectPayType(): VipPayBean?

    /**
     * 分享折扣活动是否分享
     */
    fun isShared(): Boolean {
        return false
    }
}