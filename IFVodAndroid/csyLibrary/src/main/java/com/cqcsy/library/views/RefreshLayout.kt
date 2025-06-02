package com.cqcsy.library.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smartrefresh.layout.SmartRefreshLayout

class RefreshLayout : SmartRefreshLayout {
    var lastRefreshTime = 0L

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun autoRefresh(): Boolean {
        computeChildScrollToTop(this)
        return super.autoRefresh()
    }

    private fun computeChildScrollToTop(viewGroup: ViewGroup) {
        for (child in viewGroup.children) {
            if(child is RecyclerView) {
                child.scrollToPosition(0)
            } else if(child is ViewGroup) {
                computeChildScrollToTop(child)
            }
        }
    }
}