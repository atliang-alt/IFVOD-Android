package com.cqcsy.lgsp.video.view

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.PopupWindow
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import org.json.JSONObject

/**
 * 作者：wangjianxiong
 * 创建时间：2023/2/9
 *
 *
 */
class DanmuHoldPopupWindow(private val context: Context, isFullScreen: Boolean) :
    PopupWindow(context) {
    private var content: TextView
    private var name: TextView
    private var like: CheckedTextView
    private var report: TextView
    private var shield: TextView
    private var copy: TextView
    private var data: BarrageBean? = null
    private var danmakuId: Long = 0
    var listener: OnDanmuActionListener? = null

    init {
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = SizeUtils.dp2px(80f)
        isOutsideTouchable = true
        isTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        contentView = LayoutInflater.from(context).inflate(R.layout.layout_danmu_pop_menu, null)
        content = contentView.findViewById(R.id.content)
        name = contentView.findViewById(R.id.name)
        like = contentView.findViewById(R.id.like)
        report = contentView.findViewById(R.id.report)
        copy = contentView.findViewById(R.id.copy)
        shield = contentView.findViewById(R.id.shield)
        if (isFullScreen) {
            content.maxEms = 25
            name.maxEms = 6
            //name.filters = arrayOf(InputFilter.LengthFilter(6))
        } else {
            content.maxEms = 13
            name.maxEms = 5
            //name.filters = arrayOf(InputFilter.LengthFilter(5))
        }
        like.setOnClickListener {
            data?.let {
                listener?.onLikeClick(danmakuId, it)
            }
            dismiss()
        }
        copy.setOnClickListener {
            data?.let {
                listener?.onPasteClick(it)
            }
            dismiss()
        }
        shield.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                context.startActivity(Intent(context, LoginActivity::class.java))
                return@setOnClickListener
            }
            data?.let {
                forbiddenUser(it)
            }
            dismiss()
        }
        report.setOnClickListener {
            data?.let {
                listener?.onReportClick(it)
            }
            dismiss()
        }
    }

    fun updateDanmuData(danmakuId: Long, data: BarrageBean) {
        this.danmakuId = danmakuId
        this.data = data
        content.text = data.contxt
        name.text = data.nickName
        if (data.good > 0) {
            like.text = data.good.toString()
        } else {
            like.text = context.getString(R.string.fabulous)
        }
        like.isChecked = data.isLike
        contentView.requestLayout()
    }

    private fun forbiddenUser(bean: BarrageBean) {
        val params = HttpParams()
        params.put("uid", bean.uid)
        HttpRequest.post(RequestUrls.FORBIDDEN_USER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listener?.onForbiddenSuccess(bean)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    interface OnDanmuActionListener {
        fun onLikeClick(danmakuId: Long, data: BarrageBean)
        fun onReportClick(data: BarrageBean)
        fun onPasteClick(data: BarrageBean)
        fun onForbiddenSuccess(data: BarrageBean)
    }
}