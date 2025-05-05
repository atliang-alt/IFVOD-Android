package com.cqcsy.lgsp.delegate

import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.delegate.util.FullDelegate

/**
 ** 2023/9/19
 ** des：底部铺满提示
 **/

class FooterDelegate : FullDelegate<String, RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: String) {
        (holder.itemView as TextView).text = item
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecyclerView.ViewHolder {
        val textView = TextView(parent.context)
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        textView.setTextColor(ColorUtils.getColor(R.color.word_color_6))
        val size20 = SizeUtils.dp2px(20f)
        textView.setPadding(size20, size20, size20, size20)
        textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        return object : RecyclerView.ViewHolder(textView) {}
    }
}
