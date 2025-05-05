package com.cqcsy.lgsp.upload

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.library.GlideApp
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/12
 *
 *
 */
class AlbumGalleryPagerAdapter(list: MutableList<LocalMediaBean>?) :
    BaseDelegateMultiAdapter<LocalMediaBean, BaseViewHolder>(list) {
    init {
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<LocalMediaBean>() {
            override fun getItemType(data: List<LocalMediaBean>, position: Int): Int {
                return if (data[position].isVideo) {
                    1
                } else {
                    0
                }
            }
        })
        getMultiTypeDelegate()?.addItemType(0, R.layout.layout_gallery_picture)
        getMultiTypeDelegate()?.addItemType(1, R.layout.layout_video)
    }

    override fun convert(holder: BaseViewHolder, item: LocalMediaBean) {
        when (holder.itemViewType) {
            1 -> {
                setVideo(holder, item)
            }
            else -> {
                setImage(holder, item)
            }
        }
    }

    private fun setImage(holder: BaseViewHolder, item: LocalMediaBean) {
        val imageView = holder.getView<SubsamplingScaleImageView>(R.id.pictureImage)
        val gifView = holder.getView<ImageView>(R.id.pictureGif)
        val path = item.path
        if (path.endsWith(".gif")) {
            imageView.isVisible = false
            gifView.isVisible = true
            GlideApp.with(gifView).asGif().load(path).into(gifView)
        } else {
            imageView.isVisible = true
            gifView.isVisible = false
            GlideApp.with(context).asBitmap().load(path).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    if (resource.height > resource.width * 3) {
                        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                        imageView.setImage(ImageSource.cachedBitmap(resource))
                    } else {
                        imageView.setImage(ImageSource.cachedBitmap(resource))
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
        }
    }

    private fun setVideo(holder: BaseViewHolder, item: LocalMediaBean) {
        val videoPlayer = holder.getView<PreviewVideoPlayer>(R.id.video_player)
        videoPlayer.setUp(item.path, false, "")
        videoPlayer.titleTextView.visibility = View.GONE
        videoPlayer.backButton.visibility = View.GONE
        videoPlayer.fullscreenButton.visibility = View.GONE
        videoPlayer.isLooping = true
        //目前需求只显示一个，所以初始化完成就开始播放
        videoPlayer.startPlayLogic()
    }
}