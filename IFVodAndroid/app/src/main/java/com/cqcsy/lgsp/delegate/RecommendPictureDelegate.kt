package com.cqcsy.lgsp.delegate

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.upper.pictures.PictureListActivity
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.views.SquareImageView
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/6
 ** des：首页-推荐相册
 **/

class RecommendPictureDelegate : ItemViewBinder<RecommendMultiBean, RecPictureViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecPictureViewHolder {
        val view = inflater.inflate(R.layout.layout_recommend_picture_item, parent, false)
        return RecPictureViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecPictureViewHolder, item: RecommendMultiBean) {
        val context = holder.itemView.context
        holder.viewCount.text = NormalUtil.formatPlayCount(item.viewCount)
        holder.userName.text = item.upperName
        holder.pictureName.text = item.title
        if (item.description.isNullOrEmpty()) {
            holder.pictureDes.isVisible = false
        } else {
            holder.pictureDes.isVisible = true
            holder.pictureDes.text = Html.fromHtml(item.description.replace("\n", "<br>"))
        }
        holder.focusedUser.isVisible = item.focusStatus
        ImageUtil.loadCircleImage(context, item.headImg, holder.userImage)
        ImageUtil.loadImage(context, item.coverImgUrl, holder.image)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PictureListActivity::class.java)
            intent.putExtra(UpperPicturesFragment.PICTURES_PID, item.mediaKey)
            intent.putExtra(UpperPicturesFragment.PICTURES_TITLE, item.title)
            context.startActivity(intent)
        }
    }
}

class RecPictureViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: SquareImageView = view.findViewById(R.id.picture_cover)
    val focusedUser: TextView = view.findViewById(R.id.focus_user)
    val pictureName: TextView = view.findViewById(R.id.picture_name)
    val pictureDes: TextView = view.findViewById(R.id.picture_des)
    val userImage: ImageView = view.findViewById(R.id.user_image)
    val userName: TextView = view.findViewById(R.id.user_name)
    val viewCount: TextView = view.findViewById(R.id.view_count)
}