package com.cqcsy.lgsp.upper

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.LoadingView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.json.JSONObject

/**
 * up主所有数据
 */
class AllResourceFragment : RefreshFragment() {
    var userId: Int = 0
    private var mResourceData: MutableList<RecommendMultiBean> = ArrayList()

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.setText(R.string.empty_tip)
        refreshLayout?.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f), 0)
        if (arguments?.getInt("userId") != null) {
            userId = arguments?.getInt("userId")!!
        }
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.layoutManager = LinearLayoutManager(context)
        val decoration = DividerItemDecoration(context, LinearLayout.VERTICAL)
        ResourcesCompat.getDrawable(resources, R.drawable.line_divider, null)
            ?.let { decoration.setDrawable(it) }
        recyclerView.addItemDecoration(decoration)
        recyclerView.adapter = AllResourceAdapter(requireActivity(), mResourceData)

        showRefresh()
    }

    private fun loadData() {
        val params = HttpParams()
        params.put("userId", userId)
        params.put("page", page)
        params.put("size", size)
        HttpRequest.get(RequestUrls.UPPER_ALL_RESOURCE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                if (page == 1) {
                    mResourceData.clear()
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
                mResourceData.addAll(list)
                if (page == 1) {
                    recyclerView.adapter?.notifyDataSetChanged()
                } else {
                    recyclerView.adapter?.notifyItemRangeChanged(mResourceData.size - list.size + 1, mResourceData.size)
                }
                if (list.isNullOrEmpty()) {
                    finishLoadMoreWithNoMoreData()
                } else {
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (mResourceData.size == 0) {
                    showFailed {
                        showRefresh()
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

    override fun showEmpty() {
        val view = View.inflate(requireContext(), R.layout.layout_status_empty, null)
        view.findViewById<ImageView>(R.id.image_empty).setImageDrawable(emptyImage.drawable)
        view.findViewById<TextView>(R.id.large_tip).text = emptyLargeTip.text
        view.findViewById<TextView>(R.id.little_tip).text = emptyLittleTip.text
        (recyclerView.adapter as BaseMultiItemQuickAdapter<*, *>).setEmptyView(view)
    }

    override fun showFailed(listener: View.OnClickListener) {
        finishLoadMore()
        finishRefresh()
        if (isSafe()) {
            val view = LoadingView(requireContext())
            view.showFailed(listener)
            (recyclerView.adapter as BaseMultiItemQuickAdapter<*, *>).setEmptyView(view)
        }
    }
}