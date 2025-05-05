package com.cqcsy.lgsp.video.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.banner.BannerViewAdapter
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.download.DownloadUtil
import com.cqcsy.lgsp.event.*
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.UpperActivity.Companion.UPPER_ID
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.AnthologyActivity
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.lgsp.views.dialog.ShareBoard
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.donkingliang.consecutivescroller.ConsecutiveScrollerLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.youth.banner.Banner
import com.youth.banner.indicator.RectangleIndicator
import kotlinx.android.synthetic.main.layout_introduction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 视频简介fragment
 */
class VideoIntroductionFragment : BaseFragment(), View.OnClickListener {
    // 选集适配器
    private var anthologyAdapter: BaseQuickAdapter<VideoBaseBean, BaseViewHolder>? = null

    // 相关剧集适配器
    private var relevantAdapter: BaseQuickAdapter<VideoBaseBean, BaseViewHolder>? = null

    // 短视频适配器
    private var shortVideoAdapter: BaseQuickAdapter<ShortVideoBean, BaseViewHolder>? = null

    // 选集数据
    private var anthologyData: MutableList<VideoBaseBean> = ArrayList()

    // 相关推荐数据
    private var relevantData: MutableList<VideoBaseBean> = ArrayList()

    // 小视频
    private var shortVideoData: MutableList<ShortVideoBean> = ArrayList()

    private var mVideoInfo: VideoDetailsBean? = null
    private var mUpperInfo: UserInfoBean? = null

    private var mRating: VideoRatingBean? = null
    private var mLikeBean: VideoLikeBean? = null
    private var mDislikeBean: VideoLikeBean? = null
    private var mCollectBean: VideoLikeBean? = null
    private var isBlackList = false
    private var isFocusUpper = false

    // 记录选集播放位置
    private var anthologySelectPosition = -1

    private var page = 1
    private var size = 20

    // 用于判断选集样式 true为综艺样式显示，false剧集样式显示
    private var isVarietyView = false

    private val mViewModel: VideoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_introduction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        initView()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initObserve() {
        mViewModel.mVideoInfo.observe(this) {
            dismissProgressView()
            if (it?.userInfo == null || it.detailInfo == null) {
                if (it?.detailInfo != null) {
                    mVideoInfo = it.detailInfo
                }
                showFailedView { reset() }
            } else {
                val detail = it.detailInfo!!
                if (mVideoInfo?.mediaKey != detail.mediaKey && (detail.videoType == Constant.VIDEO_TELEPLAY || detail.videoType == Constant.VIDEO_VARIETY || detail.isLive)) {
                    mViewModel.getEpisodeInfo(detail.mediaKey)
                }
                mVideoInfo = it.detailInfo
                mUpperInfo = it.userInfo
                mLikeBean = it.like
                mDislikeBean = it.disLike
                isFocusUpper = it.focusStatus
                isBlackList = it.isBlackList
                mCollectBean = it.favorites
                if (detail.videoType == Constant.VIDEO_SHORT) {
                    setVideoInfo(detail)
                }
            }
        }
        mViewModel.mRecommendShort.observe(this) {
            setRecommendShort(it)
        }
        mViewModel.mFeedBackUrl.observe(this) {
            startFeedBack(it)
        }
        mViewModel.mEpisodeList.observe(this) {
            if (it.first) {
                if (!it.second.isNullOrEmpty()) {
                    setAnthologyData(it.second)
                }
            } else {
                val downloaded = arguments?.getSerializable(VideoBaseActivity.DOWNLOADED_EPISODE)
                if (downloaded != null && downloaded is MutableList<*>) {
                    (downloaded as MutableList<VideoBaseBean>).forEach { item ->
                        val temp = VideoBaseBean()
                        temp.resetByOtherModel(item)
                        temp.filePath = item.filePath
                        it.second.add(temp)
                    }
                }
            }
        }
        mViewModel.mVideoRating.observe(this) {
            setRating(it)
        }
        mViewModel.mVideoLike.observe(this) {
            if (it.selected) {
                ImageUtil.clickAnim(requireActivity(), videoCollectionImage)
            }
            mCollectBean?.selected = it.selected
            mCollectBean?.count = it.count

            val result = VideoActionResultEvent()
            result.id = mVideoInfo?.mediaKey ?: ""
            result.type = 4
            result.selected = it.selected
            result.count = it.count
            result.actionType =
                if (mVideoInfo?.videoType == Constant.VIDEO_SHORT) VideoActionResultEvent.TYPE_SHORT else VideoActionResultEvent.TYPE_EPISODE
            EventBus.getDefault().post(result)

            initCollectView(it)
        }
        mViewModel.mVideoDislike.observe(this) {
            setLikeAndDislike(it)
        }
        mViewModel.mRecommendVideo.observe(this) {
            setRecommendVideo(it)
        }
        mViewModel.mFocusState.observe(this) {
            var fansCount = mUpperInfo?.fansCount ?: 0
            if (it.get()) {
                mUpperInfo?.fansCount = ++fansCount
                ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
            } else {
                if (fansCount > 0) {
                    mUpperInfo?.fansCount = --fansCount
                }
            }
            setFansCount(fansCount)
            val result = VideoActionResultEvent()
            result.id = mUpperInfo?.id.toString()
            result.action = if (it.get()) VideoActionResultEvent.ACTION_ADD else VideoActionResultEvent.ACTION_REMOVE
            result.userLogo = mUpperInfo?.avatar ?: ""
            result.userName = mUpperInfo?.nickName ?: ""
            result.type = 1
            result.selected = it.get()
            EventBus.getDefault().post(result)

            isFocusUpper = it.get()
            setUserStatus(it.get(), isBlackList)
        }
        mViewModel.mPlayClarity.observe(this) {
            if (it == null) return@observe
            if (it.videoType == Constant.VIDEO_TV) {
                anthologyTitle.setText(R.string.select_tv_channel)
//                allAnthology.setText(R.string.movieAll)
            }
            allAnthology.isVisible = !it.isLive
            downloadVideo.isVisible = !it.isLive
            mVideoInfo?.let { it1 ->
                it.setValueToBase(it1)
                if (it1.videoType != Constant.VIDEO_SHORT) {
                    setVideoInfo(it1)
                }
            }
            refreshAnthologyData(it.uniqueID)
        }
        mViewModel.mAdBanner.observe(this) {
            if (it != null) {
                setAdBanner(it)
            }
        }
    }

