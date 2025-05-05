package com.cqcsy.lgsp.upper.chat

import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.*
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import java.io.Serializable
import kotlin.math.abs

/**
 * 私信适配器
 */
class MessageAdapter(data: MutableList<ChatMessageBean>) : BaseMultiItemQuickAdapter<ChatMessageBean, BaseViewHolder>(data) {
    private val divider = 5 * 60 * 1000 //消息间隔时间
    var fromUserImage: String = ""

    init {
        addItemType(1, R.layout.layout_chat_from)
        addItemType(2, R.layout.layout_chat_to)
    }

    override fun convert(holder: BaseViewHolder, item: ChatMessageBean) {
        val imageUrl: String? = if (item.fromUid == GlobalValue.userInfoBean?.id) {
            GlobalValue.userInfoBean?.avatar
        } else {
            fromUserImage
        }
        ImageUtil.loadCircleImage(context, imageUrl ?: "", holder.getView(R.id.userLogo))
        holder.getView<ImageView>(R.id.userLogo).setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.fromUid)
            context.startActivity(intent)
        }
        holder.setGone(R.id.failedRefresh, true)
        if (item.messageType == 8) {
            holder.setGone(R.id.message, true)
            holder.setGone(R.id.messageImg, false)
            holder.setGone(R.id.messageExpression, true)
            setImageMsg(holder, item)
        } else {
            holder.setGone(R.id.message, false)
            holder.setGone(R.id.messageImg, true)
            holder.setGone(R.id.imageSending, true)
            setNormalMsg(holder, item)
        }
        val serviceTag = holder.getView<ImageView>(R.id.serviceTag)
        if (item.type == 1) {
            serviceTag.visibility = View.VISIBLE
        } else {
            serviceTag.visibility = View.GONE
        }
        val time = holder.getView<TextView>(R.id.message_time)
        val date = TimesUtils.formatDate(item.sendTime)
        time.text = TimeUtils.date2String(date, "yyyy-MM-dd HH:mm:ss")
        val position = holder.adapterPosition
        if (position == 0) {
            time.visibility = View.VISIBLE
        } else {
            val last = TimesUtils.formatDate(getItem(position - 1).sendTime)
            if (abs(last!!.time - date!!.time) >= divider) {
                time.visibility = View.VISIBLE
            } else {
                time.visibility = View.GONE
            }
        }
    }

    private fun setImageMsg(holder: BaseViewHolder, item: ChatMessageBean) {
        val status = holder.getView<RelativeLayout>(R.id.imageSending)
        val imageView = holder.getView<ImageView>(R.id.messageImg)
        val refresh = holder.getView<ImageView>(R.id.failedRefresh)
        refresh.setOnClickListener { notifyItemChanged(holder.layoutPosition) }
        refresh.visibility = View.GONE

        var imageUrl = item.context
        if (!item.localPath.isNullOrEmpty()) {
            imageUrl = item.localPath!!
            when (item.pictureStatus) {
                PictureUploadStatus.FINISH -> {
                    status.visibility = View.GONE
                    holder.setGone(R.id.loadingProgress, true)
                    holder.setGone(R.id.sendFailed, true)
                }

                PictureUploadStatus.ERROR -> {
                    status.visibility = View.VISIBLE
                    holder.setGone(R.id.sendFailed, false)
                    holder.setGone(R.id.loadingProgress, true)
                }

                else -> {
                    status.visibility = View.VISIBLE
                    holder.setGone(R.id.loadingProgress, false)
                    holder.setGone(R.id.sendFailed, true)
                }
            }
        } else {
//            imageUrl +="?width=$maxWith&scale=both"
            status.visibility = View.GONE
            holder.setGone(R.id.loadingProgress, true)
            holder.setGone(R.id.sendFailed, true)
        }
        ImageUtil.loadImage(context, imageUrl, imageView, requestListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                imageView.scaleType = ImageView.ScaleType.CENTER
                imageView.setImageResource(R.mipmap.icon_chat_image_load_failed)
                holder.setGone(R.id.failedRefresh, false)
                return true
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageDrawable(resource)
                holder.setGone(R.id.failedRefresh, true)
                return true
            }

        })
        imageView.setOnClickListener {
            showAllPictures(holder.adapterPosition)
        }
    }

    private fun showAllPictures(position: Int) {
        val pictures: MutableList<String> = ArrayList()
        var showPosition = 0
        for ((index, temp) in this.data.withIndex()) {
            if (temp.messageType == 8 && temp.context.isNotEmpty()) {
                pictures.add(temp.context)
                if (index < position) {
                    showPosition++
                }
            }
        }
        val intent = Intent(context, PictureViewerActivity::class.java)
        intent.putExtra(PictureViewerActivity.SHOW_INDEX, showPosition)
        intent.putExtra(PictureViewerActivity.SHOW_COUNTS, pictures.size)
        intent.putExtra(PictureViewerActivity.SHOW_URLS, pictures as Serializable)
        context.startActivity(intent)
    }

    private fun setNormalMsg(holder: BaseViewHolder, item: ChatMessageBean) {
        val message = holder.getView<TextView>(R.id.message)
        val messageExpression = holder.getView<ImageView>(R.id.messageExpression)
        val isVipText = SpanStringUtils.isVipText(item.context)
        if (isVipText) {
            message.visibility = View.GONE
            messageExpression.visibility = View.VISIBLE
            ImageUtil.loadGif(context, EmotionUtils.getVipImgByName(item.context), messageExpression, ImageView.ScaleType.FIT_END, true)
        } else {
            message.visibility = View.VISIBLE
            messageExpression.visibility = View.GONE
            if (SpanStringUtils.hasEmoji(item.context)) {
                message.text = SpanStringUtils.getEmotionContent(context, 14f, item.context)
            } else {
                message.text = Html.fromHtml(item.context.replace("\n", "<br/>"))
            }
        }
    }
}