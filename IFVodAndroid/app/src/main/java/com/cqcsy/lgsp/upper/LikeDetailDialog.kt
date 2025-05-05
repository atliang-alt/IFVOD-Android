package com.cqcsy.lgsp.upper

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import kotlinx.android.synthetic.main.layout_like_detail.*

/**
 * 作者：wangjianxiong
 * 创建时间：2022/9/5
 *
 *
 */
class LikeDetailDialog(context: Context, val likeCount: Int) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_like_detail)
        val params: WindowManager.LayoutParams? = window?.attributes
        params?.apply {
            width = SizeUtils.dp2px(270f)
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        window?.setBackgroundDrawableResource(R.color.black_5)
        window?.attributes = params
        tv_like_count.text = StringUtils.getString(R.string.upper_get_zan, likeCount)
        tv_known.setOnClickListener { dismiss() }
    }
}