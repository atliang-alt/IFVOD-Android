package com.cqcsy.lgsp.vip

import com.cqcsy.lgsp.bean.ActivityStatusBean
import com.cqcsy.library.pay.model.VipClassifyBean

/**
 ** 2023/7/6
 ** des：
 **/

interface OnPayItemClickListener {
    /**
     * 选中套餐
     */
    fun onSelectClassify(vipClassifyBean: VipClassifyBean)

    /**
     * 显示隐藏底部区域
     */
    fun showBottom(show: Boolean)

    /**
     * 活动开始
     */
    fun onActivityStart(statusBean: ActivityStatusBean?)

    /**
     * 刷新分享活动状态
     */
    fun refreshStatus() {

    }

    /**
     * 刷新套餐
     */
    fun refreshPackage() {

    }
}