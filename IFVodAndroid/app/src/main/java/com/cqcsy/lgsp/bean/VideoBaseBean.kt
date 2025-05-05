package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.utils.Constant

/**
 * 跳转到视频播放页所需要的数据Bean,封装抽象类
 */
open class VideoBaseBean : BaseBean() {

    companion object {
        val SHOW_INTRO = 0
        val SHOW_CHAT = 1
        val SHOW_COMMENT = 2
    }

    // 视频资源类型 0：电影 1：电视剧 2：综艺 3：小视频
    var videoType: Int = -1

    // 数据ID
    var mediaId: String = ""  // 统一使用mediaKey，此字段只用于小视频上传部分以及聊天室

    // 数据key
    var mediaKey: String = ""

    // 媒体播放链接
    var mediaUrl: String? = ""

    // 封面图片地址
    var coverImgUrl: String? = ""

    // 标题
    var title: String? = ""

    // 视频二级分类，如真人秀、喜剧、战争、爱情等
    var contentType: String? = ""

    var cidMapper: String? = null

    var regional: String? = null

    // 正在观看的集/期对应的ID
    var episodeId: Int = 0

    // 某个视频ID，不区分清晰度
    var uniqueID: Int = 0

    // 是否显示推荐图标
    var isVip: Boolean = false

    // 观看的时间长度 (电影 短视频) 秒
    var watchingProgress: Long = 0

    // 作者
    var upperName: String? = ""

    // 作者ID
    var userId: Int = 0

    // 剧集、期数名称
    var episodeTitle: String? = ""

    // 语言类型
//    var langType = 0
    // 语言
    var lang: String? = ""

    // 清晰度
    var resolution: String? = ""

    // 描述
    var remark: String? = ""

    // 清晰度描述
    var resolutionDes: String? = ""

    // 时长
    var duration: String = ""

    // 播放记录ID
    var playID: Int = 0

    // 片头时间
    var opSecond: Long = 0

    // 片尾时间
    var epSecond: Long = 0

    // 剧集标识
    var episodeKey = ""

    // 用于离线下载视频的参数
    // 缓存路径
    var filePath: String? = ""

    // 大小
    var size: Long = 0

    // 视频数量
    var videoNumber = 0

    // 时长
    var time = ""

    // 是否观看过离线视频
    var isWatched = false

    var isFilterAds: Boolean = false // 是否已经使用金币跳过广告

    // 收藏时间
    var collectionTime: String = ""

    var barrageStatus: Int = 0 // 3:显示聊天，不显示弹幕  2：全部显示，包括聊天室   1： 显示弹幕，不显示聊天室  0：都不不显示

    // 添加播放记录地址
    var playRecordUrl: String = ""

    // 全集/更新至xx期
    var updateStatus: String = ""

    // 默认显示简介、聊天、评论===>0  简介 1 聊天  2 评论
    var detailShowType: Int = SHOW_INTRO

    // 是否是热播
    var isHot: Boolean = false

    // 是否是最新
    var isLast: Boolean = false

    // 直播状态：true 表示维护中，false 正常数据
    var maintainStatus: Boolean = false

    val isLive: Boolean
        get() = videoType == Constant.VIDEO_TV || videoType == Constant.VIDEO_LIVE

    fun resetByOtherModel(source: VideoBaseBean) {
        copy(source, this)
        watchingProgress = 0
        filePath = ""
        isWatched = false
        isFilterAds = false
    }

    open fun clone(): VideoBaseBean {
        val target = VideoBaseBean()
        copy(this, target)
        return target
    }

    private fun copy(source: VideoBaseBean, target: VideoBaseBean) {
        target.videoType = source.videoType
        target.mediaId = source.mediaId
        target.mediaKey = source.mediaKey
        target.mediaUrl = source.mediaUrl
        target.title = source.title
        target.episodeId = source.episodeId
        target.uniqueID = source.uniqueID
        target.isVip = source.isVip
        target.watchingProgress = source.watchingProgress
        target.episodeTitle = source.episodeTitle
        target.lang = source.lang
        target.resolution = source.resolution
        target.resolutionDes = source.resolutionDes
        target.duration = source.duration
        target.playID = source.playID
        target.opSecond = source.opSecond
        target.epSecond = source.epSecond
        target.episodeKey = source.episodeKey
        target.filePath = source.filePath
//        target.size = source.size
//        target.videoNumber = source.videoNumber
//        target.time = source.time
        target.isWatched = source.isWatched
        target.isFilterAds = source.isFilterAds
        target.maintainStatus = source.maintainStatus
//        target.isHot = source.isHot
        target.isLast = source.isLast
//        target.playRecordUrl = source.playRecordUrl
//        target.collectionTime = source.collectionTime
        target.barrageStatus = source.barrageStatus
        if (!source.coverImgUrl.isNullOrEmpty()) {
            target.coverImgUrl = source.coverImgUrl
        }
        if(!source.contentType.isNullOrEmpty()) {
            target.contentType = source.contentType
        }
        if (!source.cidMapper.isNullOrEmpty()) {
            target.cidMapper = source.cidMapper
        }
        if (!source.regional.isNullOrEmpty()) {
            target.regional = source.regional
        }
        if(!source.upperName.isNullOrEmpty()) {
            target.upperName = source.upperName
        }
        if(!source.updateStatus.isNullOrEmpty()) {
            target.updateStatus = source.updateStatus
        }
    }
}