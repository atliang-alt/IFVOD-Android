package com.cqcsy.lgsp.delegate.util

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/8
 ** des：横向铺满
 **/

abstract class FullDelegate<T, VH : RecyclerView.ViewHolder> : ItemViewBinder<T, VH>() {

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        val lp: ViewGroup.LayoutParams = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = true
        }
    }
}