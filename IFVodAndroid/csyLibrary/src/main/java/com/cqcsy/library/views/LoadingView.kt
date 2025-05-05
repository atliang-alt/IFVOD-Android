package com.cqcsy.library.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.cqcsy.library.R
import kotlinx.android.synthetic.main.layout_status_empty.view.emptyContainer
import kotlinx.android.synthetic.main.layout_status_empty.view.large_tip
import kotlinx.android.synthetic.main.layout_status_empty.view.little_tip
import kotlinx.android.synthetic.main.layout_status_loading.view.failedContainer
import kotlinx.android.synthetic.main.layout_status_loading.view.progressContainer
import kotlinx.android.synthetic.main.layout_status_loading.view.refreshButton
import kotlinx.android.synthetic.main.layout_status_loading.view.tv_loading_tip

/**
 * page级别加载状态
 */
class LoadingView : FrameLayout {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init()
    }

    fun init() {
        View.inflate(context, R.layout.layout_status_loading, this)
        setOnTouchListener { _, _ -> true }
        visibility = View.GONE
        setBackgroundResource(R.color.colorPrimary)
    }

    fun hide() {
        visibility = View.GONE
    }

    fun showProgress(loadingText: String? = null) {
        visibility = View.VISIBLE
        if (!loadingText.isNullOrEmpty()) {
            tv_loading_tip.text = loadingText
        }
        emptyContainer?.visibility = View.GONE
        failedContainer?.visibility = View.GONE
        progressContainer.visibility = View.VISIBLE
    }

    fun dismissProgress() {
        visibility = View.GONE
        emptyContainer?.visibility = View.GONE
        failedContainer?.visibility = View.GONE
        progressContainer?.visibility = View.GONE
    }

    fun showFailed(listener: OnClickListener) {
        visibility = View.VISIBLE
        emptyContainer?.visibility = View.GONE
        failedContainer?.visibility = View.VISIBLE
        progressContainer?.visibility = View.GONE
        refreshButton?.setOnClickListener {
            hide()
            listener.onClick(it)
        }
    }

    fun showEmpty(emptyLargeTip: String? = null, emptyLittleTip: String? = null) {
        visibility = View.VISIBLE
        emptyContainer?.visibility = View.VISIBLE
        failedContainer?.visibility = View.GONE
        progressContainer?.visibility = View.GONE
        if (emptyLargeTip != null) {
            emptyContainer.large_tip.text = emptyLargeTip
        }
        if (emptyLittleTip != null) {
            emptyContainer.little_tip.text = emptyLittleTip
        }
    }

    fun getEmptyContainer(): View {
        return emptyContainer
    }
}