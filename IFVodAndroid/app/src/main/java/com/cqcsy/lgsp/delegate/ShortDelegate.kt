package com.cqcsy.lgsp.delegate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/6
 ** des：小视频样式
 **/

class ShortDelegate : ItemViewBinder<MovieModuleBean, ShortViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ShortViewHolder {
        val view = inflater.inflate(R.layout.layout_short_item, parent, false)
        return ShortViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShortViewHolder, item: MovieModuleBean) {
        val context = holder.itemView.context
        holder.itemView.setOnClickListener { v: View? ->
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            context.startActivity(intent)
        }
        holder.playCount.text = NormalUtil.formatPlayCount(item.playCount)
        holder.title.text = item.title
        holder.duration.text = item.duration
        item.coverImgUrl?.let { ImageUtil.loadImage(context, it, holder.image) }

        holder.hotTag.isVisible = item.isHot
        holder.liveState.isVisible = item.maintainStatus
    }

}

class ShortViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val hotTag: ImageView = view.findViewById(R.id.hot_tag)
    val image: ImageView = view.findViewById(R.id.short_image)
    val playCount: TextView = view.findViewById(R.id.play_count)
    val duration: TextView = view.findViewById(R.id.duration)
    val title: TextView = view.findViewById(R.id.short_title)
    val liveState: TextView = view.findViewById(R.id.live_repair)
}