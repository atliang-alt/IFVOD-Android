package com.cqcsy.library.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.library.R
import kotlinx.android.synthetic.main.layout_tip_dialog.*

/**
 * 通用消息提示框
 */
class TipsDialog(context: Context) : Dialog(context, R.style.dialog_style) {
    private var dTitle: CharSequence = ""
    private var msg: CharSequence = ""
    private var leftListener: View.OnClickListener? = null
    private var rightListener: View.OnClickListener? = null
    private var leftText: Int = 0
    private var leftTextIndex: Int = Gravity.CENTER
    private var rightText: Int = 0

    fun setDialogTitle(title: Int) {
        this.dTitle = StringUtils.getString(title)
    }

    fun setDialogTitle(title: CharSequence) {
        this.dTitle = title
    }

    fun setMsg(msg: CharSequence) {
        this.msg = msg
    }

    fun setMsg(msg: Int) {
        this.msg = StringUtils.getString(msg)
    }

    fun setLeftListener(leftText: Int = R.string.cancel, listener: View.OnClickListener) {
        this.leftListener = listener
        this.leftText = leftText
    }


    fun setRightListener(rightText: Int = R.string.ensure, listener: View.OnClickListener) {
        this.rightListener = listener
        this.rightText = rightText
    }

    fun setGravity(index: Int) {
        leftTextIndex = index
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_tip_dialog)
        if (dTitle.isNotEmpty()) {
            dialogTitle.text = dTitle
        } else {
            dialogTitle.isVisible = false
        }
        if (msg.isNotEmpty()) {
            dialogMsg.text = msg
        }
        dialogMsg.gravity = leftTextIndex
        if (leftText > 0) {
            leftButton.setText(leftText)
            leftButton.visibility = View.VISIBLE
        } else {
            leftButton.visibility = View.GONE
            verticalLine.visibility = View.GONE
        }
        if (leftListener != null) {
            leftButton.setOnClickListener(leftListener)
        }

        if (rightText > 0) {
            rightButton.setText(rightText)
            rightButton.visibility = View.VISIBLE
        } else {
            rightButton.visibility = View.GONE
            verticalLine.visibility = View.GONE
        }
        if (rightButton != null) {
            rightButton.setOnClickListener(rightListener)
        }
        if (leftListener == null && rightListener == null) {
            horizontalLine.visibility = View.GONE
        }
    }

    override fun show() {
        super.show()

        val attribute = window?.attributes
        val width = context.resources.displayMetrics.widthPixels
        attribute?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attribute?.width = (width * 0.72f).toInt()
        attribute?.gravity = Gravity.CENTER
        window?.attributes = attribute
    }
}