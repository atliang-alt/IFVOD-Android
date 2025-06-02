package com.cqcsy.lgsp.bean


import com.google.gson.annotations.SerializedName

/**
 * 客服列表
 */
class ServiceItemBean(
    @SerializedName("appParam")
    val appParam: Any? = null,
    @SerializedName("img")
    val img: String? = "",
    @SerializedName("remark")
    val remark: String? = "",
    @SerializedName("title")
    val title: String? = ""
)