package com.cqcsy.lgsp.main.home

import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.net.HomeNetBean
import com.cqcsy.lgsp.delegate.BannerDelegate
import com.cqcsy.lgsp.delegate.PageFilterDelegate
import com.cqcsy.lgsp.delegate.VideoDelegate
import com.cqcsy.lgsp.delegate.util.HomeDelegateUtils
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.library.views.CustomStaggeredGridLayoutManager
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.drakeet.multitype.MultiTypeAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XStaggeredGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * 首页电影、剧集分类Fragment
 */
class MovieFragment : RefreshFragment() {
    private val mMovieAdapter: MultiTypeAdapter = MultiTypeAdapter()
    private val mMovieList: MutableList<Any> = ArrayList()

    // 首页导航分类ID
    private var categoryId = ""
    private var categoryName = ""

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

    override fun initData() {
        categoryId = arguments?.getString("categoryId") ?: ""
        categoryName = arguments?.getString("categoryName") ?: ""
        getHttpData(true)
    }

    override fun initView() {
        emptyLargeTip.text = StringUtils.getString(R.string.searchNoData)
        emptyLittleTip.text = StringUtils.getString(R.string.searchNoDataTips)
        // 初始化上下拉刷新
        setEnableRefresh(true)
        setEnableLoadMore(true)
        val layoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val decoration = XStaggeredGridBuilder(requireContext())
            .setVLineSpacing(12f)
            .setHLineSpacing(10f)
            .setIncludeEdge(true)
            .setIgnoreFullSpan(false).build()
        recyclerView.addItemDecoration(decoration)
        recyclerView.layoutManager = layoutManager
        mMovieAdapter.register(BannerDelegate(this))
        mMovieAdapter.register(VideoDelegate())
        mMovieAdapter.register(PageFilterDelegate())
        recyclerView.adapter = mMovieAdapter
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        getHttpData(false)
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getHttpData(false)
    }

    /**
     * 获取网络数据
     */
    private fun getHttpData(isShow: Boolean) {
        if (isShow && page == 1) {
            showProgress()
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        params.put("titleid", categoryId)
        HttpRequest.post(
            RequestUrls.HOME_RELATION_VIDEOS,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (isShow && page == 1) {
                        dismissProgress()
                    } else if (page == 1) {
                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        if (page == 1) {
                            mMovieList.clear()
                            mMovieAdapter.notifyDataSetChanged()
                            showEmpty()
                        } else {
                            finishLoadMoreWithNoMoreData()
                        }
                        return
                    }
                    val loadSize = parseData(jsonArray)
                    if (loadSize > 0) {
                        page += 1
                        finishLoadMore()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page == 1) {
                        finishRefresh()
                        showFailed {
                            getHttpData(true)
                        }
                    } else {
                        errorLoadMore()
                    }
                }
            }, params, this
        )
    }

    /**
     * 数据解析添加View
     * 返回普通视频条数
     */
    private fun parseData(jsonArray: JSONArray): Int {
        if (page == 1) {
            mMovieList.clear()
        }
        val lastSize = mMovieList.size
        val responseList: MutableList<HomeNetBean> = Gson().fromJson(jsonArray.toString(), object : TypeToken<MutableList<HomeNetBean>>() {}.type)
        HomeDelegateUtils.addDelegateItem(mMovieList, responseList, false)
        var pageResponseSize = 0
        val typeList = responseList.filter { it.type.toString() == categoryId }
        typeList.forEach {
            pageResponseSize += it.list?.size ?: 0
        }
        if (page == 1) {
            mMovieAdapter.items = mMovieList
            mMovieAdapter.notifyDataSetChanged()
        } else {
            mMovieAdapter.notifyItemRangeChanged(mMovieList.size - lastSize, pageResponseSize)
        }
        return pageResponseSize
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if (event.position == R.id.button_home && mIsFragmentVisible && mMovieList.isNotEmpty()) {
            recyclerView.scrollToTop(refreshLayout)
        }
    }
}