package com.cqcsy.library.pay.model


import com.cqcsy.library.base.BaseBean
import com.google.gson.annotations.SerializedName

class ProvinceBean(
    @SerializedName("countryCode")
    val countryCode: String = "",
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("stateCode")
    val stateCode: String = "",
    @SerializedName("stateName")
    val stateName: String = ""
) : BaseBean()