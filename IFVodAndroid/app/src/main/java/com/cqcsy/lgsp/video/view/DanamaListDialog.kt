package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_danama_list.*
import org.json.JSONObject

/**
 * 弹幕列表菜单
 */
class DanamaListDialog(context: Context) : VideoMenuDialog(context) {

    var listData: MutableList<BarrageBean>? = null
    var listener: OnReportItemListener? = null
    var popupWindow: PopupWindow ?= null

    interface OnReportItemListener {
        fun onReport(bean: BarrageBean)
        fun onForbiddenSuccess(bean: BarrageBean)
    }

    fun setOnReportItemListener(l: OnReportItemListener) {
        this.listener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_danama_list)
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
        if (listData != null) {
            setDanamaList()
        }
    }

    private fun setDanamaList() {
        val linearLayoutManager = LinearLayoutManager(context)
        danamaList.layoutManager = linearLayoutManager
        val adapter = object :
            BaseQuickAdapter<BarrageBean, BaseViewHolder>(
                R.layout.layout_danama_list_item,
                listData
            ) {
            override fun convert(holder: BaseViewHolder, item: BarrageBean) {
                holder.setText(
                    R.id.item_time,
                    TimeUtils.millis2String((item.second * 1000).toLong(), "mm:ss")
                )
                holder.setText(R.id.item_detail, item.contxt)
            }

        }
        adapter.setOnItemClickListener { _, view, position ->
            val barrageBean = adapter.getItem(position)
            if (barrageBean.uid == GlobalValue.userInfoBean?.id) {
                return@setOnItemClickListener
            }
            val last = linearLayoutManager.findLastVisibleItemPosition()
//            var first = linearLayoutManager.findFirstVisibleItemPosition()
            if (last < 3) {
                showPopMenu(view, true, barrageBean)
            } else if (last - position > 3) {
                showPopMenu(view, true, barrageBean)
            } else {
                linearLayoutManager.findViewByPosition(position - 3)
                    ?.let { showPopMenu(it, false, barrageBean) }
            }
        }
        danamaList.adapter = adapter
    }

    private fun showPopMenu(view: View, isDropDown: Boolean, bean: BarrageBean) {
        popupWindow?.dismiss()
        popupWindow = null
        val menu = LayoutInflater.from(context).inflate(R.layout.layout_danamaku_pop_menu, null)
        popupWindow = PopupWindow(
            menu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            false
        )
        menu.findViewById<TextView>(R.id.report).setOnClickListener {
            listener?.onReport(bean)
            popupWindow?.dismiss()
            dismiss()
        }
        menu.findViewById<TextView>(R.id.forbidden).setOnClickListener {
            forbiddenUser(bean)
            popupWindow?.dismiss()
        }
        if (isDropDown) {
            menu.setBackgroundResource(R.mipmap.image_drop_down)
        } else {
            menu.setBackgroundResource(R.mipmap.image_drop_up)
        }
        popupWindow?.width = SizeUtils.dp2px(160f)
        popupWindow?.isOutsideTouchable = true
        popupWindow?.contentView = menu
        val offset = SizeUtils.dp2px(10f)
        popupWindow?.showAsDropDown(
            view,
            Gravity.CENTER,
//            if (isDropDown) Math.negateExact(offset) else offset,
            if (isDropDown) 0 - offset else offset,
            0
        )
    }

    private fun forbiddenUser(bean: BarrageBean) {
        val params = HttpParams()
        params.put("uid", bean.uid)
        HttpRequest.post(RequestUrls.FORBIDDEN_USER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listData = listData?.filter { it.uid != bean.uid }?.toMutableList()
                if (listData != null) {
                    (danamaList.adapter as BaseQuickAdapter<BarrageBean, BaseViewHolder>).setList(listData)
                }
                listener?.onForbiddenSuccess(bean)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    override fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
        super.dismiss()
    }
}