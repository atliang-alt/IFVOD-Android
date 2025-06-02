package com.cqcsy.library.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cqcsy.library.R
import com.cqcsy.library.views.LoadingView

/**
 * 普通fragment基类
 * 包含：页面加载效果、失败、空数据
 * 子类根布局必须是merge，减少一层嵌套
 */
abstract class NormalFragment : BaseFragment() {
    var isFirst = true
    lateinit var emptyLargeTip: TextView
    lateinit var emptyLittleTip: TextView
    lateinit var emptyImage: ImageView
    protected lateinit var statusView: LoadingView

    abstract fun getContainerView(): Int

    open fun initView() {

    }

    open fun initData() {

    }

    open fun onViewCreate(view: View) {

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: FrameLayout =
            inflater.inflate(R.layout.layout_base_fragment, container, false) as FrameLayout
        val layout = getContainerView()
        val child: LinearLayout = root.findViewById(R.id.childContainer)
        if (layout > 0) {
            LayoutInflater.from(context).inflate(layout, child, true)
        }
        statusView = LoadingView(requireContext())
        val lp = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        root.addView(statusView, lp)

        emptyLargeTip = statusView.findViewById(R.id.large_tip)
        emptyLittleTip = statusView.findViewById(R.id.little_tip)
        emptyImage = statusView.findViewById(R.id.image_empty)

        onViewCreate(root)

        return root
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            initData()
            initView()
            isFirst = false
        }
    }

    fun showProgress() {
        if (isSafe()) {
            statusView.showProgress()
        }
    }

    open fun dismissProgress() {
        if (isSafe()) {
            statusView.dismissProgress()
        }
    }

    open fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            statusView.showFailed(listener)
        }
    }

    open fun showEmpty() {
        if (isSafe()) {
            statusView.showEmpty()
        }
    }

}