    private fun initView() {
        // 解决嵌套recycleView滑动不流畅问题
        relevantRecycle.setHasFixedSize(true)
        relevantRecycle.isNestedScrollingEnabled = false
        shortVideoRecycle.setHasFixedSize(true)
        shortVideoRecycle.isNestedScrollingEnabled = false
        setClickListener()
        resetView()
    }

    override fun onResume() {
        super.onResume()
        adBanner.start()
    }

    override fun onPause() {
        super.onPause()
        adBanner.stop()
    }

    /**
     * 初始化下拉刷新
     */
    private fun initRefreshLayout() {
        introductionRefresh.setEnableRefresh(false)
        introductionRefresh.setEnableLoadMore(true)
        introductionRefresh.setOnLoadMoreListener {
            // 刷新数据，请求接口
            mViewModel.getRecommendShort(mVideoInfo?.mediaKey, mVideoInfo?.title, page, size)
            adBanner.stop()
        }
    }

    private fun setRecommendVideo(videoList: MutableList<VideoBaseBean>?) {
        relevantData.clear()
        if (!videoList.isNullOrEmpty()) {
            relevantData.addAll(videoList)
        }
        if (mVideoInfo?.videoType == Constant.VIDEO_SHORT || relevantData.isNullOrEmpty()) {
            relevantRecycle.isVisible = false
            relevantSpace.isVisible = false
            relevantTitle.isVisible = false
        } else {
            relevantRecycle.isVisible = true
            relevantSpace.isVisible = true
            relevantTitle.isVisible = true
        }
        if (relevantAdapter == null) {
            relevantRecycle.layoutManager = GridLayoutManager(context, 3)
            relevantRecycle.addItemDecoration(XGridBuilder(context).setVLineSpacing(10f).setHLineSpacing(10f).setIncludeEdge(true).build())
            relevantAdapter = object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(R.layout.item_video_recomment, relevantData) {
                override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                    holder.setText(R.id.itemVideoTitle, item.title)
                    holder.setText(R.id.itemType, item.cidMapper?.replace(",", " "))
                    item.coverImgUrl?.let {
                        ImageUtil.loadImage(context, it, holder.getView(R.id.itemVideoImage))
                    }
                    holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                        showProgressView()
                        resetView()
                        mViewModel.getVideoInfo(item.mediaKey, videoType = item.videoType, videoTitle = item.title)
                    }
                }
            }
            relevantRecycle.adapter = relevantAdapter
        } else {
            relevantAdapter?.notifyDataSetChanged()
        }
    }

    private fun setClickListener() {
        videoDetail.setOnClickListener(this)
        allAnthology.setOnClickListener(this)
        videoDetailShare.setOnClickListener(this)
        videoFabulousLayout.setOnClickListener(this)
        videoDebunkLayout.setOnClickListener(this)
        videoCollectionLayout.setOnClickListener(this)
        downloadVideo.setOnClickListener(this)
        followText.setOnClickListener(this)
        blackList.setOnClickListener(this)
        photoLayout.setOnClickListener(this)
        feedBack.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.videoDetail -> showDetail(mRating)
            R.id.allAnthology -> {
                val intent = Intent(context, AnthologyActivity::class.java)
                intent.putExtra("mediaKey", mVideoInfo?.mediaKey)
                intent.putExtra("videoType", mVideoInfo?.videoType)
                intent.putExtra("resolution", mVideoInfo?.resolution)
                intent.putExtra("episodeTitle", mVideoInfo?.episodeTitle)
                intent.putExtra("uniqueId", mVideoInfo?.uniqueID)
                intent.putExtra("lang", mVideoInfo?.lang)
                startActivityForResult(intent, 1000)
            }

            R.id.videoDetailShare -> videoDetailShare()
            R.id.videoFabulousLayout -> mViewModel.videoFabulous(mVideoInfo?.mediaKey, mVideoInfo?.videoType)
            R.id.videoDebunkLayout -> mViewModel.videoDebunkClick(mVideoInfo?.mediaKey, mVideoInfo?.videoType)
            R.id.videoCollectionLayout -> mViewModel.videoCollectionClick(mVideoInfo?.mediaKey, mVideoInfo?.videoType)
            R.id.downloadVideo -> startDownload()
            R.id.followText -> mUpperInfo?.id?.let { mViewModel.followClick(it) }
            R.id.blackList -> mUpperInfo?.let {
                showBlackTip(it.id)
            }

            R.id.photoLayout -> {
                mUpperInfo?.let {
                    val intent = Intent(context, UpperActivity::class.java)
                    intent.putExtra(UPPER_ID, mUpperInfo?.id)
                    startActivity(intent)
                }
            }

            R.id.feedBack -> feedBack()
        }
    }

    private fun showDetail(ratingBean: VideoRatingBean?) {
        val bundle = Bundle()
        bundle.putSerializable("videoRating", ratingBean)
        bundle.putSerializable("videoDetailsBean", mVideoInfo)
        val fragment = VideoDetailsFragment()
        fragment.arguments = bundle
        val transaction = parentFragmentManager.beginTransaction()
        transaction.add(R.id.bottomContainer, fragment, VideoDetailsFragment::class.java.simpleName)
        transaction.show(fragment)
        transaction.commitNowAllowingStateLoss()
    }

    private fun startDownload() {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        if (!GlobalValue.isVipUser()) {
            showVipInfoFragment()
            return
        }
        if (mVideoInfo?.videoType == Constant.VIDEO_VARIETY || mVideoInfo?.videoType == Constant.VIDEO_TELEPLAY) {
            val intent = Intent(context, AnthologyActivity::class.java)
            intent.putExtra("mediaKey", mVideoInfo?.mediaKey)
            intent.putExtra("videoType", mVideoInfo?.videoType)
            intent.putExtra("resolution", mVideoInfo?.resolution)
            intent.putExtra("episodeTitle", mVideoInfo?.episodeTitle)
            intent.putExtra("uniqueId", mVideoInfo?.uniqueID)
            intent.putExtra("lang", mVideoInfo?.lang)
            intent.putExtra("pageAction", 1)
            intent.putExtra("coverImage", mVideoInfo?.coverImgUrl)
            startActivity(intent)
        } else {
            mVideoInfo?.let {
                DownloadUtil.showSelectQuality(requireContext(), mViewModel.mClarityList.value, it)
            }
        }
    }

    /**
     * vip套餐支付页Fragment
     */
    private fun showVipInfoFragment() {
        var vipFragment = parentFragmentManager.findFragmentByTag(VipPayFragment::class.java.simpleName)
        val transaction = parentFragmentManager.beginTransaction()
        if (vipFragment == null) {
            vipFragment = VipPayFragment()
            val bundle = Bundle()
            bundle.putString("pathInfo", VideoPlayVerticalActivity::class.java.simpleName + "/" + mVideoInfo?.mediaKey)
            vipFragment.arguments = bundle
            transaction.add(R.id.bottomContainer, vipFragment, VipPayFragment::class.java.simpleName)
        }
        transaction.show(vipFragment)
        transaction.commitNowAllowingStateLoss()
    }

    private fun setRecommendShort(shortList: MutableList<ShortVideoBean>?) {
        if (page == 1) {
            shortVideoData.clear()
            shortVideoAdapter?.notifyDataSetChanged()
        }
        if (shortVideoAdapter == null) {
            shortVideoRecycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            // 设置垂直间隔
            shortVideoRecycle.addItemDecoration(XLinearBuilder(context).setSpacing(20f).build())
            shortVideoAdapter = object : BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(R.layout.item_short_video, shortVideoData) {

                override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
                    item.coverImgUrl?.let {
                        ImageUtil.loadImage(context, it, holder.getView(R.id.shortVideoImage))
                    }
                    holder.setText(R.id.shortVideoTitle, item.title)
                    holder.setText(R.id.times, item.duration)
                    holder.setText(R.id.shortVideoUserName, item.upperName)
                    holder.setText(R.id.shortVideoPlayCount, NormalUtil.formatPlayCount(item.playCount))
                    holder.setText(
                        R.id.shortVideoUpdateTime,
                        TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd")
                    )
                    holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                        showProgressView()
                        resetView()
                        mViewModel.getVideoInfo(item.mediaKey, videoType = item.videoType, videoTitle = item.title ?: "")
                    }
                }
            }
            shortVideoRecycle.adapter = shortVideoAdapter
        }
        if (shortList.isNullOrEmpty()) {
            if (page == 1) {
                shortVideoTopSpace.isVisible = false
                shortVideoTitle.isVisible = false
                shortVideoMiddleSpace.isVisible = false
                shortVideoRecycle.isVisible = false
            }
            introductionRefresh.finishLoadMoreWithNoMoreData()
        } else {
            shortVideoTopSpace.isVisible = true
            shortVideoTitle.isVisible = true
            shortVideoMiddleSpace.isVisible = true
            shortVideoRecycle.isVisible = true
            shortVideoData.addAll(shortList)
            shortVideoAdapter?.notifyDataSetChanged()
            page += 1
            introductionRefresh.finishLoadMore()
        }
    }

    private fun feedBack() {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val feedUrl = SPUtils.getInstance().getString(Constant.KEY_FEED_BACK)
        if (feedUrl.isNullOrEmpty()) {
            mViewModel.getFeedBackUrl()
        } else {
            startFeedBack(feedUrl)
        }
    }

    private fun startFeedBack(url: String) {
        val resultUrl = Uri.parse(url).buildUpon()
            .appendQueryParameter("name", mVideoInfo?.title)
            .appendQueryParameter("from", "android")
            .appendQueryParameter("videoTitle", mVideoInfo?.episodeTitle)
            .build()
            .toString()
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, resultUrl)
        startActivity(intent)
    }

    /**
     * 获取视频详情信息
     */
    private fun setVideoInfo(videoInfo: VideoDetailsBean) {
        initVideoInfoView(videoInfo)
        if (videoInfo.isLive) {
            introductionRefresh.setEnableRefresh(false)
            introductionRefresh.setEnableLoadMore(false)
        } else {
            initRefreshLayout()
            refreshRelevantAdapter()
            page = 1
            mViewModel.getRecommendShort(mVideoInfo?.mediaKey, mVideoInfo?.title, page, size)
        }
    }

    private fun showBlackTip(userId: Int) {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.blacklist_remove)
        dialog.setMsg(R.string.in_black_list_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.ensure) {
            dialog.dismiss()
            mViewModel.forbidden(userId, isBlackList)
        }
        dialog.show()
    }

    private fun setAdBanner(list: MutableList<AdvertBean>) {
        val banner: Banner<AdvertBean, BannerViewAdapter> = adBanner as Banner<AdvertBean, BannerViewAdapter>
        if (list.isEmpty()) {
            banner.isVisible = false
            adBannerSpace.isVisible = false
            return
        }
        adBannerSpace.isVisible = true
        banner.isVisible = true
        banner.addBannerLifecycleObserver(requireActivity())
        banner.setLoopTime(Constant.DELAY_TIME)
        banner.setIndicator(RectangleIndicator(context), false)
        banner.setBannerRound(SizeUtils.dp2px(2f).toFloat())
        banner.addView(banner.indicator.indicatorView)
        banner.setAdapter(BannerViewAdapter(list, requireContext()))
    }

    /**
     * 设置选集数据
     */
    private fun setAnthologyData(episodes: MutableList<VideoBaseBean>) {
        if (mVideoInfo?.videoType == Constant.VIDEO_SHORT || mVideoInfo?.videoType == Constant.VIDEO_MOVIE || episodes.isEmpty()) {
            // 隐藏选集布局
            anthologyLayout.isVisible = false
            anthologySpace.isVisible = false
            anthologyTitleSpace.isVisible = false
            anthologyRecycle.isVisible = false
            return
        }
        anthologyLayout.isVisible = true
        anthologySpace.isVisible = true
        anthologyTitleSpace.isVisible = true
        anthologyRecycle.isVisible = true
        val beanList = episodes.filter { (it.episodeTitle?.length ?: 0) > 5 }
        isVarietyView = !beanList.isNullOrEmpty()
        anthologyData.clear()
        anthologyData.addAll(episodes)
        anthologySelectPosition = anthologyData.indexOfFirst { it.uniqueID == mVideoInfo?.uniqueID }
        if (anthologyAdapter == null) {
            if (mVideoInfo?.isLive == true) {
                val params = anthologySpace.layoutParams as ConsecutiveScrollerLayout.LayoutParams
                params.height = SizeUtils.dp2px(3f)
                anthologySpace.layoutParams = params
                // 设置布局
                anthologyRecycle.layoutManager = GridLayoutManager(context, 2)
                // 设置垂直间隔
                anthologyRecycle.addItemDecoration(XGridBuilder(context).setHLineSpacing(12f).setVLineSpacing(10f).setIncludeEdge(true).build())
            } else {
                val params = anthologySpace.layoutParams as ConsecutiveScrollerLayout.LayoutParams
                params.height = SizeUtils.dp2px(15f)
                anthologySpace.layoutParams = params
                // 设置布局
                anthologyRecycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                // 设置垂直间隔
                anthologyRecycle.addItemDecoration(XLinearBuilder(context).setSpacing(10f).setTopPadding(24f).build())
            }
            anthologyAdapter = object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(R.layout.item_anthology_recycle, anthologyData) {
                override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                    val textView = holder.getView<TextView>(R.id.anthologyNumb)
                    textView.text = item.episodeTitle
                    textView.maxLines = if (item.isLive) 1 else 2
                    if (item.isLast) {
                        holder.setVisible(R.id.vipImage, true)
                    } else {
                        holder.setVisible(R.id.vipImage, false)
                    }
                    textView.updateLayoutParams<FrameLayout.LayoutParams> {
                        gravity = if (item.isLive) Gravity.CENTER else Gravity.START
                    }
                    if (isVarietyView || item.isLive) {
                        // 综艺
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    } else {
                        // 电视剧
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    }
                    val imageView = holder.getView<SVGAImageView>(R.id.playImage)
                    if (item.maintainStatus && item.isLive) {
                        imageView.isVisible = true
                        textView.isSelected = false
                        if (item.uniqueID == mVideoInfo?.uniqueID) {
                            textView.isEnabled = false
                            holder.itemView.setBackgroundResource(R.drawable.red10_corner_2_bg)
                            imageView.setImageResource(R.mipmap.icon_live_error_selected)
                        } else {
                            textView.isEnabled = true
                            holder.itemView.setBackgroundResource(R.drawable.anthology_item_bg_corner_2)
                            imageView.setImageResource(R.mipmap.icon_live_error_normal)
                        }
                    } else if (item.uniqueID == mVideoInfo?.uniqueID) {
                        holder.itemView.setBackgroundResource(R.drawable.anthology_item_bg_corner_2)
                        textView.isEnabled = true
                        textView.isSelected = true
                        imageView.isVisible = true
                        var animName = "playing_blue.svga"
                        if (item.isLive) {
                            animName = "playing_live.svga"
                        }
                        SVGAParser(context).decodeFromAssets(animName, object : SVGAParser.ParseCompletion {
                            override fun onComplete(videoItem: SVGAVideoEntity) {
                                val svg = SVGADrawable(videoItem)
                                imageView.setImageDrawable(svg)
                                imageView.startAnimation()
                            }

                            override fun onError() {

                            }

                        })
                    } else {
                        holder.itemView.setBackgroundResource(R.drawable.anthology_item_bg_corner_2)
                        textView.isEnabled = true
                        textView.isSelected = false
                        imageView.isVisible = false
                        imageView.stopAnimation()
                    }
                    val params = if (item.isLive) {
                        FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(56f))
                    } else {
                        val temp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, SizeUtils.dp2px(56f))
                        if (holder.absoluteAdapterPosition == 0) {
                            temp.leftMargin = SizeUtils.dp2px(7f)
                            temp.rightMargin = 0
                        } else if (holder.absoluteAdapterPosition == anthologyData.size) {
                            temp.leftMargin = 0
                            temp.rightMargin = SizeUtils.dp2px(7f)
                        } else {
                            temp.leftMargin = 0
                            temp.rightMargin = 0
                        }
                        temp
                    }
                    holder.itemView.layoutParams = params
                    holder.itemView.setOnClickListener {
                        if (item.uniqueID == mVideoInfo?.uniqueID) {
                            return@setOnClickListener
                        }
//                        mVideoInfo?.resetByOtherModel(item)
                        mViewModel.mSelectedVideoBean.value = item
                        notifyItemChanged(anthologySelectPosition)
                        anthologySelectPosition = holder.absoluteAdapterPosition
                        notifyItemChanged(anthologySelectPosition)
                    }
                }
            }
            anthologyRecycle.adapter = anthologyAdapter
        } else {
            anthologyAdapter?.notifyDataSetChanged()
        }
        anthologyRecycle.post {
            setRecyclerPosition()
        }
    }

    /**
     * 屏刷新相关剧集适配器
     */
    private fun refreshRelevantAdapter() {
        val size = relevantData.size
        relevantData.clear()
        relevantAdapter?.notifyItemRangeRemoved(0, size)
        mViewModel.getRecommendVideo(mVideoInfo?.mediaKey)
    }

    /**
     * 从选集页返回刷新播放状态
     */
    private fun refreshAnthologyData(id: Int) {
        anthologySelectPosition = anthologyData.indexOfFirst { it.uniqueID == id }
        val item = anthologyData.find { it.uniqueID == id }
        if (item != null) {
            mVideoInfo?.resetByOtherModel(item)
        }
        setRecyclerPosition()
        anthologyAdapter?.notifyDataSetChanged()
    }

    /**
     * 设置当前播放的选集位置
     */
    private fun setRecyclerPosition() {
        if (anthologySelectPosition < 0 || anthologyData.size <= 0 || anthologySelectPosition >= anthologyData.size) {
            return
        }
        if (mVideoInfo?.isLive == true/* && anthologySelectPosition >= 4*/) {
            val layoutManager = anthologyRecycle.layoutManager as GridLayoutManager
            val view = layoutManager.findViewByPosition(anthologySelectPosition)
            if (!getLocalVisibleRect(view)) {
                introductionScroll.scrollToChild(anthologyRecycle)
            }
        }
        anthologyRecycle?.post {
            anthologyRecycle?.scrollToPosition(anthologySelectPosition)
        }
    }

    private fun getLocalVisibleRect(view: View?): Boolean {
        if (!isSafe() || view == null) return false

        val screenWidth: Int = ScreenUtils.getScreenWidth()
        val screenHeight: Int = ScreenUtils.getScreenHeight()
        val rect = Rect(0, 0, screenWidth, screenHeight)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        view.tag = location[1] //存储y方向的位置
        return view.getLocalVisibleRect(rect)
    }

    /**
     * 获取数据后初始化视频相关布局
     */
    private fun initVideoInfoView(videoInfo: VideoDetailsBean?) {
        val vipLevel = mUpperInfo?.vipLevel ?: 0
        ImageUtil.loadCircleImage(requireContext(), mUpperInfo?.avatar, uploadByImage)
        if (mUpperInfo?.bigV == true || vipLevel > 0) {
            userVip.isVisible = true
            userVip.setImageResource(
                if (mUpperInfo?.bigV == true) R.mipmap.icon_big_v
                else VipGradeImageUtil.getVipImage(vipLevel)
            )
        } else {
            userVip.isVisible = false
        }
        uploadByName.text = mUpperInfo?.nickName

        if (videoInfo?.videoType != Constant.VIDEO_SHORT) {
            date.isVisible = false
            mViewModel.getRating(videoInfo?.mediaKey)
            val stringBuffer = StringBuffer()
            if (!videoInfo?.cidMapper.isNullOrEmpty()) {
                stringBuffer.append(videoInfo?.cidMapper?.replace(",", "·"))
            }
            if (!videoInfo?.regional.isNullOrEmpty()) {
                stringBuffer.append("·" + videoInfo?.regional)
            }
            if (!videoInfo?.lang.isNullOrEmpty()) {
                stringBuffer.append("·" + videoInfo?.lang)
            }
            contentType.text = stringBuffer

            val sb = StringBuffer()
            sb.append(videoInfo?.updateStatus)
            if (!videoInfo?.updateMsg.isNullOrEmpty()) {
                sb.append(" ")
                sb.append(StringUtils.getString(R.string.each, videoInfo?.updateMsg))
            }
            allAnthology.text = sb
        } else {
            contentType.text = videoInfo.contentType
            score.isVisible = false
            rating_logo.isVisible = false
            date.isVisible = true
            if (!videoInfo.publishTime.isNullOrEmpty()) {
                date.text = TimeUtils.date2String(TimesUtils.formatDate(videoInfo.publishTime), "yyyy-MM-dd")
            }
        }
        setFansCount(mUpperInfo?.fansCount ?: 0)
        playCount.text = NormalUtil.formatPlayCount(videoInfo?.playCount ?: 0)
        videoName.text = videoInfo?.title
        initLikeView(mLikeBean)
        initDisLikeView(mDislikeBean)
        initCollectView(mCollectBean)
        setUserStatus(isFocusUpper, isBlackList)
        initShareCountView(videoInfo?.shareCount ?: 0)
    }

    private fun setFansCount(fansCount: Int) {
        this.fansCount.text = StringUtils.getString(R.string.fansCounts, NormalUtil.formatPlayCount(fansCount))
    }

    private fun setLikeState(like: VideoLikeBean, dislike: VideoLikeBean) {
        mLikeBean?.count = like.count
        mLikeBean?.selected = like.selected
        mDislikeBean?.count = dislike.count
        mDislikeBean?.selected = dislike.selected
    }

    private fun setLikeAndDislike(response: JSONObject?) {
        if (response == null) {
            return
        }
        val videoLikeBean: VideoLikeBean = Gson().fromJson(response.optString("like"), object : TypeToken<VideoLikeBean>() {}.type)
        val videoDislikeBean: VideoLikeBean = Gson().fromJson(response.optString("dislike"), object : TypeToken<VideoLikeBean>() {}.type)
        val dislike = VideoActionResultEvent()
        dislike.id = mVideoInfo?.mediaKey ?: ""
        dislike.type = 3
        dislike.selected = videoDislikeBean.selected
        dislike.count = videoDislikeBean.count
        EventBus.getDefault().post(dislike)

        val like = VideoActionResultEvent()
        like.id = mVideoInfo?.mediaKey ?: ""
        like.type = 2
        like.selected = videoLikeBean.selected
        like.count = videoLikeBean.count
        EventBus.getDefault().post(like)

        setLikeState(videoLikeBean, videoDislikeBean)
        initLikeView(videoLikeBean)
        initDisLikeView(videoDislikeBean)
    }

    /**
     * 初始化点赞布局
     */
    private fun initLikeView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoFabulousImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoFabulousCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoFabulousCount.text = resources.getString(R.string.fabulous)
        }
    }

    /**
     * 初始化不喜欢布局
     */
    private fun initDisLikeView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoDebunkImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoDebunkCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoDebunkCount.text = resources.getString(R.string.unlike)
        }
    }

    /**
     * 初始化收藏布局
     */
    private fun initCollectView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoCollectionImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoCollectionCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoCollectionCount.text = resources.getString(R.string.collection)
        }
    }

    /**
     * 初始化分享数量显示布局
     */
    private fun initShareCountView(count: Int) {
        if (count > 0) {
            shareCount.text = NormalUtil.formatPlayCount(count)
        } else {
            shareCount.text = resources.getString(R.string.share)
        }
    }

    /**
     * 设置用户状态
     */
    private fun setUserStatus(isFocus: Boolean, isBlackList: Boolean) {
        if (GlobalValue.userInfoBean?.id == mUpperInfo?.id) {
            followText.isVisible = false
            blackList.isVisible = false
        } else if (isBlackList) {
            followText.isVisible = false
            blackList.isVisible = true
        } else {
            blackList.isVisible = false
            followText.isVisible = true
            followText.isSelected = isFocus
            followText.text = if (isFocus) {
                resources.getString(R.string.followed)
            } else {
                resources.getString(R.string.attention)
            }
        }
    }

    /**
     * 点击分享 popup
     */
    private fun videoDetailShare() {
        if (mVideoInfo == null || mUpperInfo == null) return
        val share = ShareBoard(requireContext(), mUpperInfo!!.id, mVideoInfo!!)
        share.show()
        share.isGoneOtherLayout(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShareCountEvent) {
        initShareCountView(event.count)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        if (mUpperInfo?.id == event.uid) {
            isBlackList = event.status
            setUserStatus(false, event.status)
        }
    }

    private fun reset() {
        showProgressView()
        resetView()
        mViewModel.getVideoInfo(mVideoInfo?.mediaKey, videoType = mVideoInfo?.videoType ?: 0, videoTitle = mVideoInfo?.title)
        mViewModel.getRecommendVideo(mVideoInfo?.mediaKey)
        mViewModel.getRecommendShort(mVideoInfo?.mediaKey, mVideoInfo?.title, page, size)
    }

    private fun resetView() {
        mDislikeBean = null
        mLikeBean = null
        mUpperInfo = null
        mVideoInfo = null
        mRating = null
        mCollectBean = null
        isBlackList = false
        isFocusUpper = false

        page = 1
        relevantData.clear()
        anthologyData.clear()
        shortVideoData.clear()
        relevantAdapter?.notifyDataSetChanged()
        anthologyAdapter?.notifyDataSetChanged()
        shortVideoAdapter?.notifyDataSetChanged()
        anthologyLayout.isVisible = false
        anthologySpace.isVisible = false
        anthologyTitleSpace.isVisible = false
        anthologyRecycle.isVisible = false

        relevantRecycle.isVisible = false
        relevantSpace.isVisible = false
        relevantTitle.isVisible = false

        shortVideoTopSpace.isVisible = false
        shortVideoTitle.isVisible = false
        shortVideoMiddleSpace.isVisible = false
        shortVideoRecycle.isVisible = false
    }

    private fun showProgressView() {
        if (isSafe()) {
            statusView?.showProgress()
        }
    }

    private fun dismissProgressView() {
        if (isSafe()) {
            statusView?.dismissProgress()
        }
    }

    private fun showFailedView(listener: View.OnClickListener) {
        if (isSafe()) {
            statusView?.showFailed(listener)
        }
    }

    /**
     * 设置评分
     */
    private fun setRating(rating: VideoRatingBean?) {
        if (rating == null) {
            return
        }
        mRating = rating
        score.isVisible = true
        score.text = rating.rating
        if (!rating.ratingLogo.isNullOrEmpty()) {
            rating_logo.isVisible = true
            ImageUtil.loadImage(this, rating.ratingLogo, rating_logo)
            score.setTextColor(ColorUtils.getColor(R.color.orange))
            score.setPadding(0, 0, 0, 0)
        } else {
            rating_logo.isVisible = false
            score.setTextColor(ColorUtils.getColor(R.color.word_color_5))
            score.setPadding(SizeUtils.dp2px(6f), 0, 0, 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShareEvent(event: ShareCountEvent) {
        initShareCountView(event.count)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                val videoBaseBean = data?.getSerializableExtra("anthologyBean") as VideoBaseBean?
                if (videoBaseBean != null) {
                    mViewModel.mSelectedVideoBean.value = videoBaseBean
                    refreshAnthologyData(videoBaseBean.uniqueID)
                }
            }
        }
    }

}