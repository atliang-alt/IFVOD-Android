package com.cqcsy.lgsp.delegate

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.lgsp.delegate.util.HorizontalRecyclerViewHolder
import com.cqcsy.lgsp.delegate.util.ListWrapper
import com.cqcsy.lgsp.record.RecordActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XLinearBuilder

/**
 ** 2022/12/6
 ** des：正在追
 **/

class WatchingDelegate : FullDelegate<ListWrapper, HorizontalRecyclerViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): HorizontalRecyclerViewHolder {
        val view = inflater.inflate(R.layout.layout_only_recyclerview, parent, false)
        val holder = HorizontalRecyclerViewHolder(view)
        val layoutManager = LinearLayoutManager(parent.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        holder.recyclerView.layoutManager = layoutManager
        holder.recyclerView.addItemDecoration(XLinearBuilder(parent.context).setSpacing(10f).setShowFirstTopLine(true).setShowLastLine(true).build())
        return holder
    }

    override fun onBindViewHolder(watchingViewHolder: HorizontalRecyclerViewHolder, item: ListWrapper) {
        if (item.data.isNullOrEmpty() || item.data!![0] !is MovieModuleBean) {
            return
        }
        val adapter =
            object : BaseQuickAdapter<MovieModuleBean, BaseViewHolder>(R.layout.layout_watching_item, item.data as MutableList<MovieModuleBean>) {
                override fun convert(holder: BaseViewHolder, item: MovieModuleBean) {
                    holder.setText(R.id.update_des, item.updateStatus)
                    holder.setText(R.id.title, item.title)
                    holder.setText(R.id.play_count, NormalUtil.formatPlayCount(item.playCount))
                    holder.setText(
                        R.id.watching_progress, if (item.seriesWatchProgress.isNullOrEmpty()) {
                            ""
                        } else {
                            StringUtils.getString(R.string.watch_progress, item.seriesWatchProgress)
                        }
                    )
                    val updateCount: TextView = holder.getView(R.id.update_count)
                    if (item.updateCount > 0) {
                        updateCount.visibility = View.VISIBLE
                        if (item.videoType == Constant.VIDEO_MOVIE) {
                            updateCount.setText(R.string.newTips)
                        } else {
                            updateCount.text = item.updateCount.toString()
                        }
                    } else {
                        updateCount.visibility = View.GONE
                    }
                    item.coverImgUrl?.let {
                        ImageUtil.loadImage(holder.itemView.context, it, holder.getView(R.id.image))
                    }
                }

            }
        adapter.setOnItemClickListener { adapter, view, position ->
            val bean = adapter.getItem(position) as MovieModuleBean
            val context = watchingViewHolder.itemView.context
            val intent = Intent(context, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, bean)
            context.startActivity(intent)
        }
        adapter.addFooterView(getFooter(watchingViewHolder.itemView.context), orientation = LinearLayout.HORIZONTAL)
        watchingViewHolder.recyclerView.adapter = adapter
    }

    /**
     * 查看更多
     */
    private fun getFooter(context: Context): View {
        val imageView = ImageView(context)
        imageView.setBackgroundResource(R.mipmap.icon_following_more)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.width = SizeUtils.dp2px(140f)
        params.height = SizeUtils.dp2px(187f)
        imageView.layoutParams = params
        imageView.setOnClickListener {
            context.startActivity(Intent(context, RecordActivity::class.java))
        }
        return imageView
    }
}