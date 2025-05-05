package com.cqcsy.lgsp.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.utils.ImageUtil

class RecommendMultiBean : BaseBean(), MultiItemEntity {
    val duration: String? = null
    val videoType: Int = 0
    var watchingProgress: Long = 0
    val userId: Int = 0
    val updateCount: Int = 0
    val businessType: Int = 0  //0 剧集  1 小视频 2 动态 3 相册
    var likeCount: Int = 0
    val comments: Int = 0
    var viewCount: Int = 0

    //1 动态图片 2 动态视频
    val photoCount: Int = 0
    val photoType: Int = 1
    val uniqueID: Int = 0
    val episodeId: String? = null
    val videoId: String? = null
    val upperName: String? = null
    val headImg: String? = null
    val mediaKey: String = ""
    val smallMediaKey: String = ""
    val mediaUrl: String? = null
    var coverImgUrl: String? = null
    val title: String? = null
    val description: String? = null
    val updateStatus: String? = null
    val lang: String? = null
    val cidMapper: String? = null
    val contentType: String? = null
    val score: String? = null
    val date: String? = null
    var favorites: VideoLikeBean? = null
    val isRecommend: Boolean = false
    val isHot: Boolean = false
    var like: Boolean = false
    var focusStatus: Boolean = false
    val details: MutableList<ImageBean>? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var detailedAddress: String? = null // 详细地址
    var address: String? = null // 显示地址
    var ratio: String? = null // 图片比例

    // VIP等级
    var vipLevel: Int = 0

    // 是否是大V
    var bigV: Boolean = false
    var isBlackList: Boolean = false

    override val itemType: Int
        get() = businessType

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

    val isLongImage: Boolean
        get() {
            return if (ratio != null) {
                try {
                    val str = ratio!!.split(":")
                    val imageWidth = str[0].toInt()
                    val imageHeight = str[1].toInt()
                    ImageUtil.isLongImage(imageWidth, imageHeight)
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }
}