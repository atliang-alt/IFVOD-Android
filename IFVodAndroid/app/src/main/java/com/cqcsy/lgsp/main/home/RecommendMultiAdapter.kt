package com.cqcsy.lgsp.main.home

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil

/**
 * 首页为你推荐瀑布流
 */

class RecommendMultiAdapter(data: MutableList<RecommendMultiBean>) :
    BaseMultiItemQuickAdapter<RecommendMultiBean, BaseViewHolder>(data) {

    companion object {
        const val TYPE_VIDEO = 0
        const val TYPE_SHORT = 1
        const val TYPE_DYNAMIC = 2
        const val TYPE_PICTURES = 3
//        const val TYPE_LIVE = 1
    }

    init {
        addItemType(TYPE_VIDEO, R.layout.item_min_content_module)
        addItemType(TYPE_SHORT, R.layout.layout_recommend_short_video)
        addItemType(TYPE_DYNAMIC, R.layout.layout_recommend_dynamic)
        addItemType(TYPE_PICTURES, R.layout.layout_recommend_picture_item)
//        addItemType(TYPE_LIVE, R.layout.item_min_content_module)
    }

    override fun convert(holder: BaseViewHolder, item: RecommendMultiBean) {
        when (item.itemType) {
            TYPE_VIDEO -> setVideo(holder, item)
            TYPE_SHORT -> setShort(holder, item)
            TYPE_PICTURES -> setPictures(holder, item)
            TYPE_DYNAMIC -> setDynamic(holder, item)
        }
    }

    private fun setVideo(holder: BaseViewHolder, item: RecommendMultiBean) {
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.itemContentImage))
        holder.setText(R.id.video_score, item.score)
        holder.setText(R.id.itemPlayCount, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.itemTitle, item.title)
        holder.setText(R.id.itemType, item.cidMapper?.replace(",", " "))
        // 电影需隐藏全集或更新到几期的标签
        if (item.videoType != Constant.VIDEO_MOVIE) {
            holder.setVisible(R.id.itemTotal, true)
            holder.setText(R.id.itemTotal, item.updateStatus)
        } else {
            holder.setVisible(R.id.itemTotal, false)
        }
        if (item.updateCount > 0) {
            holder.setVisible(R.id.itemUpdateCount, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.itemUpdateCount, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.itemUpdateCount, item.updateCount.toString())
            }
        } else {
            holder.setVisible(R.id.itemUpdateCount, false)
        }
        // 是否显示推荐按钮
        if (item.isRecommend) {
            holder.setVisible(R.id.itemRecommendImage, true)
        } else {
            holder.setVisible(R.id.itemRecommendImage, false)
        }
    }

    private fun setShort(holder: BaseViewHolder, item: RecommendMultiBean) {
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.popularContentImage))
        holder.setText(R.id.popularPlayCount, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.popularTitle, item.title)
        holder.setText(R.id.popularTime, item.duration)
        if (item.cidMapper.isNullOrEmpty()) {
            holder.setGone(R.id.contentType, true)
        } else {
            holder.setVisible(R.id.contentType, true)
            holder.setText(R.id.contentType, item.cidMapper)
        }
        if (item.isHot) {
            holder.setVisible(R.id.popularHot, true)
        } else {
            holder.setVisible(R.id.popularHot, false)
        }
    }

    private fun setPictures(holder: BaseViewHolder, item: RecommendMultiBean) {
        holder.setText(R.id.lookCount, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.user_name, item.upperName)
        holder.setText(R.id.picture_name, item.title)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.picture_des, true)
        } else {
            holder.setVisible(R.id.picture_des, true)
            holder.setText(
                R.id.picture_des,
                Html.fromHtml(item.description.replace("\n", "<br>"))
            )
        }
        val focusText = holder.getView<TextView>(R.id.focus_user)
        if (item.focusStatus) {
            focusText.visibility = View.VISIBLE
        } else {
            focusText.visibility = View.GONE
        }
        ImageUtil.loadCircleImage(context, item.headImg, holder.getView(R.id.user_image))
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.picture_cover))
    }

    private fun setDynamic(holder: BaseViewHolder, item: RecommendMultiBean) {
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_des, true)
        } else {
            holder.setVisible(R.id.dynamic_des, true)
            holder.setText(R.id.dynamic_des, Html.fromHtml(item.description.replace("\n", "<br>")))
        }
        val focusText = holder.getView<TextView>(R.id.focus_user)
        if (item.focusStatus) {
            focusText.visibility = View.VISIBLE
        } else {
            focusText.visibility = View.GONE
        }
        ImageUtil.loadCircleImage(context, item.headImg, holder.getView(R.id.user_image))
        holder.setText(R.id.lookCount, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.user_name, item.upperName)
        val imageView = holder.getView<ImageView>(R.id.dynamic_cover)
        holder.getView<ImageView>(R.id.gifTag).isVisible =
            item.coverImgUrl?.contains(".gif", true) == true
        holder.setGone(R.id.longImageTag, !item.isLongImage)
        var scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY
        val imageWidth = imageView.width
        var imageHeight = imageView.width
//        if (item.photoType == 2) {
//            imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                if (item.imageRatioValue > 1) {
//                    imageHeight = (imageWidth * 9f / 16f).toInt()
//                    this.dimensionRatio = "h,16:9"
//                } else if (item.imageRatioValue == 1f) {
//                    imageHeight = imageWidth
//                    this.dimensionRatio = "h,1:1"
//                } else {
//                    imageHeight = (imageWidth * 4f / 3f).toInt()
//                    this.dimensionRatio = "h,3:4"
//                }
//            }
//        } else {
        imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (item.imageRatioValue < 3f / 4f) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                imageHeight = (imageWidth * 4f / 3f).toInt()
                this.dimensionRatio = "h,3:4"
            } else if (item.imageRatioValue > 4f / 3f) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                imageHeight = (imageWidth * 3f / 4f).toInt()
                this.dimensionRatio = "h,4:3"
            } else {
                scaleType = ImageView.ScaleType.FIT_XY
                imageHeight = (imageWidth / item.imageRatioValue).toInt()
                this.dimensionRatio = "h,${item.ratio}"
            }
        }
//        }
        ImageUtil.loadImage(
            context,
            item.coverImgUrl,
            imageView,
            corner = 2,
            scaleType = scaleType,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )
    }

}