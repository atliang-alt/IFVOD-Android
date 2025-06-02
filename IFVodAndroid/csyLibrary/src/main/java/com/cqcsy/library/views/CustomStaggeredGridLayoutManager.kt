package com.cqcsy.library.views

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 ** 2023/1/19
 ** des：onLayoutChildren异常捕获
 **/

class CustomStaggeredGridLayoutManager(spanCount: Int, orientation: Int) : StaggeredGridLayoutManager(spanCount, orientation) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: Exception) {
            println("CustomStaggeredGridLayoutManager:onLayoutChildren error  ${e.message}")
        }
    }

}