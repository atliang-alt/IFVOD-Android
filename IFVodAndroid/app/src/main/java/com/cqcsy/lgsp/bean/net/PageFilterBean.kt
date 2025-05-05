package com.cqcsy.lgsp.bean.net


import com.google.gson.annotations.SerializedName

/**
 * 页面中筛选
 */
data class PageFilterBean(
    @SerializedName("ids")
    var ids: String? = null,
    @SerializedName("normalIcon")
    var normalIcon: String? = null,
    @SerializedName("title")
    var title: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("type")
    var type: Int? = null
)