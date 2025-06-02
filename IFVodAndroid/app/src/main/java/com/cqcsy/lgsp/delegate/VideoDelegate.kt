package com.cqcsy.lgsp.delegate

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/6
 ** des：普通视频样式
 **/

class VideoDelegate : ItemViewBinder<MovieModuleBean, VideoViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VideoViewHolder {
        val view = inflater.inflate(R.layout.layout_video_item, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, item: MovieModuleBean) {
        holder.score.text = item.score
        holder.playCount.text = NormalUtil.formatPlayCount(item.playCount)
        holder.title.text = item.title
        holder.videoType.text = item.cidMapper?.replace(",", " ")
        item.coverImgUrl?.let { ImageUtil.loadImage(holder.itemView.context, it, holder.image) }
        // 电影需隐藏全集或更新到几期的标签
        if (item.videoType != Constant.VIDEO_MOVIE) {
            holder.updateDes.text = item.updateStatus
        } else {
            holder.updateDes.text = ""
        }
        if (item.updateCount > 0) {
            holder.updateCount.visibility = View.VISIBLE
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.updateCount.text = StringUtils.getString(R.string.newTips)
            } else {
                holder.updateCount.text = item.updateCount.toString()
            }
        } else {
            holder.updateCount.visibility = View.GONE
        }
        // 是否显示推荐按钮
        if (item.isRecommend) {
            holder.recommendTag.visibility = View.VISIBLE
        } else {
            holder.recommendTag.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { v: View? ->
            val context = holder.itemView.context
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            context.startActivity(intent)
        }
    }
}

class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val recommendTag: ImageView = view.findViewById(R.id.recommend_tag)
    val updateCount: TextView = view.findViewById(R.id.update_count)
    val score: TextView = view.findViewById(R.id.video_score)
    val playCount: TextView = view.findViewById(R.id.play_count)
    val updateDes: TextView = view.findViewById(R.id.update_des)
    val title: TextView = view.findViewById(R.id.title)
    val videoType: TextView = view.findViewById(R.id.video_type)
}