package com.cqcsy.lgsp.video.player

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.bean.net.VideoIntroductionNetBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.ShareCountEvent
import com.cqcsy.lgsp.event.VideoActionEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.IVideoController
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XLinearBuilder
import kotlinx.android.synthetic.main.layout_short_full_complete.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 小视频播放完后全屏推荐
 */
class RecommendShortView(
    context: Context,
    val player: LiteVideoPlayer,
    val controller: IVideoController?,
    val recommendList: MutableList<ShortVideoBean>?,
    val videoDetail: VideoIntroductionNetBean?
) :
    LinearLayout(context), View.OnClickListener {

    init {
        tag = this::class.java.simpleName
        orientation = VERTICAL
        View.inflate(context, R.layout.layout_short_full_complete, this)
        setListener()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
        ImageUtil.loadCircleImage(context, videoDetail?.userInfo?.avatar, uploadByImage)
        upperName.text = videoDetail?.userInfo?.nickName
        setFollow(videoDetail?.focusStatus ?: false, videoDetail?.isBlackList ?: false)
        if (videoDetail?.userInfo?.bigV == true || (videoDetail?.userInfo?.vipLevel ?: 0) > 0) {
            userVip.visibility = View.VISIBLE
            userVip.setImageResource(
                if (videoDetail?.userInfo?.bigV == true) R.mipmap.icon_big_v
                else VipGradeImageUtil.getVipImage(videoDetail?.userInfo?.vipLevel ?: 0)
            )
        } else {
            userVip.visibility = View.GONE
        }
        photoLayout.setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, videoDetail?.userInfo?.id)
            context.startActivity(intent)
        }
        setLikeView(videoDetail?.like)
        setDisLikeView(videoDetail?.disLike)
        setCollectView(videoDetail?.favorites)
        setShareCountView(videoDetail?.detailInfo?.shareCount)
        setRecommendList()
    }

    override fun onDetachedFromWindow() {
        EventBus.getDefault().unregister(this)
        super.onDetachedFromWindow()
    }

    private fun setLikeView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoFabulousImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoFabulousCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoFabulousCount.text = resources.getString(R.string.fabulous)
        }
    }

    private fun setDisLikeView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoDebunkImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoDebunkCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoDebunkCount.text = resources.getString(R.string.unlike)
        }
    }

    private fun setCollectView(videoLikeBean: VideoLikeBean?) {
        if (videoLikeBean == null) {
            return
        }
        videoCollectionImage.isSelected = videoLikeBean.selected
        if (videoLikeBean.count > 0) {
            videoCollectionCount.text = NormalUtil.formatPlayCount(videoLikeBean.count)
        } else {
            videoCollectionCount.text = resources.getString(R.string.collection)
        }
    }

    private fun setFollow(isFocus: Boolean, isInBlackList: Boolean) {
        if (isInBlackList) {
            followText.isVisible = false
            blackList.isVisible = true
        } else {
            blackList.isVisible = false
            followText.isVisible = true
            followText.isSelected = isFocus
            followText.text = if (isFocus) {
                resources.getString(R.string.followed)
            } else {
                resources.getString(R.string.attention)
            }
        }
    }

    private fun setListener() {
        replayLayout.setOnClickListener(this)
        videoFabulousLayout.setOnClickListener(this)
        videoDebunkLayout.setOnClickListener(this)
        videoCollectionLayout.setOnClickListener(this)
        downloadVideo.setOnClickListener(this)
        videoDetailShare.setOnClickListener(this)
        followText.setOnClickListener(this)
        blackList.setOnClickListener(this)
    }

    private fun setRecommendList() {
        if (!recommendList.isNullOrEmpty()) {
            val adapterData = if (recommendList.size > 20) {
                recommendList.subList(0, 20)
            } else {
                recommendList
            }
            recommendRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recommendRecyclerView.addItemDecoration(XLinearBuilder(context).setSpacing(15f).setShowFirstTopLine(true).build())
            val adapter = object :
                BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(
                    R.layout.layout_player_recommend_short,
                    adapterData
                ) {
                override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
                    holder.setText(
                        R.id.popularPlayCount,
                        NormalUtil.formatPlayCount(item.playCount)
                    )
                    holder.setText(R.id.popularTitle, item.title)
                    holder.setText(R.id.popularTime, item.duration)
                    item.coverImgUrl?.let {
                        ImageUtil.loadImage(
                            context,
                            it, holder.getView(R.id.popularContentImage)
                        )
                    }
                    if (item.isHot) {
                        holder.setVisible(R.id.popularHot, true)
                    } else {
                        holder.setVisible(R.id.popularHot, false)
                    }
                }
            }
            adapter.setOnItemClickListener { adapter, view, position ->
                controller?.playShortVideo(
                    adapter.getItem(position) as ShortVideoBean
                )
            }
            recommendRecyclerView.adapter = adapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.replayLayout -> {
                player.currentPlayer.startPlay()
            }
            R.id.videoFabulousLayout -> {
                EventBus.getDefault().post(VideoActionEvent(2))
            }
            R.id.videoDebunkLayout -> {
                EventBus.getDefault().post(VideoActionEvent(3))
            }
            R.id.videoCollectionLayout -> {
                EventBus.getDefault().post(VideoActionEvent(4))
            }
            R.id.downloadVideo -> {
                controller?.exitFullScreen()
                controller?.onDownloadClick()
            }
            R.id.videoDetailShare -> {
                controller?.onShareClick()
            }
            R.id.followText -> {
                EventBus.getDefault().post(VideoActionEvent(1))
            }
            R.id.blackList -> {
                EventBus.getDefault().post(VideoActionEvent(5))
            }
        }
    }

    private fun setShareCountView(count: Int?) {
        if (count != null && count > 0) {
            shareCount.text = NormalUtil.formatPlayCount(count)
        } else {
            shareCount.text = resources.getString(R.string.share)
        }
    }

    /**
     * 1：关注
     * 2：点赞
     * 3：踩
     * 4：收藏
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onActionResult(event: VideoActionResultEvent) {
        when (event.type) {
            1 -> {
                setFollow(event.selected, false)
            }
            2 -> {
                val like = VideoLikeBean()
                like.count = event.count
                like.selected = event.selected
                setLikeView(like)
                if (event.selected && context is Activity) {
                    ImageUtil.clickAnim(context as Activity, videoFabulousImage)
                }
            }
            3 -> {
                val dislike = VideoLikeBean()
                dislike.count = event.count
                dislike.selected = event.selected
                setDisLikeView(dislike)
            }
            4 -> {
                val collect = VideoLikeBean()
                collect.count = event.count
                collect.selected = event.selected
                setCollectView(collect)
                if (event.selected && context is Activity) {
                    ImageUtil.clickAnim(context as Activity, videoCollectionImage)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        if (videoDetail?.userInfo?.id == event.uid) {
            videoDetail.isBlackList = event.status
            setFollow(false, event.status)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShareEvent(event: ShareCountEvent) {
        videoDetail?.detailInfo?.shareCount = event.count
        setShareCountView(event.count)
    }
}