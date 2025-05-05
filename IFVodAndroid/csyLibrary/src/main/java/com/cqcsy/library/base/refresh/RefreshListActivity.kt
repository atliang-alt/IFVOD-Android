package com.cqcsy.library.base.refresh

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.library.R
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.LoadingRecyclerView
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_base_recyclerview.*
import kotlinx.android.synthetic.main.layout_base_refresh.headerLayout
import kotlinx.android.synthetic.main.layout_status_empty.little_tip
import org.json.JSONArray
import org.json.JSONObject

abstract class RefreshListActivity<T> : RefreshActivity(){
    private var mAdapter: BaseQuickAdapter<T, BaseViewHolder>? = null
    private var mDataList: MutableList<T> = ArrayList()
    private var isGetHttpData = false

    override fun getRefreshChild(): Int {
        return R.layout.layout_base_recyclerview
    }

    override fun onChildAttach() {
        setAdapter()
        setData(isHttpTag())
        val header = addHeaderLayout()
        if (header != null) {
            if (isHeaderPin()) {
                headerLayout.addView(header)
            } else {
                (recyclerView.adapter as BaseQuickAdapter<*, *>).addHeaderView(header)
            }
        }
        recyclerView.itemAnimator = null
    }

    /**
     * 设置数据
     */
    open fun setData(isHttpTag: Boolean) {
        if (isHttpTag()) {
            setEnableLoadMore(true)
            setEnableRefresh(true)
            getHttpData()
        } else {
            setEnableLoadMore(false)
            setEnableRefresh(false)
        }
    }

    /**
     * 传入标识是否请求数据 true:请求网络数据  false:不请求
     * 考虑有的使用本地数据
     */
    open fun isHttpTag(): Boolean {
        return true
    }

    /**
     * 顶部是否固定
     */
    open fun isHeaderPin(): Boolean {
        return true
    }

    /**
     * 添加顶部布局
     */
    open fun addHeaderLayout(): View? {
        return null
    }

    /**
     * 添加recycleview修饰
     */
    open fun addDecoration(recyclerView: RecyclerView) {}

    /**
     * 请求的URl
     */
    abstract fun getUrl(): String

    /**
     * 设置recyclerView布局属性
     */
    open fun getLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(this)
    }

    /**
     * 获取每页数量
     */
    open fun getParamsSize(): Int {
        return size
    }

    /**
     * 请求的参数
     */
    open fun getHttpParams(): HttpParams {
        return HttpParams()
    }

    /**
     * 适配器子布局
     */
    abstract fun getItemLayout(): Int

    /**
     * 解析数据
     */
    abstract fun parsingData(jsonArray: JSONArray): MutableList<T>

    /**
     * 处理适配器子布局逻辑
     */
    abstract fun setItemView(holder: BaseViewHolder, item: T, position: Int)

    /**
     * 点击事件
     */
    open fun onItemClick(position: Int, dataBean: T) {}

    open fun setAdapter() {
        recyclerView.layoutManager = getLayoutManager()
        addDecoration(recyclerView)
        mAdapter = object : BaseQuickAdapter<T, BaseViewHolder>(getItemLayout(), mDataList) {
            override fun convert(holder: BaseViewHolder, item: T) {
                setItemView(holder, item, getItemPosition(item))
            }
        }
        mAdapter?.setOnItemClickListener { _, _, position ->
            onItemClick(position, mDataList[position])
        }
        recyclerView.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        if (isGetHttpData) {
            getHttpData()
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        getHttpData()
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getHttpData()
    }

    /**
     * 获取recyclerView控件
     */
    fun getRecyclerView(): LoadingRecyclerView {
        return recyclerView
    }

    /**
     * 获取全部数据集合
     */
    fun getDataList(): MutableList<T> {
        return mDataList
    }

    /**
     * 获取总量
     */
    fun getCount(): Int {
        return mDataList.size
    }

    /**
     * 刷新页面
     */
    fun refreshView() {
        mAdapter?.notifyDataSetChanged()
    }

    /**
     * 刷新页面
     */
    fun refreshView(position: Int) {
        mAdapter?.notifyItemChanged(position)
    }

    protected fun getHttpData() {
        val url = getUrl()
        if (url.isEmpty()) {
            return
        }
        if (mDataList.isEmpty()) {
            showProgress()
        }
        val params = getHttpParams()
        params.put("page", page)
        if (!params.urlParamsMap.keys.contains("size")) {
            params.put("size", getParamsSize())
        }
        HttpRequest.post(
            url,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (page == 1) {
                        mDataList.clear()
                        dismissProgress()
                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        checkShow(0)
                        return
                    }
                    val list = parsingData(jsonArray)
                    mDataList.addAll(list)
                    checkShow(list.size)
                    page += 1
                    isGetHttpData = false
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (mDataList.isEmpty()) {
                        showFailed { getHttpData() }
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                }
            }, params, this
        )
    }

    private fun checkShow(addSize: Int) {
        if (mDataList.isEmpty()) {
            isGetHttpData = true
            little_tip.visibility = View.GONE
            showEmpty()
        } else {
            if (addSize >= getParamsSize()) {
                finishLoadMore()
            } else {
                finishLoadMoreWithNoMoreData()
            }
            if (addSize > 0) {
                mAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun clearAll() {
        mDataList.clear()
        refreshView()
        showEmpty()
    }

    fun removeData(data: MutableList<*>?) {
        if (data != null) {
            mDataList.removeAll(data)
            refreshView()
            if (mDataList.isEmpty()) {
                showEmpty()
            }
        }
    }
}