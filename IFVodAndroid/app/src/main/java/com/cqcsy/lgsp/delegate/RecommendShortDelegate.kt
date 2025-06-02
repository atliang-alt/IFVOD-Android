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
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/6
 ** des：首页-推荐小视频
 **/

class RecommendShortDelegate : ItemViewBinder<RecommendMultiBean, RecShortViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecShortViewHolder {
        val view = inflater.inflate(R.layout.layout_recommend_short_video, parent, false)
        return RecShortViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecShortViewHolder, item: RecommendMultiBean) {
        ImageUtil.loadImage(holder.itemView.context, item.coverImgUrl, holder.image)
        holder.playCount.text = NormalUtil.formatPlayCount(item.viewCount)
        holder.title.text = item.title
        holder.duration.text = item.duration
//        if (item.cidMapper.isNullOrEmpty()) {
//            holder.videoType.visibility = View.GONE
//        } else {
//            holder.videoType.visibility = View.VISIBLE
//            holder.videoType.text = item.cidMapper
//        }
        holder.upperImage.isVisible = true
        ImageUtil.loadCircleImage(holder.itemView.context, item.headImg, holder.upperImage)
        holder.videoType.text = item.upperName
        if (item.isHot) {
            holder.hotTag.visibility = View.VISIBLE
        } else {
            holder.hotTag.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { v: View? ->
            val context = holder.itemView.context
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, item.mediaKey)
            intent.putExtra(VideoBaseActivity.VIDEO_TYPE, item.videoType)
            context.startActivity(intent)
        }
    }

}

class RecShortViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val hotTag: ImageView = view.findViewById(R.id.hot_tag)
    val playCount: TextView = view.findViewById(R.id.play_count)
    val duration: TextView = view.findViewById(R.id.duration)
    val title: TextView = view.findViewById(R.id.title)
    val videoType: TextView = view.findViewById(R.id.video_type)
    val upperImage: ImageView = view.findViewById(R.id.upper_image)

}