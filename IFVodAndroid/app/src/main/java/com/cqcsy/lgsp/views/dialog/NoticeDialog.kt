package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.cqcsy.lgsp.R
import kotlinx.android.synthetic.main.layout_notice_dialog.*

/**
 * 公告弹框
 */
class NoticeDialog(context: Context) : Dialog(context, R.style.dialog_style) {
    private var title: String = ""
    private var content: String = ""
    fun setData(title: String, content: String) {
        this.title = title
        this.content = content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_notice_dialog)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        if (title.isEmpty()) {
            noticeTitle.visibility = View.GONE
        } else {
            noticeTitle.text = title
        }
        noticeContent.text = Html.fromHtml(content)
        noticeContent.movementMethod = ScrollingMovementMethod.getInstance()
        noticeCancel.setOnClickListener {
            dismiss()
        }
    }
}