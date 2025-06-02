package com.cqcsy.lgsp.video.player

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.app.VideoApplication
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.video.IVideoController
import com.cqcsy.library.utils.StatusBarUtil
import com.shuyu.gsyvideoplayer.GSYVideoADManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import kotlinx.android.synthetic.main.layout_ad_error.view.*
import kotlinx.android.synthetic.main.layout_video_ad.view.*

/**
 * 广告播放器
 */
class LiteVideoAdPlayer : GSYADVideoPlayer {
    private var controllerI: IVideoController? = null
    var currentAd: AdvertBean? = null
    var totalTime: Long = 0
        set(value) {
            field = value
            currentPlayer.buyVip.visibility = View.VISIBLE
            (currentPlayer as LiteVideoAdPlayer).adTime.text = "${value}秒"
        }
    var isMute = false
    var isPlayerVertical = false

    fun setVideoController(listener: IVideoController) {
        controllerI = listener
    }

    constructor(
        context: Context?,
        fullFlag: Boolean?
    ) : super(context, fullFlag)

    constructor(context: Context?) : super(context)

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ) : super(context, attrs)

    override fun getLayoutId(): Int {
        return R.layout.layout_video_ad
    }

    override fun init(context: Context) {
        super.init(context)
        voiceController.setOnClickListener {
            toggleMute()
            setMuteState(GSYVideoADManager.instance().isNeedMute)
        }
        adDetail.setOnClickListener {
            controllerI?.onDetailClick(currentAd!!)
        }
        buyVip.setOnClickListener {
            controllerI?.onSkipAd()
        }
    }

    private fun toggleMute() {
        GSYVideoADManager.instance().isNeedMute = !GSYVideoADManager.instance().isNeedMute
        isMute = GSYVideoADManager.instance().isNeedMute
    }

    private fun setMuteState(isMute: Boolean) {
        if (isMute) {
            voiceController.setImageResource(R.mipmap.icon_voice_off)
        } else {
            voiceController.setImageResource(R.mipmap.icon_voice_on)
        }
    }

    override fun setProgressAndTime(
        progress: Int,
        secProgress: Int,
        currentTime: Long,
        totalTime: Long,
        forceChange: Boolean
    ) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
        if (adTime != null && currentTime > 0) {
            val current = currentTime / 1000
            val total = this.totalTime - current
            if (total > 0)
                adTime.text = "${total}秒"
            else {
                adTime.text = "0秒"
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        setMuteState(GSYVideoADManager.instance().isNeedMute)
    }

    override fun getShrinkImageRes(): Int {
        return R.mipmap.icon_exit_full_screen
    }

    override fun getEnlargeImageRes(): Int {
        return R.mipmap.icon_full_screen
    }

    override fun onAutoCompletion() {
        super.onAutoCompletion()
        totalTime -= duration / 1000
        adTime.text = "${totalTime}秒"
    }

    override fun changeUiToNormal() {
        super.changeUiToNormal()
        clearProgressAnimation()
        isMute = false
        isFirstPrepared = false
        adBottomContainer.visibility = View.VISIBLE
        voiceController.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
    }

    override fun changeUiToPreparingShow() {
        if (mUrl.isNullOrEmpty()) {
            changeUiToError()
            return
        }
        super.changeUiToPreparingShow()
        adTime.text = "${totalTime}秒"
        showProgressAnimation()
    }

    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        setMuteState(isMute)
        GSYVideoADManager.instance().isNeedMute = isMute
        clearProgressAnimation()
    }

    override fun changeUiToError() {
        super.changeUiToError()
        startErrorShow()
        removeCallbacks(countDownThread)
        postDelayed(countDownThread, 1000)
    }

    fun isErrorShow(): Boolean {
        return errorContainer.visibility == View.VISIBLE
    }

    private fun startErrorShow() {
        adBottomContainer.visibility = View.GONE
        voiceController.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        if (isIfCurrentIsFullscreen) {
            errorImage.setImageResource(R.mipmap.image_ad_error_full)
        } else {
            errorImage.setImageResource(R.mipmap.image_ad_error_normal)
        }
    }

    private val countDownThread = object : Runnable {
        override fun run() {
            totalTime--
            if (totalTime > 0) {
                adTime.text = "${totalTime}秒"
                postDelayed(this, 1000)
            } else {
                adTime.text = "0秒"
                mVideoAllCallBack.onAutoComplete("", currentPlayer)
            }
        }

    }

    override fun isVerticalVideo(): Boolean {
        return super.isVerticalVideo() || isPlayerVertical
    }

    override fun hideAllWidget() {
        super.hideAllWidget()
        clearProgressAnimation()
    }

    private fun showProgressAnimation() {
        setViewShowState(mLoadingProgressBar, View.VISIBLE)
    }

    private fun clearProgressAnimation() {
        setViewShowState(mLoadingProgressBar, View.GONE)
    }

    override fun startWindowFullscreen(
        context: Context?,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer {
        val gsyBaseVideoPlayer =
            super.startWindowFullscreen(context, actionBar, statusBar)
        removeCallbacks(countDownThread)
        if (gsyBaseVideoPlayer != null) {
            val gsyVideoPlayer = gsyBaseVideoPlayer as LiteVideoAdPlayer
            gsyVideoPlayer.controllerI = controllerI
            gsyVideoPlayer.currentAd = currentAd
            gsyVideoPlayer.totalTime = totalTime
            gsyVideoPlayer.isMute = isMute
            GSYVideoADManager.instance().isNeedMute = isMute
            if (isVerticalVideo) {
                gsyVideoPlayer.adTopContainer.updateLayoutParams<RelativeLayout.LayoutParams> {
                    topMargin = context?.let { StatusBarUtil.getStatusBarHeight(it) } ?: SizeUtils.dp2px(44f)
                }
                gsyVideoPlayer.adBottomContainer.updateLayoutParams<RelativeLayout.LayoutParams> {
                    bottomMargin = SizeUtils.dp2px(80f)
                }
            } else {
                gsyVideoPlayer.adTopContainer.setPadding(
                    SizeUtils.dp2px(40f),
                    SizeUtils.dp2px(9f),
                    SizeUtils.dp2px(40f),
                    SizeUtils.dp2px(9f)
                )
                gsyVideoPlayer.adBottomContainer.setPadding(
                    SizeUtils.dp2px(40f),
                    SizeUtils.dp2px(9f),
                    SizeUtils.dp2px(40f),
                    SizeUtils.dp2px(9f)
                )
            }
        }
        return gsyBaseVideoPlayer
    }

    override fun resolveNormalVideoShow(
        oldF: View?,
        vp: ViewGroup?,
        gsyVideoPlayer: GSYVideoPlayer?
    ) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        if (gsyVideoPlayer != null) {
            val player = gsyVideoPlayer as LiteVideoAdPlayer
            currentAd = player.currentAd
            totalTime = player.totalTime
            isMute = player.isMute
            GSYVideoADManager.instance().isNeedMute = isMute
            player.removeCallbacks(countDownThread)
        }
        GSYVideoManager.backFromWindowFull(context)
    }

    override fun release() {
        super.release()
        if (adTime != null) {
            adTime.visibility = View.VISIBLE
        }
    }

    override fun onVideoPause() {
        super.onVideoPause()
        if (isErrorShow()) {
            removeCallbacks(countDownThread)
        }
    }

    override fun onVideoResume() {
        super.onVideoResume()
        if (isErrorShow()) {
            postDelayed(countDownThread, 1000)
        }
    }

    override fun onGankAudio() {
        try {
            if (!(context.applicationContext as VideoApplication).isAppBackground) {
                onVideoResume()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}