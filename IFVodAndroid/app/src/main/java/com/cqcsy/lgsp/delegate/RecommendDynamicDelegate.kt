package com.cqcsy.lgsp.delegate

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.RecommendMultiBean
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.ImageUtil
import com.drakeet.multitype.ItemViewBinder

/**
 ** 2022/12/6
 ** des：首页-推荐动态
 **/

class RecommendDynamicDelegate : ItemViewBinder<RecommendMultiBean, RecDynamicViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): RecDynamicViewHolder {
        val view = inflater.inflate(R.layout.layout_recommend_dynamic, parent, false)
        return RecDynamicViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecDynamicViewHolder, item: RecommendMultiBean) {
        val context = holder.itemView.context
        if (item.description.isNullOrEmpty()) {
            holder.dynamicDes.visibility = View.GONE
        } else {
            holder.dynamicDes.visibility = View.VISIBLE
            holder.dynamicDes.text = Html.fromHtml(item.description.replace("\n", "<br>"))
        }
        if (item.focusStatus) {
            holder.focusedUser.visibility = View.VISIBLE
        } else {
            holder.focusedUser.visibility = View.GONE
        }
        ImageUtil.loadCircleImage(context, item.headImg, holder.userImage)
        holder.viewCount.text = NormalUtil.formatPlayCount(item.viewCount)
        holder.userName.text = item.upperName
        val imageView = holder.image
        var scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY
        val imageWidth = imageView.width
        var imageHeight = imageView.width
        if (item.photoType == 2) {
            holder.dynamicTag.isVisible = true
            holder.dynamicTag.setImageResource(R.mipmap.icon_video_tag)
//            imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
//                imageHeight = imageWidth
//                this.dimensionRatio = "h,1:1"
//            }
        } else if (item.isLongImage) {
            holder.dynamicTag.isVisible = true
            holder.dynamicTag.setImageResource(R.mipmap.icon_long_image_flag)
        } else if (item.coverImgUrl?.contains(".gif", true) == true) {
            holder.dynamicTag.isVisible = true
            holder.dynamicTag.setImageResource(R.mipmap.icon_gif_tag)
        } else {
            holder.dynamicTag.isVisible = false
        }
        imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (item.imageRatioValue < 3f / 4f) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                imageHeight = (imageWidth * 4f / 3f).toInt()
                this.dimensionRatio = "h,3:4"
            } else if (item.imageRatioValue > 4f / 3f) {
                scaleType = ImageView.ScaleType.CENTER_CROP
                imageHeight = (imageWidth * 3f / 4f).toInt()
                this.dimensionRatio = "h,4:3"
            } else {
                scaleType = ImageView.ScaleType.FIT_XY
                imageHeight = (imageWidth / item.imageRatioValue).toInt()
                this.dimensionRatio = "h,${item.ratio}"
            }
        }
//        }
        ImageUtil.loadImage(
            context,
            item.coverImgUrl,
            imageView,
            corner = 2,
            scaleType = scaleType,
            imageWidth = imageWidth,
            imageHeight = imageHeight
        )
        holder.itemView.setOnClickListener {
            val bean = DynamicBean().copy(item)
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey
                dynamicVideoList = mutableListOf(bean)
                dynamicType = item.photoType
            }
        }
    }
}

class RecDynamicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val image: ImageView = view.findViewById(R.id.dynamic_cover)
    val dynamicTag: ImageView = view.findViewById(R.id.dynamic_tag)
    val focusedUser: TextView = view.findViewById(R.id.focus_user)
    val dynamicDes: TextView = view.findViewById(R.id.dynamic_des)
    val userImage: ImageView = view.findViewById(R.id.user_image)
    val userName: TextView = view.findViewById(R.id.user_name)
    val viewCount: TextView = view.findViewById(R.id.view_count)
}