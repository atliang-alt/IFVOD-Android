package com.cqcsy.library.base.refresh

import android.os.Bundle
import android.view.LayoutInflater
import com.cqcsy.library.R
import com.cqcsy.library.base.NormalActivity
import kotlinx.android.synthetic.main.layout_base_refresh.refreshLayout
import kotlinx.android.synthetic.main.layout_status_empty.emptyContainer

/**
 * 下拉刷新activity基类
 */

abstract class RefreshActivity : NormalActivity(),
    RefreshIml {
    protected var page = 1
    protected val size = 30

    abstract fun getRefreshChild(): Int
    // 子view加载完成
    abstract fun onChildAttach()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRefresh()
        val childLayout = getRefreshChild()
        if (childLayout > 0) {
            LayoutInflater.from(this).inflate(childLayout, refreshLayout)
        }
        onChildAttach()
    }

    override fun getContainerView(): Int {
        return R.layout.layout_base_refresh
    }

    override fun initRefresh() {
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setEnableAutoLoadMore(true)
        refreshLayout.setEnableOverScrollBounce(true)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)

        refreshLayout.setDisableContentWhenLoading(false)
        refreshLayout.setDisableContentWhenRefresh(false)

        refreshLayout.setOnRefreshListener { onRefresh() }
        refreshLayout.setOnLoadMoreListener { onLoadMore() }
    }

    override fun setEnableLoadMore(enable: Boolean) {
        refreshLayout.setEnableLoadMore(enable)
    }

    override fun setEnableRefresh(enable: Boolean) {
        refreshLayout.setEnableRefresh(enable)
    }

    override fun finishLoadMoreWithNoMoreData() {
        refreshLayout.finishLoadMoreWithNoMoreData()
    }

    override fun disableRefresh() {
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()
    }

    override fun enableRefresh() {
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(true)
    }

    override fun setRefreshView(layout: Int) {
        refreshLayout.addView(LayoutInflater.from(this).inflate(layout, null))
    }

    override fun finishRefresh() {
        refreshLayout.finishRefresh()
    }

    override fun showRefresh() {
        refreshLayout.autoRefresh(100, 50, 0f, false)
    }

    override fun finishLoadMore() {
        refreshLayout.finishLoadMore()
    }

    override fun resetToNormal() {
        refreshLayout.resetNoMoreData()
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
}