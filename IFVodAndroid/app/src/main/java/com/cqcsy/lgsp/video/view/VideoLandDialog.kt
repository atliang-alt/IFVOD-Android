package com.cqcsy.lgsp.video.view

import android.content.Context
import android.content.DialogInterface
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.AnthologyDataUtil
import com.cqcsy.library.views.BottomBaseDialog

/**
 * 播放器横屏全屏弹窗
 */
class VideoLandDialog(override var context: Context) : IVideoDialog {

    override var bottomDialog: BottomBaseDialog? = null

    override var videoMenuDialog: VideoMenuDialog? = null

    override var isVertical: Boolean = false

    override fun showGroupEpisode(
        menuColumn: Int,
        currentPlayVideoBean: VideoBaseBean,
        episodeList: MutableList<VideoBaseBean>,
        stickyListener: VideoStickySelectDialog.OnItemSelectListener,
        clickListener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        videoMenuDialog = VideoStickySelectDialog(context)
        (videoMenuDialog as VideoStickySelectDialog).setMenuColumn(menuColumn)
        (videoMenuDialog as VideoStickySelectDialog).setData(
            AnthologyDataUtil.getGroupData(
                currentPlayVideoBean.videoType,
                episodeList,
                currentPlayVideoBean.uniqueID
            )
        )
        (videoMenuDialog as VideoStickySelectDialog).currentPlay = currentPlayVideoBean
        (videoMenuDialog as VideoStickySelectDialog).setItemClickListener(stickyListener)
    }
}