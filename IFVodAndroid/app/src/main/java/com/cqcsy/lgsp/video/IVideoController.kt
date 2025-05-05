package com.cqcsy.lgsp.video

import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.net.VideoIntroductionNetBean
import com.cqcsy.lgsp.video.danmaku.BaseDanmakuItemData
import com.cqcsy.library.bean.UserInfoBean

/**
 * 播放器所有事件
 */
interface IVideoController {

    /**
     * 播放下集，没有就不响应
     */
    fun playNext() {}

    /**
     * 判断是否有下一集
     */
    fun hasNext(): Boolean {
        return false
    }

    /**
     * 判断是否有播放列表
     */
    fun hasPlayList(): Boolean {
        return false
    }

    /**
     * 广告查看详情点击回调
     */
    fun onDetailClick(advertBean: AdvertBean) {}

    /**
     * 购买VIP回调
     */
    fun onBuyVipClick() {}

    /**
     * 语言点击
     */
    fun onLanguageClick() {}

    /**
     * 分享点击
     */
    fun onShareClick() {}

    /**
     * 倍速点击
     */
    fun onSpeedClick() {}

    /**
     * 清晰度点击
     */
    fun onClarityClick() {}

    /**
     * 剧集点击
     */
    fun onEpisodeClick() {}

    /**
     * 设置点击
     */
    fun onSettingClick() {}

    /**
     * 弹幕输入框点击
     */
    fun onDanamaInputClick() {}

    /**
     * 弹幕点击
     */
    fun onDanamaClick(data: BaseDanmakuItemData) {}

    /**
     * 弹幕
     */
    fun onDanamaSettingClick() {}

    /**
     * 发送弹幕
     */
    fun onSendDanama(content: String) {}

    /**
     * 举报弹幕
     */
    fun onReportDanama(data: BarrageBean) {}

    /**
     * 屏蔽弹幕
     */
    fun onForbiddenDanama(data: BarrageBean) {}

    /**
     * 点赞弹幕
     */
    fun onLikeDanama(danmakuId: Long, data: BarrageBean) {}

    /**
     * 弹幕广告-查看详情
     */
    fun onCheckDanmakuAdDetails(danmakuId: Long, data: BarrageBean) {}

    /**
     * 弹幕广告-领取金币
     */
    fun onReceiveDanmakuAdCoin(danmakuId: Long, data: BarrageBean) {}

    /**
     *  检查是否VIP用户
     */
    fun isVip(): Boolean {
        return false
    }

    /**
     *  检查影片是否只有VIP可看
     */
//    fun isVipFilm(): Boolean

    /**
     * 投屏
     */
    fun onScreenShare() {}

    /**
     * 是否离线模式
     */
    fun isOfflineMode(): Boolean {
        return false
    }

    /**
     * 是否允许选择语言
     */
    fun isAllowLanguage(): Boolean {
        return false
    }

    /**
     * 当前语言
     */
    fun getLanguage(): String {
        return ""
    }

    /**
     * 是否允许选择清晰度
     */
    fun isAllowQuality(): Boolean {
        return false
    }

    /**
     * 当前清晰度
     */
    fun getQuality(): String {
        return ""
    }

    /**
     * 保持操作功能一直显示
     */
    fun isStayShow(): Boolean {
        return false
    }

    /**
     * 播放按钮点击
     */
    fun onPlayClick() {}

    /**
     * 跳过广告
     */
    fun onSkipAd() {}

    /**
     * 点击下载
     */
    fun onDownloadClick() {}

    /**
     * 开始播放视频(初次进入、切换视频)
     */
    fun onVideoStartPlay() {

    }

    /**
     * 是否从小视频列表播放时进入
     */
    fun isFromList(): Boolean {
        return false
    }

    /**
     * 退出全屏
     */
    fun exitFullScreen() {

    }

    /**
     * 是否允许显示错误提示，只用于直播超时切流
     */
    fun isEnableErrorShow(): Boolean {
        return true
    }

    /**
     * 获取up主用户id
     */
    fun getUpperInfo(): UserInfoBean? {
        return null
    }

    /**
     * 是否小视频
     */
    fun isShortVideo(): Boolean {
        return false
    }

    /**
     * 获取推荐小视频
     */
    fun getRecommendShort(): MutableList<ShortVideoBean>? {
        return null
    }

    /**
     * 播放指定小视频
     */
    fun playShortVideo(video: ShortVideoBean) {

    }

    /**
     * 获取详情
     */
    fun getDetailInfo(): VideoIntroductionNetBean? {
        return null
    }

    /**
     * 弹幕是否可用
     */
    fun isBarrageEnable(): Boolean {
        return true
    }

    /**
     * 是否直播
     */
    fun isLive(): Boolean {
        return false
    }

    /**
     * 是否电视直播
     */
    fun isTV(): Boolean {
        return false
    }

    /**
     * 是否允许显示错误提示
     */
    fun isEnableError(): Boolean {
        return true
    }

    /**
     * 直播播放失败后，点击重试，重新刷新数据
     */
    fun retryLive() {

    }
}