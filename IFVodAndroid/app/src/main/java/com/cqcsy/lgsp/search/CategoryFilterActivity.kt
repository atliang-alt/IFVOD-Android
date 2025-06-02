package com.cqcsy.lgsp.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.CategoryFilterAdapter
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.CategoryBean
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.bean.net.CategoryNetBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.views.widget.HorizontalFilterView
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.views.LoadingView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_fliter_category.*
import org.json.JSONObject

/**
 * 分类筛选页面 剧集的
 */
class CategoryFilterActivity : NormalActivity() {

    var filterHeight = 0

    // 一级导航id
    var categoryId = ""

    // 二级导航Name、id
    var classifyId = "0"
    var classifyName = ""
    var classifyIndex = 0

    // 排序id 默认最新上传
    var sortId = "0"
    var sortName = ""

    // 地区ID 默认全部地区
    var areaId = "0"

    // 语言ID 默认全部语言
    var languageId = "0"

    // 年份ID 默认全部年份
    var yearId = "0"

    // 状态ID 默认全部状态
    var stateId = "0"

    var page = 1

    // 请求返回数据的数量
    private var size = 30

    private var adapter: CategoryFilterAdapter? = null

    private var filterContainer: LinearLayout? = null

    private var footView: LoadingView? = null

    override fun getContainerView(): Int {
        return R.layout.activity_fliter_category
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(intent.getStringExtra("categoryName") ?: "")
        sortId = intent.getStringExtra("sortId") ?: "0"
        sortName = StringUtils.getString(R.string.movieLatestUpload)
        categoryId = intent.getStringExtra("categoryId") ?: ""
        val classifyId = intent.getStringExtra("classifyId") ?: "0"
        classifyIndex = intent.getIntExtra("classifyIndex", 0)
        setClassifyIdByType(classifyIndex, classifyId)
        classifyName = if (classifyIndex == 1) {
            intent.getStringExtra("classifyName") ?: StringUtils.getString(R.string.allClassify)
        } else {
            StringUtils.getString(R.string.allClassify)
        }
        setRightImageVisible(View.VISIBLE)
        initRefresh()
        initView()
        setScroll()
        getSecondHttpData()
        getResultData()
    }

