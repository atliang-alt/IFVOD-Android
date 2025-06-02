package com.cqcsy.lgsp.upper

import android.app.Activity
import android.content.Intent
import android.text.Html
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.main.home.RecommendMultiAdapter
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.lgsp.upper.pictures.PictureListActivity
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.DynamicUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil

class AllResourceAdapter(
    val activity: Activity,
    data: MutableList<RecommendMultiBean>
) :
    BaseMultiItemQuickAdapter<RecommendMultiBean, BaseViewHolder>(data) {
    val padding = SizeUtils.dp2px(20f)
    private val itemWidth = ScreenUtils.getAppScreenWidth() - SizeUtils.dp2px(12f) * 2

    init {
        addItemType(RecommendMultiAdapter.TYPE_VIDEO, R.layout.layout_up_all_video)
        addItemType(RecommendMultiAdapter.TYPE_SHORT, R.layout.layout_up_all_short)
        addItemType(RecommendMultiAdapter.TYPE_DYNAMIC, R.layout.layout_up_all_dynamic)
        addItemType(RecommendMultiAdapter.TYPE_PICTURES, R.layout.layout_up_all_pictures)
    }

    override fun convert(holder: BaseViewHolder, item: RecommendMultiBean) {
        val itemView = holder.itemView
        if (holder.adapterPosition == 0) {
            itemView.setPadding(0, 0, 0, padding)
        } else {
            itemView.setPadding(0, padding, 0, padding)
        }
        when (item.itemType) {
            RecommendMultiAdapter.TYPE_VIDEO -> setVideo(holder, item)
            RecommendMultiAdapter.TYPE_SHORT -> setShort(holder, item)
            RecommendMultiAdapter.TYPE_DYNAMIC -> setDynamic(holder, item)
            RecommendMultiAdapter.TYPE_PICTURES -> setPictures(holder, item)
        }
    }

    private fun setVideo(holder: BaseViewHolder, item: RecommendMultiBean) {
        holder.setText(
            R.id.video_type,
            StringUtils.getString(
                R.string.video_type_time,
                TimesUtils.friendDate(item.date ?: "")
            )
        )
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.image_film))
        holder.setText(R.id.video_name, item.title)
        if (item.videoType != Constant.VIDEO_MOVIE) {
            holder.setVisible(R.id.video_episode, true)
            holder.setText(R.id.video_episode, item.updateStatus)
        } else {
            holder.setVisible(R.id.video_episode, false)
        }
        holder.setText(R.id.video_hot, NormalUtil.formatPlayCount(item.viewCount))
        if (item.updateCount > 0) {
            holder.setVisible(R.id.item_update, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.item_update, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.item_update, item.updateCount.toString())
            }
        } else {
            holder.setVisible(R.id.item_update, false)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, item.mediaKey)
            intent.putExtra(VideoBaseActivity.VIDEO_TYPE, item.videoType)
            context.startActivity(intent)
        }
    }

    private fun setShort(holder: BaseViewHolder, item: RecommendMultiBean) {
        holder.setText(
            R.id.video_type,
            StringUtils.getString(
                R.string.short_type_time,
                TimesUtils.friendDate(item.date ?: "")
            )
        )
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.image_short_video))
        holder.setText(R.id.short_video_time, item.duration)
        holder.setText(R.id.short_video_name, item.title)
        holder.setText(R.id.play_count, NormalUtil.formatPlayCount(item.viewCount))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, item.mediaKey)
            intent.putExtra(VideoBaseActivity.VIDEO_TYPE, item.videoType)
            context.startActivity(intent)
        }
    }

    private fun setDynamic(holder: BaseViewHolder, item: RecommendMultiBean) {
        holder.setText(
            R.id.video_type,
            StringUtils.getString(
                R.string.dynamic_type_time,
                TimesUtils.friendDate(item.date ?: "")
            )
        )
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_des, true)
        } else {
            holder.setGone(R.id.dynamic_des, false)
            holder.setText(R.id.dynamic_des, Html.fromHtml(item.description.replace("\n", "<br>")))
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.setText(R.id.look_count, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))

        val imageContainer = holder.getView<LinearLayout>(R.id.image_container)
        val videoContainer = holder.getView<FrameLayout>(R.id.videoContainer)

        if (item.photoType == 1) {
            imageContainer.isVisible = true
            videoContainer.isVisible = false
            DynamicUtils.addDynamicImages(
                context,
                imageContainer,
                item.details,
                item.photoCount
            )
        } else {
            imageContainer.isVisible = false
            videoContainer.isVisible = true
            val videoCover = holder.getView<ImageView>(R.id.iv_video_cover)
            videoCover.setImageDrawable(null)
            val size = DynamicUtils.getCoverSize(item.imageRatioValue, DynamicUtils.getCellWidth())
            videoContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                this.width = size.width
                this.height = size.height
            }
            ImageUtil.loadImage(
                context,
                item.coverImgUrl,
                videoCover,
                imageWidth = size.width,
                imageHeight = size.height,
                corner = 2
            )
        }
        holder.itemView.setOnClickListener {
            item.viewCount += item.photoCount
            notifyItemChanged(holder.adapterPosition)
            val list = mutableListOf<DynamicBean>()
            var currentIndex = 0
            if (item.photoType == 2) {
                var index = 0
                data.forEach {
                    if (it.businessType == 2 && it.photoType == 2) {
                        list.add(DynamicBean().copy(it))
                        if (it.uniqueID == item.uniqueID) {
                            currentIndex = index
                        }
                        index++
                    }
                }
            }
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey
                dynamicType = item.photoType
                videoIndex = currentIndex
                dynamicVideoList = list
                openRecommend = false
                fromUpperHomePage = true
                upperId = item.userId
            }
        }
    }

    private fun setPictures(holder: BaseViewHolder, item: RecommendMultiBean) {
        holder.setText(
            R.id.video_type,
            StringUtils.getString(
                R.string.picture_type_time,
                TimesUtils.friendDate(item.date ?: "")
            )
        )
        ImageUtil.loadImage(
            context, item.coverImgUrl, holder.getView(R.id.picture_cover),
            defaultImage = R.mipmap.pictures_cover_default
        )
        holder.setText(R.id.picture_size, item.photoCount.toString())
        holder.setText(R.id.picture_name, item.title)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.picture_des, true)
        } else {
            holder.setVisible(R.id.picture_des, true)
            holder.setText(R.id.picture_des, item.description)
        }
        holder.setText(R.id.look_count, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PictureListActivity::class.java)
            intent.putExtra(UpperPicturesFragment.PICTURES_PID, item.mediaKey)
            intent.putExtra(UpperPicturesFragment.PICTURES_TITLE, item.title)
            context.startActivity(intent)
        }
    }
}