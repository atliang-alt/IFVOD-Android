package com.cqcsy.lgsp.views.dialog

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.views.BottomBaseDialog
import kotlinx.android.synthetic.main.layout_recyclerview.*

class HostSelectDialog(context: Context) : BottomBaseDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_recyclerview)
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.item_center_text,
            StringUtils.getStringArray(R.array.host_select).toMutableList()
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setText(R.id.text, item)
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            ToastUtils.showLong("重启APP生效")
            SPUtils.getInstance().put("hostType", position + 1)
            dismiss()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
}