package com.cqcsy.lgsp.video.view

import android.content.Context
import android.content.DialogInterface
import com.blankj.utilcode.util.ScreenUtils
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.AnthologyDataUtil
import com.cqcsy.library.views.BottomBaseDialog

/**
 ** 2023/12/22
 ** des：播放器竖屏全屏弹窗
 **/

class VideoPortraitDialog(override var context: Context) : IVideoDialog {

    override var bottomDialog: BottomBaseDialog? = null

    override var videoMenuDialog: VideoMenuDialog? = null

    override var isVertical: Boolean = true

    override fun showGroupEpisode(
        menuColumn: Int,
        currentPlayVideoBean: VideoBaseBean,
        episodeList: MutableList<VideoBaseBean>,
        stickyListener: VideoStickySelectDialog.OnItemSelectListener,
        clickListener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        val groupData = AnthologyDataUtil.getGroupData(
            currentPlayVideoBean.videoType,
            episodeList,
            currentPlayVideoBean.uniqueID
        )
        videoMenuDialog = VideoEpisodeGroupDialog(context, currentPlayVideoBean, groupData, stickyListener)
        videoMenuDialog?.isVertical = isVertical
        videoMenuDialog?.setHeight(ScreenUtils.getAppScreenWidth())
        videoMenuDialog?.setMenuColumn(menuColumn)
        videoMenuDialog?.show()
    }
}