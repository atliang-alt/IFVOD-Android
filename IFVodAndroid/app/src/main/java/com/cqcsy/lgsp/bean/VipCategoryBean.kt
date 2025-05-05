package com.cqcsy.lgsp.bean

import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import java.io.Serializable

/**
 * 创建时间：2022/8/11
 */
data class VipCategoryBean(
    val remark: String?,
    val id: Int,
    /**
     * 1:直接购买 2:本国 3:分享折扣 4:砍价活动
     */
    val styleType: Int,
    val name: String?,
    val data: MutableList<VipClassifyBean>?,
    val payTypes: MutableList<VipPayBean>?,
) : Serializable