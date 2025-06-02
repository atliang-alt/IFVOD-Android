package com.cqcsy.library.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/**
 * 列表中存在图片，处理滑动过程中停止加载图片，滑动停止后加载图片
 */
open class LoadingRecyclerView : RecyclerView {

    interface OnScrollerListener {
        fun onScroll(dx: Int, dy: Int)
        fun onScrollStop()
    }

    private var mRefreshLayout: RefreshLayout? = null

    // 是否设置滑动处理图片
    var isScrollOption: Boolean = false

    private var mScrollerListener: OnScrollerListener? = null

    fun setScrollListener(listener: OnScrollerListener?) {
        mScrollerListener = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        isNestedScrollingEnabled = false
        addScrollListener()
    }

    private fun addScrollListener() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    // 停止滚动
                    SCROLL_STATE_IDLE -> {
                        mScrollerListener?.onScrollStop()
                        mRefreshLayout?.autoRefresh(200, 100, 0f, false)
                        mRefreshLayout = null
                        if (isScrollOption) {
                            try {
                                if (context != null) {
                                    Glide.with(context).resumeRequests()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    // 手指按住屏幕滑动
                    // 惯性滑动
                    SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING -> {
                        if (isScrollOption) {
                            try {
                                if (context != null) {
                                    Glide.with(context).pauseRequests()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mScrollerListener?.onScroll(dx, dy)
            }
        })
    }

    fun scrollToTop(refreshLayout: RefreshLayout?) {
        if (refreshLayout != null && !canScrollVertically(-1)) {
            refreshLayout.autoRefresh(200, 100, 0f, false)
        } else {
            mRefreshLayout = refreshLayout
            smoothScrollToPosition(0)
        }
    }

    fun scrollToBottom() {
        if (adapter != null && adapter!!.itemCount > 0) {
            scrollToPosition(adapter!!.itemCount - 1)
        }
    }

    fun scrollFastToTop() {
        scrollToPosition(0)
    }

}