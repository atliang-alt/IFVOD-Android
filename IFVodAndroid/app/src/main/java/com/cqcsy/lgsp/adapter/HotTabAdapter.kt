package com.cqcsy.lgsp.adapter

import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity

/**
 * 热播适配器
 */
class HotTabAdapter(data: MutableList<MovieModuleBean>) :
    BaseMultiItemQuickAdapter<MovieModuleBean, BaseViewHolder>(data) {

    init {
        addItemType(0, R.layout.item_hot_tab_top_module)
        addItemType(1, R.layout.item_hot_tab_module)
    }

    override fun convert(holder: BaseViewHolder, item: MovieModuleBean) {
        val position = getItemPosition(item)
        when (item.itemType) {
            0 -> setTopView(holder, item, position)
            1 -> setBottomView(holder, item, position)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            context.startActivity(intent)
        }
    }

    private fun setTopView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                context,
                it, holder.getView(R.id.hotImage)
            )
        }
        when (position) {
            0 -> holder.getView<ImageView>(R.id.hotNumb)
                .setImageResource(R.mipmap.icon_hot_first)
            1 -> holder.getView<ImageView>(R.id.hotNumb)
                .setImageResource(R.mipmap.icon_hot_second)
            2 -> holder.getView<ImageView>(R.id.hotNumb)
                .setImageResource(R.mipmap.icon_hot_three)
            else -> holder.getView<ImageView>(R.id.hotNumb)
                .setImageResource(R.mipmap.icon_hot_first)
        }
        holder.getView<TextView>(R.id.hotTitle).text = item.title
        item.cidMapper?.replace(",", " ")?.let {
            formatTypeData(
                it,
                item.publishTime,
                holder.getView(R.id.yearText),
                holder.getView(R.id.contentType)
            )
        }
    }

    private fun setBottomView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
        if (position == 3) {
            holder.setVisible(R.id.lineView, true)
        } else {
            holder.setGone(R.id.lineView, true)
        }
        holder.setText(R.id.hotNumb, (position + 1).toString())
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                context,
                it, holder.getView(R.id.hotImage)
            )
        }
        holder.setText(R.id.hotTitle, item.title)
        item.cidMapper?.replace(",", " ")?.let {
            formatTypeData(
                it,
                item.publishTime,
                holder.getView(R.id.yearText),
                holder.getView(R.id.contentType)
            )
        }
    }

    private fun formatTypeData(
        type: String,
        publishTime: String,
        yearText: TextView,
        contentTypeText: TextView
    ) {
        var contentType = ""
        var year = TimesUtils.getYear(publishTime)
        val typeList = type.split("·")
        for (i in typeList.indices) {
            if (i <= 1) {
                contentType = if (contentType.isEmpty()) {
                    typeList[i]
                } else {
                    contentType + "·" + typeList[i]
                }
            } else {
                year = year + "/" + typeList[i]
            }
        }
        yearText.text = year
        contentTypeText.text = contentType
    }
}