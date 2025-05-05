package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.DynamicVideoBean
import com.cqcsy.lgsp.preload.PreloadManager2
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.video.player.DynamicVideoManager
import com.cqcsy.lgsp.views.RefreshFooter
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.fragment_dynamic_video_detail.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/22
 *
 *
 */
open class DynamicVideoDetailFragment : BaseFragment() {

    companion object {

        /**
         * 最大离屏数
         */
        const val MAX_OFF_SCREEN_PAGE_LIMIT = 10
        fun newInstance(
            index: Int,
            mediaKey: String?,
            dynamicList: MutableList<DynamicBean>,
            commentId: Int = 0,
            replyId: Int = 0,
            showComment: Boolean = false,
            isFromMineDynamic: Boolean = false,
            openRecommend: Boolean = true
        ): DynamicVideoDetailFragment {
            val args = Bundle()
            args.apply {
                putSerializable("dynamic_list", dynamicList as Serializable)
                putInt("index", index)
                putString("dynamic_id", mediaKey)
                putInt("comment_id", commentId)
                putInt("reply_id", replyId)
                putBoolean("show_comment", showComment)
                putBoolean("from_mine_dynamic", isFromMineDynamic)
                putBoolean("open_recommend", openRecommend)
            }
            val fragment = DynamicVideoDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var currentIndex = 0
    protected var page: Int = 1
    private var commentId: Int = 0
    private var mediaKey: String? = null
    private var replyId: Int = 0
    private var showComment: Boolean = false
    private var isFromMineDynamic: Boolean = false
    protected lateinit var pagerAdapter: DynamicVideoPagerAdapter
    protected lateinit var dynamicList: MutableList<DynamicBean>
    private var openRecommend = true
    private var isRequesting = false
    private var preloadNum = 3
    private var isExitPause = false     // 是否离开页面暂停，此时回来页面需要恢复播放状态，否则不处理
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dynamic_video_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initArguments()
        initRefreshLayout()
        initPager()
        initData()
        PreloadManager2.init()
    }

    private fun initView() {
        val showGuide =
            SPUtils.getInstance().getBoolean(Constant.KEY_FIRST_SWITCH_VIDEO_GUIDE, false)
        first_guide.isVisible = !showGuide
        first_guide.setOnClickListener {
            SPUtils.getInstance().put(Constant.KEY_FIRST_SWITCH_VIDEO_GUIDE, true)
            first_guide.isVisible = false
        }
    }

    open fun initArguments() {
        currentIndex = arguments?.getInt("index", 0) ?: 0
        mediaKey = arguments?.getString("dynamic_id")
        commentId = arguments?.getInt("comment_id", 0) ?: 0
        replyId = arguments?.getInt("reply_id", 0) ?: 0
        showComment = arguments?.getBoolean("show_comment") ?: false
        isFromMineDynamic = arguments?.getBoolean("from_mine_dynamic", false) ?: false
        openRecommend = arguments?.getBoolean("open_recommend", true) ?: true
        dynamicList =
            arguments?.getSerializable("dynamic_list") as? MutableList<DynamicBean>
                ?: mutableListOf()
    }

    private fun initRefreshLayout() {
        refreshLayout.setRefreshFooter(RefreshFooter(requireContext()).apply {
            setFinishDuration(0)
            setProgressResource(R.mipmap.icon_little_progress)
            setArrowResource(R.mipmap.icon_little_progress)
        })
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableAutoLoadMore(false)
        refreshLayout.setEnableScrollContentWhenLoaded(false)
        refreshLayout.setOnLoadMoreListener {
            onLoadMore()
        }
        refreshLayout.setEnableLoadMore(openRecommend)
    }

    private fun initPager() {
        pagerAdapter = DynamicVideoPagerAdapter(
            this,
            dynamicList,
            isFromMineDynamic,
            showComment,
            currentIndex,
            commentId,
            replyId
        )
        viewPager.offscreenPageLimit = if (dynamicList.size > MAX_OFF_SCREEN_PAGE_LIMIT) {
            MAX_OFF_SCREEN_PAGE_LIMIT
        } else {
            dynamicList.size
        }
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            private var currPosition = 0
            private var isReverseScroll = false

            override fun onPageSelected(position: Int) {
                onPageIndex(isReverseScroll, position)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    currPosition = viewPager.currentItem
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position == currPosition) {
                    return
                }
                isReverseScroll = position < currPosition
            }
        })
        viewPager.setCurrentItem(currentIndex, false)
    }

    open fun initData() {
        getRecommendList(jump = false)
    }

    open fun onPageIndex(isReverseScroll: Boolean, position: Int) {
        currentIndex = position
        val fragment = pagerAdapter.getItem(position)
        if (fragment is DynamicVideoFragment) {
            fragment.startPlay()
        }
        val size = pagerAdapter.dataList.size
        if (!isReverseScroll && position >= size - preloadNum) {
            preloadData(false)
        }
        if (isReverseScroll && position == 0) {
            preloadData(true)
        }
    }

    /**
     * 预加载数据
     * @param isReverseScroll 是否反向滑动
     */
    open fun preloadData(isReverseScroll: Boolean) {
        if (!isReverseScroll) {
            getRecommendList(page, false)
        }
    }

    open fun onLoadMore() {
        getRecommendList(page)
    }

    fun remove(mediaKey: String) {
        for ((i, fragment) in pagerAdapter.fragmentList.withIndex()) {
            if (fragment.mediaKey == mediaKey) {
                pagerAdapter.remove(i)
                break
            }
        }
        if (pagerAdapter.fragmentList.isEmpty()) {
            activity?.finish()
        }
    }

    /**
     * 获取动态小视频推荐列表
     */
    private fun getRecommendList(page: Int = 1, jump: Boolean = true) {
        if (isRequesting) {
            return
        }
        isRequesting = true
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        param.put("page", page)
        param.put("label", LabelUtil.getAllLabels())
        param.put("videoType", 3)
        HttpRequest.get(RequestUrls.DYNAMIC_VIDEO_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                val list: MutableList<DynamicVideoBean>? = Gson().fromJson(
                    jsonArray?.toString(),
                    object : TypeToken<MutableList<DynamicVideoBean>>() {}.type
                )
                if (!list.isNullOrEmpty()) {
                    this@DynamicVideoDetailFragment.page++
                    val dynamicList = mutableListOf<DynamicBean>()
                    for ((i, bean) in list.withIndex()) {
                        val mediaUrl = bean.mediaUrl
                        val mediaKey = bean.mediaKey
                        if (!mediaUrl.isNullOrEmpty() && i != currentIndex && !mediaKey.isNullOrEmpty()) {
                            PreloadManager2.addPreloadTask(mediaKey, mediaUrl)
                        }
                        val dynamicBean = DynamicBean()
                        dynamicBean.copy(bean)
                        dynamicList.add(dynamicBean)
                    }
                    refreshLayout.finishLoadMore()
                    pagerAdapter.addData(dynamicList)
                    if (jump) {
                        viewPager.currentItem = pagerAdapter.dataList.size - dynamicList.size
                    }
                } else {
                    refreshLayout.finishLoadMoreWithNoMoreData()
                }
                isRequesting = false
            }

            override fun onError(response: String?, errorMsg: String?) {
                isRequesting = false
                refreshLayout.finishLoadMore(false)
            }
        }, param, this)
    }

    override fun onVisible() {
        super.onVisible()
        PreloadManager2.resumeAllPreload()
        if (isExitPause) {
            DynamicVideoManager.instance(requireActivity()).onResume()
        }
        isExitPause = false
    }

    override fun onInvisible() {
        super.onInvisible()
        PreloadManager2.pauseAllPreload()
        if (DynamicVideoManager.instance(requireActivity()).isPlaying) {
            isExitPause = true
        }
        DynamicVideoManager.instance(requireActivity()).onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DynamicVideoManager.instance(requireActivity()).releaseAllVideos()
        PreloadManager2.release()
    }
}