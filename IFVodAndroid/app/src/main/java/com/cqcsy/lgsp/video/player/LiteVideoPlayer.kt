package com.cqcsy.lgsp.video.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.media.SoundPool
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ResourceUtils.getDrawable
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.app.VideoApplication
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.AnimationUtil
import com.cqcsy.lgsp.video.IVideoController
import com.cqcsy.lgsp.video.danmaku.AdvertModel
import com.cqcsy.lgsp.video.danmaku.BaseDanmakuItemData
import com.cqcsy.lgsp.video.danmaku.DanmakuAdRenderer
import com.cqcsy.lgsp.video.danmaku.DanmakuViewModel
import com.cqcsy.lgsp.video.danmaku.OnDanmakuListener
import com.cqcsy.lgsp.video.danmaku.SpannedModel
import com.cqcsy.lgsp.video.danmaku.SpannedRenderer
import com.cqcsy.lgsp.video.view.DanmuHoldPopupWindow
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.layout_replay.view.*
import kotlinx.android.synthetic.main.layout_video_advertisement.view.*
import kotlinx.android.synthetic.main.layout_video_bottom.view.*
import kotlinx.android.synthetic.main.layout_video_error.view.*
import kotlinx.android.synthetic.main.layout_video_player.view.*
import kotlinx.android.synthetic.main.layout_video_top.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.abs


/**
 * 配置弹幕使用的播放器
 * so只有v5 v7 x86、没有64，要配置上ndk过滤。
 */
open class LiteVideoPlayer : StandardGSYVideoPlayer, NetworkUtils.OnNetworkStatusChangedListener, OnDanmakuListener {
    var controllerI: IVideoController? = null

    private val animationTime = 200L

    lateinit var mHandler: MyHandler

    lateinit var danmakuViewModel: DanmakuViewModel

    private var holdPopupWindow: DanmuHoldPopupWindow? = null
    private var errorPosition: Long = 0L  // 播放错误时间点

    val isPlaying: Boolean
        get() = currentPlayer.currentState == CURRENT_STATE_PLAYING

    val isBuffering: Boolean
        get() = currentPlayer.currentState == CURRENT_STATE_PLAYING_BUFFERING_START

    val isPaused: Boolean
        get() = currentPlayer.currentState == CURRENT_STATE_PAUSE


    fun setVideoController(listener: IVideoController) {
        controllerI = listener
    }

    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun getLayoutId(): Int {
        return R.layout.layout_video_player
    }

    override fun init(context: Context) {
        super.init(context)
        mHandler = MyHandler(this)
        danmakuViewModel = DanmakuViewModel(danmakuView, this)
        danmakuView.onDanmakuListener = this

        setDialogProgressBar(getDrawable(R.drawable.progress_dp2))
        setDialogVolumeProgressBar(getDrawable(R.drawable.volume_progress))

        setListener()
    }

    private fun setListener() {
        NetworkUtils.registerNetworkStatusChangedListener(this)

        startLand.setOnClickListener(this)
        bulletOpen.setOnClickListener(this)
        bulletEdit.setOnClickListener(this)
        bulletSetting.setOnClickListener(this)
        bullet_flow_setting.setOnClickListener(this)
        bullet_flow_edit.setOnClickListener(this)

        screenCut.setOnClickListener(this)
        failedRefresh.setOnClickListener(this)
        screenShare.setOnClickListener(this)
        playNext.setOnClickListener(this)
        videoLanguage.setOnClickListener(this)
        videoSpeed.setOnClickListener(this)
        videoSource.setOnClickListener(this)
        videoEpisode.setOnClickListener(this)
        screenOption.setOnClickListener(this)
        screenUpload.setOnClickListener(this)
        closeAd.setOnClickListener(this)
        centerStart.setOnClickListener(this)
        replay.setOnClickListener(this)
        imageAdvert.setOnClickListener(this)
        exitFullscreen.setOnClickListener(this)
        replayContent.setOnTouchListener { v, event -> true }
    }

    fun setOfflineProgress() {
        mProgressBar.progressDrawable = getDrawable(R.drawable.progress_dp2_white_30_offline)
        mProgressBar.thumb = getDrawable(R.drawable.progress_thumb)
    }

    fun setNormalProgress() {
        mProgressBar.progressDrawable = getDrawable(R.drawable.progress_dp2_white_30)
        mProgressBar.thumb = getDrawable(R.drawable.progress_thumb)
    }

