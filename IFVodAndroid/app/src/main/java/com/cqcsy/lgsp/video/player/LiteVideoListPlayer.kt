package com.cqcsy.lgsp.video.player

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.library.utils.ImageUtil
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import kotlinx.android.synthetic.main.layout_video_bottom.view.*
import kotlinx.android.synthetic.main.layout_video_player.view.*
import kotlinx.android.synthetic.main.layout_video_top.view.*

/**
 * 列表播放器
 */
class LiteVideoListPlayer : LiteVideoPlayer {
    private var thumbUrl: String? = null

    constructor(context: Context?, fullFlag: Boolean?) : super(
        context,
        fullFlag
    )

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    override fun init(context: Context) {
        super.init(context)
        centerStart.visibility = View.VISIBLE
//        mTitleTextView.text = mTitle
        setViewShowState(mBackButton, View.GONE)
        screenUpload.visibility = View.GONE
        isShowFullAnimation = false
        if (!isIfCurrentIsFullscreen) {
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(30f)
            )
            topContent.layoutParams = layoutParams
        }
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        setViewShowState(mLoadingProgressBar, VISIBLE)
        setViewShowState(mThumbImageViewLayout, View.GONE)
    }

    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        setViewShowState(mLoadingProgressBar, View.VISIBLE)
        setBackVisibility()
        centerStart.visibility = View.GONE
        if (seekOnStart == 0L) {
            setViewShowState(mThumbImageViewLayout, View.VISIBLE)
        } else {
            setViewShowState(mThumbImageViewLayout, View.GONE)
        }
    }

    override fun changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear()
        if (isIfCurrentIsFullscreen) {
            setViewShowState(mBackButton, View.VISIBLE)
        }
        setBackVisibility()
    }

    override fun changeToLand() {
        setAllowAction(this, controllerI)
        screenShare.visibility = View.VISIBLE
        landBottom.visibility = View.VISIBLE
        screenUpload.visibility = View.GONE
        setViewShowState(mStartButton, View.GONE)
        if (controllerI?.isOfflineMode() == true) {
            exitFullscreen.visibility = View.GONE
        }
    }

    override fun changeUiToPlayingShow() {
        super.changeUiToPlayingShow()
        setBackVisibility()
        screenUpload.visibility = View.GONE
        screenOption.visibility = View.GONE
//        if (isIfCurrentIsFullscreen) {
//            mLockScreen.visibility = View.VISIBLE
//        } else {
//            mLockScreen.visibility = View.GONE
//        }
//        if (!mHadPlay && !isIfCurrentIsFullscreen) {    // 首次开始播放并且不是全屏，默认隐藏所有控件
//            hideAllActionContent()
//        }
    }

    fun setThumbImageUrl(url: String?) {
        thumbUrl = url
        loadThumb()
    }

    private fun loadThumb() {
        if (!thumbUrl.isNullOrEmpty()) {
            val imageView = ImageView(context)
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setBackgroundColor(Color.TRANSPARENT)
            thumbImageView = imageView
            ImageUtil.loadImage(context, thumbUrl, imageView, 0)
        }
    }

    override fun changeUiToCompleteShow() {
        changeUiToNormal()
        setBackVisibility()
        if (isIfCurrentIsFullscreen && isVerticalVideo && !thumbUrl.isNullOrEmpty()) {
            loadThumb()
        }
    }

    override fun changeUiToPauseShow() {
        super.changeUiToPauseShow()
        setViewShowState(screenOption, View.GONE)
        setViewShowState(screenUpload, View.GONE)
    }

    override fun changeUiToNormal() {
        super.changeUiToNormal()
        hideAllWidget()
        setViewShowState(topContent, View.GONE)
        setViewShowState(mTitleTextView, View.GONE)
        centerStart.visibility = View.VISIBLE
        setViewShowState(mBottomProgressBar, View.GONE)
        setViewShowState(mBackButton, View.GONE)
    }

    private fun setBackVisibility() {
        if (isIfCurrentIsFullscreen) {
            showTopContent(true)
            showRightContent()
            setViewShowState(mBackButton, View.VISIBLE)
        } else {
            setViewShowState(mBackButton, View.GONE)
        }
    }

    override fun cloneParams(from: GSYBaseVideoPlayer, to: GSYBaseVideoPlayer) {
        super.cloneParams(from, to)

        if (!isVerticalVideo && from.thumbImageView != null) {
            if (from.thumbImageView.parent != null && from.thumbImageView.parent is ViewGroup) {
                (from.thumbImageView.parent as ViewGroup).removeView(from.thumbImageView)
            }
            to.thumbImageView = from.thumbImageView
        }
        if (to is LiteVideoListPlayer && from is LiteVideoListPlayer) {
            to.thumbUrl = from.thumbUrl
        }
    }

    override fun startWindowFullscreen(
        context: Context,
        actionBar: Boolean,
        statusBar: Boolean
    ): GSYBaseVideoPlayer? {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        if (gsyBaseVideoPlayer != null && gsyBaseVideoPlayer is LiteVideoListPlayer) {
            gsyBaseVideoPlayer.screenOption.visibility = View.GONE
            setViewShowState(mStartButton, GONE)
        }
        return gsyBaseVideoPlayer
    }

    override fun resolveNormalVideoShow(
        oldF: View?,
        vp: ViewGroup?,
        gsyVideoPlayer: GSYVideoPlayer?
    ) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
//        setBackVisibility()
//        (gsyVideoPlayer as LiteVideoListPlayer).hideAllActionContent()  // 全屏返回默认隐藏所有控件
        if (gsyVideoPlayer?.currentState == CURRENT_STATE_AUTO_COMPLETE) {
            gsyVideoPlayer.onAutoCompletion()
        }
    }

    override fun showBottomContent() {
        if (!isInPlayingState) {
            return
        }
        super.showBottomContent()
        if (!isIfCurrentIsFullscreen)
            setViewShowState(topContent, View.GONE)
    }

    override fun showTopContent(isForceShow: Boolean) {
        super.showTopContent(isForceShow)
        screenUpload.visibility = View.GONE
    }

    override fun onCompletion() {
        super.onCompletion()
//        exitFull()
    }

    override fun onAutoCompletion() {
        super.onAutoCompletion()
        exitFull()
    }

    override fun onVideoPause() {
        super.onVideoPause()
//        changeUiToNormal()
    }

    override fun showAllActionContent() {
        super.showAllActionContent()
        centerStart.isVisible = true
    }

    fun exitFull() {
        if (isIfCurrentIsFullscreen) controllerI?.exitFullScreen()
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (isEnableTouch()) {
            super.touchSurfaceMove(deltaX, deltaY, y)
        }
    }

    override fun isEnableTouch(): Boolean {
        return isIfCurrentIsFullscreen && super.isEnableTouch()
    }
}