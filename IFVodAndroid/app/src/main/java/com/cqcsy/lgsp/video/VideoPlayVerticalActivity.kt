package com.cqcsy.lgsp.video

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.bean.net.VideoIntroductionNetBean
import com.cqcsy.lgsp.download.DownloadUtil
import com.cqcsy.lgsp.event.BarrageEvent
import com.cqcsy.lgsp.event.BarrageType
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.event.VideoActionEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.fragment.*
import com.cqcsy.lgsp.video.player.SwitchUtil
import com.cqcsy.lgsp.views.dialog.BarrageEditDialog
import com.cqcsy.lgsp.views.dialog.BarrageEditDialog.SendBarrageListener
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import kotlinx.android.synthetic.main.layout_video_details.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 竖屏播放页面
 */
class VideoPlayVerticalActivity : VideoBaseActivity(), OnClickListener {

    private var commentCounts = -1

    private var barrageEditDialog: BarrageEditDialog? = null

    // 是否需要刷新推荐数据
    private var refreshRecommendView = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isLive() && !GlobalValue.isLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setListener()
        detail_pager.isUserInputEnabled = false

        mViewModel.getVideoInfo(getMediaKey(), getEpisodeKey(), getVideoType(), getVideoTitle())
    }

    override fun initObserve() {
        super.initObserve()
        mViewModel.mVideoInfo.observe(this) {
            removeDialogFragment()
            if (isOfflineMode()) {
                it?.detailInfo?.let { it1 -> getVideoBaseBean()?.resetByOtherModel(it1) }
                return@observe
            }
            if (it?.userInfo != null && it.detailInfo != null) {
                val detail = it.detailInfo!!
                // 当前是直播，切换到非直播
                if (isLive() && (detail.videoType != Constant.VIDEO_TV || detail.videoType != Constant.VIDEO_LIVE)) {
                    reset()
                }
                commentCounts = detail.comments
                playWithModel(detail, detail.watchingProgress)
                if (isFullPlaying()) {
                    refreshRecommendView = true
                }
                mViewModel.updateCollectStatus(getMediaKey(), getVideoType())
                mViewModel.getAdsList(getMediaKey())
            } else {
                errorView(it?.detailInfo?.mediaKey ?: "", it?.detailInfo?.videoType ?: 0, it?.detailInfo?.title ?: "")
            }
        }
        mViewModel.mAdBanner.observe(this) {
            it?.forEach { item ->
                item.viewURL?.let { it1 -> advertCallBack(it1) }
            }
        }
        mViewModel.mAdDanmaku.observe(this) {
            danmakuAdvertPresenter.danmakuAdList = it
            val duration = gsyVideoPlayer.currentPlayer.duration
            val barrageList = danmakuAdvertPresenter.getAdBarrageList(duration)
            if (!barrageList.isNullOrEmpty()) {
                gsyVideoPlayer.currentPlayer.danmakuViewModel.updateDanmaKuData(barrageList)
                danmakuAdvertPresenter.loadCompleted = true
            }
        }
        mViewModel.mAdInsert.observe(this) {
            mAdvertPresenter?.setInsertAdvert(it)
        }
        mViewModel.mAdPause.observe(this) {
            mAdvertPresenter?.setPauseAdvert(it)
        }
        mViewModel.mLikeDanmaku.observe(this) {
            updateDanamakuItem(it.first, it.second)
        }
        mViewModel.mCoinSkipAd.observe(this) {
            if (it.get()) {
                skipAdvert()
                setFilterAd()
            }
        }
    }

    private fun setListener() {
        barrage_edit.setOnClickListener(this)
        barrage_switch.setOnClickListener(this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val mediaKey = intent?.getStringExtra(PLAY_VIDEO_MEDIA_KEY)
        val videoType = intent?.getIntExtra(VIDEO_TYPE, 0) ?: 0
        val videoBean = intent?.getSerializableExtra(PLAY_VIDEO_BEAN)
        if (!mediaKey.isNullOrEmpty()) {
            mViewModel.getVideoInfo(mediaKey, videoType = videoType)
        } else if (videoBean != null && videoBean is VideoBaseBean) {
            setViewBaseBean(videoBean)
            if (isOfflineMode()) {
                playOffline()
            } else {
                mViewModel.getVideoInfo(videoBean.mediaKey, episodeKey = videoBean.episodeKey, videoType = videoBean.videoType)
            }
        }
    }

    override fun onDestroy() {
        mTabPresenter.destory()
        super.onDestroy()
    }

    private fun isEnableComment(): Boolean {
        return mViewModel.mVideoInfo.value?.detailInfo?.commentStatus == 0
    }

    private fun setUpTabs() {
        val tabArray = StringUtils.getStringArray(R.array.play_detail_tabs)
        val tabItems = if (!isEnableChat()) {
            val list = tabArray.toMutableList()
            list.removeAt(1)
            list
        } else {
            tabArray.toMutableList()
        }
        if (!isEnableComment()) {
            tabItems.removeLast()
        }
       
    }

    private fun createIntro(): Fragment {
        val fragment = VideoIntroductionFragment()
        val bundle = Bundle()
        val downloaded = intent.getSerializableExtra(DOWNLOADED_EPISODE)
        if (downloaded != null) {
            bundle.putSerializable(DOWNLOADED_EPISODE, downloaded)
        }
        return fragment
    }

    private fun createChat(): Fragment {
        return VideoChatFragment()
    }

    private fun createComment(): Fragment {
        val bundle = Bundle()
        val fragment = VideoCommentFragment()
        bundle.putString("mediaKey", getMediaKey())
        bundle.putInt("videoType", getVideoType() ?: 0)
        bundle.putInt(COMMENT_ID, commentId)
        bundle.putInt(REPLY_ID, mReplyId)
        bundle.putBoolean(
            VideoCommentFragment.SHOW_INPUT,
            intent.getBooleanExtra(VideoCommentFragment.SHOW_INPUT, true)
        )
        fragment.arguments = bundle
        return fragment
    }

    private fun getDefaultTab(): Int {
        if (!SwitchUtil.isRelease() || commentId > 0) {
            return if (isEnableChat()) 2 else 1
        }
        return when (getShowType()) {
            VideoBaseBean.SHOW_COMMENT -> {
                if (isEnableChat()) {
                    2
                } else {
                    1
                }
            }

            VideoBaseBean.SHOW_CHAT -> {
                1
            }

            else -> {
                0
            }
        }
    }

    override fun getBottomLayoutId(): Int {
        return R.layout.layout_video_details
    }

    override fun sendDanmaku(input: String) {
        mViewModel.sendBarrage(getMediaKey(), getUniqueId(), getVideoType(), input, getCurrentPlayingMiliTime() / 1000L)
    }

    override fun likeDanmaku(danmakuId: Long, data: BarrageBean) {
        mViewModel.like(danmakuId, data)
    }

    override fun changeLanguage(mediaKey: String, videoType: Int) {
        if (isShortVideo()) {
            reset()
        }
        mViewModel.getVideoInfo(mediaKey, videoType = videoType)
    }

    private fun reset() {
        mTabPresenter.destory()
        commentId = 0
        refreshRecommendView = false
    }

    override fun onPlayInfoResponseSuccess(episodeId: Int) {
        initFragment()
    }

    override fun onPlayInfoResponseError() {
        showError()
    }

    override fun onVideoStartPlay() {
        super.onVideoStartPlay()
        if (mLiveSourceChange?.isClarityChange == false) {
            mViewModel.playRecord(getMediaKey())
            refreshBarrage()
            if (isShortVideo()) {
                val cidMapper = getVideoBaseBean()?.contentType
                if (!cidMapper.isNullOrEmpty()) {
                    cidMapper.let { LabelUtil.addLabels(it, getVideoBaseBean()?.userId ?: 0, Constant.KEY_SHORT_VIDEO_LABELS) }
                }
            }
        }
        mLiveSourceChange?.isClarityChange = false
    }

    override fun getUpperInfo(): UserInfoBean? {
        return mViewModel.mVideoInfo.value?.userInfo
    }

    override fun getDetailInfo(): VideoIntroductionNetBean? {
        return mViewModel.mVideoInfo.value
    }

    private fun refreshBarrage() {
        if (isPlaying() && isBarrageEnable()) {
            barrage_status_container.isVisible = true
            if (!getDanamakuState()) {
                barrage_switch.isSelected = true
                barrage_edit.isVisible = false
            } else {
                barrage_switch.isSelected = false
                barrage_edit.isVisible = true
            }
        } else {
            barrage_status_container.isVisible = false
        }
    }

    override fun onBuyVipClick() {
        super.onBuyVipClick()
        showVipInfoFragment()
    }

    override fun onSkipAd() {
        if (!GlobalValue.isLogin()) {
            startLogin()
        } else if (GlobalValue.isVipUser()) {
            skipAdvert()
        } else {
            // 弹金币购买和开通VIP选择
            coinSkipAdDialog()
        }
    }

    override fun onDownloadClick() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        if (!GlobalValue.isVipUser()) {
            showVipInfoFragment()
            return
        }
        val videoInfo = getVideoBaseBean()
        if (videoInfo?.videoType == Constant.VIDEO_VARIETY || videoInfo?.videoType == Constant.VIDEO_TELEPLAY) {
            val intent = Intent(this, AnthologyActivity::class.java)
            intent.putExtra("mediaKey", videoInfo.mediaKey)
            intent.putExtra("videoType", videoInfo.videoType)
            intent.putExtra("resolution", videoInfo.resolution)
            intent.putExtra("episodeTitle", videoInfo.episodeTitle)
            intent.putExtra("uniqueId", videoInfo.uniqueID)
            intent.putExtra("lang", videoInfo.lang)
            intent.putExtra("pageAction", 1)
            intent.putExtra("coverImage", videoInfo.coverImgUrl)
            startActivity(intent)
        } else {
            videoInfo?.let {
                DownloadUtil.showSelectQuality(this, mViewModel.mClarityList.value, it)
            }
        }

    }

    override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
        super.onQuitFullscreen(url, *objects)
        initFragment()
        refreshBarrage()
    }

    override fun onSoftBoardHide() {
        super.onSoftBoardHide()
        barrageEditDialog?.dismiss()
    }

    override fun onPlayClick() {
        if (!getMediaKey().isNullOrEmpty() && !isOfflineMode()) {
            mViewModel.getVideoInfo(getMediaKey(), getEpisodeKey(), getVideoType(), getVideoTitle())
        }
    }

    /**
     *  弹幕按钮开关
     */
    private fun barrageSwitch() {
        toggleDanamaku()
        if (barrage_switch.isSelected) {
            barrage_switch.isSelected = false
            barrage_edit.isVisible = true
        } else {
            barrage_switch.isSelected = true
            barrage_edit.isVisible = false
        }
    }

    private fun errorView(mediaKey: String, videoType: Int, videoTitle: String) {
        if (!SwitchUtil.isRelease()) {
            stopPlayAll()
        }
        gsyVideoPlayer.currentPlayer.changeUiToError()
        if (getMediaKey() != mediaKey) {
            resetMediaId(mediaKey, videoType, videoTitle)
        }
        showError()
    }

    private fun showError() {
        page_error.isVisible = true
        page_error.showFailed {
            page_error.isVisible = false
            mViewModel.getVideoInfo(getMediaKey(), getEpisodeKey(), getVideoType(), getVideoTitle())
        }
    }

    private fun initFragment() {
        if (isOfflineMode()) return
        if (mViewModel.mVideoInfo.value == null) {
            mViewModel.getVideoInfo(getMediaKey(), getEpisodeKey(), getVideoType(), getVideoTitle())
            return
        }
        if (isFullPlaying()) return
        if (!mTabPresenter.isAttached()) {
            setUpTabs()
        }

        setCommentNumber(commentCounts)
    }

    /**
     * 增加评论数
     */
    private fun addCommentCount() {
        commentCounts += 1
        setCommentNumber(commentCounts)
    }

    private fun setCommentNumber(commentCount: Int) {
        val index = if (isEnableChat()) {
            2
        } else {
            1
        }
        val tab = mTabPresenter.tab.getTabAt(index) ?: return
        if (tab.customView != null) {
            val number = tab.customView!!.findViewById<TextView>(R.id.tab_number)
            number.text = NormalUtil.formatPlayCount(commentCount)
            number.isVisible = commentCount > 0
        }
    }

    /**
     * 点击弹出弹幕输入框
     */
    private fun barrageEditClick() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        barrageEditDialog = BarrageEditDialog(object : SendBarrageListener {
            override fun sendBarrage(inputText: String) {
                mViewModel.sendBarrage(getMediaKey(), getUniqueId(), getVideoType(), inputText, getCurrentPlayingMiliTime() / 1000L)
            }
        })
        barrageEditDialog?.show(supportFragmentManager, "barrageEditDialog")
    }

    /**
     * 金币跳过广告弹框
     */
    private fun coinSkipAdDialog() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(
            StringUtils.getString(
                R.string.skipAdTitle,
                mViewModel.mVideoInfo.value?.detailInfo?.adGold
            )
        )
        tipsDialog.setMsg(R.string.skipAdTips)
        tipsDialog.setLeftListener(R.string.ad_buy_vip_tip) {
            mViewModel.coinSkipAdHttp(getUniqueId())
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.open_vip) {
            exitFullScreen()
            showVipInfoFragment()
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * vip套餐支付页Fragment
     */
    private fun showVipInfoFragment() {
        var vipFragment: Fragment? = supportFragmentManager.findFragmentByTag(VipPayFragment::class.java.simpleName)
        val transaction = supportFragmentManager.beginTransaction()
        if (vipFragment == null) {
            vipFragment = VipPayFragment()
            val bundle = Bundle()
            bundle.putString("pathInfo", this::class.java.simpleName + "/" + getMediaKey())
            vipFragment.arguments = bundle
            transaction.add(R.id.bottomContainer, vipFragment, VipPayFragment::class.java.simpleName)
        }
        transaction.show(vipFragment)
        transaction.commitNowAllowingStateLoss()
    }

    private fun removeDialogFragment() {
        val vipPayFragment = supportFragmentManager.findFragmentByTag(VipPayFragment::class.java.simpleName)
        if (vipPayFragment != null) {
            supportFragmentManager.beginTransaction().remove(vipPayFragment).commitNowAllowingStateLoss()
        }
        val detailFragment = supportFragmentManager.findFragmentByTag(VideoDetailsFragment::class.java.simpleName)
        if (detailFragment != null) {
            supportFragmentManager.beginTransaction().remove(detailFragment).commitNowAllowingStateLoss()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && !isFullPlaying()) {
            val vipPayFragment = supportFragmentManager.findFragmentByTag(VipPayFragment::class.java.simpleName)
            if (vipPayFragment != null) {
                supportFragmentManager.beginTransaction().remove(vipPayFragment).commitNowAllowingStateLoss()
                return true
            }
            val detailFragment = supportFragmentManager.findFragmentByTag(VideoDetailsFragment::class.java.simpleName)
            if (detailFragment != null) {
                supportFragmentManager.beginTransaction().remove(detailFragment).commitNowAllowingStateLoss()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onReloginEvent(event: ReloginEvent) {
//        if (event.needLogin) {
//            finish()
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReleaseComment(event: CommentEvent) {
        addCommentCount()
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun resetMediaId(mediaKey: String, videoType: Int, videoTitle: String) {
        val bean = VideoBaseBean()
        bean.mediaKey = mediaKey
        bean.videoType = videoType
        bean.title = videoTitle
        setViewBaseBean(bean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvent(event: BarrageEvent) {
        if (event.eventType == BarrageType.EVENT_ONLINE && event.onlineNumber >= 0) {
            mViewModel.mOnlineNumber.value = event.onlineNumber
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.barrage_edit -> {
                barrageEditClick()
            }

            R.id.barrage_switch -> {
                barrageSwitch()
            }
        }
    }

    /**
     * 1：关注
     * 2：点赞
     * 3：踩
     * 4：收藏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRecommendAction(event: VideoActionEvent) {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        mViewModel.mVideoInfo.value?.apply {
            when (event.type) {
                1 -> userInfo?.id?.let { mViewModel.followClick(it) }
                2 -> mViewModel.videoFabulous(detailInfo?.mediaKey, detailInfo?.videoType)
                3 -> mViewModel.videoDebunkClick(detailInfo?.mediaKey, detailInfo?.videoType)
                4 -> mViewModel.videoCollectionClick(detailInfo?.mediaKey, detailInfo?.videoType)
                5 -> userInfo?.id?.let { mViewModel.forbidden(it, isBlackList) }
            }
        }
    }
}