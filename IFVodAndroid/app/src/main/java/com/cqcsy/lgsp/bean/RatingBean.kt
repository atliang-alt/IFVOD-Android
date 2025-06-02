package com.cqcsy.lgsp.bean


import com.cqcsy.library.base.BaseBean
import com.google.gson.annotations.SerializedName

data class RatingBean(
    @SerializedName("fromUrl")
    var fromUrl: String? = null,
    @SerializedName("logo")
    var logo: String? = null,
    @SerializedName("rating")
    var rating: Double? = null,
    @SerializedName("refId")
    var refId: Int? = null,
    @SerializedName("templateKey")
    var templateKey: String? = null
) : BaseBean()