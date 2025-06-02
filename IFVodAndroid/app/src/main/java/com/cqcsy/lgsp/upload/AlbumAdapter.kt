package com.cqcsy.lgsp.upload

import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.medialoader.ChooseMode
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.medialoader.MimeTypeUtil
import com.cqcsy.library.utils.ImageUtil
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/10
 *
 *
 */
class AlbumAdapter(
    private val maxChooseCount: Int,
    private val maxVideoCount: Int,
    private val chooseModel: ChooseMode,
    var currentMediaType: MediaType,
) : BaseQuickAdapter<LocalMediaBean, BaseViewHolder>(R.layout.item_album_image), LoadMoreModule {
    var currentChooseList: MutableList<LocalMediaBean> = mutableListOf()

    override fun convert(holder: BaseViewHolder, item: LocalMediaBean) {
        if (item.mimeType.endsWith("gif")) {
            ImageUtil.loadGif(
                context,
                item.path,
                holder.getView(R.id.image),
                ImageView.ScaleType.CENTER_CROP,
                true
            )
        } else {
            ImageUtil.loadImage(
                context,
                item.path,
                holder.getView(R.id.image),
                0,
                scaleType = ImageView.ScaleType.CENTER_CROP
            )
        }
        if (item.isVideo) {
            holder.setGone(R.id.duration, false)
                .setGone(R.id.check_container, true)
                .setText(
                    R.id.duration, String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(item.duration),
                        TimeUnit.MILLISECONDS.toSeconds(item.duration)
                                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(item.duration))
                    )
                )
        } else {
            holder.setGone(R.id.duration, true)
                .setGone(R.id.check_container, false)
        }
        val ivMask = holder.getView<ImageView>(R.id.iv_mask)
        if (getSelectCount() >= maxChooseCount) {
            ivMask.isVisible = !item.isChecked
        } else {
            if (chooseModel == ChooseMode.ONLY) {
                if (currentMediaType == MediaType.PHOTO) { //只能选择图片，把不是图片的全部蒙上蒙层
                    ivMask.isVisible = !MimeTypeUtil.isImage(item.mimeType)
                } else if (currentMediaType == MediaType.VIDEO) {
                    if (!MimeTypeUtil.isVideo(item.mimeType)) {
                        ivMask.isVisible = true
                    } else {
                        ivMask.isVisible = getSelectCount() >= maxVideoCount && !item.isChecked
                    }
                } else {
                    ivMask.isVisible = false
                }
            } else {
                ivMask.isVisible = false
            }
        }
        val checkBox = holder.getView<CheckedTextView>(R.id.checkbox)
        checkBox.isChecked = item.isChecked
        if (item.isChecked) {
            checkBox.text = item.index.toString()
        } else {
            checkBox.text = ""
        }
    }

    fun getSelectCount(): Int {
        return currentChooseList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}