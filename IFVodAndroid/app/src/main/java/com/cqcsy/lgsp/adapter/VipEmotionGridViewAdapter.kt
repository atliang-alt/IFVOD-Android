package com.cqcsy.lgsp.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.cqcsy.lgsp.utils.EmotionUtils.getVipImgByName
import com.cqcsy.library.utils.ImageUtil

/**
 * 自定义表情Grid适配器
 */
class VipEmotionGridViewAdapter(
    private val context: Context,
    private val emotionUrl: MutableList<String>,
    private val itemWidth: Int
) : BaseAdapter() {
    override fun getCount(): Int {
        return emotionUrl.size
    }

    override fun getItem(position: Int): String {
        return emotionUrl[position]
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
        val emotionName = emotionUrl[position]
        ivEmotion.scaleType = ImageView.ScaleType.CENTER_INSIDE
        ImageUtil.loadGif(
            context,
            getVipImgByName(emotionName),
            ivEmotion,
            ImageView.ScaleType.CENTER_INSIDE,
            true, defaultImage = 0
        )
        return ivEmotion
    }
}