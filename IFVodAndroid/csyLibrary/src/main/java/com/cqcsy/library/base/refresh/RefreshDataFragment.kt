package com.cqcsy.library.base.refresh

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.library.R
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.LoadingRecyclerView
import com.cqcsy.library.views.LoadingView
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_base_recyclerview.*
import kotlinx.android.synthetic.main.layout_base_refresh.headerLayout
import org.json.JSONArray
import org.json.JSONObject

/**
 * 列表页实现分页加载
 */
abstract class RefreshDataFragment<T> : RefreshFragment() {
    var mAdapter: BaseQuickAdapter<T, BaseViewHolder>? = null
    private var mDataList: MutableList<T> = ArrayList()
    lateinit var refreshRecyclerView: LoadingRecyclerView

    override fun getRefreshChild(): Int {
        return R.layout.layout_base_recyclerview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setData(isHttpTag())
        val header = addPinHeaderLayout()
        if (header != null) {
            headerLayout.addView(header)
        }
        refreshRecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.itemAnimator = null
    }

    fun isFirstPage(): Boolean {
        return page == 1
    }

    /**
     * 设置数据
     */
    open fun setData(isHttpTag: Boolean) {
        if (isHttpTag()) {
            setEnableLoadMore(true)
            setEnableRefresh(true)
            showRefresh()
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
     * 添加顶部固定布局
     */
    open fun addPinHeaderLayout(): View? {
        return null
    }

    /**
     * 添加顶部跟随滑动布局
     */
    fun addScrollHeaderLayout(view: View) {
        mAdapter?.addHeaderView(view)
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
        return LinearLayoutManager(context)
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
    open fun getParams(): HttpParams {
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
    open fun onItemClick(position: Int, dataBean: T, holder: BaseViewHolder) {}

    /**
     * 数据加载完成
     */
    open fun onLoadFinish() {}

    /**
     * 数据为空调用
     */
    open fun onDataEmpty() {

    }

    open fun setAdapter() {
        recyclerView.layoutManager = getLayoutManager()
        addDecoration(recyclerView)
        mAdapter = object : BaseQuickAdapter<T, BaseViewHolder>(getItemLayout(), mDataList) {
            override fun convert(holder: BaseViewHolder, item: T) {
                setItemView(holder, item, getItemPosition(item))
                holder.itemView.setOnClickListener {
                    onItemClick(holder.adapterPosition, item, holder)
                }
            }
        }

        recyclerView.adapter = mAdapter
    }

    override fun onRefresh() {
        super.onRefresh()
        OkGo.getInstance().cancelTag(this)
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

    private fun getHttpData() {
        val url = getUrl()
        if (url.isEmpty()) {
            return
        }
//        if (mDataList.isEmpty() && mAdapter?.hasEmptyView() == false) {
//            showProgress()
//        }
        val params = getParams()
        params.put("page", page)
        if (!params.urlParamsMap.keys.contains("size")) {
            params.put("size", getParamsSize())
        }
        HttpRequest.get(
            url,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (page == 1) {
                        mDataList.clear()
                        mAdapter?.notifyDataSetChanged()
                        dismissProgress()
//                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (response == null || jsonArray == null || jsonArray.length() == 0) {
                        checkShow(page, 0)
                        return
                    }
                    val list = parsingData(jsonArray)
                    mDataList.addAll(list)
                    checkShow(page, list.size)
                    onLoadFinish()
                    page += 1
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page == 1) {
                        finishRefresh()
                        showFailed {
                            showRefresh()
                        }
                    } else {
                        errorLoadMore()
                    }
                    onLoadFinish()
                }
            }, params, this
        )
    }

    open fun checkShow(page: Int, addSize: Int) {
        if (page == 1) {
            finishRefresh()
        } else {
            // 让列表可以一直刷，没有数据就表示没有更多数据了
            if (addSize == 0) {
                finishLoadMoreWithNoMoreData()
            } else {
                finishLoadMore()
            }
        }
        if (mDataList.isEmpty()) {
            onDataEmpty()
            showEmpty()
        } else if (addSize > 0) {
            mAdapter?.notifyDataSetChanged()
        }
    }

    fun clearAll() {
        mDataList.clear()
        refreshView()
        showEmpty()
        onDataEmpty()
    }

    fun removeData(data: MutableList<*>?) {
        if (data != null) {
            mDataList.removeAll(data)
            refreshView()
            if (mDataList.isEmpty()) {
                showEmpty()
                onDataEmpty()
            }
        }
    }

    override fun showEmpty() {
        if (isSafe()) {
            val view = View.inflate(requireContext(), R.layout.layout_status_empty, null)
            view.findViewById<ImageView>(R.id.image_empty).setImageDrawable(emptyImage.drawable)
            view.findViewById<TextView>(R.id.large_tip).text = emptyLargeTip.text
            view.findViewById<TextView>(R.id.little_tip).text = emptyLittleTip.text
            view.setOnClickListener {
                if (isEnableClickLoading()) {
                    showProgress()
                    onRefresh()
                }
            }
            mAdapter?.setEmptyView(view)
        }
    }

    override fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            val view = LoadingView(requireContext())
            view.showFailed(listener)
            mAdapter?.setEmptyView(view)
        }
    }
}