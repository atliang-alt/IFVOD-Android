package com.cqcsy.lgsp.main.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshListActivity
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import org.json.JSONArray

/**
 * 正在追详情页面
 */
class FollowingDetailsActivity : RefreshListActivity<MovieModuleBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.following)
        emptyLargeTip.text = getString(R.string.noData)
        emptyLittleTip.visibility = View.GONE
    }

    override fun getUrl(): String {
        return RequestUrls.FOLLOWING_LIST
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(this, 3)
    }

    override fun getParamsSize(): Int {
        return 15
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XGridBuilder(this).setVLineSpacing(10f).setHLineSpacing(10f).setIncludeEdge(true).build())
    }

    override fun getItemLayout(): Int {
        return R.layout.item_following_details
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MovieModuleBean> {
        return Gson().fromJson(jsonArray.toString(), object : TypeToken<MutableList<MovieModuleBean>>() {}.type)
    }

    override fun setItemView(holder: BaseViewHolder, item: MovieModuleBean, position: Int) {
        val total = TimesUtils.formatTime(item.duration)
        val progress = holder.getView<ProgressBar>(R.id.itemFollowingProgressBar)
        if (GlobalValue.isVipUser()) {
            progress.visibility = View.VISIBLE
            progress.progress = if (total != 0) {
                (item.watchingProgress * 100 / total).toInt()
            } else {
                0
            }
        } else {
            progress.visibility = View.GONE
        }
        holder.setText(R.id.itemFollowingTitle, item.title)
        if (item.seriesWatchProgress.isEmpty()) {
            holder.setVisible(R.id.itemFollowingPosition, false)
        } else {
            holder.setText(
                R.id.itemFollowingPosition,
                StringUtils.getString(R.string.watch_progress, item.seriesWatchProgress)
            )
            holder.setVisible(R.id.itemFollowingPosition, true)
        }
        item.coverImgUrl?.let {
            ImageUtil.loadImage(
                this, it, holder.getView(R.id.itemFollowingImage)
            )
        }
        if (item.updateCount > 0) {
            holder.setVisible(R.id.itemFollowingUpdateCount, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.itemFollowingUpdateCount, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.itemFollowingUpdateCount, item.updateCount.toString())
            }
        } else {
            holder.setVisible(R.id.itemFollowingUpdateCount, false)
        }
        holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
            val intent = Intent(this, VideoPlayVerticalActivity::class.java)
            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
            startActivity(intent)
        }
    }
}