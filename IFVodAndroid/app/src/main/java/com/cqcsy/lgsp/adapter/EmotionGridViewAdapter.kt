package com.cqcsy.lgsp.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.utils.EmotionUtils.getImgByName

/**
 * 自定义表情Grid适配器
 */
class EmotionGridViewAdapter(
    private val context: Context,
    private val emotionNames: MutableList<String>,
    private val itemWidth: Int
) : BaseAdapter() {
    override fun getCount(): Int {
        // +1 最后一个为删除按钮
        return emotionNames.size + 1
    }

    override fun getItem(position: Int): String {
        return emotionNames[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val ivEmotion = ImageView(context)
        val params = ViewGroup.LayoutParams(itemWidth, itemWidth)
        ivEmotion.layoutParams = params
        ivEmotion.scaleType = ImageView.ScaleType.CENTER_INSIDE
        //判断是否为最后一个item
        if (position == count - 1) {
            ivEmotion.setImageResource(R.mipmap.icon_backspace)
        } else {
            val emotionName = emotionNames[position]
            ivEmotion.setImageResource(getImgByName(emotionName))
        }
        return ivEmotion
    }
}