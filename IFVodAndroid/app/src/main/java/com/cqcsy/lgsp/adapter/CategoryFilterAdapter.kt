package com.cqcsy.lgsp.adapter

import android.content.Intent
import android.widget.LinearLayout
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil

/**
 * 导航筛选结果item适配器
 */
class CategoryFilterAdapter :
    BaseQuickAdapter<MovieModuleBean, BaseViewHolder>(R.layout.item_category_filter, ArrayList()) {
    override fun convert(holder: BaseViewHolder, item: MovieModuleBean) {
        holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            context.startActivity(intent)
        }
        holder.setText(R.id.scoreText, item.score)
        holder.setText(R.id.itemPlayCount, NormalUtil.formatPlayCount(item.playCount))
        holder.setText(R.id.itemTitle, item.title)
        holder.setText(R.id.itemType, item.cidMapper?.replace(",", " "))
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                context,
                it, holder.getView(R.id.itemContentImage)
            )
        }
        if (item.updateCount > 0) {
            holder.setVisible(R.id.itemUpdateCount, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.itemUpdateCount, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.itemUpdateCount, item.updateCount.toString())
            }
        } else {
            holder.setVisible(R.id.itemUpdateCount, false)
        }
        // 是否显示推荐按钮
        if (item.isRecommend) {
            holder.setVisible(R.id.itemRecommendImage, true)
        } else {
            holder.setVisible(R.id.itemRecommendImage, false)
        }
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }
}