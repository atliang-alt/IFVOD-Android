package com.cqcsy.library.base.refresh

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.views.LoadingRecyclerView
import com.cqcsy.library.views.RefreshLayout
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import kotlinx.android.synthetic.main.layout_status_empty.emptyContainer

/**
 * 下拉刷新fragment基类
 */
abstract class RefreshFragment : NormalFragment(), RefreshIml {
    private val MAX_STOP_TIME_RESTART = 2 * 60 * 60 * 1000    // 后台最大时间2小时，超过时间就发送刷新数据事件
    var refreshLayout: RefreshLayout? = null
    protected var page = 1
    protected val size = 30

    abstract fun getRefreshChild(): Int

    override fun getContainerView(): Int {
        return R.layout.layout_base_refresh
    }

    override fun onViewCreate(view: View) {
        val layout = getRefreshChild()
        if (layout > 0) {
            val container: SmartRefreshLayout = view.findViewById(R.id.refreshLayout)
            LayoutInflater.from(context).inflate(layout, container)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout = view.findViewById(R.id.refreshLayout)
        initRefresh()
    }

    override fun initRefresh() {
        refreshLayout?.setEnableRefresh(true)
        refreshLayout?.setEnableLoadMore(true)
        refreshLayout?.setEnableAutoLoadMore(true)
        refreshLayout?.setEnableOverScrollBounce(true)
        refreshLayout?.setEnableLoadMoreWhenContentNotFull(false)

        refreshLayout?.setDisableContentWhenLoading(false)
        refreshLayout?.setDisableContentWhenRefresh(false)

        refreshLayout?.setOnRefreshListener { onRefresh() }
        refreshLayout?.setOnLoadMoreListener { onLoadMore() }
    }

    override fun setEnableLoadMore(enable: Boolean) {
        refreshLayout?.setEnableLoadMore(enable)
    }

    override fun setEnableRefresh(enable: Boolean) {
        refreshLayout?.setEnableRefresh(enable)
    }

    override fun disableRefresh() {
        refreshLayout?.setEnableRefresh(false)
        refreshLayout?.setEnableLoadMore(false)
        refreshLayout?.finishLoadMore()
        refreshLayout?.finishRefresh()
    }

    override fun enableRefresh() {
        refreshLayout?.setEnableRefresh(true)
        refreshLayout?.setEnableLoadMore(true)
    }

    override fun setRefreshView(layout: Int) {
        LayoutInflater.from(context).inflate(layout, refreshLayout)
    }

    override fun finishRefresh() {
        if (refreshLayout?.state != RefreshState.Refreshing) {
            return
        }
        refreshLayout?.lastRefreshTime = System.currentTimeMillis()
        refreshLayout?.finishRefresh()
    }

    override fun showRefresh() {
        refreshLayout?.autoRefresh(100, 50, 0f, false)
    }

    override fun finishLoadMore() {
        refreshLayout?.lastRefreshTime = System.currentTimeMillis()
        refreshLayout?.finishLoadMore()
    }

    override fun errorLoadMore() {
        refreshLayout?.finishLoadMore(false)
    }

    override fun finishLoadMoreWithNoMoreData() {
        refreshLayout?.lastRefreshTime = System.currentTimeMillis()
        refreshLayout?.finishLoadMoreWithNoMoreData()
    }

    override fun resetToNormal() {
        refreshLayout?.resetNoMoreData()
        enableRefresh()
    }

    override fun showEmpty() {
        super.showEmpty()
        emptyContainer.setOnClickListener {
            if (isEnableClickLoading()) {
                showProgress()
                onRefresh()
            }
        }
    }

    open fun isEnableClickLoading(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        resetLoading()
        if (refreshLayout?.lastRefreshTime != 0L && System.currentTimeMillis() - refreshLayout?.lastRefreshTime!! > MAX_STOP_TIME_RESTART && isSafe() && mIsFragmentVisible) {
            refresh()
        }
    }

    override fun onVisible() {
        super.onVisible()
        if (isSafe()) {
            resetLoading()
            if (refreshLayout?.lastRefreshTime != 0L && System.currentTimeMillis() - refreshLayout?.lastRefreshTime!! > MAX_STOP_TIME_RESTART) {
                refresh()
            }
        }
    }

    private fun refresh() {
        refreshLayout?.scrollTo(0, 0)
        val child = refreshLayout?.getChildAt(0)
        if (child is LinearLayout && child.childCount > 0 && child.getChildAt(0) is LoadingRecyclerView) {
            (child.getChildAt(0) as LoadingRecyclerView).scrollFastToTop()
        }
        Handler().postDelayed({
            showRefresh()
        }, 500)
    }

    fun setRefreshPadding(left: Int, top: Int, right: Int, bottom: Int) {
        refreshLayout?.setPadding(left, top, right, bottom)
    }

    private fun resetLoading() {
        if (refreshLayout?.state == RefreshState.Refreshing) {
            refreshLayout?.finishRefresh(true)
        } else if (refreshLayout?.state == RefreshState.Loading) {
            refreshLayout?.finishLoadMore(true)
        }
    }

}