package com.cqcsy.lgsp.bean

/**
 * 作者：wangjianxiong
 * 创建时间：2022/8/31
 *
 *
 */
data class UserStatusChangeBean(
    val from: Int,
    val to: Int,
    val action: Int,
    val context: String,
)