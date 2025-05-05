package com.cqcsy.lgsp.bean


import com.cqcsy.library.base.BaseBean
import com.google.gson.annotations.SerializedName

data class VideoRatingBean(
    @SerializedName("rating")
    var rating: String? = null,
    @SerializedName("ratingList")
    var ratingList: List<RatingBean?>? = null,
    @SerializedName("ratingLogo")
    var ratingLogo: String? = null
) : BaseBean()