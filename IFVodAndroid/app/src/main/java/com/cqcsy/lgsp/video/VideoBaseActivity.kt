package com.cqcsy.lgsp.video

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.app.VideoApplication
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.database.AddWatchRecordUtil
import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.event.BarrageEvent
import com.cqcsy.lgsp.event.BarrageType
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.SocketClient
import com.cqcsy.lgsp.screenshare.ScreenShareActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.bean.VideoItemBean
import com.cqcsy.lgsp.video.danmaku.AdvertModel
import com.cqcsy.lgsp.video.player.LiteVideoAdPlayer
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.lgsp.video.player.SwitchUtil
import com.cqcsy.lgsp.video.presenter.*
import com.cqcsy.lgsp.video.view.*
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.hpplay.sdk.source.api.ILelinkPlayerListener
import com.hpplay.sdk.source.api.LelinkPlayerInfo
import com.hpplay.sdk.source.api.LelinkSourceSDK
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo
import com.lzy.okgo.db.DownloadManager
import com.shuyu.gsyvideoplayer.GSYBaseADActivityDetail
import com.shuyu.gsyvideoplayer.GSYVideoADManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import kotlinx.android.synthetic.main.activity_video_base.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 详情页面播放器基类
 * 子类在manifest里面一定要设置
 * android:configChanges="keyboard|keyboardHidden|orientation|screenSize|screenLayout|smallestScreenSize|uiMode"
 */