    /**
     * 设置暂停广告，只有暂停时有效
     */
    fun setPauseAdvert(advertBean: AdvertBean, player: LiteVideoPlayer = currentPlayer) {
        if (advertBean.showURL.isEmpty() || player.currentState != GSYVideoView.CURRENT_STATE_PAUSE) {
            return
        }
        player.advertisement.isVisible = true
        player.closeAd.isVisible = false
        setImageAdParams(player.adContainer)
        ImageUtil.loadImage(
            context.applicationContext,
            advertBean.showURL,
            player.imageAdvert,
            0,
            needAuthor = true,
            defaultImage = 0,
            requestListener = object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    player.imageAdvert.tag = advertBean
                    player.advertisement.isVisible = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (player.currentState == CURRENT_STATE_PAUSE) {
                        player.centerStart.isVisible = true
                        player.imageAdvert.tag = advertBean
                        player.advertisement.isVisible = true
                        player.closeAd.isVisible = true
                    } else {
                        player.advertisement.isVisible = false
                    }
                    return false
                }

            }
        )
    }

    /**
     * 动态设置暂停图片广告尺寸
     */
    private fun setImageAdParams(adContainer: FrameLayout) {
        // 宽高比16:9
        // 宽度：播放区域的6/11
        var width = ScreenUtils.getScreenWidth() * 6 / 11
        var height = width * 9 / 16
        if (mIfCurrentIsFullscreen) {
            if (isVerticalVideo) {
                width = (ScreenUtils.getScreenWidth() * 0.73).toInt()
                height = (width * 0.57).toInt()
            } else {
                // 宽度：播放区域的5/11
                width = ScreenUtils.getScreenWidth() * 5 / 11
                height = width * 9 / 16
            }
        }
        val layoutParams = LinearLayout.LayoutParams(width, height)
        adContainer.layoutParams = layoutParams
    }

    /**
     * 设置广告倒计时提示
     */
    fun setTopTip(tips: String, isAutoHide: Boolean = true, colorRes: Int = R.color.grey_4) {
        if (isVerticalVideo && isIfCurrentIsFullscreen) {
            currentPlayer.videoTopTip.isVisible = false
            currentPlayer.vertical_tip.text = tips
            currentPlayer.vertical_tip.isVisible = true
            currentPlayer.vertical_tip.setTextColor(ColorUtils.getColor(colorRes))
        } else {
            currentPlayer.vertical_tip.isVisible = false
            currentPlayer.videoTopTip.text = tips
            currentPlayer.videoTopTip.isVisible = true
            currentPlayer.videoTopTip.setTextColor(ColorUtils.getColor(colorRes))
            setTopTipLayout()
        }
        if (isAutoHide) {
            mHandler.postDelayed({
                hideTopTip()
            }, dismissControlTime.toLong())
        }
    }

    fun showReceiveCoinTip(tips: String, isAutoHide: Boolean = true) {
        currentPlayer.receive_coin_tip.text = tips
        currentPlayer.receive_coin_tip.isVisible = true
        if (isAutoHide) {
            mHandler.postDelayed({
                hideReceiveCoinTip()
            }, dismissControlTime.toLong())
        }
    }

    private fun setTopTipLayout() {
        val layoutParams = currentPlayer.videoTopTip.layoutParams as RelativeLayout.LayoutParams
        if (isIfCurrentIsFullscreen) {
            layoutParams.rightMargin = SizeUtils.dp2px(180f)
        } else {
            layoutParams.rightMargin = SizeUtils.dp2px(45f)
        }
        currentPlayer.videoTopTip.layoutParams = layoutParams
    }

    fun hideTopTip() {
        currentPlayer.videoTopTip.isVisible = false
        currentPlayer.vertical_tip.isVisible = false
    }

    fun hideReceiveCoinTip() {
        currentPlayer.receive_coin_tip.isVisible = false
    }

    override fun getCurrentPlayer(): LiteVideoPlayer {
        val player = super.getCurrentPlayer()
        return player as LiteVideoPlayer
    }

    /**
     * 设置普通其他提示
     */
    fun setBottomTip(tips: String, isAutoHide: Boolean = true) {
        cancelDismiss()
        mHandler.removeCallbacksAndMessages(null)
        if (isVerticalVideo && isIfCurrentIsFullscreen) {
            currentPlayer.vertical_tip.text = tips
            currentPlayer.vertical_tip.isVisible = true
            currentPlayer.videoBottomTip.isVisible = false
        } else {
            showAllActionContent()
            currentPlayer.videoBottomTip.text = tips
            currentPlayer.videoBottomTip.isVisible = true
            currentPlayer.vertical_tip.isVisible = false
        }
        if (isAutoHide) {
            mHandler.postDelayed({
                hideBottomTip()
                hideAllActionContent()
            }, dismissControlTime.toLong())
        }
    }

    fun hideBottomTip() {
        currentPlayer.videoBottomTip.isVisible = false
        currentPlayer.vertical_tip.isVisible = false
    }

    private fun hideAllTip() {
        hideTopTip()
        hideReceiveCoinTip()
        hideBottomTip()
        cancelDismiss()
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun setStateAndUi(state: Int) {
        if (controllerI?.isLive() == true && state == CURRENT_STATE_AUTO_COMPLETE) { // 直播
            return
        }
        super.setStateAndUi(state)
    }

    override fun getShrinkImageRes(): Int {
        return R.mipmap.icon_exit_full_screen
    }

    override fun getEnlargeImageRes(): Int {
        return R.mipmap.icon_full_screen
    }

    override fun onVideoPause() {
        super.onVideoPause()
//        gsyVideoManager.pause()
        danmakuViewModel.danmakuOnPause()
    }

    override fun onVideoResume(seek: Boolean) {
        super.onVideoResume(false)
//        gsyVideoManager.start()
        danmakuViewModel.startDanmaku()
    }

    override fun release() {
        mUrl = null
        mOriginUrl = null
        danmakuViewModel.releaseDanmaku()
        if (gsyVideoManager.lastListener() != null) {
            val player = gsyVideoManager.lastListener() as LiteVideoPlayer
            player.danmakuViewModel.releaseDanmaku()
        }
        super.release()
    }

    fun getCurrentSpeed(): String {
        val player = gsyVideoManager.listener() as LiteVideoPlayer
        return player.videoSpeed.text.toString()
    }

    fun setSpeedText(text: String) {
        val player = gsyVideoManager.listener() as LiteVideoPlayer
        player.videoSpeed.text = text
    }

    open fun showAllActionContent() {
        if (isLocked()) {
            return
        }
        if (mCurrentState == GSYVideoView.CURRENT_STATE_PLAYING) {
            startDismiss()
        } else {
            cancelDismiss()
        }
        showTopContent(false)
        showRightContent()
        if (isShowRecommendShort()) {
            setViewShowState(bottomContent, View.GONE)
        } else {
            showBottomContent()
        }

        centerStart.isVisible = isEnableTouch() && controllerI?.isLive() == false
    }

    open fun showBottomContent() {
        setViewShowState(mBottomContainer, View.VISIBLE)
        setViewShowState(mBottomProgressBar, View.GONE)
        AnimationUtil.with()?.bottomMoveToViewLocation(bottomContent, animationTime)
    }

    fun showRightContent() {
        if (!isIfCurrentIsFullscreen) {
            return
        }
        AnimationUtil.with()?.rightMoveToViewLocation(rightContent, animationTime)
    }

    open fun showTopContent(isForceShow: Boolean) {
        if (isIfCurrentIsFullscreen || isForceShow) {
            AnimationUtil.with()?.topMoveToViewLocation(topContent, animationTime)
        } else {
            screenUpload.isVisible = true
        }

        if (isIfCurrentIsFullscreen/* && isVerticalVideo*/) {
            setViewShowState(mTitleTextView, VISIBLE)
            setViewShowState(mTopContainer, VISIBLE)
        }
    }

    private fun getLocalVisibleRect(context: Context, view: View): Boolean {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return false
        }
        val p = Point()
        (context as Activity).windowManager.defaultDisplay.getSize(p)
        val screenWidth: Int = p.x
        val screenHeight: Int = p.y
        val rect = Rect(0, 0, screenWidth, screenHeight)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        view.tag = location[1] //存储y方向的位置
        return view.getLocalVisibleRect(rect)
    }

    fun hideAllActionContent() {
        if (!isPlaying) {
            return
        }
        if (isIfCurrentIsFullscreen) {
            setViewShowState(mBottomProgressBar, GONE)
            AnimationUtil.with()?.moveToViewTop(topContent, animationTime)
        }

        hideRightContent()
        hideBottomContent()
        centerStart.isVisible = false
    }

    private fun hideRightContent() {
        if (isLocked()) {

        } else {
            AnimationUtil.with()?.moveToViewRight(rightContent, animationTime)
        }
    }

    private fun hideBottomContent() {
        if (getLocalVisibleRect(context, bottomContent)) {
            AnimationUtil.with()?.moveToViewBottom(bottomContent, animationTime, mBottomContainer)
        } else {
            setViewShowState(mBottomContainer, View.INVISIBLE)
        }
    }

    override fun changeUiToPlayingBufferingClear() {
        super.changeUiToPlayingBufferingClear()
        hideRightContent()
        showTopContent(true)
        hideAllTip()
    }

    override fun setProgressAndTime(
        progress: Int,
        secProgress: Int,
        currentTime: Long,
        totalTime: Long,
        forceChange: Boolean
    ) {
        if (mCurrentState != CURRENT_STATE_PLAYING) {
            return
        }
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
        mCurrentPosition = currentTime
    }

    override fun setSpeed(speed: Float, soundTouch: Boolean) {
        super.setSpeed(speed, soundTouch)
        danmakuViewModel.setPlaySpeed(speed)
    }

    override fun changeUiToPlayingBufferingShow() {
        super.changeUiToPlayingBufferingShow()
        danmakuViewModel.danmakuOnPause()
        if (currentPositionWhenPlaying == 0L) {
            setViewShowState(mLoadingProgressBar, GONE)
            setViewShowState(mThumbImageViewLayout, VISIBLE)
        } else {
            changeUiToPlayingBufferingClear()
        }
        if (!mIfCurrentIsFullscreen) {
            setViewShowState(mStartButton, View.VISIBLE)
        } else {
            setViewShowState(mStartButton, View.GONE)
        }
        centerStart.isVisible = false
        bottomContent.isVisible = false
    }

    override fun changeUiToPreparingShow() {
        super.changeUiToPreparingShow()
        if (mUrl.isNullOrEmpty()) {
            setStateAndUi(GSYVideoView.CURRENT_STATE_ERROR)
            return
        }
        danmakuViewModel.danmakuOnPause()
        showTopContent(true)
        setViewShowState(mBackButton, View.VISIBLE)
        bottomContent.isVisible = false
        errorContent.isVisible = false
        screenUpload.isVisible = false
    }

    override fun changeUiToPrepareingClear() {
        super.changeUiToPrepareingClear()
        setViewShowState(mBackButton, View.VISIBLE)
    }

    public override fun changeUiToPlayingShow() {
        if (!mHadPlay) {
            controllerI?.onVideoStartPlay()
            if (NetworkUtils.isMobileData())
                setBottomTip(StringUtils.getString(R.string.not_wifi_play_tips), true)
        }
        if ((context.applicationContext as VideoApplication).isAppBackground) {
            onVideoPause()
            return
        } else if (controllerI?.isOfflineMode() == false && mSeekOnStart > 0 && controllerI?.isFromList() == false) { // 初始化播放时seek到对应位置，不做任何处理，否则播放器看上去会闪一下
            return
        } else if (isIfCurrentIsFullscreen && isLocked()) { // 全屏并且锁定的时候，切换到加载状态回来需要做下面处理
            setViewShowState(mLoadingProgressBar, GONE)
            setViewShowState(mThumbImageViewLayout, GONE)
            mHandler.postDelayed({
                danmakuViewModel.startDanmaku()
            }, 100)
            return
        }
        super.changeUiToPlayingShow()
        mHandler.postDelayed({
            danmakuViewModel.startDanmaku()
        }, 100)
        errorContent.isVisible = false
        advertisement.isVisible = false
        advertisement.imageAdvert.tag = null
        setViewShowState(mBackButton, View.VISIBLE)
        if (isIfCurrentIsFullscreen && !isLocked()) {
            screenCut.isVisible = true
            setViewShowState(mTitleTextView, View.VISIBLE)
            mHandler.postDelayed({
                showAllActionContent()
            }, 100)
        } else if (mCurrentState == GSYVideoView.CURRENT_STATE_AUTO_COMPLETE) {
            screenCut.isVisible = false
        } else {
            showAllActionContent()
        }
        setBulletShow()
        setLandByState()
    }

    override fun changeUiToPauseShow() {
        try {
            super.changeUiToPauseShow()
            showAllActionContent()
            videoTopTip.isVisible = false
            setLandByState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun changeUiToPauseClear() {
        if (mCurrentState == CURRENT_STATE_PAUSE) return
        super.changeUiToPauseClear()
    }

    override fun changeUiToCompleteShow() {
        super.changeUiToCompleteShow()
        setViewShowState(mBackButton, View.VISIBLE)
        if (isIfCurrentIsFullscreen) {
            setViewShowState(mStartButton, View.GONE)
            landBottom.isVisible = true
        } else {
            landBottom.isVisible = false
            setViewShowState(mStartButton, View.VISIBLE)
        }
        // 全屏底部操作
        playNext.isVisible = false
        videoLanguage.isVisible = false
        screenCut.isVisible = false
        screenShare.isVisible = false
        videoSpeed.isVisible = false
        screenOption.isVisible = false
        videoSource.isVisible = false
        setViewShowState(mLockScreen, GONE)
        setBulletShow()

        videoEpisode.isVisible = controllerI?.isOfflineMode() == false && controllerI?.hasPlayList() == true
        if (controllerI?.hasNext() == true) {
            controllerI?.playNext()
        } else if (isShowRecommendShort()) {
            setRecommendShort()
        } else {
            setViewShowState(mThumbImageViewLayout, View.GONE)
            replayContent.isVisible = true
        }
        showAllActionContent()
    }

    override fun changeUiToCompleteClear() {
        super.changeUiToCompleteClear()
        setViewShowState(mBottomContainer, VISIBLE)
    }

    override fun changeUiToNormal() {
        super.changeUiToNormal()
        dismissProgressDialog()
        dismissBrightnessDialog()
        dismissVolumeDialog()
        mThumbImageViewLayout.removeAllViews()
        if (mThumbImageView != null) {
            if (mThumbImageView.parent != null && mThumbImageView.parent is ViewGroup) {
                (mThumbImageView.parent as ViewGroup).removeView(thumbImageView)
            }
            mThumbImageViewLayout.addView(mThumbImageView)
        }
        if (isVerticalVideo && isIfCurrentIsFullscreen) {
            danmakuViewModel.setVerticalMargin(SizeUtils.dp2px(90f))
        }
        if (isIfCurrentIsFullscreen) {
            setViewShowState(mStartButton, View.GONE)
        }
        hideAllTip()
        centerStart.isVisible = false
        replayContent.isVisible = false
        errorContent.isVisible = false
        advertisement.isVisible = false
        bottomContent.isVisible = false
        videoSpeed.isVisible = true
        advertisement.imageAdvert.tag = null
    }

    override fun changeUiToPlayingClear() {
        hideAllActionContent()
    }

    public override fun changeUiToError() {
        if (controllerI?.isEnableError() == false) {
            Debuger.printfLog("changeUiToError but current is live")
        } else {
            super.changeUiToError()
            errorContent.isVisible = true
            screenUpload.isVisible = false
        }
    }

    override fun onError(what: Int, extra: Int) {
        if (what != 38 && what != -38 && controllerI?.isEnableErrorShow() == true) {
//            val source = controllerI?.changeLiveSource()
//            if (controllerI?.isLive() == true && !source.isNullOrEmpty()) {
//                if (source != mOriginUrl) {
//                    setUp(source, false, mTitle)
//                    startPlayLogic()
//                    setBottomTip(StringUtils.getString(R.string.change_source), true)
//                } else {
//                    onVideoResume(false)
//                }
//            } else {
            errorPosition = currentPositionWhenPlaying
            setStateAndUi(CURRENT_STATE_ERROR)
            deleteCacheFileWhenError()
            if (mVideoAllCallBack != null) {
                mVideoAllCallBack.onPlayError(mOriginUrl, mTitle, this)
            }
//            }
        }
    }

    override fun hideAllWidget() {
        if (((controllerI != null && controllerI!!.isStayShow()) || currentState in arrayOf(
                GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START,
                GSYVideoView.CURRENT_STATE_PAUSE,
                GSYVideoView.CURRENT_STATE_AUTO_COMPLETE,
                GSYVideoView.CURRENT_STATE_PREPAREING,
                GSYVideoView.CURRENT_STATE_ERROR
            )) && !isVerticalVideo
        ) {
            cancelDismiss()
            return
        }
        hideAllActionContent()
    }

    private fun isLocked(): Boolean {
        return mIfCurrentIsFullscreen && mLockCurScreen && mNeedLockFull
    }

    fun startDismiss() {
        restartTimerTask()
    }

    fun cancelDismiss() {
        cancelDismissControlViewTimer()
    }

    private fun setLandByState() {
        if (mIfCurrentIsFullscreen) {
            changeToLand()
        } else {
            changeToNormal()
        }
    }

    open fun changeToLand() {
        screenShare.isVisible = true
        landBottom.isVisible = true
        setViewShowState(mStartButton, View.GONE)
        exitFullscreen.isVisible = controllerI?.isOfflineMode() == false
        setAllowAction(currentPlayer, controllerI)
        screenUpload.isVisible = controllerI?.isLive() == true
    }

    private fun verticalShow(player: LiteVideoPlayer) {
        setViewShowState(player.screenCut, VISIBLE)
        setViewShowState(player.startLand, VISIBLE)
        setViewShowState(mCurrentTimeTextView, VISIBLE)
        setViewShowState(mTotalTimeTextView, VISIBLE)
        val padding = if (isVerticalVideo) {
            player.rightContent.updateLayoutParams<RelativeLayout.LayoutParams> {
                rightMargin = SizeUtils.dp2px(12f)
            }
            setViewShowState(player.rightContent, GONE)
            setViewShowState(mLockScreen, GONE)
            setViewShowState(mFullscreenButton, GONE)
            player.mProgressBar.setPadding(0, 0, 0, 0)
            if (SPUtils.getInstance().getBoolean(Constant.IS_NOTCH_IN_SCREEN)) {
                val params = player.topContent.layoutParams as RelativeLayout.LayoutParams
                params.topMargin = SizeUtils.dp2px(40f)
                player.topContent.layoutParams = params
            }
            flow_bullet_container.isVisible = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
            SizeUtils.dp2px(8f)
        } else {
            player.rightContent.updateLayoutParams<RelativeLayout.LayoutParams> {
                rightMargin = SizeUtils.dp2px(30f)
            }
            setViewShowState(player.rightContent, VISIBLE)
            setViewShowState(mLockScreen, VISIBLE)
            setViewShowState(mFullscreenButton, VISIBLE)
            player.mProgressBar.setPadding(
                SizeUtils.dp2px(15f),
                SizeUtils.dp2px(9f),
                SizeUtils.dp2px(15f),
                SizeUtils.dp2px(9f)
            )
            setAllowAction(player, controllerI)

            SizeUtils.dp2px(15f)
        }
        player.videoLanguage.setPadding(padding, 0, padding, 0)
        player.videoSpeed.setPadding(padding, 0, padding, 0)
        player.videoSource.setPadding(padding, 0, padding, 0)
        player.videoEpisode.setPadding(padding, 0, padding, 0)
    }

    private fun changeToNormal() {
        screenShare.isVisible = false
        screenOption.isVisible = false
        landBottom.isVisible = false
        if (controllerI != null) {
            screenUpload.isVisible = currentState != GSYVideoView.CURRENT_STATE_AUTO_COMPLETE
            if (controllerI!!.isLive()) {
                setViewShowState(mProgressBar, View.INVISIBLE)
                setViewShowState(mCurrentTimeTextView, View.GONE)
                setViewShowState(mTotalTimeTextView, View.GONE)
//                setViewShowState(screenUpload, View.GONE)
                setViewShowState(centerStart, View.GONE)
                setViewShowState(mStartButton, View.GONE)
            } else {
                setViewShowState(mProgressBar, View.VISIBLE)
                setViewShowState(mCurrentTimeTextView, View.VISIBLE)
                setViewShowState(mTotalTimeTextView, View.VISIBLE)
            }
        }
    }

    override fun onClickUiToggle(e: MotionEvent) {
        if (controllerI != null && controllerI!!.isStayShow()) {
            return
        }
        super.onClickUiToggle(e)
    }

    override fun lockTouchLogic() {
        super.lockTouchLogic()
        if (mLockCurScreen) {
            screenCut.isVisible = false
            mLockScreen.setImageResource(R.mipmap.icon_lock)
        } else {
            screenCut.isVisible = true
            mLockScreen.setImageResource(R.mipmap.icon_unlock)
            if (isPlaying) {
                changeUiToPlayingShow()
            }
        }
    }

    override fun updateStartImage() {
        val imageView = mStartButton as ImageView
        when (mCurrentState) {
            GSYVideoView.CURRENT_STATE_PLAYING -> {
                imageView.setImageResource(R.mipmap.icon_play_pause)
                startLand.setImageResource(R.mipmap.icon_play_pause)
                if (isIfCurrentIsFullscreen) {
                    centerStart.setImageResource(R.mipmap.icon_pause_transparent_large)
                } else {
                    centerStart.setImageResource(R.mipmap.icon_pause_transparent)
                }
            }

            else -> {
                imageView.setImageResource(R.mipmap.icon_play)
                startLand.setImageResource(R.mipmap.icon_play)
                if (isIfCurrentIsFullscreen) {
                    centerStart.setImageResource(R.mipmap.icon_play_transparent_large)
                } else {
                    centerStart.setImageResource(R.mipmap.icon_play_transparent)
                }
            }
        }
    }

    override fun startAfterPrepared() {
        if (seekOnStart > 0) {
            danmakuViewModel.resolveDanmakuSeek(seekOnStart)
        }
        super.startAfterPrepared()
    }

    override fun onSeekComplete() {
        super.onSeekComplete()
        if (mHadPlay && danmakuViewModel.isStarted()) {
            val time = mProgressBar.progress * duration / 100L
            danmakuViewModel.resolveDanmakuSeek(time)
        }
    }

    override fun onClick(v: View) {
        if (isLocked() && v.id != R.id.lock_screen) {
            return
        }
        super.onClick(v)
        when (v.id) {
            R.id.exitFullscreen -> controllerI?.exitFullScreen()
            R.id.startLand -> clickStartIcon()
            R.id.bulletOpen -> dealBulletState()
            R.id.bulletEdit, R.id.bullet_flow_edit -> controllerI?.onDanamaInputClick()
            R.id.screenCut -> shotCut()
            R.id.failedRefresh -> {
                if(controllerI?.isLive() == true) {
                    controllerI?.retryLive()
                } else {
                    seekOnStart = errorPosition
                    danmakuViewModel.resolveDanmakuSeek(seekOnStart)
                    startPlay()
                }
            }

            R.id.screenShare -> {
                hideAllActionContent()
                controllerI?.onShareClick()
            }

            R.id.playNext -> controllerI?.playNext()
            R.id.videoLanguage -> controllerI?.onLanguageClick()
            R.id.videoSpeed -> controllerI?.onSpeedClick()
            R.id.videoSource -> controllerI?.onClarityClick()
            R.id.videoEpisode -> controllerI?.onEpisodeClick()
            R.id.screenOption -> controllerI?.onSettingClick()
            R.id.bulletSetting, R.id.bullet_flow_setting -> controllerI?.onDanamaSettingClick()
            R.id.screenUpload -> controllerI?.onScreenShare()
            R.id.closeAd -> {
                advertisement.isVisible = false
                advertisement.imageAdvert.tag = null
            }

            R.id.centerStart -> {
                if (mUrl.isNullOrEmpty()) {
                    controllerI?.onPlayClick()
                } else {
                    clickStartIcon()
                }
            }

            R.id.replay -> {
                replayContent.isVisible = false
                danmakuViewModel.resolveDanmakuSeek(0)
                startPlay()
            }

            R.id.imageAdvert -> {
                if (isIfCurrentIsFullscreen && !isVerticalVideo) {
                    clearFullscreenLayout()
                }
                if (imageAdvert.tag is AdvertBean) {
                    controllerI?.onDetailClick(imageAdvert.tag as AdvertBean)
                }
            }
        }
    }

    fun startPlay() {
        if (mUrl.isNullOrEmpty()) {
            controllerI?.onPlayClick()
        } else {
            startPlayLogic()
        }
    }

    @Synchronized
    private fun shotCut() {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(GlobalValue.IMAGE_CACHE_PATH + "/" + System.currentTimeMillis())
            if (file.exists()) {
                file.delete()
            }
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
            saveFrame(file) { success, f ->
                if (success) {
                    try {
                        val danmakuImage = ImageUtils.view2Bitmap(danmakuView)
                        val result = combineBitmap(f, danmakuImage)
                        ImageUtils.save2Album(result, Bitmap.CompressFormat.JPEG)
                        f.delete()
                        val msg = Message()
                        msg.target = mHandler
                        msg.obj = result
                        msg.sendToTarget()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    mHandler.sendEmptyMessage(0)
                }
            }
        }
    }

    fun dealWithResult(msg: Message) {
        mHandler.removeCallbacksAndMessages(null)
        if (msg.obj != null && msg.obj is Bitmap) {
            if (isVerticalVideo) {
                vertical_shot_tip.isVisible = true
                shot_image.setImageBitmap(msg.obj as Bitmap)
            } else {
                screenImage.setImageBitmap(msg.obj as Bitmap)
                screenImage.isVisible = true
                setBottomTip(StringUtils.getString(R.string.shot_cut_success))
            }
            playShotCutRingtone()
        } else {
            setBottomTip(StringUtils.getString(R.string.shot_cut_failed))
        }
        mHandler.postDelayed({
            screenImage.setImageBitmap(null)
            screenImage.isVisible = false
            videoTopTip.isVisible = false
            vertical_shot_tip.isVisible = false
        }, dismissControlTime.toLong())
    }

    class MyHandler(player: LiteVideoPlayer) : Handler() {
        private val mPlayer: WeakReference<LiteVideoPlayer> = WeakReference(player)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (mPlayer.get() != null) {
                mPlayer.get()!!.dealWithResult(msg)
            }
        }
    }

    /**
     * 合并视频和弹幕
     */
    private fun combineBitmap(filmImage: File, danmakuImage: Bitmap): Bitmap {
        val bitmap = BitmapFactory.decodeFile(filmImage.absolutePath)
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawBitmap(danmakuImage, 0f, 0f, null)
        canvas.save()
        canvas.restore()
        return result
    }

    private fun playShotCutRingtone() {
        val soundPool = SoundPool.Builder().build()
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {
                soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
            }
        }
        soundPool.load(context, R.raw.screen_shot, 0)
    }

    override fun onVideoSizeChanged() {
        super.onVideoSizeChanged()
        setBulletShow()
    }

    /**
     * 根据播放状态、是否全屏、弹幕开关、是否离线、视频类型控制弹幕相关显示隐藏以及图标
     */
    private fun setBulletShow() {
        val state = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        if (!mIfCurrentIsFullscreen || !isInPlayingState || controllerI?.isBarrageEnable() == false) { // 非全屏\离线播放\非播放中状态\后端配置弹幕不可用 弹幕相关全部隐藏
            flow_bullet_container.isVisible = false
            bulletOpen.isVisible = false
            bulletSetting.isVisible = false
            bulletEdit.isVisible = false
        } else if (isVerticalVideo) {
            flow_bullet_container.isVisible = state
            bulletOpen.isVisible = true
            bulletSetting.isVisible = false
            bulletEdit.isVisible = false
        } else {
            flow_bullet_container.isVisible = false
            bulletOpen.isVisible = true
            if (state) {
                bulletSetting.isVisible = true
                bulletEdit.isVisible = true
            } else {
                bulletSetting.isVisible = false
                bulletEdit.isVisible = false
            }
        }
        if (state) {
            bulletOpen.setImageResource(R.mipmap.icon_play_barrage)
        } else {
            bulletOpen.setImageResource(R.mipmap.icon_bullet_chat_disable)
        }
    }

    // 横屏底部弹幕开关处理
    private fun dealBulletState() {
        val state = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        SPUtils.getInstance().put(Constant.KEY_SWITCH_DANAMA, !state)
        setBulletShow()
        danmakuViewModel.resolveDanmakuShow()
    }

    override fun cloneParams(from: GSYBaseVideoPlayer, to: GSYBaseVideoPlayer) {
        (to as LiteVideoPlayer).danmakuViewModel.resolveDanmakuSeek((from as LiteVideoPlayer).currentPositionWhenPlaying.toLong())
        danmakuViewModel.clone(from.danmakuViewModel.getConfig())
        to.controllerI = from.controllerI
        if (to.thumbImageView == null && from.thumbImageView != null) {
            if (from.thumbImageView.parent != null) {
                (from.thumbImageView.parent as ViewGroup).removeView(from.thumbImageView)
            }
            to.thumbImageView = from.thumbImageView
        }
        super.cloneParams(from, to)
    }

    /**
     * 处理播放器在全屏切换时，弹幕显示的逻辑
     * 需要格外注意的是，因为全屏和小屏，是切换了播放器，所以需要同步之间的弹幕状态
     */
    override fun startWindowFullscreen(context: Context, actionBar: Boolean, statusBar: Boolean): GSYBaseVideoPlayer? {
        val gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar)
        if (gsyBaseVideoPlayer is LiteVideoPlayer) {
            //对弹幕设置偏移记录
            setViewShowState(gsyBaseVideoPlayer.mFullscreenButton, View.GONE)

            gsyBaseVideoPlayer.danmakuViewModel.setDanmaKuData(danmakuViewModel.danmakusList)

            gsyBaseVideoPlayer.mLockScreen.isVisible = true
            gsyBaseVideoPlayer.mBackButton.isVisible = true
            gsyBaseVideoPlayer.titleTextView.isVisible = true
            gsyBaseVideoPlayer.batteryView.isVisible = true
            gsyBaseVideoPlayer.videoTopTip.visibility = videoTopTip.visibility
            gsyBaseVideoPlayer.videoBottomTip.visibility = videoBottomTip.visibility

            if (!isVerticalVideo) {
                val padding = SizeUtils.dp2px(40f)
                gsyBaseVideoPlayer.bottomContent.setPadding(padding, 0, padding, 0)
                gsyBaseVideoPlayer.topContent.setPadding(padding, 0, padding, 0)
            }

            gsyBaseVideoPlayer.screenOption.setImageDrawable(screenOption.drawable)
            setAdvertisementOnScreenChange(gsyBaseVideoPlayer, this)

            gsyBaseVideoPlayer.setDialogProgressColor(
                ColorUtils.getColor(R.color.white),
                ColorUtils.getColor(R.color.white)
            )
            if (speed != 1f) {
                setSpeedText("$speed X")
            }
            setTips(this, gsyBaseVideoPlayer)
            verticalShow(gsyBaseVideoPlayer)
            setBulletShow()
        }
        return gsyBaseVideoPlayer
    }

    private fun setTips(lastPlayer: LiteVideoPlayer, currentPlayer: LiteVideoPlayer) {
        if (currentPlayer.videoTopTip.visibility == View.VISIBLE) {
            if (isVerticalVideo && isIfCurrentIsFullscreen) {
                currentPlayer.vertical_tip.text = lastPlayer.videoTopTip.text
                currentPlayer.vertical_tip.setTextColor(lastPlayer.videoTopTip.textColors)
                currentPlayer.vertical_tip.isVisible = true
            } else {
                currentPlayer.videoTopTip.text = lastPlayer.videoTopTip.text
                currentPlayer.videoTopTip.setTextColor(lastPlayer.videoTopTip.textColors)
                setTopTipLayout()
            }
        }
        if (currentPlayer.videoBottomTip.visibility == View.VISIBLE) {
            setBottomTip(lastPlayer.videoBottomTip.text.toString())
        }
    }

    fun setAllowAction(player: LiteVideoPlayer, listener: IVideoController?) {
        if (listener != null && player.isInPlayingState && player.isIfCurrentIsFullscreen) {
            if (listener.isAllowLanguage()) {
                player.videoLanguage.isVisible = true
                player.videoLanguage.text = listener.getLanguage()
            } else {
                player.videoLanguage.isVisible = false
            }
            if (listener.isAllowQuality()) {
                player.videoSource.isVisible = true
                player.videoSource.text = listener.getQuality()
            } else {
                player.videoSource.isVisible = false
            }
            if (listener.hasPlayList()) {
                player.videoEpisode.isVisible = true
                if (listener.isTV()) {
                    player.videoEpisode.setText(R.string.select_tv_channel)
                } else {
                    player.videoEpisode.setText(R.string.video_show)
                }
            } else {
                player.videoEpisode.isVisible = false
            }
            if (listener.hasNext()) {
                player.playNext.isEnabled = true
                player.playNext.isVisible = true
            } else {
                player.playNext.isEnabled = false
                player.playNext.isVisible = false
            }
            if (listener.isLive() || listener.isOfflineMode()) {
                setViewShowState(player.screenOption, View.GONE)
            } else {
                setViewShowState(player.screenOption, View.VISIBLE)
            }
            if (listener.isLive()) {
                setViewShowState(player.mProgressBar, View.GONE)
                setViewShowState(player.mCurrentTimeTextView, View.GONE)
                setViewShowState(player.mTotalTimeTextView, View.GONE)
                setViewShowState(player.screenShare, View.GONE)
                setViewShowState(player.videoLanguage, View.GONE)
                setViewShowState(player.videoSpeed, View.INVISIBLE)
                setViewShowState(player.startLand, View.GONE)
                setViewShowState(player.playNext, View.GONE)
            }
        }
    }

    /**
     * 处理播放器在退出全屏时，弹幕显示的逻辑
     * 需要格外注意的是，因为全屏和小屏，是切换了播放器，所以需要同步之间的弹幕状态
     */
    public override fun resolveNormalVideoShow(oldF: View?, vp: ViewGroup?, gsyVideoPlayer: GSYVideoPlayer?) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer)
        if (gsyVideoPlayer == null) {
            return
        }
        val gsyDanmaVideoPlayer = gsyVideoPlayer as LiteVideoPlayer
        danmakuViewModel.setDanmaKuData(gsyDanmaVideoPlayer.danmakuViewModel.danmakusList)
        setAdvertisementOnScreenChange(this, gsyDanmaVideoPlayer)
        gsyDanmaVideoPlayer.titleTextView.visibility = View.INVISIBLE

        changeToNormal()
        setViewShowState(mFullscreenButton, VISIBLE)
        showAllActionContent()
        showTopContent(true)

        videoTopTip.visibility = gsyDanmaVideoPlayer.videoTopTip.visibility
        videoBottomTip.visibility = gsyDanmaVideoPlayer.videoBottomTip.visibility
        setTips(gsyDanmaVideoPlayer, this)

        setDanmakuSpeed()
        setBulletShow()
    }

    private fun setAdvertisementOnScreenChange(targetPlayer: LiteVideoPlayer, lastPlayer: LiteVideoPlayer) {
        targetPlayer.advertisement.visibility = lastPlayer.advertisement.visibility
        if (targetPlayer.advertisement.visibility == View.VISIBLE && lastPlayer.imageAdvert.tag != null && lastPlayer.imageAdvert.tag is AdvertBean) {
            mHandler.postDelayed({
                val advertBean = lastPlayer.imageAdvert.tag as AdvertBean
                targetPlayer.setPauseAdvert(advertBean, targetPlayer)
            }, 50)
        }
    }

    open fun saveState(): LiteVideoPlayer? {
        val switchVideo = LiteVideoPlayer(context)
        cloneParams(this, switchVideo)
        return switchVideo
    }

    open fun cloneState(switchVideo: LiteVideoPlayer?) {
        if (switchVideo != null) {
            cloneParams(switchVideo, this)
        }
    }

    fun showLoading(show: Boolean) {
        if (show) {
            setViewShowState(mLoadingProgressBar, VISIBLE)
        } else {
            setViewShowState(mLoadingProgressBar, GONE)
        }
    }

    open fun setSurfaceToPlay() {
        mHandler.postDelayed({
            addTextureView()
            gsyVideoManager.setListener(this)
            checkoutState()
            gsyVideoManager.start()
            setStateAndUi(CURRENT_STATE_PLAYING)
        }, 500)
    }

    open fun setSwitchUrl(url: String) {
        mUrl = url
        mOriginUrl = url
    }

    open fun setSwitchCache(cache: Boolean) {
        mCache = cache
    }

    open fun setSwitchTitle(title: String) {
        mTitle = title
    }

    override fun onDetachedFromWindow() {
        mHandler.removeCallbacksAndMessages(null)
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
        super.onDetachedFromWindow()
    }

    override fun onDisconnected() {

    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        if (!isInPlayingState) {
            return
        }
        if (networkType != NetworkUtils.NetworkType.NETWORK_WIFI) {
            setBottomTip(StringUtils.getString(R.string.not_wifi_play_tips), true)
        }
    }

    override fun touchDoubleUp(e: MotionEvent?) {
        if (controllerI?.isLive() == true) {
            return
        }
        super.touchDoubleUp(e)
    }

    override fun touchSurfaceMove(deltaX: Float, deltaY: Float, y: Float) {
        if (controllerI?.isLive() == true) mChangePosition = false
        parent.requestDisallowInterceptTouchEvent(true)
        super.touchSurfaceMove(deltaX, deltaY, y)
    }

    override fun touchSurfaceMoveFullLogic(absDeltaX: Float, absDeltaY: Float) {
        if ((absDeltaX > mThreshold || absDeltaY > mThreshold)) {
            if (!isEnableTouch() && absDeltaX >= mThreshold && abs(ScreenUtils.getAppScreenWidth() - mDownX) > mSeekEndOffset) {
                //防止全屏虚拟按键
                mChangePosition = controllerI?.isLive() == false
                mDownPosition = currentPositionWhenPlaying
            } else {
                super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY)
            }
        }
    }

    /**直播期间、非播放状态不需要触摸
     * 是否允许触摸播放器操作
     * true 允许
     */
    open fun isEnableTouch(): Boolean {
        return mCurrentState == CURRENT_STATE_PLAYING || mCurrentState == CURRENT_STATE_PAUSE
    }

    fun hasPlayed(): Boolean {
        return mHadPlay
    }

    fun getIsVerticalVideo(): Boolean {
        return isVerticalVideo
    }

    override fun showProgressDialog(
        deltaX: Float,
        seekTime: String?,
        seekTimePosition: Long,
        totalTime: String?,
        totalTimeDuration: Long
    ) {
        super.showProgressDialog(deltaX, seekTime, seekTimePosition, totalTime, totalTimeDuration)
        centerStart.isVisible = false
    }

    override fun dismissProgressDialog() {
        super.dismissProgressDialog()
        if (getLocalVisibleRect(context, bottomContent)) {
            centerStart.isVisible = controllerI?.isLive() == false
        }
    }

    override fun onGankAudio() {
//        try {
//            if (!(context.applicationContext as VideoApplication).isAppBackground) {
//                onVideoResume()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }


    /**
     * 小视频播放完毕，切横屏全屏有效，其他时候调用无效
     */
    private fun setRecommendShort() {
        if (isShowRecommendShort()) {
            val shortList = controllerI?.getRecommendShort()
            mThumbImageViewLayout.removeAllViews()
            setViewShowState(mThumbImageViewLayout, View.VISIBLE)
            mThumbImageViewLayout.addView(
                RecommendShortView(
                    context,
                    this,
                    controllerI,
                    shortList,
                    controllerI?.getDetailInfo()
                ),
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun isShowRecommendShort(): Boolean {
        return controllerI?.isShortVideo() == true && isIfCurrentIsFullscreen
                && !isVerticalVideo && mCurrentState == CURRENT_STATE_AUTO_COMPLETE
                && controllerI?.isOfflineMode() == false
                && (controllerI?.getRecommendShort()?.size ?: 0) > 0
    }

    fun getCurrentUrl(): String {
        return mOriginUrl
    }

    /********************************弹幕相关操作***************************************/

    fun setDanmakuSpeed() {
        currentPlayer.danmakuViewModel.setDanmakuSpeed()
    }

    fun setDanmakuTextSize() {
        currentPlayer.danmakuViewModel.setDanmakuTextSize()
    }

    fun setDanmakuAlpha() {
        currentPlayer.danmakuViewModel.setDanmakuAlpha()
    }

    fun setForbiddenPosition() {
        currentPlayer.danmakuViewModel.setForbiddenPosition()
    }

    fun setForbiddenWord(forbiddenList: MutableList<CharSequence>?) {
        currentPlayer.danmakuViewModel.setForbiddenWord(forbiddenList)
    }

    fun setForbiddenUserDanmaku(userId: Long) {
        currentPlayer.danmakuViewModel.setForbiddenUserDanmaku(userId)
    }

    fun setForbiddenUserDanmaku(data: MutableList<Long>) {
        currentPlayer.danmakuViewModel.setForbiddenUserDanmaku(data)
    }

    fun removeForbiddenUserDanmaku(userId: Long) {
        currentPlayer.danmakuViewModel.removeForbiddenUserDanmaku(userId)
    }

    fun getDanmakuList(): MutableList<BarrageBean> {
        return currentPlayer.danmakuViewModel.danmakusList
    }

    fun setDanmaKuData(danmakuList: MutableList<BarrageBean>) {
        currentPlayer.danmakuViewModel.setDanmaKuData(danmakuList)
    }

    fun addDanmaku(barrageBean: BarrageBean) {
        currentPlayer.danmakuViewModel.addDanmaku(barrageBean)
    }

    /**
     * 更新弹幕
     */
    fun updateDanamakuItem(danmakuId: Long, data: BarrageBean) {
        val danmakuItemList = currentPlayer.danmakuViewModel.getDanmakuItemList()
        if (!danmakuItemList.isNullOrEmpty()) {
            for (item in danmakuItemList) {
                if (item.data.danmakuId == danmakuId) {
                    if (item.data is BaseDanmakuItemData) {
                        (item.data as BaseDanmakuItemData).barrageBean = data
                        currentPlayer.danmakuViewModel.getConfig().apply {
                            updateCache()
                            updateRender()
                        }
                    }
                    break
                }
            }
        }
    }

    fun toggleDanmakuShow() {
        currentPlayer.danmakuViewModel.toggleDanmakuShow()
    }

    private val holdRunnable = Runnable {
        //danmakuViewModel.hold(null)
        holdPopupWindow?.dismiss()
    }

    override fun onDanmakuClickListener(x: Float, y: Float, item: MutableList<DanmakuItem>) {
        if (item.size != 1 || isLocked()) {
            return
        }
        val danmakuItem = item[0]
        val data = danmakuItem.data
        if (data is BaseDanmakuItemData) {
            if (data is SpannedModel) {
                if (data.userImage != null && data.nickName != null) {
                    val avatarRange = SpannedRenderer.getAvatarArea(
                        danmakuViewModel.getConfig().textSizeScale,
                        danmakuItem.rect,
                        data
                    )
                    if (x < avatarRange[1] && x > avatarRange[0]) {
                        val intent = Intent(context, UpperActivity::class.java)
                        intent.putExtra(UpperActivity.UPPER_ID, data.userId?.toInt())
                        context.startActivity(intent)
                        return
                    }
                } else if (data.barrageBean.good > 0) {
                    val likeRange = SpannedRenderer.getLikeArea(
                        danmakuViewModel.getConfig().textSizeScale,
                        danmakuItem.rect,
                        data
                    )
                    if (x < likeRange[1] && x > likeRange[0]) {
                        controllerI?.onLikeDanama(
                            data.danmakuId,
                            data.barrageBean
                        )
                        return
                    }
                }
                if (data.barrageBean.uid == GlobalValue.userInfoBean?.id) {
                    return
                }
                danmakuViewModel.hold(danmakuItem)
                showHoldDanmuPopupWindow(danmakuItem.rect, data)
                mHandler.postDelayed(holdRunnable, 3000)
            } else if (data is AdvertModel) {
                val coinRange = DanmakuAdRenderer.getCoinArea(
                    danmakuViewModel.getConfig().textSizeScale,
                    danmakuItem.rect
                )
                val detailRange = DanmakuAdRenderer.getCheckDetailArea(
                    danmakuViewModel.getConfig().textSizeScale,
                    danmakuItem.rect,
                    data
                )
                if (x <= coinRange[1] && x >= coinRange[0]) {
                    controllerI?.onReceiveDanmakuAdCoin(
                        data.danmakuId,
                        data.barrageBean
                    )
                } else if (x <= detailRange[1] && x >= detailRange[0]) {
                    if (!data.linkUrl.isNullOrEmpty()) {
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra(WebViewActivity.urlKey, data.linkUrl)
                        context.startActivity(intent)
                    }
                }
            } else {
                if (data.barrageBean.good > 0) {
                    val likeRange = SpannedRenderer.getLikeArea(
                        danmakuViewModel.getConfig().textSizeScale,
                        danmakuItem.rect,
                        data
                    )
                    if (x <= likeRange[1] && x >= likeRange[0]) {
                        controllerI?.onLikeDanama(
                            data.danmakuId,
                            data.barrageBean
                        )
                        return
                    }
                }
                if (data.barrageBean.uid == GlobalValue.userInfoBean?.id) {
                    return
                }
                danmakuViewModel.hold(danmakuItem)
                showHoldDanmuPopupWindow(danmakuItem.rect, data)
                mHandler.postDelayed(holdRunnable, 3000)
            }
        }
    }

    private fun showHoldDanmuPopupWindow(position: RectF, danmakuItem: BaseDanmakuItemData) {
        if (holdPopupWindow == null) {
            holdPopupWindow = DanmuHoldPopupWindow(context, isIfCurrentIsFullscreen)
            holdPopupWindow?.setOnDismissListener {
                danmakuViewModel.hold(null)
                mHandler.removeCallbacks(holdRunnable)
            }
            holdPopupWindow?.listener = object : DanmuHoldPopupWindow.OnDanmuActionListener {
                override fun onLikeClick(danmakuId: Long, data: BarrageBean) {
                    controllerI?.onLikeDanama(danmakuId, data)
                }

                override fun onReportClick(data: BarrageBean) {
                    controllerI?.onReportDanama(data)
                }

                override fun onPasteClick(data: BarrageBean) {
                    controllerI?.onSendDanama(data.contxt)
                }

                override fun onForbiddenSuccess(data: BarrageBean) {
                    controllerI?.onForbiddenDanama(data)
                }
            }
        }
        holdPopupWindow?.updateDanmuData(danmakuItem.danmakuId, danmakuItem.barrageBean)
        //DanmuHoldPopupWindow内部高度设置成具体值，所以此处能获取到
        val popupWindowHeight = holdPopupWindow?.height ?: 0
        val y = if (danmakuView.height - position.bottom < popupWindowHeight) {
            danmakuView.height - position.top + popupWindowHeight
        } else {
            danmakuView.height - position.bottom
        }
        holdPopupWindow?.showAsDropDown(danmakuView, position.left.toInt(), -y.toInt())
    }
}