    private fun initRefresh() {
        // 初始化下拉刷新
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableAutoLoadMore(true)
        refreshLayout.setEnableOverScrollBounce(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        refreshLayout.setDisableContentWhenLoading(false)
        refreshLayout.setDisableContentWhenRefresh(false)

        refreshLayout.setOnLoadMoreListener { onLoadMore() }
    }

    private fun setScroll() {
        filterContainer = LinearLayout(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, SizeUtils.dp2px(10f))
        filterContainer!!.layoutParams = params
        filterContainer!!.orientation = LinearLayout.VERTICAL
        adapter?.addHeaderView(filterContainer!!)
        topFilterContent.visibility = View.GONE
        filterResult.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (filterHeight == 0) {
                    topFilterContent.visibility = View.INVISIBLE
                    return
                }
                classifyTags.text = "$sortName · $classifyName"
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val position = layoutManager.findFirstVisibleItemPosition()
                if (position > 0) {
                    topFilterContent.alpha = 1f
                    topFilterContent.visibility = View.VISIBLE
                } else {
                    val firstVisiableChildView = layoutManager.findViewByPosition(position)
                    val itemHeight = firstVisiableChildView!!.height
                    val h = position * itemHeight - firstVisiableChildView.top
                    topFilterContent.alpha = h / filterHeight.toFloat()
                    if (h <= filterHeight / 2) {
                        topFilterContent.visibility = View.INVISIBLE
                    } else {
                        topFilterContent.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun onRightClick(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    fun setFilter(list: MutableList<CategoryBean>?, type: Int) {
        if (!list.isNullOrEmpty()) {
            filterContainer?.let { addFilter(it, list, type) }
        }
    }

    private fun initView() {
        filterResult.isNestedScrollingEnabled = false
        filterResult.layoutManager = GridLayoutManager(this, 3)
        filterResult.addItemDecoration(XGridBuilder(this).setHLineSpacing(10f).setVLineSpacing(10f).setIncludeEdge(true).build())
        adapter = CategoryFilterAdapter()
        footView = LoadingView(this)
        adapter!!.addFooterView(footView!!)
        filterResult.adapter = adapter
        topFilterContent.setOnClickListener {
            filterResult.scrollToTop(null)
            topFilterContent.alpha = 0f
        }
    }

    fun onLoadMore() {
        getResultData()
    }

    private fun addFilter(layout: LinearLayout, list: MutableList<CategoryBean>, type: Int) {
        val layoutParams: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val horizontalFilterView = HorizontalFilterView(this)
        horizontalFilterView.setView(list)
        var selectedIndex = 0
        for (i in list.indices) {
            when (type) {
                // 第一行排序
                0 -> if (sortId == list[i].classifyId) {
                    sortName = list[i].classifyName
                    selectedIndex = i
                }
                // 二级导航分类
                1 -> if (classifyId == list[i].classifyId) {
                    selectedIndex = i
                }
                // 地区
                2 -> if (areaId == list[i].classifyId) {
                    selectedIndex = i
                }
                // 语言
                3 -> if (languageId == list[i].classifyId) {
                    selectedIndex = i
                }
                // 年份
                4 -> if (yearId == list[i].classifyId) {
                    selectedIndex = i
                }
                // 状态
                5 -> if (stateId == list[i].classifyId) {
                    selectedIndex = i
                }
            }
        }
        horizontalFilterView.performClick(list[selectedIndex])
        horizontalFilterView.setOnItemSelectListener(object :
            HorizontalFilterView.OnItemSelectListener {
            override fun onItemSelect(categoryBean: CategoryBean) {
                classifyIndex = categoryBean.index
                setClassifyIdByType(type, categoryBean.classifyId)
                if (type == 0) {
                    sortName = categoryBean.classifyName
                } else if (type == 1) {
                    classifyName = categoryBean.classifyName
                }
                resetToNormal()
                showProgressView()
                getResultData()
            }
        })
        layout.addView(horizontalFilterView, layoutParams)
    }

    private fun setClassifyIdByType(type: Int, classifyIndex: String) {
        when (type) {
            // 第一行排序
            0 -> {
                sortId = classifyIndex
            }
            // 二级导航分类
            1 -> {
                classifyId = classifyIndex
            }
            // 地区
            2 -> areaId = classifyIndex
            // 语言
            3 -> languageId = classifyIndex
            // 年份
            4 -> yearId = classifyIndex
            // 状态
            5 -> stateId = classifyIndex
        }
    }

    fun resetToNormal() {
        refreshLayout.setEnableRefresh(false)
        page = 1
        adapter?.clear()
    }

    /**
     * 获取筛选结果数据
     */
    private fun getResultData() {
        OkGo.getInstance().cancelTag(RequestUrls.FILTER_RESULT)
        val params = HttpParams()
        params.put("titleid", categoryId)
        params.put("ids", appendIds())
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(
            RequestUrls.FILTER_RESULT,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (page == 1) {
                        adapter?.clear()
                        dismissProgressDialog()
                        dismissProgressView()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        if (page == 1) {
                            showEmptyView()
                        } else {
                            refreshLayout.finishLoadMoreWithNoMoreData()
                        }
                        return
                    }
                    val list: List<MovieModuleBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<MovieModuleBean>>() {}.type
                    )
                    adapter?.addData(list)
                    if (list.size >= size) {
                        page += 1
                        refreshLayout.finishLoadMore()
                    } else {
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    refreshLayout.finishLoadMore()
                    dismissProgressDialog()
                    showFailedView {
                        resetToNormal()
                        showProgressView()
                        getResultData()
                    }
                }
            }, params, RequestUrls.FILTER_RESULT
        )
    }

    /**
     * 获取二级导航分类数据
     */
    private fun getSecondHttpData() {
        showProgressDialog()
        val params = HttpParams()
        params.put("SecondaryID", categoryId)
        HttpRequest.post(
            RequestUrls.SECOND_NAVIGATION_BAR,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgressDialog()
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        return
                    }
                    filterHeight = SizeUtils.dp2px(41f) * jsonArray.length() + SizeUtils.dp2px(30f)
                    for (i in 0 until jsonArray.length()) {
                        val categoryNetBean = Gson().fromJson(jsonArray[i].toString(), CategoryNetBean::class.java)
//                        var type = 0
//                        when (categoryNetBean.name) {
//                            resources.getString(R.string.sortTag) -> {
//                                type = 0
//                            }
//                            resources.getString(R.string.secondTag) -> {
//                                type = 1
//                            }
//                            resources.getString(R.string.area) -> {
//                                type = 2
//                            }
//                            resources.getString(R.string.language) -> {
//                                type = 3
//                            }
//                            resources.getString(R.string.year) -> {
//                                type = 4
//                            }
//                            resources.getString(R.string.state) -> {
//                                type = 5
//                            }
//                        }
                        setFilter(categoryNetBean.list, i)
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                    showFailedView {
                        showProgressDialog()
                        getSecondHttpData()
                    }
                }
            }, params, this
        )
    }

    /**
     * 拼接请求参数id
     */
    private fun appendIds(): String {
        return "$sortId,$classifyId,$areaId,$languageId,$yearId,$stateId"
    }

    private fun showProgressView() {
        footView?.showProgress()
    }

    private fun dismissProgressView() {
        footView?.dismissProgress()
    }

    private fun showFailedView(listener: View.OnClickListener) {
        footView?.showFailed(listener)
    }

    private fun showEmptyView() {
        footView?.showEmpty()
    }
}