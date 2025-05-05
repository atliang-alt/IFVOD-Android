package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 动态对象
 */
class DynamicBean : BaseBean() {
    var comments: Int = 0
    var videoType: Int = 0
    var coverPath: String? = null
    var coverImgUrl: String? = null
    var mediaUrl: String? = null
    var createTime: String? = null
    var description: String? = null
    var focus: Boolean = false
    var like: Boolean = false
    var likeCount: Int = 0
    var mediaKey: String? = null
    var smallMediaKey: String? = null
    var photoCount: Int = 0
    var sort: Int = 0
    var title: String? = null
    var trendsDetails: MutableList<ImageBean>? = null
    var uid: Int = 0
    var viewCount: Int = 0
    var headImg: String? = ""

    //视频地址，临时保存
    var videoPath: String? = null

    // 是否是大V
    var bigV: Boolean = false

    // VIP等级
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
    var isCollected: Boolean = false
    var isBlackList: Boolean = false

    // 1:动态图片 2:动态视频
    var photoType: Int = 1
    var ratio: String? = null
    var videoId: String? = null

    fun copy(dynamicBean: DynamicBean) {
        uid = dynamicBean.uid
        videoId = dynamicBean.videoId
        comments = dynamicBean.comments
        coverPath = dynamicBean.coverPath
        coverImgUrl = dynamicBean.coverImgUrl
        createTime = dynamicBean.createTime
        description = dynamicBean.description
        focus = dynamicBean.focus
        like = dynamicBean.like
        likeCount = dynamicBean.likeCount
        photoCount = dynamicBean.photoCount
        sort = dynamicBean.sort
        title = dynamicBean.title
        trendsDetails = dynamicBean.trendsDetails
        viewCount = dynamicBean.viewCount
        headImg = dynamicBean.headImg
        upperName = dynamicBean.upperName
        latitude = dynamicBean.latitude
        longitude = dynamicBean.longitude
        detailedAddress = dynamicBean.detailedAddress
        address = dynamicBean.address
        vipLevel = dynamicBean.vipLevel
        bigV = dynamicBean.bigV
        isBlackList = dynamicBean.isBlackList
        mediaUrl = dynamicBean.mediaUrl
        mediaKey = dynamicBean.mediaKey
        isCollected = dynamicBean.isCollected
    }

    fun copy(data: DynamicVideoBean): DynamicBean {
        uid = data.userId
        mediaKey = data.mediaKey
        videoId = data.videoId
        mediaUrl = data.mediaUrl
        comments = data.comments
        coverPath = data.coverImgUrl
        coverImgUrl = data.coverImgUrl
        createTime = data.createTime
        description = data.description
        focus = data.focusStatus
        like = data.like
        likeCount = data.likeCount
        photoCount = data.photoCount
        sort = data.sort
        title = data.title
        trendsDetails = data.details
        viewCount = data.viewCount
        headImg = data.headImg
        upperName = data.upperName
        latitude = data.latitude
        longitude = data.longitude
        detailedAddress = data.detailedAddress
        address = data.address
        label = data.label
        vipLevel = data.vipLevel
        bigV = data.bigV
        isBlackList = data.isBlackList
        isCollected = data.favorites
        return this
    }

    fun copy(data: RecommendMultiBean): DynamicBean {
        uid = data.userId
        mediaKey = data.mediaKey
        smallMediaKey = data.smallMediaKey
        videoId = data.videoId
        mediaUrl = data.mediaUrl
        comments = data.comments
        coverPath = data.coverImgUrl
        createTime = data.date
        description = data.description
        focus = data.focusStatus
        like = data.like
        likeCount = data.likeCount
        photoCount = data.photoCount
        title = data.title
        trendsDetails = data.details
        viewCount = data.viewCount
        headImg = data.headImg
        upperName = data.upperName
        latitude = data.latitude
        longitude = data.longitude
        detailedAddress = data.detailedAddress
        address = data.address
        vipLevel = data.vipLevel
        bigV = data.bigV
        isCollected = data.favorites?.selected ?: false
        return this
    }

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
    val cover: String?
        get() {
            return if (coverPath.isNullOrEmpty()) {
                coverImgUrl
            } else {
                coverPath
            }
        }
}