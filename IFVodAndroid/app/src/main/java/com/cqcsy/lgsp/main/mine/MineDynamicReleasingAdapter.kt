package com.cqcsy.lgsp.main.mine

import android.text.Html
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.utils.VideoUtil
import com.cqcsy.library.utils.ImageUtil
import java.util.*


/**
 * 动态列表适配器
 */
class MineDynamicReleasingAdapter : BaseDelegateMultiAdapter<DynamicCacheBean, BaseViewHolder>() {

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<DynamicCacheBean>() {
            override fun getItemType(data: List<DynamicCacheBean>, position: Int): Int {
                return data[position].dynamicType
            }
        })
        getMultiTypeDelegate()?.addItemType(DynamicType.IMAGE, R.layout.layout_releasing_dynamic)
        getMultiTypeDelegate()?.addItemType(DynamicType.VIDEO, R.layout.layout_releasing_dynamic)
    }


    override fun convert(holder: BaseViewHolder, item: DynamicCacheBean) {
        when (holder.itemViewType) {
            DynamicType.IMAGE -> {
                setImageDynamic(holder, item)
            }
            else -> {
                setVideoDynamic(holder, item)
            }
        }
    }

    private fun setImageDynamic(holder: BaseViewHolder, item: DynamicCacheBean) {
        holder.setText(R.id.tv_title, Html.fromHtml(item.description.replace("\n", "<br>")))
        holder.setGone(R.id.iv_video_play, true)
        val videoCover = holder.getView<ImageView>(R.id.iv_cover)
        ImageUtil.loadImage(
            context,
            item.coverPath,
            videoCover,
            corner = 2
        )
    }

    private fun setVideoDynamic(holder: BaseViewHolder, item: DynamicCacheBean) {
        holder.setText(R.id.tv_title, Html.fromHtml(item.description.replace("\n", "<br>")))
        holder.setGone(R.id.iv_video_play, false)
        val videoCover = holder.getView<ImageView>(R.id.iv_cover)
        val progress = holder.getView<ProgressBar>(R.id.progress)
        progress.progress = item.progress
        val duration = holder.getView<TextView>(R.id.tv_duration)
        val videoUtil = VideoUtil(item.videoPath)
        duration.text = formatDuration(videoUtil.getVideoDuration() / 1000)
        ImageUtil.loadImage(
            context,
            item.coverPath,
            videoCover,
            corner = 2, scaleType = ImageView.ScaleType.CENTER_CROP
        )
    }

    private fun formatDuration(duration: Int): String {
        val standardTime: String
        if (duration <= 0) {
            standardTime = "00:00"
        } else if (duration < 60) {
            standardTime = java.lang.String.format(Locale.getDefault(), "00:%02d", duration % 60)
        } else if (duration < 3600) {
            standardTime = java.lang.String.format(
                Locale.getDefault(),
                "%02d:%02d",
                duration / 60,
                duration % 60
            )
        } else {
            standardTime = java.lang.String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                duration / 3600,
                duration % 3600 / 60,
                duration % 60
            )
        }
        return standardTime
    }
}