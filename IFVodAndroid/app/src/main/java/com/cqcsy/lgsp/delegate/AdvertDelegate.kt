package com.cqcsy.lgsp.delegate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.ImageUtil

/**
 ** 2022/12/7
 ** des：页面中间大幅广告
 **/

class AdvertDelegate : FullDelegate<AdvertBean, AdvertViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): AdvertViewHolder {
        val view = inflater.inflate(R.layout.layout_advert_large, parent, false)
        return AdvertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdvertViewHolder, item: AdvertBean) {
        val context = holder.itemView.context
        ImageUtil.loadImage(holder.itemView.context, item.showURL, holder.image)
        holder.title.text = item.title
        holder.imageClose.setOnClickListener {
            // TODO 移除事件处理
        }
        holder.itemView.setOnClickListener {
            if (JumpUtils.isJumpHandle(item.appParam)) {
                JumpUtils.jumpAnyUtils(context, item.appParam!!)
            } else if (!item.linkURL.isNullOrEmpty()) {
                val intent = Intent(context, WebViewActivity::class.java)
                intent.putExtra(WebViewActivity.urlKey, item.linkURL)
                context.startActivity(intent)
            } else if (item.mediaItem != null) {
                val intent = Intent(context, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item.mediaItem)
                context.startActivity(intent)
            }
        }
    }
}

class AdvertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val imageClose: ImageView = view.findViewById(R.id.image_close)
    val title: TextView = view.findViewById(R.id.title)
}