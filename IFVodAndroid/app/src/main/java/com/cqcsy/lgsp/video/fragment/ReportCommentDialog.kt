package com.cqcsy.lgsp.video.fragment

import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_report_comment_dialog.*
import org.json.JSONObject

/**
 * 投诉举报
 */
class ReportCommentDialog(context: Context) : BottomBaseDialog(context) {
    var reportId: String = ""
    var userId: Int = 0
    var selectedItem = -1
    var listener: OnReportListener? = null

    interface OnReportListener {
        fun onReportSuccess(reportId: String)
    }

    fun setReportContent(reportId: String, userId: Int) {
        this.reportId = reportId
        this.userId = userId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_report_comment_dialog)
        report.setOnClickListener {
            report(
                StringUtils.getStringArray(R.array.comment_report_reasons)[selectedItem]
            )
        }
        setReasonList()
    }

    private fun setReasonList() {
        reasonList.layoutManager = GridLayoutManager(context, 2)
        reasonList.addItemDecoration(XGridBuilder(context).setHLineSpacing(15f).build())
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.layout_report_item,
            StringUtils.getStringArray(R.array.comment_report_reasons).toMutableList()
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                val checkBox = holder.getView<CheckBox>(R.id.item_check)
                checkBox.text = item
                checkBox.isChecked = holder.adapterPosition == selectedItem
                checkBox.setOnClickListener {
                    report.isEnabled = true
                    selectedItem = holder.adapterPosition
                    notifyDataSetChanged()
                }
            }

        }
        reasonList.adapter = adapter
    }

    private fun report(reason: String) {
        val params = HttpParams()
        params.put("id", reportId)
        params.put("toUid", userId)
        params.put("reason", reason)
        HttpRequest.post(RequestUrls.REPORT_COMMENT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listener?.onReportSuccess(reportId)
                dismiss()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }
}