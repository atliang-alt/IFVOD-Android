package com.cqcsy.library.base

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.library.R
import com.cqcsy.library.views.LoadingView
import kotlinx.android.synthetic.main.activity_normal.*
import kotlinx.android.synthetic.main.layout_header.*

/**
 * 普通页面基类
 * 包含：普通header，返回按钮+title
 * 页面级加载、失败、空数据
 * 加载dialogue
 */
abstract class NormalActivity : BaseActivity() {
    lateinit var emptyLargeTip: TextView
    lateinit var emptyLittleTip: TextView
    lateinit var failedLargeTip: TextView
    lateinit var failedLittleTip: TextView
    lateinit var rightTextView: TextView
    lateinit var rightImageView: ImageView
    lateinit var leftImageView: ImageView
    lateinit var rightContent: View
    private lateinit var rootStatusView: LoadingView

    // 返回的布局必须是merge
    abstract fun getContainerView(): Int

    open fun onViewCreate() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal)
        val layout = getContainerView()
        if (layout > 0) {
            LayoutInflater.from(this).inflate(layout, bodyContainer, true)
        }
        rightImageView = findViewById(R.id.rightImage)
        rightContent = findViewById(R.id.rightContent)
        leftImageView = findViewById(R.id.leftImage)
        rightTextView = findViewById(R.id.rightText)

        rootStatusView = LoadingView(this)
        bodyContainer.addView(rootStatusView)
        emptyLargeTip = rootStatusView.findViewById(R.id.large_tip)
        emptyLittleTip = rootStatusView.findViewById(R.id.little_tip)
        failedLargeTip = rootStatusView.findViewById(R.id.failedLargeTip)
        failedLittleTip = rootStatusView.findViewById(R.id.failedLittleTip)

        onViewCreate()
    }

    open fun onRightClick(view: View) {}

    fun setRightImageVisible(visibility: Int) {
        rightImage.visibility = visibility
    }

    fun setRightImage(@DrawableRes res: Int) {
        rightImage.setImageResource(res)
        rightImage.visibility = View.VISIBLE
    }

    fun setRightText(@StringRes res: Int) {
        rightText.setText(res)
        rightText.visibility = View.VISIBLE
    }

    fun setRightTextColorRes(@ColorRes color: Int) {
        rightText.setTextColor(ColorUtils.getColor(color))
    }

    fun setRightTextColor(@ColorInt color: Int) {
        rightText.setTextColor(color)
    }

    fun setRightTextColor(colorStateList: ColorStateList?) {
        rightText.setTextColor(colorStateList)
    }

    fun setRightTextVisible(visibility: Int) {
        rightText.visibility = visibility
    }

    fun setRightTextEnabled(enabled: Boolean) {
        rightText.isEnabled = enabled
    }

    fun setHeaderTitle(res: Int) {
        headerTitle.setText(res)
    }

    fun setHeaderTitle(title: String) {
        headerTitle.text = title
    }

    fun showProgress() {
        if (isSafe()) {
            rootStatusView.showProgress()
        }
    }

    fun dismissProgress() {
        if (isSafe()) {
            rootStatusView.dismissProgress()
        }
    }

    fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            dismissProgressDialog()
            rootStatusView.showFailed(listener)
        }
    }

    open fun showEmpty() {
        if (isSafe()) {
            dismissProgressDialog()
            rootStatusView.showEmpty()
        }
    }
}