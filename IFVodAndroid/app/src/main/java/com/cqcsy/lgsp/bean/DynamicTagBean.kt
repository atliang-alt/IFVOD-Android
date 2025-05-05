package com.cqcsy.lgsp.bean


import com.google.gson.annotations.SerializedName

data class DynamicTagBean(
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("parentLabel")
    var parentLabel: String? = null,
    @SerializedName("subLabels")
    var subLabels: MutableList<String>? = null
)