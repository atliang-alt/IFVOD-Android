package com.cqcsy.lgsp.upper

import android.text.Html
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.utils.DynamicUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.ImageUtil

/**
 * 动态列表适配器
 */
class DynamicListAdapter(data: MutableList<DynamicBean>) :
    BaseDelegateMultiAdapter<DynamicBean, BaseViewHolder>(data) {

    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<DynamicBean>() {
            override fun getItemType(data: List<DynamicBean>, position: Int): Int {
                return data[position].photoType
            }
        })
        getMultiTypeDelegate()?.addItemType(1, R.layout.layout_dynamic_item)
        getMultiTypeDelegate()?.addItemType(2, R.layout.item_dynamic_video)
    }

    val padding = SizeUtils.dp2px(10f)

    override fun convert(holder: BaseViewHolder, item: DynamicBean) {
        when (holder.itemViewType) {
            1 -> {
                setImageDynamic(holder, item)
            }
            else -> {
                setVideoDynamic(holder, item)
            }
        }
    }

    private fun setImageDynamic(holder: BaseViewHolder, item: DynamicBean) {
        val itemView = holder.itemView
        if (holder.adapterPosition == 0) {
            itemView.setPadding(0, 0, 0, padding)
        } else {
            itemView.setPadding(0, padding, 0, padding)
        }
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.item_des, true)
        } else {
            holder.setText(R.id.item_des, Html.fromHtml(item.description!!.replace("\n", "<br>")))
            holder.setGone(R.id.item_des, false)
        }
        if (!item.createTime.isNullOrEmpty()) {
            holder.setText(R.id.time, TimesUtils.friendDate(item.createTime!!))
        } else {
            holder.setText(R.id.time, "")
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
        DynamicUtils.addDynamicImages(
            context,
            holder.getView(R.id.imageContainer),
            item.trendsDetails,
            item.photoCount
        )
    }

    private fun setVideoDynamic(holder: BaseViewHolder, item: DynamicBean) {
        val itemView = holder.itemView
        if (holder.adapterPosition == 0) {
            itemView.setPadding(0, 0, 0, padding)
        } else {
            itemView.setPadding(0, padding, 0, padding)
        }
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.item_des, true)
        } else {
            holder.setText(R.id.item_des, Html.fromHtml(item.description!!.replace("\n", "<br>")))
            holder.setGone(R.id.item_des, false)
        }
        if (!item.createTime.isNullOrEmpty()) {
            holder.setText(R.id.time, TimesUtils.friendDate(item.createTime!!))
        } else {
            holder.setText(R.id.time, "")
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
        val videoCover = holder.getView<ImageView>(R.id.iv_video_cover)
        val imageContainer = holder.getView<FrameLayout>(R.id.imageContainer)
        videoCover.setImageDrawable(null)
        val size = DynamicUtils.getCoverSize(item.imageRatioValue, DynamicUtils.getCellWidth())
        imageContainer.updateLayoutParams<LinearLayout.LayoutParams> {
            this.width = size.width
            this.height = size.height
        }
        ImageUtil.loadImage(
            context,
            item.cover,
            videoCover,
            imageWidth = size.width,
            imageHeight = size.height,
            corner = 2
        )
    }
}