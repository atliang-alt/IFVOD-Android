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
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil

/**
 ** 2022/12/6
 ** des：视频横幅，大图显示
 **/

class VideoFullDelegate : FullDelegate<MovieModuleBean, VideoFullViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VideoFullViewHolder {
        val view = inflater.inflate(R.layout.layout_video_full, parent, false)
        return VideoFullViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoFullViewHolder, item: MovieModuleBean) {
        holder.title.text = item.title
        holder.videoType.text = item.cidMapper?.replace(",", " ")
        holder.score.text = item.score
        holder.playCount.text = NormalUtil.formatPlayCount(item.playCount)
        if (item.videoType != Constant.VIDEO_MOVIE) {
            holder.updateDes.text = item.updateStatus
        } else {
            holder.updateDes.text = ""
        }
        item.coverImgUrl?.let { ImageUtil.loadImage(holder.itemView.context, it, holder.image) }
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
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            context.startActivity(intent)
        }
    }
}

class VideoFullViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.image)
    val updateCount: TextView = view.findViewById(R.id.update_count)
    val score: TextView = view.findViewById(R.id.video_score)
    val playCount: TextView = view.findViewById(R.id.play_count)
    val updateDes: TextView = view.findViewById(R.id.update_des)
    val title: TextView = view.findViewById(R.id.title)
    val videoType: TextView = view.findViewById(R.id.video_type)
}