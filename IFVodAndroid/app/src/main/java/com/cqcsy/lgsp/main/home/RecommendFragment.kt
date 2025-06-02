package com.cqcsy.lgsp.main.home

import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ItemTitleBean
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.bean.net.HomeNetBean
import com.cqcsy.lgsp.delegate.*
import com.cqcsy.lgsp.delegate.util.HomeDelegateUtils
import com.cqcsy.lgsp.delegate.util.ListWrapper
import com.cqcsy.lgsp.event.*
import com.cqcsy.lgsp.record.RecordActivity
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.views.CustomStaggeredGridLayoutManager
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.MultiTypeAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XStaggeredGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * 首页推荐分类Fragment
 */
class RecommendFragment : RefreshFragment(), View.OnClickListener {
    private val mRecommendAdapter: MultiTypeAdapter = MultiTypeAdapter()
    private val mRecDataList: MutableList<Any> = ArrayList()
    private var isLoadingMore = false

    private var isShowData = true

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onVisible() {
        super.onVisible()
        dispatch(Lifecycle.Event.ON_START)
    }

    override fun onInvisible() {
        super.onInvisible()
        dispatch(Lifecycle.Event.ON_STOP)
    }

    override fun initView() {
        setEnableRefresh(true)
        setEnableLoadMore(true)
        // 设置布局
        val layoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val decoration = XStaggeredGridBuilder(requireContext())
            .setVLineSpacing(12f)
            .setHLineSpacing(10f)
            .setIncludeEdge(true)
            .setIgnoreFullSpan(false).build()
        recyclerView.addItemDecoration(decoration)
        mRecommendAdapter.register(TypeActionDelegate())
        mRecommendAdapter.register(AdvertDelegate())
        mRecommendAdapter.register(ItemTitleDelegate())
        mRecommendAdapter.register(ListWrapper::class.java).to(BannerDelegate(this), WatchingDelegate())
            .withLinker { position, item ->
                if (item.type == Constant.FOLLOWING_TYPE) {
                    1
                } else {
                    0
                }
            }
        mRecommendAdapter.register(MovieModuleBean::class.java)
            .to(ShortDelegate(), VideoDelegate(), VideoFullDelegate())
            .withLinker { position, item ->
                when (item.type) {
                    Constant.POPULAR_TYPE -> 0
                    else -> {
                        if (item.isFull) {
                            2
                        } else {
                            1
                        }
                    }
                }
            }
        mRecommendAdapter.register(RecommendMultiBean::class.java)
            .to(
                RecommendVideoDelegate(),
                RecommendShortDelegate(),
                RecommendDynamicDelegate(),
                RecommendPictureDelegate()
            )
            .withLinker { position, item ->
                item.businessType
            }

        recyclerView.adapter = mRecommendAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(r: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(r, dx, dy)
                val positions = IntArray(layoutManager.spanCount)
                layoutManager.findLastVisibleItemPositions(positions)
                if (!isLoadingMore && dy > 0 && mRecDataList.isNotEmpty() && mRecDataList.size - positions[0] <= 6) {
                    getRecommendedData()
                }
            }
        })
        getHttpData(true)
    }

    override fun initData() {
        emptyLargeTip.text = StringUtils.getString(R.string.searchNoData)
        emptyLittleTip.text = StringUtils.getString(R.string.searchNoDataTips)
    }

    override fun onLogin() {
        getFollowingData()
    }

    override fun onLoginOut() {
        removeFollowing()
    }

    private fun removeFollowing() {
        val position = mRecDataList.indexOfFirst { it is ItemTitleBean && it.type == Constant.FOLLOWING_TYPE }
        if (position == -1) {
            return
        }
        mRecDataList.removeAll { it is ItemTitleBean && it.type == Constant.FOLLOWING_TYPE || it is ListWrapper && it.type == Constant.FOLLOWING_TYPE }
        mRecommendAdapter.notifyItemRangeRemoved(position, 2)
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        isShowData = true
        getHttpData(false)
    }

    /**
     * 获取网络数据
     */
    private fun getHttpData(isShowProcess: Boolean) {
        if (isShowProcess) {
            showProgress()
        }
        val params = HttpParams()
        params.put("IsNewEdition", true)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.post(RequestUrls.HOME_RECOMMEND_URL, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (isShowProcess) {
                    dismissProgress()
                } else {
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (mRecDataList.isEmpty()) {
                        showEmpty()
                    } else {
                        isShowData = false
                    }
                    return
                }
                // 解决左右滑动回来page=2,不显示为你推荐模块
                page = 1
                addView(jsonArray)
                if (GlobalValue.isLogin()) {
                    getFollowingData()
                }
                getRecommendedData()
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (isShowProcess) {
                    dismissProgress()
                } else {
                    finishRefresh()
                }
                isShowData = false
                showFailed(this@RecommendFragment)
            }
        }, params, this)
    }

    /**
     * 下拉加载更多，获取为你推荐数据
     */
    private fun getRecommendedData() {
        if (isLoadingMore) {
            return
        }
        isLoadingMore = true
        val params = HttpParams()
        params.put("page", page)
        params.put("userid", SPUtils.getInstance().getInt(Constant.KEY_LAST_SHORT_UPPER_ID))
        params.put("label", LabelUtil.getAllLabels())
        params.put("slabel", LabelUtil.getAllLabels(Constant.KEY_SHORT_VIDEO_LABELS))
        HttpRequest.post(RequestUrls.RECOMMEND_INDEX, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (response == null || jsonArray == null || jsonArray.length() == 0) {
                    if (mRecDataList.isEmpty() && page == 1) {
                        isShowData = false
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list: List<RecommendMultiBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<RecommendMultiBean>>() {}.type
                )
                var changeCount = list.size
                if (page == 1) {
                    val title = ItemTitleBean(
                        Constant.RECOMMENDED_TYPE,
                        StringUtils.getString(R.string.recommendedForYou),
                        null
                    )
                    mRecDataList.add(title)
                    changeCount++
                }
                mRecDataList.addAll(list)
                recyclerView?.adapter?.notifyItemRangeChanged(
                    mRecDataList.size - list.size + 1,
                    changeCount
                )
                if (list.isNotEmpty()) {
                    finishLoadMore()
                    page += 1
                } else {
                    finishLoadMoreWithNoMoreData()
                }
                isLoadingMore = false
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (page == 1) {
                    finishLoadMoreWithNoMoreData()
                    isShowData = true
                } else {
                    errorLoadMore()
                }
                isLoadingMore = false
            }
        }, params, this)
    }

    /**
     * 获取集合数据的id,用于传参给换一换接口
     */
    private fun getIdString(type: Int): String {
        val list = mRecDataList.filter { it is MovieModuleBean && it.type == type && !it.isFull }
        if (list.isEmpty()) {
            return ""
        }
        val stringBuffer = StringBuffer()
        for (item in list) {
            stringBuffer.append((item as MovieModuleBean).mediaKey + ",")
        }
        return stringBuffer.substring(0, stringBuffer.length - 1)
    }

    /**
     * 点击换一换获取网络数据
     */
    private fun changeHttpData(type: Int, view: ImageView?) {
        ImageUtil.showCircleAnim(view)
        val params = HttpParams()
        params.put("mediaKey", getIdString(type))
        params.put("TitleType", type)
        HttpRequest.post(RequestUrls.HOME_CHANGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                ImageUtil.closeCircleAnim(view)
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    ToastUtils.showLong(R.string.noMoreData)
                    return
                }
                val jsonList: MutableList<MovieModuleBean> =
                    Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<MutableList<MovieModuleBean>>() {}.type
                    )
                resetData(type, jsonList)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ImageUtil.closeCircleAnim(view)
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun resetData(type: Int, data: MutableList<MovieModuleBean>) {
        val position = HomeDelegateUtils.resetTypeData(type, mRecDataList, data)
        if (position == -1) {
            return
        }
        mRecommendAdapter.notifyDataSetChanged()
    }

    /**
     * 数据解析添加View
     */
    private fun addView(jsonArray: JSONArray) {
        mRecDataList.clear()
        mRecommendAdapter.notifyDataSetChanged()
        val responseList: MutableList<HomeNetBean> = Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<HomeNetBean>>() {}.type
        )
        HomeDelegateUtils.addDelegateItem(
            mRecDataList,
            responseList
        ) { if (it is Int) changeTab(it) }
        mRecommendAdapter.items = mRecDataList
        mRecommendAdapter.notifyDataSetChanged()
    }

    private fun getFollowingData() {
        removeFollowing()
        val params = HttpParams()
        params.put("size", 10)
        HttpRequest.post(RequestUrls.FOLLOWING_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list = Gson().fromJson<MutableList<MovieModuleBean>>(
                    jsonArray.toString(), object : TypeToken<MutableList<MovieModuleBean>>() {}.type
                )
                val position = HomeDelegateUtils.addFollowing(mRecDataList, list) {
                    startActivity(
                        Intent(context, RecordActivity::class.java)
                    )
                }
                mRecommendAdapter.notifyItemRangeInserted(position, 2)
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params, this)
    }

    /**
     * 切换首页tab
     */
    private fun changeTab(type: Int) {
        val event = TabChangeEvent(0, type)
        EventBus.getDefault().post(event)
    }

    /**
     * 点击刷新请求数据
     */
    override fun onClick(v: View?) {
        page = 1
        getHttpData(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClearFollowing(event: RecordClearEvent) {
        if (event.type == RecordClearEvent.TYPE_EPISODE) {
            onLoginOut()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if (event.position == R.id.button_home && mIsFragmentVisible && isShowData) {
            recyclerView.scrollToTop(refreshLayout)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFollowEvent(event: VideoActionResultEvent) {
        for ((i, data) in mRecDataList.withIndex()) {
            if (data is RecommendMultiBean && data.userId.toString() == event.id) {
                if (event.action == VideoActionResultEvent.ACTION_ADD) {
                    data.focusStatus = true
                    mRecommendAdapter.notifyItemChanged(i)
                } else if (event.action == VideoActionResultEvent.ACTION_REMOVE) {
                    data.focusStatus = false
                    mRecommendAdapter.notifyItemChanged(i)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshType(event: RefreshTypeEvent) {
        changeHttpData(event.type, event.imageView)
    }
}