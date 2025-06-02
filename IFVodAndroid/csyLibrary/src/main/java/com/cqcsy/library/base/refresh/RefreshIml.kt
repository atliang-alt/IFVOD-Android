package com.cqcsy.library.base.refresh

/**
 * 支持下拉刷新页面接口
 */
interface RefreshIml {

    // 下拉刷新回调
    fun onRefresh() {
        resetToNormal()
    }

    // 上拉加载更多回调
    fun onLoadMore() {

    }

    fun initRefresh()

    fun setEnableLoadMore(enable: Boolean) {
    }

    fun setEnableRefresh(enable: Boolean) {
    }

    fun disableRefresh() {
    }

    fun enableRefresh() {
    }

    fun setRefreshView(layout: Int) {
    }

    fun finishRefresh() {
    }

    fun showRefresh() {
    }

    fun finishLoadMore() {
    }

    fun errorLoadMore() {
    }

    fun finishLoadMoreWithNoMoreData(){
    }

    fun resetToNormal(){

    }
}