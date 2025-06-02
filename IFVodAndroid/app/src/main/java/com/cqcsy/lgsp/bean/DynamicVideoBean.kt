package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 动态小视频数据模型
 */
class DynamicVideoBean : BaseBean() {
    var uniqueID: Int = 0
    var comments: Int = 0
    var isHot: Boolean = false
    var coverImgUrl: String? = null
    var createTime: String? = null
    var description: String? = null
    var id: Int = 0
    var focusStatus: Boolean = false
    var like: Boolean = false
    var likeCount: Int = 0
    var mediaKey: String? = null
    var mediaUrl: String? = null
    var photoCount: Int = 0
    var sort: Int = 0
    var userId: Int = 0
    var title: String? = null
    var details: MutableList<ImageBean>? = null
    var viewCount: Int = 0
    var headImg: String? = ""
    var bigV: Boolean = false
    var vipLevel: Int = 0
    var upperName: String? = ""
    var latitude: Double? = null
    var longitude: Double? = null
    var detailedAddress: String? = null // 详细地址
    var address: String? = null // 显示地址
    var label: String? = null // 标签

    // 是否失效
    var isUnAvailable: Boolean = false

    // 收藏
    var favorites: Boolean = false
    var isBlackList: Boolean = false

    // 1:动态图片 2:动态视频
    var photoType: Int = 1
    var ratio: String? = null
    var videoId: String? = null

    val imageRatioValue: Float
        get() {
            val ratio = ratio ?: return 0f
            return try {
                val str = ratio.split(":")
                val imageWidth = str[0].toInt().toFloat()
                val imageHeight = str[1].toInt().toFloat()
                imageWidth / imageHeight
            } catch (e: Exception) {
                1f
            }
        }

}