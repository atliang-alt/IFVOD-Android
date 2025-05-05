package com.cqcsy.lgsp.base

import android.os.Handler
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.event.ListToFullEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.shuyu.gsyvideoplayer.GSYVideoManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

abstract class BaseVideoListFragment : RefreshDataFragment<ShortVideoBean>() {
    private var isPausedByOnPause = false
    private var isToFull = false

    override fun getItemLayout(): Int {
        return R.layout.layout_video_list_item
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<ShortVideoBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<ShortVideoBean>>() {}.type
        )
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(XLinearBuilder(recyclerView.context).setSpacing(10f).build())
    }

    open fun isShowPlayCount(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        startPlay()
    }

    override fun onPause() {
        super.onPause()
        pausePlay()
    }

    private fun startPlay() {
        val player = VideoListItemHolder.getCurrentPlayer()
        if (player != null && player.isInPlayingState && isPausedByOnPause && !isToFull) {
            Handler().postDelayed({
                player.currentPlayer.startAfterPrepared()
            }, 500)
        }
        isPausedByOnPause = false
        isToFull = false
    }

    private fun pausePlay() {
        val player = VideoListItemHolder.getCurrentPlayer()
        if (!isToFull && player != null && player.currentPlayer.isPlaying) {
            player.onVideoPause()
            isPausedByOnPause = true
        }
    }

    override fun onVisible() {
        super.onVisible()
        startPlay()
    }

    override fun onInvisible() {
        super.onInvisible()
        pausePlay()
    }

    override fun onDestroy() {
        GSYVideoManager.instance().stop()
        GSYVideoManager.releaseAllVideos()
        super.onDestroy()
    }

    override fun setItemView(holder: BaseViewHolder, item: ShortVideoBean, position: Int) {
        holder.setGone(R.id.video_publish_time, true)
        val videoListItemHolder = VideoListItemHolder(requireActivity())
        videoListItemHolder.isShowPlayCount = isShowPlayCount()
        videoListItemHolder.setItemView(holder, item)
    }

    override fun setAdapter() {
        super.setAdapter()
        getRecyclerView().addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var firstVisibleItem = 0
            var lastVisibleItem = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem =
                    (getRecyclerView().layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                lastVisibleItem =
                    (getRecyclerView().layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                //大于0说明有播放
                if (GSYVideoManager.instance().playPosition >= 0) { //当前播放的位置
                    val position = GSYVideoManager.instance().playPosition
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().playTag == BaseVideoListFragment::class.java.simpleName && (position < firstVisibleItem || position > lastVisibleItem)) {
                        if (!GSYVideoManager.isFullState(activity)) {
                            GSYVideoManager.instance().stop()
                            GSYVideoManager.releaseAllVideos()
                        }
                    }
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoActionResultEvent(event: VideoActionResultEvent) {
        if (event.type != 1 && event.type != 4) {
            return
        }
        val layoutManager = refreshRecyclerView.layoutManager as LinearLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        for ((index, bean) in getDataList().withIndex()) {
            if (bean.userId.toString() == event.id) {
                bean.focusStatus = event.action == VideoActionResultEvent.ACTION_ADD
                if (index in (first..last)) {
                    val btnAttention = layoutManager.findViewByPosition(index)
                        ?.findViewById<Button>(R.id.btn_attention)
                    btnAttention?.isSelected = bean.focusStatus
                    if (btnAttention?.isSelected == true) {
                        btnAttention.setText(R.string.followed)
                    } else {
                        btnAttention?.setText(R.string.attention)
                    }
                }
            } else if (bean.mediaKey == event.id) {
                val favorites = VideoLikeBean()
                favorites.count = event.count
                favorites.selected = event.selected
                bean.favorites = favorites
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentEvent(event: CommentEvent) {
        for ((index, bean) in getDataList().withIndex()) {
            if (bean.mediaKey == event.mediaKey) {
                bean.comments++
                mAdapter?.notifyItemChanged(index)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onWatchRecord(record: WatchRecordBean) {
        for ((index, bean) in getDataList().withIndex()) {
            if (bean.mediaKey == record.mediaKey) {
                bean.watchingProgress = record.watchTime.toLong()
                mAdapter?.notifyItemChanged(index)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onListToFull(event: ListToFullEvent) {
        isToFull = true
    }

}