abstract class VideoBaseActivity : GSYBaseADActivityDetail<LiteVideoPlayer, GSYADVideoPlayer>(),
    IVideoController, IVideoPageController, DialogInterface.OnDismissListener {
    companion object {
        const val PLAY_VIDEO_BEAN = "playVideoBean"
        const val PLAY_VIDEO_MEDIA_KEY = "playVideoMediaKey"    // 传mediaKey必须和videoType一起传
        const val PLAY_CHILD_MEDIA_KEY = "playChildMediaKey"
        const val VIDEO_TYPE = "videoType"
        const val OPTION_VIEW = "optionView"
        const val DOWNLOADED_EPISODE = "downloadedEpisode"
        const val COMMENT_ID = "commentId"
        const val REPLY_ID = "replyId"

        const val AD_TIP_BEFORE_TIME = 5
    }

    private var currentPlayVideoBean: VideoBaseBean? = null
    private var autoSkip: Boolean
        get() {
            return SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_SKIP, true)
        }
        set(value) {
            SPUtils.getInstance().put(Constant.KEY_AUTO_SKIP, value)
        }

    // 从我的评论回复、收到赞传入的评论id
    protected var commentId = 0

    // 从我的评论回复、收到赞传入的评论回复id
    protected var mReplyId = 0

    private var lastKey = ""

    protected val mHandler = Handler(Looper.getMainLooper())

    private var lelinkPlayerInfo: LelinkServiceInfo? = null

    private var isStopFromPlaying = false

    private var videoDialog: IVideoDialog? = null

    protected var mAdvertPresenter: AdvertPresenter? = null
    protected lateinit var danmakuAdvertPresenter: DanmakuAdvertPresenter

    private var mOfflinePresenter: OfflinePresenter? = null

    private var isOfflineMode = false

    protected val mViewModel: VideoViewModel by viewModels()

    protected var mLiveSourceChange: LiveSourceChange? = null

    open fun initObserve() {
        mViewModel.mGetAdCoin.observe(this) {
            val tip = StringUtils.getString(R.string.receive_coin_tip, it.second.nickName, it.second.coin)
            gsyVideoPlayer.currentPlayer.showReceiveCoinTip(tip)
            updateDanamakuItem(it.first, it.second)
            val danmakuItemList = gsyVideoPlayer.currentPlayer.danmakuViewModel.getDanmakuItemList()
            if (!danmakuItemList.isNullOrEmpty()) {
                for (item in danmakuItemList) {
                    val itemData = item.data
                    if (itemData is AdvertModel && itemData.advertId == it.second.advertId) {
                        itemData.barrageBean.isDrawCoin = true
                    }
                }
            }
        }
        mViewModel.mForbiddenUserList.observe(this) {
            if (it != null) {
                setForbiddenUserDanmaku(it)
            }
        }
        mViewModel.mForbiddenWordList.observe(this) {
            gsyVideoPlayer.setForbiddenWord(it)
        }
        mViewModel.mBarrageList.observe(this) {
            danmakuAdvertPresenter.isPrepared = true
            val duration = gsyVideoPlayer.currentPlayer.duration
            val adBarrageList = danmakuAdvertPresenter.getAdBarrageList(duration)
            if (!adBarrageList.isNullOrEmpty()) {
                it.addAll(adBarrageList)
                danmakuAdvertPresenter.loadCompleted = true
            }
            setDanamaku(it)
        }
//        mViewModel.mClarityList.observe(this) {
//            if (it == null) {
//                playClarityError(getString(R.string.on_video_source))
//            }
//        }
        mViewModel.mPlayClarity.observe(this) {
            if (SwitchUtil.isRelease()) {
                if (it == null) {
                    playClarityError(getString(R.string.on_video_source))
                } else {
                    playClarity(it)
                }
            }
        }
        mViewModel.mSelectedVideoBean.observe(this) {
            playWithModel(it)
        }

        mViewModel.mEpisodeList.observe(this) {
            if (isOfflineMode() && isFullPlaying()) {
                gsyVideoPlayer.currentPlayer.changeToLand()
            }
        }
    }

    /**
     * 输入法隐藏
     */
    open fun onSoftBoardHide() {
        dismissAllDialog()
        gsyVideoPlayer.currentPlayer.startDismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_base)
        val layoutId = getBottomLayoutId()
        if (layoutId > 0) {
            bottomContainer.addView(LayoutInflater.from(this).inflate(layoutId, null))
        }
        val data = intent.getSerializableExtra(PLAY_VIDEO_BEAN)
        if (data != null && data is VideoBaseBean) {
            currentPlayVideoBean = data
            isOfflineMode = !data.filePath.isNullOrEmpty()  // 根据首次进入时判断是否离线播放模式
            if (currentPlayVideoBean!!.uniqueID > 0) {
                lastKey = "${NormalUtil.formatMediaId(currentPlayVideoBean!!.mediaId)}_${currentPlayVideoBean!!.uniqueID}"
            }
        } else {
            currentPlayVideoBean = VideoBaseBean()
            currentPlayVideoBean?.mediaKey = intent.getStringExtra(PLAY_VIDEO_MEDIA_KEY) ?: ""
            currentPlayVideoBean?.videoType = intent.getIntExtra(VIDEO_TYPE, 0)
        }
        val episodeKey = intent.getStringExtra(PLAY_CHILD_MEDIA_KEY)
        if (!episodeKey.isNullOrEmpty()) {
            currentPlayVideoBean?.episodeKey = episodeKey
        }
        commentId = intent.getIntExtra(COMMENT_ID, 0)
        mReplyId = intent.getIntExtra(REPLY_ID, 0)
        mOfflinePresenter = OfflinePresenter(currentPlayVideoBean)
        mAdvertPresenter = AdvertPresenter(gsyVideoPlayer, gsyadVideoPlayer)
        danmakuAdvertPresenter = DanmakuAdvertPresenter()
        initObserve()
        initPlayer()
        checkPlay()
        getNormalData()
    }

    private fun getNormalData() {
        if (GlobalValue.userInfoBean != null) {
            mViewModel.getForbiddenUserList()
            mViewModel.getForbiddenWordList()
        }
    }

    private fun checkPlay() {
        if (isOfflineMode()) {
            playOffline()
        } else if (!SwitchUtil.isRelease()) {
            videoPlayer.setSwitchCache(true)
            videoPlayer.setSwitchTitle(currentPlayVideoBean!!.title!!)
            videoPlayer.setSwitchUrl(NormalUtil.urlEncode(currentPlayVideoBean!!.mediaUrl))
            SwitchUtil.clonePlayState(videoPlayer)
            gsyVideoPlayer.setBackFromFullScreenListener { onBackPressed() }
            gsyVideoPlayer.setVideoAllCallBack(this)
            gsyVideoPlayer.controllerI = this

            if (videoPlayer.isInPlayingState) {
                videoPlayer.setSurfaceToPlay()
            } else {
                startPlayVideo()
            }

            // 这里指定了被共享的视图元素
            ViewCompat.setTransitionName(videoPlayer, OPTION_VIEW)
        }
    }

    protected fun playOffline() {
        full_loading.showProgress()
        if (currentPlayVideoBean?.videoType == Constant.VIDEO_TELEPLAY || currentPlayVideoBean?.videoType == Constant.VIDEO_VARIETY) {
            mViewModel.getEpisodeInfo(currentPlayVideoBean?.mediaKey)
        }
        startPlayVideo()
    }

    protected fun setOfflinePlay() {
        val playUrl = getPlayUrl(currentPlayVideoBean)
        gsyVideoPlayer.currentPlayer.setUp(playUrl, false, getShowTitle())
        gsyVideoPlayer.currentPlayer.setOfflineProgress()
        gsyVideoPlayer.currentPlayer.seekOnStart = getPlayRecordTime()
        gsyVideoPlayer.currentPlayer.startPlayLogic()
//        if (!isFullPlaying()) {
//            showFull()
//        }
    }

    override fun onResume() {
        super.onResume()
        if (!(application as VideoApplication).isAppBackground) {
            if (isADStarted || (gsyadVideoPlayer.currentPlayer as LiteVideoAdPlayer).isErrorShow()) {
                GSYVideoADManager.instance().start()
            } else if (!isScreenShare() && isStopFromPlaying) {
                GSYVideoManager.instance().start()
                isStopFromPlaying = false
            }
            setSoftListener()
            // 首次进来或跳转其他页面返回，需要重启计时器
            mLiveSourceChange?.startChangeTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlay()
        KeyboardUtils.unregisterSoftInputChangedListener(window)
    }

    override fun onStop() {
        super.onStop()
        mHandler.removeCallbacksAndMessages(null)
        mLiveSourceChange?.clearLiveTimer()
        if (currentPlayVideoBean != null) {
            addPlayRecord()
        }
    }

    private fun pausePlay() {
        if (isADStarted && GSYVideoADManager.instance().isPlaying) {
            GSYVideoADManager.instance().pause()
        } else {
            if (gsyVideoPlayer.currentPlayer.isPlaying || gsyVideoPlayer.currentPlayer.isBuffering) {
                isStopFromPlaying = true
            }
            GSYVideoManager.instance().pause()
        }
        currentPlayVideoBean?.watchingProgress = getCurrentPlayingTime()
        currentPlayVideoBean?.time = getTotalTime().toString()
        mAdvertPresenter?.stopPrepare()
    }

    override fun onDestroy() {
        reset()
        stopPlay()
        mAdvertPresenter?.stopAdPlay()
        if (isScreenShare()) {
            LelinkSourceSDK.getInstance().unBindSdk()
        }
        videoPlayer.gsyVideoManager.setListener(videoPlayer.gsyVideoManager.lastListener())
        videoPlayer.gsyVideoManager.setLastListener(null)
        if (orientationUtils != null) orientationUtils.releaseListener()
        GSYVideoManager.releaseAllVideos()
        HttpRequest.cancelRequest(mViewModel)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isOfflineMode()) {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setSoftListener() {
        KeyboardUtils.registerSoftInputChangedListener(this) { height ->
            if (height == 0 && videoDialog?.isAllowHideChange() == true) {
                onSoftBoardHide()
            } else {
                onSoftBoardShow()
            }
        }
    }

    private fun setPlayerHeight() {
        val layoutParams = playerContent.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = ScreenUtils.getScreenWidth() * 9 / 16
        playerContent.layoutParams = layoutParams
    }

    private fun initPlayer() {
        setPlayerHeight()
        gsyVideoPlayer.backButton.setOnClickListener { finish() }
        gsyVideoPlayer.setBackFromFullScreenListener { onBackPressed() }

        initVideoBuilderMode()

        gsyVideoPlayer.setLockClickListener { view, lock ->
            if (orientationUtils != null) { //配合下方的onConfigurationChanged
                orientationUtils.isEnable = !lock
            }
        }
        gsyVideoPlayer.setVideoController(this)

        gsyVideoPlayer.setDialogProgressColor(ColorUtils.getColor(R.color.white), ColorUtils.getColor(R.color.white))
        setPlayerListener()

        adPlayer.backButton.setOnClickListener { finish() }
        adPlayer.setVideoController(this)
        setAdPlayerListener()
    }

    private fun setAdPlayerListener() {
        gsyadVideoPlayer.setVideoAllCallBack(object : GSYSampleCallBack() {
            override fun onStartPrepared(url: String?, vararg objects: Any?) {
                super.onStartPrepared(url, *objects)
                //开始播放了才能旋转和全屏
                mADOrientationUtils.isEnable = detailOrientationRotateAuto
            }

            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                skipAdvert()
            }

            override fun onQuitFullscreen(url: String?, vararg objects: Any?) { //退出全屏逻辑
                if (mADOrientationUtils != null) {
                    mADOrientationUtils.backToProtVideo()
                }
                exitFullScreen()
            }
        })
    }

    private fun setPlayerListener() {
        if (!isOfflineMode()) {
            gsyVideoPlayer.setGSYVideoProgressListener(object : GSYVideoProgressListener {
                private var preSecond = 0L
                private val showTipTime = 5

                override fun onProgress(progress: Int, secProgress: Long, currentPosition: Long, duration: Long) {
                    val currentSecond = currentPosition / 1000
                    if (preSecond == currentSecond) {
                        return
                    }
                    mLiveSourceChange?.mRetryTime = 0
                    preSecond = currentSecond
                    if (isLive()) {
                        mLiveSourceChange?.startChangeTimer()
                        return
                    }
                    if (currentPlayVideoBean!!.videoType == Constant.VIDEO_SHORT) {
                        return
                    }

                    if (currentPlayVideoBean!!.watchingProgress == 0L && currentPlayVideoBean!!.opSecond > 0 && currentSecond == currentPlayVideoBean!!.opSecond && autoSkip) {
                        gsyVideoPlayer.setBottomTip(getString(R.string.skip_video_head))
                    }
                    if (isScreenShare() && gsyVideoPlayer.currentPlayer.isInPlayingState) {
                        gsyVideoPlayer.currentPlayer.onVideoPause()
                    } else if (currentPlayVideoBean!!.epSecond > 0 && hasNext() && currentPlayVideoBean!!.epSecond >= currentSecond && currentPlayVideoBean!!.epSecond - currentSecond <= showTipTime && autoSkip) {
                        gsyVideoPlayer.setBottomTip(getString(R.string.skip_video_tail), false)
                    } else if (currentPlayVideoBean!!.epSecond > 0 && hasNext() && currentSecond >= currentPlayVideoBean!!.epSecond && autoSkip) {
                        playNext()
                    } else {
                        mAdvertPresenter?.advertCheck(currentSecond, duration, currentPlayVideoBean!!.isFilterAds)
                    }
                }
            })
        }
    }

    private fun stopPlay() {
        gsyVideoPlayer.currentPlayer.release()
        gsyVideoPlayer.currentPlayer.onVideoReset()
    }

    fun stopPlayAll() {
        mAdvertPresenter?.stopAdPlay()
        stopPlay()
    }

    protected fun startPlayVideo() {
        gsyVideoPlayer.currentPlayer.visibility = View.VISIBLE
        gsyVideoPlayer.currentPlayer.seekOnStart = getPlayRecordTime()
        // 每次都看本地是否下载过，如果下载过，就直接播放本地，暂时去掉
//        currentPlayVideoBean!!.filePath = checkDownloadLocalFile(currentPlayVideoBean!!.episodeKey)
        if (isOfflineMode() && !currentPlayVideoBean?.filePath.isNullOrEmpty()) {
            setOfflinePlay()
        } else {
            if (isLive()) {
                SPUtils.getInstance().put(Constant.KEY_LIVE_LINE, currentPlayVideoBean?.resolutionDes)
            }
            gsyVideoPlayer.currentPlayer.setNormalProgress()
            val playUrl = getPlayUrl(currentPlayVideoBean)
            gsyVideoPlayer.currentPlayer.setUp(playUrl, !isLive(), getShowTitle())

            if (!isPause) {
                gsyVideoPlayer.currentPlayer.startPlayLogic()
                mLiveSourceChange?.startChangeTimer()
            }
            SocketClient.joinVideoRoom(lastKey)
        }
    }

    fun isFullPlaying(): Boolean {
        return (gsyadVideoPlayer.currentPlayer.visibility == View.VISIBLE && gsyadVideoPlayer.currentPlayer.isIfCurrentIsFullscreen)
                || (gsyVideoPlayer.currentPlayer.visibility == View.VISIBLE && gsyVideoPlayer.currentPlayer.isIfCurrentIsFullscreen)
    }

    override fun exitFullScreen() {
        if (isFullPlaying()) {
            onBackPressed()
        }
    }

    private fun getVideoOtherInfo(getbarrage: Boolean) {
        if (currentPlayVideoBean == null) {
            return
        }
        lastKey = "${NormalUtil.formatMediaId(currentPlayVideoBean!!.mediaId)}_${currentPlayVideoBean!!.uniqueID}"
        if (getbarrage) {
            mViewModel.getBarrageList(currentPlayVideoBean?.mediaKey, currentPlayVideoBean?.uniqueID, currentPlayVideoBean?.videoType)
        }
    }

    /**
     * 判断是否剧集
     */
    private fun isEpisodeType(): Boolean {
        return currentPlayVideoBean!!.videoType == Constant.VIDEO_TELEPLAY || currentPlayVideoBean!!.videoType == Constant.VIDEO_VARIETY || isLive()
    }

    /**
     * 传入对象播放
     */
    fun playWithModel(bean: VideoBaseBean, progress: Long = 0) {
        if (!SwitchUtil.isRelease() && bean.mediaKey == getMediaKey()) {  // 从发现列表进入
            onPlayInfoResponseSuccess(bean.episodeId)
            return
        }
        if (bean.uniqueID == currentPlayVideoBean?.uniqueID && isPlaying()) {   // 当前
            return
        }
        var resetList = false
        if (bean.mediaKey != (currentPlayVideoBean?.mediaKey ?: 0)) {
            pausePlay()
            addPlayRecord()
            resetList = true
        }
        reset()
        if (!currentPlayVideoBean?.mediaUrl.isNullOrEmpty()) {
            stopPlay()
        }
        mAdvertPresenter?.stopAdPlay()
        if (currentPlayVideoBean == null || resetList) {
            currentPlayVideoBean = bean
        } else {
            currentPlayVideoBean?.resetByOtherModel(bean)
        }
        if (isEpisodeType()) {
            if (resetList) {
                mViewModel.mEpisodeList.value?.second?.clear()
            }
        } else {
            mViewModel.mEpisodeList.value?.second?.clear()
        }
        currentPlayVideoBean?.watchingProgress = progress
        mViewModel.getPlayInfo(getMediaKey(), getUniqueId(), getVideoType(), resolution = currentPlayVideoBean?.resolution)
    }

    /**
     * 播放指定URL视频
     */
    private fun play(mediaUrl: String?, getbarrage: Boolean = true) {
        if (mediaUrl.isNullOrEmpty()) {
            return
        }
        getVideoOtherInfo(getbarrage)
        if (!SwitchUtil.isRelease() && videoPlayer.isInPlayingState) {
            return
        }
        currentPlayVideoBean!!.mediaUrl = mediaUrl
        mOfflinePresenter?.videoBaseBean = currentPlayVideoBean
        if (isLive()) {
            mLiveSourceChange?.reset()
            mLiveSourceChange = LiveSourceChange(gsyVideoPlayer, currentPlayVideoBean, mViewModel, mViewModel.mClarityList.value) { startPlayVideo() }
        } else {
            mLiveSourceChange = null
        }
        when {
            isScreenShare() && lelinkPlayerInfo != null -> {
                setShareController(lelinkPlayerInfo!!)
            }

            else -> {
                startPlayVideo()
            }
        }
    }

    /**
     * 获取当前视频对象，只用于下载缓存，不允许修改
     */
    fun getVideoBaseBean(): VideoBaseBean? {
        return currentPlayVideoBean
    }

    fun setViewBaseBean(videoBaseBean: VideoBaseBean) {
        currentPlayVideoBean = videoBaseBean
        mOfflinePresenter?.videoBaseBean = videoBaseBean
        isOfflineMode = !videoBaseBean.filePath.isNullOrEmpty()
    }

    /**
     * 获取当前视频类型
     */
    fun getVideoType(): Int {
        return currentPlayVideoBean?.videoType ?: Constant.VIDEO_MOVIE
    }

    /**
     * 获取当前视频加密ID
     */
    fun getEpisodeKey(): String? {
        return currentPlayVideoBean?.episodeKey
    }

    /**
     * 获取当前视频title
     */
    fun getVideoTitle(): String? {
        return currentPlayVideoBean?.title
    }

    fun getUniqueId(): Int {
        return currentPlayVideoBean?.uniqueID ?: 0
    }


    fun getMediaKey(): String {
        return currentPlayVideoBean?.mediaKey ?: ""
    }

    private fun reset() {
        if (lastKey.isNotEmpty() && !isOfflineMode()) {
            SocketClient.leaveVideoRoom(lastKey)
            lastKey = ""
        }
        SwitchUtil.release()
        currentPlayVideoBean?.watchingProgress = 0
        currentPlayVideoBean?.filePath = ""
        mViewModel.mClarityList.value?.clear()
        mOfflinePresenter?.reset()
        mAdvertPresenter?.reset()
        danmakuAdvertPresenter.reset()
        mLiveSourceChange?.reset()
    }

    private fun getPlayUrl(bean: VideoBaseBean?): String? {
        if (bean == null || (bean.mediaUrl.isNullOrEmpty() && bean.filePath.isNullOrEmpty())) {
            return null
        }
        val offlineUrl = mOfflinePresenter?.getOfflineUri()
        if (!offlineUrl.isNullOrEmpty()) {
            return offlineUrl
        }
        return if (!bean.filePath.isNullOrEmpty()) {
            bean.filePath
        } else {
            NormalUtil.urlEncode(bean.mediaUrl)
        }
    }

    private fun dismissAllDialog() {
        if (gsyVideoPlayer.getIsVerticalVideo()) {
            gsyVideoPlayer.currentPlayer.hideAllActionContent()
        } else {
            gsyVideoPlayer.currentPlayer.cancelDismiss()
        }
        videoDialog?.dismissDialog()
    }

    final override fun isStayShow(): Boolean {
        return videoDialog?.isShowingDialog() == true
    }

    override fun isFromList(): Boolean {
        return !SwitchUtil.isRelease()
    }

    override fun isEnableErrorShow(): Boolean {
        return isLive() || (!isLive() && (mLiveSourceChange?.sourceSize() ?: 0) <= 1)
    }

    override fun isBarrageEnable(): Boolean {
        return currentPlayVideoBean?.barrageStatus == 1 || currentPlayVideoBean?.barrageStatus == 2
    }

    fun isEnableChat(): Boolean {
        return currentPlayVideoBean?.barrageStatus == 2 || currentPlayVideoBean?.barrageStatus == 3
    }

    override fun isLive(): Boolean {
        return currentPlayVideoBean?.isLive == true
    }

    override fun isTV(): Boolean {
        return currentPlayVideoBean?.videoType == Constant.VIDEO_TV
    }

    override fun retryLive() {
        mLiveSourceChange?.mRetryTime = 0
        mViewModel.getPlayInfo(getMediaKey(), getUniqueId())
    }

    override fun isEnableError(): Boolean {
        return !isLive() || mLiveSourceChange?.isEnableError() == true || mViewModel.mClarityList.value.isNullOrEmpty()
    }

    /**
     * 默认显示tab
     */
    fun getShowType(): Int {
        return currentPlayVideoBean?.detailShowType ?: 0
    }

    override fun isShortVideo(): Boolean {
        return currentPlayVideoBean?.videoType == Constant.VIDEO_SHORT
    }

    override fun getRecommendShort(): MutableList<ShortVideoBean>? {
        return mViewModel.mRecommendShort.value
    }

    override fun playShortVideo(video: ShortVideoBean) {
        changeLanguage(video.mediaKey, video.videoType)
    }

    /**
     * 开始倒计时隐藏
     */
    private fun startDismissTimer() {
        gsyVideoPlayer.currentPlayer.startDismiss()
        videoDialog?.destroy()
    }

    final override fun getDetailOrientationRotateAuto(): Boolean {
        if (currentPlayVideoBean == null || isOfflineMode()) {
            return false
        }
        return true
    }

    final override fun getGSYVideoPlayer(): LiteVideoPlayer {
        return videoPlayer
    }

    final override fun getGSYVideoOptionBuilder(): GSYVideoOptionBuilder? {
        return getCommonBuilder()
            .setVideoTitle(getShowTitle())
            .setSeekOnStart(getPlayRecordTime())
            .setNeedShowWifiTip(true)
    }

    final override fun getGSYADVideoPlayer(): LiteVideoAdPlayer {
        return adPlayer
    }

    final override fun getGSYADVideoOptionBuilder(): GSYVideoOptionBuilder? {
        return getCommonBuilder().setCacheWithPlay(false)
    }

    /**
     * 公用的视频配置
     */
    private fun getCommonBuilder(): GSYVideoOptionBuilder {
        return GSYVideoOptionBuilder()
            .setCacheWithPlay(!isOfflineMode() && !isLive())
            .setFullHideActionBar(true)
            .setFullHideStatusBar(true)
            .setIsTouchWiget(true)
            .setThumbImageView(View.inflate(this, R.layout.layout_prepare_loading, null))
            .setDismissControlTime(5000)
            .setLockLand(false)
            .setOnlyRotateLand(false)
            .setRotateViewAuto(false)
            .setRotateWithSystem(false)
            .setShowDragProgressTextOnSeekBar(true)
            .setShowFullAnimation(false) //打开动画
            .setNeedLockFull(true)
            .setAutoFullWithSize(true)
            .setReleaseWhenLossAudio(false)
            .setStartAfterPrepared(true)
            .setSeekRatio(3f)
    }

    override fun isNeedAdOnStart(): Boolean {
        return false
    }

    override fun onPrepared(url: String?, vararg objects: Any?) {
        super.onPrepared(url, *objects)
        val duration = gsyVideoPlayer.currentPlayer.duration
        val barrageList = danmakuAdvertPresenter.getAdBarrageList(duration)
        if (!barrageList.isNullOrEmpty() && !danmakuAdvertPresenter.loadCompleted) {
            gsyVideoPlayer.currentPlayer.danmakuViewModel.updateDanmaKuData(barrageList)
            danmakuAdvertPresenter.loadCompleted = true
        }
    }

    override fun onComplete(url: String?, vararg objects: Any?) {
        super.onComplete(url, *objects)
        dismissAllDialog()
        videoDialog = null
    }

    override fun onVideoSizeChanged(width: Int, height: Int, vararg objects: Any?) {
        val isVertical = gsyVideoPlayer.getIsVerticalVideo()
        videoDialog = if (isVertical) VideoPortraitDialog(this) else VideoLandDialog(this)
        gsyadVideoPlayer.isPlayerVertical = isVertical
        if (isVertical) {
            orientationUtils.isEnable = false
        } else {
            orientationUtils.isEnable = true
            super.onVideoSizeChanged(width, height, *objects)
        }
        if (isOfflineMode() && !isFullPlaying()) {
            full_loading.hide()
            showFull()
            gsyVideoPlayer.currentPlayer.backButton.setOnClickListener {
                finish()
            }
        }
    }

    override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
        super.onQuitFullscreen(url, *objects)
        if (GSYVideoADManager.backFromWindowFull(this)) {
            return
        }
    }

    override fun onClickStop(url: String?, vararg objects: Any?) {
        super.onClickStop(url, *objects)
        mAdvertPresenter?.showPauseAdvert()
    }

    override fun onClickStopFullscreen(url: String?, vararg objects: Any?) {
        super.onClickStopFullscreen(url, *objects)
        mAdvertPresenter?.showPauseAdvert()
    }

    override fun onAutoComplete(url: String?, vararg objects: Any?) {
        super.onAutoComplete(url, *objects)
        dismissAllDialog()
        videoDialog = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        dismissAllDialog()
        super.onConfigurationChanged(newConfig)
    }

    private fun getShowTitle(): String? {
        return if (currentPlayVideoBean?.videoType == Constant.VIDEO_TELEPLAY || currentPlayVideoBean?.videoType == Constant.VIDEO_VARIETY) {
            currentPlayVideoBean?.title + currentPlayVideoBean?.episodeTitle
        } else if (currentPlayVideoBean?.isLive == true) {
            currentPlayVideoBean?.episodeTitle
        } else {
            currentPlayVideoBean?.title
        }
    }

    /**
     * 获取上次播放时间
     */
    open fun getPlayRecordTime(): Long {
        if (currentPlayVideoBean == null || currentPlayVideoBean?.isLive == true) {
            return 0L
        }
        return if (currentPlayVideoBean!!.watchingProgress > 0) {
            currentPlayVideoBean!!.watchingProgress * 1000
        } else if (autoSkip && currentPlayVideoBean!!.opSecond > 0) {
            currentPlayVideoBean!!.opSecond * 1000
        } else {
            0
        }
    }

    /**
     * 获取当前是否播放中状态
     */
    fun isPlaying(): Boolean {
        return gsyVideoPlayer.isInPlayingState
    }

    /**
     * 获取当前播放时间秒
     */
    fun getCurrentPlayingTime(): Long {
        return gsyVideoPlayer.currentPlayer.currentPositionWhenPlaying / 1000
    }

    /**
     * 获取当前播放时间毫秒
     */
    fun getCurrentPlayingMiliTime(): Long {
        return gsyVideoPlayer.currentPlayer.currentPositionWhenPlaying
    }

    /**
     * 获取视频总时长
     */
    fun getTotalTime(): Long {
        return gsyVideoPlayer.currentPlayer.duration / 1000
    }

    /**
     * 更新弹幕
     */
    fun updateDanamakuItem(danmakuId: Long, data: BarrageBean) {
        gsyVideoPlayer.updateDanamakuItem(danmakuId, data)
    }

    /**
     * 发送弹幕
     */
    fun addDanamaku(bean: BarrageBean) {
        gsyVideoPlayer.addDanmaku(bean)
    }

    /**
     * 设置接口返回弹幕
     */
    open fun setDanamaku(data: MutableList<BarrageBean>) {
        if (isBarrageEnable()) {
            gsyVideoPlayer.setDanmaKuData(data)
            gsyVideoPlayer.setForbiddenWord(mViewModel.mForbiddenWordList.value)
        }
    }

    /**
     * 弹幕开关，直接调用，内部判断开还是关
     */
    fun toggleDanamaku() {
        gsyVideoPlayer.toggleDanmakuShow()
    }

    /**
     * 获取弹幕显示、隐藏
     */
    fun getDanamakuState(): Boolean {
        val state = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        return state
    }

    override fun onDetailClick(advertBean: AdvertBean) {
        if (JumpUtils.isJumpHandle(advertBean.appParam)) {
            JumpUtils.jumpAnyUtils(this, advertBean.appParam!!)
            return
        }
        if (!advertBean.linkURL.isNullOrEmpty()) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, advertBean.linkURL)
            startActivity(intent)
        }
    }

    override fun onBuyVipClick() {
        exitFullScreen()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        startDismissTimer()
    }

    // 语言
    final override fun onLanguageClick() {
        dismissAllDialog()
        if (mViewModel.mLanguageList.value.isNullOrEmpty()) return
        videoDialog?.showLanguage(getLanguage(), mViewModel.mLanguageList.value!!, object : VideoSelectDialog.OnMenuClickListener {

            override fun onItemClick(item: VideoItemBean) {
                val videoChooseBean = mViewModel.mLanguageList.value!![item.id]
                if (videoChooseBean.name == currentPlayVideoBean!!.lang) {
                    return
                }
                changeLanguage(videoChooseBean.mediaKey, currentPlayVideoBean!!.videoType)
                gsyVideoPlayer.setBottomTip(getString(R.string.language_change_tip, item.text))
            }

            override fun onOpenVipClick() {
                onBuyVipClick()
            }

        }, this)
    }

    // 倍速
    final override fun onSpeedClick() {
        dismissAllDialog()
        videoDialog?.showSpeed(gsyVideoPlayer.getCurrentSpeed(), object : VideoSelectDialog.OnMenuClickListener {

            override fun onItemClick(item: VideoItemBean) {
                if (!item.enbale) {
                    onBuyVipClick()
                    return
                }
                var speed = 1.0f
                when (item.id) {
                    0 -> speed = 2.0f
                    1 -> speed = 1.5f
                    2 -> speed = 1.25f
                    3 -> speed = 1.0f
                    4 -> speed = 0.75f
                    5 -> speed = 0.5f
                }
                gsyVideoPlayer.currentPlayer.setSpeed(speed, true)
                val speedText = if (item.id == 3) {
                    StringUtils.getString(R.string.video_speed)
                } else {
                    item.text ?: ""
                }
                gsyVideoPlayer.setSpeedText(speedText)
                gsyVideoPlayer.setBottomTip(getString(R.string.speed_change_tip, speed.toString()))
            }

            override fun onOpenVipClick() {
                onBuyVipClick()
            }

        }, this)
    }

    // 清晰度
    final override fun onClarityClick() {
        dismissAllDialog()
        if (mViewModel.mClarityList.value.isNullOrEmpty()) return
        videoDialog?.showClarity(
            currentPlayVideoBean!!.episodeId, mViewModel.mClarityList.value!!, isLive(), object : VideoSelectDialog.OnMenuClickListener {

                override fun onItemClick(item: VideoItemBean) {
                    val clarityBean = mViewModel.mClarityList.value!![item.id]
                    if (clarityBean.episodeId == currentPlayVideoBean?.episodeId) {
                        return
                    }
                    if (!item.enbale) {
                        onBuyVipClick()
                        return
                    }
                    mLiveSourceChange?.isClarityChange = true
                    if (clarityBean.mediaUrl.isNullOrEmpty()) {
                        mViewModel.getClarityUrl(clarityBean)
                    } else {
                        mViewModel.mPlayClarity.value = clarityBean
//                        playClarity(clarityBean, clarityBean.mediaUrl!!)
                    }
                }

                override fun onOpenVipClick() {
                    onBuyVipClick()
                }

                override fun onGoldCoinOpen(number: Int) {
                    showGoldPayTips(number)
                }

            },
            this
        )
    }

    private fun playClarity(clarityBean: ClarityBean, mediaUrl: String? = null) {
        clarityBean.setValueToBase(currentPlayVideoBean!!)
        if (mediaUrl.isNullOrEmpty()) {
            play(clarityBean.mediaUrl)
            currentPlayVideoBean?.episodeId?.let { onPlayInfoResponseSuccess(it) }
        } else {
            currentPlayVideoBean?.watchingProgress = getCurrentPlayingTime()
            play(mediaUrl, getbarrage = false)
            gsyVideoPlayer.setBottomTip(
                getString(
                    if (isLive()) R.string.live_clarity_change_tip else R.string.clarity_change_tip,
                    clarityBean.resolutionDes
                )
            )
        }
    }

    private fun judgeLogin(): Boolean {
        if (!GlobalValue.isLogin()) {
            exitFullScreen()
            startActivity(Intent(this, LoginActivity::class.java))
            return false
        }
        return true
    }

    private fun showGoldPayTips(number: Int) {
        if (!judgeLogin()) {
            return
        }
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(getString(R.string.gold_buy_number, number))
        tipsDialog.setMsg(R.string.gold_buy_tip)
        tipsDialog.setLeftListener(R.string.cancel) { tipsDialog.dismiss() }
        tipsDialog.setRightListener(R.string.ensure_buy) {
            tipsDialog.dismiss()
            mViewModel.goldPay(currentPlayVideoBean!!.mediaKey, currentPlayVideoBean!!.uniqueID, currentPlayVideoBean!!.episodeId)
        }
        tipsDialog.show()
    }

    // 选集
    final override fun onEpisodeClick() {
        dismissAllDialog()
        if (mViewModel.mEpisodeList.value?.second.isNullOrEmpty()) return
        videoDialog?.showEpisode(currentPlayVideoBean!!, mViewModel.mEpisodeList.value!!.second, object :
            VideoStickySelectDialog.OnItemSelectListener {
            override fun onItemSelect(bean: VideoBaseBean?) {
                if (!isVip() && bean?.isVip == true) {
                    onBuyVipClick()
                    return
                }
                if (bean != null) {
                    if (bean.uniqueID == currentPlayVideoBean?.uniqueID) return
                    bean.watchingProgress = 0
                    currentPlayVideoBean?.resetByOtherModel(bean)
                    mViewModel.getPlayInfo(bean.mediaKey, bean.uniqueID, bean.videoType)
                }
            }

        }, object :
            VideoSelectDialog.OnMenuClickListener {
            override fun onItemClick(item: VideoItemBean) {
                val select = mViewModel.mEpisodeList.value!!.second[item.id]
                if (!isVip() && select.isVip) {
                    onBuyVipClick()
                    return
                }
                if (select.uniqueID == currentPlayVideoBean?.uniqueID) return
                select.watchingProgress = 0
                currentPlayVideoBean?.resetByOtherModel(select)
                mViewModel.getPlayInfo(select.mediaKey, select.uniqueID, select.videoType)
            }

            override fun onOpenVipClick() {
                onBuyVipClick()
            }

        }, this)
    }

    final override fun onSettingClick() {
        dismissAllDialog()
        videoDialog?.showSetting(
            currentPlayVideoBean?.videoType != Constant.VIDEO_SHORT, object : SettingDialog.CallBack {
                override fun onDownload() {
                    videoDialog?.dismissDialog()
                    exitFullScreen()
                    onDownloadClick()
                }

                override fun onScreenShareClick() {
                    videoDialog?.dismissDialog()
                    mHandler.postDelayed({
                        onScreenShare()
                    }, 500)
                }

                override fun onAutoSkipHeaderAndTail(state: Boolean) {
                    autoSkip = state
                }

            },
            this
        )
    }

    final override fun onDanamaInputClick() {
        dismissAllDialog()
        if (!judgeLogin()) {
            return
        }
        videoDialog?.showDanmakuInput(object : DanamaInputDialog.OnSendDanamaListener {
            override fun onSend(input: String) {
                sendDanmaku(input)
            }

            override fun onOpenVip() {
                videoDialog?.dismissDialog()
                onBuyVipClick()
            }
        }, this)
    }

    final override fun onDanamaSettingClick() {
        dismissAllDialog()
        if (!judgeLogin()) {
            return
        }
        if (mViewModel.mForbiddenWordList.value.isNullOrEmpty()) {
            mViewModel.mForbiddenWordList.value = ArrayList()
        }
        videoDialog?.showDanmakuSetting(mViewModel.mForbiddenWordList.value!!, mViewModel.mForbiddenUserList.value, gsyVideoPlayer, this)
    }

    override fun onReportDanama(data: BarrageBean) {
        if (!judgeLogin()) {
            return
        }
        videoDialog?.showReportDialog(data, gsyVideoPlayer, this)
    }

    override fun onForbiddenDanama(data: BarrageBean) {
        gsyVideoPlayer.setBottomTip(StringUtils.getString(R.string.forbidden_success))
        gsyVideoPlayer.setForbiddenUserDanmaku(data.uid.toLong())
        mViewModel.getForbiddenUserList()
    }

    override fun onLikeDanama(danmakuId: Long, data: BarrageBean) {
        if (!judgeLogin()) {
            return
        }
        likeDanmaku(danmakuId, data)
    }

    override fun onReceiveDanmakuAdCoin(danmakuId: Long, data: BarrageBean) {
        if (data.isDrawCoin) {
            return
        }
        if (!judgeLogin()) {
            return
        }
        mViewModel.receiveAdCoin(danmakuId, data)
    }

    override fun onSendDanama(content: String) {
        if (!judgeLogin()) {
            return
        }
        sendDanmaku(content)
    }

    final override fun onShareClick() {
        dismissAllDialog()
        videoDialog?.showShare(getUpperInfo()?.id ?: 0, currentPlayVideoBean!!)
    }

    final override fun isVip(): Boolean {
        return GlobalValue.isVipUser()
    }

    final override fun isOfflineMode(): Boolean {
        return isOfflineMode
    }

    final override fun playNext() {
        if (!mViewModel.mEpisodeList.value?.second.isNullOrEmpty()) {
            var index = 0
            val episodeList = mViewModel.mEpisodeList.value!!.second
            for (temp in episodeList) {
                if (temp.uniqueID == currentPlayVideoBean!!.uniqueID) {
                    val bean = if (index + 1 < episodeList.size) {
                        // 播放下一集
                        episodeList[index + 1]
                    } else {
                        // 当前最后一集，从第一集播放
                        episodeList[0]
                    }
                    bean.watchingProgress = 0
                    currentPlayVideoBean?.resetByOtherModel(bean)
                    mViewModel.getPlayInfo(bean.mediaKey, bean.uniqueID, bean.videoType)
                    break
                }
                index++
            }
        }
    }

    final override fun isAllowLanguage(): Boolean {
        if (currentPlayVideoBean == null || mViewModel.mLanguageList.value == null || currentPlayVideoBean?.videoType == Constant.VIDEO_SHORT) {
            return false
        }
        return mViewModel.mLanguageList.value!!.size > 1
    }

    override fun getLanguage(): String {
        if (currentPlayVideoBean!!.lang.isNullOrEmpty()) {
            return getString(R.string.language)
        }
        return currentPlayVideoBean!!.lang!!
    }

    override fun isAllowQuality(): Boolean {
        if (mViewModel.mClarityList.value == null || isOfflineMode()) {
            return false
        }
        return mViewModel.mClarityList.value!!.size > 1 || isLive()
    }

    override fun getQuality(): String {
        return currentPlayVideoBean!!.resolutionDes!!
    }

    final override fun hasNext(): Boolean {
        if (currentPlayVideoBean == null || !isEpisodeType() || mViewModel.mEpisodeList.value?.second.isNullOrEmpty()) {
            return false
        }
        val episodeList = mViewModel.mEpisodeList.value?.second
//        val allLocal = episodeList?.filter { !it.filePath.isNullOrEmpty() }
        if (episodeList.isNullOrEmpty()/* || allLocal?.isEmpty() == false*/) {
            return false
        }
        for ((index, temp) in episodeList.withIndex()) {
            if (temp.uniqueID == currentPlayVideoBean!!.uniqueID) {
                return index < episodeList.size - 1
            }
        }
        return false
    }

    override fun hasPlayList(): Boolean {
        return mViewModel.mEpisodeList.value?.second?.isNotEmpty() == true
    }

    final override fun onScreenShare() {
        exitFullScreen()
        if (judgeLogin()) {
            val intent = Intent(this, ScreenShareActivity::class.java)
            startActivityForResult(intent, 300)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getParcelableExtra<LelinkServiceInfo>("serviceInfo") != null) {
                orientationUtils.isEnable = false
                lelinkPlayerInfo = data.getParcelableExtra("serviceInfo") as LelinkServiceInfo?
                lelinkPlayerInfo?.let { setShareController(it) }
            }
        }
    }

    private fun isScreenShare(): Boolean {
        return shareContent.visibility == View.VISIBLE
    }

    private fun resetShare() {
        gsyVideoPlayer.currentPlayer.onVideoPause()
        shareContent.visibility = View.VISIBLE
        shareTotal.text = CommonUtil.stringForTime(getTotalTime() * 1000)
        shareProgress.progress = 0
        shareCurrent.text = "00:00"
    }

    private fun setShareController(lelinkServiceInfo: LelinkServiceInfo) {
        var totalTime = 0
        shareProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                LelinkSourceSDK.getInstance().seekTo(totalTime * seekBar.progress / 100)
            }

        })

        playStatus.setOnClickListener {
            if (playStatus.tag != null && playStatus.tag.toString().toInt() > 1) {
                LelinkSourceSDK.getInstance().resume()
            } else {
                LelinkSourceSDK.getInstance().pause()
            }
        }
        LelinkSourceSDK.getInstance().setPlayListener(object : ILelinkPlayerListener {
            /**
             *  开始加载
             */
            override fun onLoading() {
                setPlayState(0)
            }

            /**
             * 播放开始
             */
            override fun onStart() {
                setPlayState(1)
            }

            /**
             * 暂停
             */
            override fun onPause() {
                setPlayState(2)
            }

            /**
             * 播放完成
             */
            override fun onCompletion() {
                setPlayState(3)
            }

            /**
             * 播放结束
             */
            override fun onStop() {
                setPlayState(4)
            }

            /**
             * 错误回调
             */
            override fun onError(what: Int, extra: Int) {
                setPlayState(5)
            }

            /**
             * 进度调节：单位为百分比（该接口暂无回调）
             */
            override fun onSeekComplete(pPosition: Int) {
            }

            /**
             * 保留接口
             */
            override fun onInfo(what: Int, extra: Int) {
            }

            override fun onInfo(p0: Int, p1: String?) {
            }

            /**
             * 音量变化回调（该接口暂无回调）
             */
            override fun onVolumeChanged(percent: Float) {
            }

            /**
             * 播放进度信息回调
             * @param duration 总长度：单位秒
             * @param position 当前进度：单位秒
             */
            override fun onPositionUpdate(duration: Long, position: Long) {
                totalTime = duration.toInt()
                if (totalTime > 0) {
                    currentPlayVideoBean!!.watchingProgress = position
                    runOnUiThread {
                        shareCurrent.text = CommonUtil.stringForTime(position * 1000)
                        shareProgress.progress = position.toInt() * 100 / totalTime
                        shareTotal.text = CommonUtil.stringForTime(totalTime * 1000L)
                    }
                }
            }
        })
        resetShare()
        val playerInfo = LelinkPlayerInfo()
        playerInfo.url = currentPlayVideoBean!!.mediaUrl
        playerInfo.type = LelinkSourceSDK.MEDIA_TYPE_VIDEO
        playerInfo.lelinkServiceInfo = lelinkServiceInfo
        playerInfo.loopMode = LelinkPlayerInfo.LOOP_MODE_DEFAULT
        playerInfo.startPosition = currentPlayVideoBean!!.watchingProgress.toInt()
        LelinkSourceSDK.getInstance().startPlayMedia(playerInfo)
    }

    private fun setPlayState(tag: Int) {
        playStatus.tag = tag
        mHandler.post {
            if (tag > 1) {
                playStatus.setImageResource(R.mipmap.icon_play)
            } else {
                playStatus.setImageResource(R.mipmap.icon_play_pause)
            }
            if (tag == 5) {
                stopShare()
            } else if (tag == 3) {
                playNext()
            }
        }
    }

    fun exitShare(view: View) {
        stopShare()
    }

    private fun stopShare() {
        LelinkSourceSDK.getInstance().stopPlay()

        shareContent.visibility = View.GONE
        lelinkPlayerInfo = null
        gsyVideoPlayer.onVideoSizeChanged()
        LelinkSourceSDK.getInstance().unBindSdk()

        play(currentPlayVideoBean?.mediaUrl)
    }

    fun changeShare(view: View) {
        val intent = Intent(this, ScreenShareActivity::class.java)
        startActivityForResult(intent, 300)
    }

    fun skipAdvert() {
        mAdvertPresenter?.skipPlayAd()
    }

    fun setFilterAd() {
        currentPlayVideoBean?.isFilterAds = true
        mViewModel.mClarityList.value?.forEach { it.isFilterAds = true }
        mAdvertPresenter?.reset()
    }

    fun advertCallBack(adUrl: String) {
        mAdvertPresenter?.showCallBack(adUrl)
    }

    private fun setForbiddenUserDanmaku(forbiddenUserList: MutableList<UserInfoBean>) {
        val ids: MutableList<Long> = ArrayList()
        for (temp in forbiddenUserList) {
            ids.add(temp.id.toLong())
        }
        gsyVideoPlayer.setForbiddenUserDanmaku(ids)
    }

    private fun playClarityError(errorMsg: String) {
        ToastUtils.showLong(errorMsg)
        gsyVideoPlayer.currentPlayer.changeUiToError()
        onPlayInfoResponseError()
    }

    /**
     * 添加播放记录
     */
    private fun addPlayRecord() {
        if (currentPlayVideoBean?.isLive == true) return

        if (isOfflineMode()) {
            val progress = DownloadManager.getInstance().get(currentPlayVideoBean!!.episodeKey)
            if (progress?.extra1 != null) {
                val downloadInfo: VideoBaseBean = GsonUtils.fromJson(progress.extra1.toString(), VideoBaseBean::class.java)
                downloadInfo.isWatched = true
                progress.extra1 = Gson().toJson(downloadInfo)
                DownloadManager.getInstance().update(progress)
            }
            return
        }
        val watchRecordBean = WatchRecordBean()
        val uid = if (GlobalValue.isLogin()) {
            GlobalValue.userInfoBean?.token!!.uid
        } else {
            0
        }
        if (currentPlayVideoBean!!.time == "0" || currentPlayVideoBean!!.time.isEmpty()) {
            return
        }
        watchRecordBean.uid = uid
        watchRecordBean.videoType = currentPlayVideoBean!!.videoType
        watchRecordBean.mediaKey = currentPlayVideoBean!!.mediaKey
        watchRecordBean.title = currentPlayVideoBean!!.title
        watchRecordBean.episodeId = currentPlayVideoBean!!.episodeId
        watchRecordBean.uniqueID = currentPlayVideoBean!!.uniqueID
        watchRecordBean.episodeTitle = currentPlayVideoBean!!.episodeTitle
        watchRecordBean.coverImgUrl = currentPlayVideoBean!!.coverImgUrl.toString()
        watchRecordBean.upperName = currentPlayVideoBean!!.upperName
        watchRecordBean.mediaUrl = currentPlayVideoBean!!.mediaUrl
        watchRecordBean.watchTime = currentPlayVideoBean!!.watchingProgress.toString()
        watchRecordBean.duration = currentPlayVideoBean!!.duration
        watchRecordBean.time = currentPlayVideoBean!!.time
        watchRecordBean.cidMapper = currentPlayVideoBean!!.cidMapper.toString()
        watchRecordBean.regional = currentPlayVideoBean!!.regional.toString()
        watchRecordBean.lang = currentPlayVideoBean!!.lang
        watchRecordBean.status = 0
        watchRecordBean.playRecordUrl = currentPlayVideoBean!!.playRecordUrl
        EventBus.getDefault().post(watchRecordBean)
        AddWatchRecordUtil.addRecord(watchRecordBean)
    }

    override fun onLogin() {
        super.onLogin()
        if (isVip() && isADStarted) {
            skipAdvert()
            mAdvertPresenter?.vipSkipAdTip()
        }
        if (isLive()) {
            mLiveSourceChange?.reset()
            mLiveSourceChange = LiveSourceChange(gsyVideoPlayer, currentPlayVideoBean, mViewModel, mViewModel.mClarityList.value) { startPlayVideo() }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onForbiddenSuccess(message: BarrageBean) {
        mViewModel.getForbiddenUserList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAdvertAction(event: AdvertActionEvent) {
        if (event.action == AdvertAction.ACTION_PLAY) {
            startAdPlay()
        } else if (event.action == AdvertAction.ACTION_FULL) {
            showFull()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveBarrage(event: BarrageEvent) {
        val message = event.message ?: return
        if (event.eventType == BarrageType.EVENT_LOCAL || (isBarrageEnable() && (event.eventType == BarrageType.EVENT_BARRAGE || event.eventType == BarrageType.EVENT_CHAT) && message.uid != GlobalValue.userInfoBean?.id)) {
            message.isLive = isLive()
            addDanamaku(message)
        }
    }

}