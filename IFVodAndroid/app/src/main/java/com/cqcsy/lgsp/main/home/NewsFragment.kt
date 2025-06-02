package com.cqcsy.lgsp.main.home

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ItemTitleBean
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.bean.net.HomeNetBean
import com.cqcsy.lgsp.delegate.*
import com.cqcsy.lgsp.delegate.util.HomeDelegateUtils
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.library.views.CustomStaggeredGridLayoutManager
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.BaseUrl
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
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
 * 首页分类新闻、娱乐、华人等小视频类型的Fragment
 * 显示样式为模块集合类型
 */
class NewsFragment : RefreshFragment() {
    private val mShortAdapter: MultiTypeAdapter = MultiTypeAdapter()
    private val mShortList: MutableList<Any> = ArrayList()

    // 导航分类
    private var navigation: NavigationBarBean? = null

    private var isShowData = true
    private var isFromFind = false

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun initData() {
        navigation = arguments?.getSerializable("navigation") as NavigationBarBean?
        isFromFind = arguments?.getBoolean("isFromFind", false) ?: false
        emptyLargeTip.text = StringUtils.getString(R.string.searchNoData)
        emptyLittleTip.text = StringUtils.getString(R.string.searchNoDataTips)
    }

    override fun onVisible() {
        super.onVisible()
        dispatch(Lifecycle.Event.ON_START)
    }

    override fun onInvisible() {
        super.onInvisible()
        dispatch(Lifecycle.Event.ON_STOP)
    }

    private fun enableLoadMore(): Boolean {
        return navigation?.styleType != Constant.VIDEO_LIVE && navigation?.styleType != Constant.VIDEO_TV
    }

    override fun initView() {
        // 初始化上下拉刷新
        setEnableLoadMore(enableLoadMore())
        val layoutManager = CustomStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        val decoration = XStaggeredGridBuilder(requireContext())
            .setVLineSpacing(12f)
            .setHLineSpacing(10f)
            .setIncludeEdge(true)
            .setIgnoreFullSpan(false).build()
        recyclerView.addItemDecoration(decoration)
        recyclerView.layoutManager = layoutManager
        mShortAdapter.register(BannerDelegate(this))
        mShortAdapter.register(ItemTitleDelegate())
        mShortAdapter.register(ShortDelegate())
        mShortAdapter.register(FooterDelegate())
        recyclerView.adapter = mShortAdapter

        getHttpData(true)
    }

    override fun resetToNormal() {
        super.resetToNormal()
        setEnableLoadMore(enableLoadMore())
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        getHttpData(false)
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getRecommendedData()
    }

    /**
     * 获取网络数据
     */
    private fun getHttpData(isShowProcess: Boolean) {
        if (isShowProcess) {
            showProgress()
        }
        val params = HttpParams()
        if (isFromFind) {
            params.put("categoryId", navigation?.categoryId)
        } else {
            params.put("titleid", navigation?.categoryId)
        }
        params.put("slabel", LabelUtil.getAllLabels(Constant.KEY_SHORT_VIDEO_LABELS))
        params.put("userid", SPUtils.getInstance().getInt(Constant.KEY_LAST_SHORT_UPPER_ID))
        HttpRequest.post(if (isFromFind) BaseUrl.BASE_URL + navigation?.url else RequestUrls.HOME_RELATION_VIDEOS,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (isShowProcess) {
                        dismissProgress()
                    } else {
                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        isShowData = false
                        mShortList.clear()
                        mShortAdapter.notifyDataSetChanged()
                        showEmpty()
                        return
                    }
                    parseData(jsonArray)
                    page = 1
                    getRecommendedData()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    finishRefresh()
                    isShowData = false
                    showFailed {
                        getHttpData(true)
                    }
                }
            }, params, this
        )
    }

    /**
     * 数据解析添加View
     */
    private fun parseData(jsonArray: JSONArray) {
        mShortList.clear()
        val responseList: MutableList<HomeNetBean> = Gson().fromJson(jsonArray.toString(), object : TypeToken<MutableList<HomeNetBean>>() {}.type)
        HomeDelegateUtils.addDelegateItem(mShortList, responseList, true) {
            startSecondList(it.toString())
        }
        val lastList = responseList.last().list
        if (!lastList.isNullOrEmpty()) {
            val remark = lastList.last().remark
            if (!remark.isNullOrEmpty()) {
                mShortList.add(remark)
            }
        }
        mShortAdapter.items = mShortList
//        mShortAdapter.notifyItemRangeChanged(0, mShortList.size)
        mShortAdapter.notifyDataSetChanged()
    }

    /**
     * 下拉加载更多，获取为你推荐数据
     */
    private fun getRecommendedData() {
        if (!enableLoadMore()) {
            return
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("userid", SPUtils.getInstance().getInt(Constant.KEY_LAST_SHORT_UPPER_ID))
        params.put("titleid", navigation?.categoryId)
        params.put("slabel", LabelUtil.getAllLabels(Constant.KEY_SHORT_VIDEO_LABELS))
        HttpRequest.post(
            RequestUrls.SHORT_VIDEO_RECOMMEND,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    val jsonArray = response?.optJSONArray("list")
                    if (response == null || jsonArray == null || jsonArray.length() == 0) {
                        if (mShortList.size == 0 && page == 1) {
                            isShowData = false
                            showEmpty()
                        } else {
                            finishLoadMoreWithNoMoreData()
                        }
                        return
                    }
                    val list: List<MovieModuleBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<MovieModuleBean>>() {}.type
                    )
                    if (page == 1) {
                        val itemTitleBean = ItemTitleBean(navigation?.categoryId ?: 0, StringUtils.getString(R.string.recommendedForYou), null)
                        mShortList.add(itemTitleBean)
                    }
                    val position = mShortList.size
                    mShortList.addAll(list)
                    val insertSize = if (page == 1) {
                        list.size + 1
                    } else {
                        list.size
                    }
                    mShortAdapter.notifyItemRangeInserted(position, insertSize)
                    if (list.isNotEmpty()) {
                        finishLoadMore()
                        page += 1
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page == 1) {
                        finishLoadMoreWithNoMoreData()
                        isShowData = true
                    } else {
                        errorLoadMore()
                    }
                }
            }, params, this
        )
    }

    /**
     * 跳转二级标题页
     * titleId: 一级ID
     * subId: 二级ID
     * subTitle: 二级标题
     */
    private fun startSecondList(subTitle: String) {
        val titleList = mShortList.filter { it is ItemTitleBean && it.itemName == subTitle }
        if (titleList.isNotEmpty() && titleList.size == 1) {
            val subId = (titleList[0] as ItemTitleBean).type
            val intent = Intent(context, NewsSecondActivity::class.java)
            intent.putExtra("titleId", navigation?.categoryId)
            intent.putExtra("subTitle", subTitle)
            intent.putExtra("subId", subId.toString())
            startActivity(intent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if ((event.position == R.id.button_home || event.position == R.id.button_find) && mIsFragmentVisible && isShowData) {
            recyclerView.scrollToTop(refreshLayout)
        }
    }
}