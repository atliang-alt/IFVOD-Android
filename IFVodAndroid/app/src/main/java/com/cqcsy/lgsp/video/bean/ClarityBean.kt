package com.cqcsy.lgsp.video.bean

import com.cqcsy.library.base.BaseBean
import com.cqcsy.lgsp.bean.VideoBaseBean

/**
 * 清晰度
 */
class ClarityBean : BaseBean() {
    var resolution: String? = ""
    var isVip: Boolean = false
    var isLive: Boolean = false
    var mediaUrl: String? = ""
    var episodeKey: String = ""
    var opSecond: Long = 0
    var epSecond: Long = 0
    var episodeId: Int = 0
    var uniqueID: Int = 0
    var episodeTitle: String = ""
    var lang: String? = ""
    var mediaKey: String = ""
    var resolutionDes: String? = ""
    var title: String = ""
    var videoType: Int = 0
    var goldOpenNumber: Int = 0
    var isBoughtByCoin: Boolean = false // 是否金币解锁
    var isFilterAds: Boolean = false // 是否已经使用金币跳过广告
    var isDefault: Boolean = false // 默认播放
    var barrageStatus: Int = 1
    var detailShowType: Int = 0

    fun setValueToBase(videoBaseBean: VideoBaseBean) {
        videoBaseBean.resolution = resolution
        videoBaseBean.isVip = isVip
        videoBaseBean.mediaUrl = mediaUrl
        videoBaseBean.episodeKey = episodeKey
        videoBaseBean.opSecond = opSecond
        videoBaseBean.epSecond = epSecond
        videoBaseBean.episodeId = episodeId
        videoBaseBean.episodeTitle = episodeTitle
        videoBaseBean.lang = lang
        videoBaseBean.mediaKey = mediaKey
        videoBaseBean.uniqueID = uniqueID
        videoBaseBean.resolutionDes = resolutionDes
        videoBaseBean.title = title
        videoBaseBean.videoType = videoType
        videoBaseBean.isFilterAds = isFilterAds
        videoBaseBean.barrageStatus = barrageStatus
        videoBaseBean.detailShowType = detailShowType
        videoBaseBean.filePath = ""
    }
}