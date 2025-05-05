package com.cqcsy.lgsp.video

import com.cqcsy.lgsp.bean.BarrageBean

/**
 * 播放器页面控制接口
 */
interface IVideoPageController {

    /**
     * 获取视频播放器下面整块布局
     */
    fun getBottomLayoutId(): Int

    /**
     * 横屏发送弹幕，需要调用接口
     */
    fun sendDanmaku(input: String)

    /**
     * 横屏发送弹幕，需要调用接口
     */
    fun likeDanmaku(danmakuId: Long, data: BarrageBean)

    /**
     * 切换语言，重新获取详情，流程重新走一遍
     */
    fun changeLanguage(mediaKey: String, videoType:Int)

    /**
     * 输入法显示
     */
    fun onSoftBoardShow() {

    }

    /**
     * 获取播放信息成功回调
     */
    fun onPlayInfoResponseSuccess(episodeId: Int) {

    }

    /**
     * 获取播放信息失败回调
     */
    fun onPlayInfoResponseError() {

    }
}