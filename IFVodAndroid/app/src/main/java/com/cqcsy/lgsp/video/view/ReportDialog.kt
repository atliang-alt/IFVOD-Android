package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_report_dialog.*
import org.json.JSONObject

/**
 * 投诉举报
 */
class ReportDialog(context: Context) : VideoMenuDialog(context) {
    var msg: BarrageBean? = null
    var selectedItem = -1
    var listener: OnReportListener? = null

    interface OnReportListener {
        fun onReportSuccess(bean: BarrageBean)
    }

    fun setMessage(msg: BarrageBean) {
        this.msg = msg
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_report_dialog)
        if (msg != null) {
            message.text = msg?.contxt
        }
        report.setOnClickListener {
            if (msg != null)
                reportDanmaku(
                    msg!!,
                    StringUtils.getStringArray(R.array.report_reasons)[selectedItem]
                )
        }
        if (isVertical) {
            itemLayout.setPadding(
                SizeUtils.dp2px(12f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(12f),
                SizeUtils.dp2px(20f)
            )
        } else {
            itemLayout.setPadding(
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f)
            )
        }
        setReasonList()
    }

    private fun setReasonList() {
        reasonList.layoutManager = GridLayoutManager(context, 2)
        reasonList.addItemDecoration(XGridBuilder(context).setHLineSpacing(15f).build())
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.layout_report_item,
            StringUtils.getStringArray(R.array.report_reasons).toMutableList()
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

    private fun reportDanmaku(bean: BarrageBean, reason: String) {
        val params = HttpParams()
        params.put("id", bean.guid)
        params.put("reason", reason)
        HttpRequest.post(RequestUrls.ACCUSATION_BARRAGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listener?.onReportSuccess(bean)
                dismiss()
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }
}