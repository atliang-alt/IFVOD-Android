package com.cqcsy.lgsp.main.find

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.VideoListItemHolder
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.ListToFullEvent
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.BaseUrl
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.LoadingRecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 发现-关注：有关注用户数据列表
 * 发现-推荐
 */
class AttentionDataListFragment : RefreshFragment() {
    private var mAttentionData: MutableList<RecommendMultiBean> = ArrayList()
    private var navigation: NavigationBarBean? = null
    private lateinit var attentionListAdapter: AttentionListAdapter
    private var isPausedByOnPause = false
    private var isToFull = false

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation = arguments?.getSerializable("navigation") as NavigationBarBean?
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(XLinearBuilder(view.context).setSpacing(10f).build())
        attentionListAdapter = AttentionListAdapter(requireActivity(), mAttentionData)
        recyclerView.adapter = attentionListAdapter
        recyclerView.setScrollListener(object : LoadingRecyclerView.OnScrollerListener {
            override fun onScroll(dx: Int, dy: Int) {
                val player = VideoListItemHolder.getCurrentPlayer() ?: return
                if (dy != 0) {
                    val position = if (player.tag is Int) player.tag.toString().toInt() else 0
                    val range = visibleItemRange()
                    if (position < range[0] || position > range[1]) {
                        VideoListItemHolder.stopPlay()
                    }
                }
            }

            override fun onScrollStop() {
                val player = VideoListItemHolder.getCurrentPlayer() ?: return
                val position = if (player.tag is Int) player.tag.toString().toInt() else 0
                val range = visibleItemRange()
                if (position < range[0] || position > range[1]) {
                    VideoListItemHolder.stopPlay()
                }
            }

        })
    }

    override fun onLazyAfterView() {
        super.onLazyAfterView()
        loadData()
    }

    private fun isEnableAttentionRefresh(): Boolean {
        return navigation?.styleType == FindType.TYPE_ATTENTION && mIsFragmentVisible && GlobalValue.userInfoBean?.newWorks == true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)

    }

    private fun visibleItemRange(): Array<Int> {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        return arrayOf(
            layoutManager.findFirstVisibleItemPosition(),
            layoutManager.findLastVisibleItemPosition()
        )
    }


    override fun onResume() {
        super.onResume()
        startPlay()
    }

    override fun onPause() {
        super.onPause()
        pausePlay()
    }

    private fun startPlay() {
        val player = VideoListItemHolder.getCurrentPlayer()
        if (player != null && player.isInPlayingState && isPausedByOnPause && !isToFull) {
            Handler().postDelayed({
                player.currentPlayer.startAfterPrepared()
            }, 500)
        }
        isPausedByOnPause = false
        isToFull = false
    }

    private fun pausePlay() {
        val player = VideoListItemHolder.getCurrentPlayer()
        if (!isToFull && player != null && player.currentPlayer.isPlaying) {
            player.onVideoPause()
            isPausedByOnPause = true
        }
    }

    override fun onVisible() {
        super.onVisible()
        if (isEnableAttentionRefresh()) {
            recyclerView.scrollToTop(refreshLayout)
        } else {
            startPlay()
        }
    }

    override fun onInvisible() {
        super.onInvisible()
        pausePlay()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusEvent(event: VideoActionResultEvent) {
        when (event.type) {
            1 -> {
                when (event.action) {
                    VideoActionResultEvent.ACTION_ADD,
                    VideoActionResultEvent.ACTION_REMOVE -> {
                        if (navigation?.styleType == FindType.TYPE_RECOMMEND
                            || navigation?.styleType == FindType.TYPE_DYNAMIC_PICTURE
                            || navigation?.styleType == FindType.TYPE_ATTENTION
                        ) {
                            refreshItems(event)
                        } else {
                            onRefresh()
                        }
                    }
                }
            }

            2 -> {
                //点赞
                if (event.isCommentLike) {
                    return
                }
                for ((i, data) in attentionListAdapter.data.withIndex()) {
                    if (data.uniqueID.toString() == event.id) {
                        data.likeCount = event.count
                        data.like = event.selected
                        attentionListAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }

            4 -> {
                //收藏
                for ((i, data) in attentionListAdapter.data.withIndex()) {
                    if (data.mediaKey == event.id) {
                        data.favorites = VideoLikeBean().apply {
                            selected = event.selected
                        }
                        attentionListAdapter.notifyItemChanged(i)
                        break
                    }
                }
            }

            else -> {}
        }
    }

    private fun refreshItems(event: VideoActionResultEvent) {
        if (recyclerView.adapter == null || recyclerView.layoutManager == null) {
            return
        }
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val adapter = recyclerView.adapter as AttentionListAdapter
        for ((index, bean) in mAttentionData.withIndex()) {
            if (bean.userId.toString() == event.id) {
                bean.focusStatus = event.action == VideoActionResultEvent.ACTION_ADD
                val btnAttention = layoutManager.findViewByPosition(index)
                    ?.findViewById<Button>(R.id.btn_attention)
                if (btnAttention != null) {
                    btnAttention.isSelected = bean.focusStatus
                    if (btnAttention.isSelected) {
                        btnAttention.setText(R.string.followed)
                    } else {
                        btnAttention.setText(R.string.attention)
                    }
                } else {
                    adapter.notifyItemChanged(index)
                }
            }
        }
    }

    private fun loadData() {
        if (mAttentionData.size == 0) {
            showProgress()
        }
        GSYVideoManager.instance().stop()
        val params = HttpParams()
        val requestUrl = BaseUrl.BASE_URL + navigation?.url
        params.put("categoryId", navigation?.categoryId)
        params.put("label", LabelUtil.getAllLabels())
        params.put("slabel", LabelUtil.getAllLabels(Constant.KEY_SHORT_VIDEO_LABELS))
        params.put("userid", SPUtils.getInstance().getInt(Constant.KEY_LAST_SHORT_UPPER_ID))
        params.put("page", page)
        HttpRequest.get(requestUrl, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                if (page == 1) {
                    mAttentionData.clear()
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (page == 1) {
                        showEmpty()
                    } else {
                        page--
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list: List<RecommendMultiBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<RecommendMultiBean>>() {}.type
                )
                mAttentionData.addAll(list)
                if (page == 1) {
                    recyclerView.adapter?.notifyDataSetChanged()
                } else {
                    recyclerView.adapter?.notifyItemRangeChanged(
                        mAttentionData.size - list.size + 1,
                        mAttentionData.size
                    )
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                page--
                if (mAttentionData.size == 0) {
                    showFailed {
                        loadData()
                    }
                } else {
                    errorLoadMore()
                }
            }
        }, params, this)
    }

    override fun onRefresh() {
        page = 1
        loadData()
    }

    override fun onLoadMore() {
        page++
        loadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if (event.position == R.id.button_find && mIsFragmentVisible && mAttentionData.isNotEmpty()) {
            recyclerView.scrollToTop(refreshLayout)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchRecord(record: WatchRecordBean) {
        for ((index, bean) in mAttentionData.withIndex()) {
            if (bean.mediaKey == record.mediaKey) {
                bean.watchingProgress = record.watchTime.toLong()
                (recyclerView.adapter as AttentionListAdapter).notifyItemChanged(index)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for ((i, data) in attentionListAdapter.data.withIndex()) {
            if (data.userId == event.uid) {
                //不管是拉黑还是取消拉黑，关注状态都会被重置
                data.focusStatus = false
                data.isBlackList = event.status
                attentionListAdapter.notifyItemChanged(i)
            }
        }
    }

    override fun onLogin() {
        super.onLogin()
        onRefresh()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onListToFull(event: ListToFullEvent) {
        isToFull = true
    }

}