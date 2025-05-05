package com.cqcsy.lgsp.video.view

import android.content.Context
import android.content.DialogInterface
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.bean.LanguageBean
import com.cqcsy.lgsp.video.bean.VideoItemBean
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.lgsp.views.dialog.ShareBoard
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.BottomBaseDialog
import com.kuaishou.akdanmaku.data.DanmakuItemData
import org.greenrobot.eventbus.EventBus

/**
 ** 2023/12/22
 ** des：播放器相关弹窗
 **/

interface IVideoDialog {
    val MAX_PAGE: Int
        get() = 50
    var bottomDialog: BottomBaseDialog?

    var videoMenuDialog: VideoMenuDialog?

    var isVertical: Boolean

    var context: Context

    fun showGroupEpisode(
        menuColumn: Int,
        currentPlayVideoBean: VideoBaseBean,
        episodeList: MutableList<VideoBaseBean>,
        stickyListener: VideoStickySelectDialog.OnItemSelectListener,
        clickListener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    )

    fun dismissDialog() {
        bottomDialog?.dismiss()
        videoMenuDialog?.dismiss()
    }

    fun isAllowHideChange(): Boolean {
        return bottomDialog == null || (bottomDialog is DanamaInputDialog && !(bottomDialog as DanamaInputDialog).isSetting())
    }

    fun isShowingDialog(): Boolean {
        return (videoMenuDialog != null && videoMenuDialog!!.isShowing) || (bottomDialog != null && bottomDialog!!.isShowing)
    }

    fun destroy() {
        dismissDialog()
        videoMenuDialog = null
        bottomDialog = null
    }

    /**
     * 切换语言
     */
    fun showLanguage(
        currentLanguage: String,
        languageList: MutableList<LanguageBean>,
        listener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        videoMenuDialog = VideoSelectDialog(context)
        videoMenuDialog?.isVertical = isVertical
        val list = ArrayList<VideoItemBean>()
        var id = 0
        val title = StringUtils.getString(R.string.language_select_options)
        languageList.forEach { i ->
            val temp = VideoItemBean()
            temp.id = id
            temp.text = i.name
            temp.isNormalMenu = true
            temp.isCurrent = i.name == currentLanguage
            temp.enableTitle = title
            list.add(temp)
            id++
        }
        (videoMenuDialog as VideoSelectDialog).setMenuData(list)
        (videoMenuDialog as VideoSelectDialog).setMenuClickListener(listener)
        (videoMenuDialog as VideoSelectDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as VideoSelectDialog).show()
    }

    /**
     * 选择倍速
     */
    fun showSpeed(currentSpeed: String, listener: VideoSelectDialog.OnMenuClickListener, dismissListener: DialogInterface.OnDismissListener) {
        videoMenuDialog = VideoSelectDialog(context)
        videoMenuDialog?.isVertical = isVertical
        val speeds = StringUtils.getStringArray(R.array.play_speed)
        val list = ArrayList<VideoItemBean>()
        val normal = StringUtils.getString(R.string.video_speed)
        val title = StringUtils.getString(R.string.speed_select_options)
        val enableTip = StringUtils.getString(R.string.enable_all_speed_tip)
        for ((id, i) in speeds.withIndex()) {
            val temp = VideoItemBean()
            temp.id = id
            temp.text = i
            temp.isNormalMenu = true
            temp.isCurrent = currentSpeed == i || (id == 3 && normal == currentSpeed)
            temp.isVip = id == 0
            temp.enbale = GlobalValue.isEnable(6) || id != 0
            temp.enableTitle = title
            temp.enableTip = enableTip
            list.add(temp)
        }
        (videoMenuDialog as VideoSelectDialog).setMenuData(list)
        (videoMenuDialog as VideoSelectDialog).setMenuClickListener(listener)
        (videoMenuDialog as VideoSelectDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as VideoSelectDialog).show()
    }

    /**
     * 选择清晰度
     */
    fun showClarity(
        currentClarityId: Int, clarityList: MutableList<ClarityBean>, isLive: Boolean,
        listener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        videoMenuDialog = VideoSelectDialog(context)
        videoMenuDialog?.isVertical = isVertical

        val list = ArrayList<VideoItemBean>()
        var id = 0
        val title = StringUtils.getString(R.string.clarity_select_options)
        val enableTip = StringUtils.getString(R.string.enable_all_clarity_tip)
        val tip = StringUtils.getString(if (isLive) R.string.vip_locked_tip else R.string.vip_locked_lv8_tip)
        clarityList.forEach { item ->
            val temp = VideoItemBean()
            temp.id = id
            temp.text = item.resolutionDes
            temp.isNormalMenu = true
            temp.enbale = !item.isVip || item.isBoughtByCoin || (!isLive && GlobalValue.isEnable(8)) || (isLive && GlobalValue.isVipUser())
            // 以下几种情况，清晰度可用
            // 1、非VIP清晰度
            // 2、已经用金币购买过
            // 3、不是直播，且是VIP或>=8级
            // 4、是直播，且是VIP
            temp.isVip = item.isVip
            temp.isCurrent = item.episodeId == currentClarityId
            temp.enableTitle = title
            temp.enableTip = enableTip
            temp.disableTip = tip
            temp.goldOpenNumber = item.goldOpenNumber
            list.add(temp)
            id++
        }
        (videoMenuDialog as VideoSelectDialog).setMenuData(list)
        (videoMenuDialog as VideoSelectDialog).setMenuClickListener(listener)
        (videoMenuDialog as VideoSelectDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as VideoSelectDialog).show()
    }

    /**
     * 剧集选集
     */
    fun showEpisode(
        currentPlayVideoBean: VideoBaseBean,
        episodeList: MutableList<VideoBaseBean>,
        stickyListener: VideoStickySelectDialog.OnItemSelectListener,
        clickListener: VideoSelectDialog.OnMenuClickListener,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        val bean = episodeList.filter { it.episodeTitle?.length ?: 0 > 5 }
        // 选集标题长度大于5，显示样式为综艺样式布局
        val menuColumn = if (bean.isNullOrEmpty()) 5 else 2
        if (episodeList.size > MAX_PAGE && !currentPlayVideoBean.isLive) {
            showGroupEpisode(menuColumn, currentPlayVideoBean, episodeList, stickyListener, clickListener, dismissListener)
        } else {
            val list: MutableList<VideoItemBean> = ArrayList()
            for ((index, temp) in episodeList.withIndex()) {
                val item = VideoItemBean()
                item.id = index
                item.isVip = temp.isVip
                item.enableTitle = StringUtils.getString(R.string.video_show)
                item.isNew = temp.isLast
                item.text = temp.episodeTitle
                item.isLive = temp.isLive
                item.isLiveError = temp.maintainStatus
                item.isCurrent = temp.uniqueID == currentPlayVideoBean.uniqueID
                list.add(item)
            }
            videoMenuDialog = VideoSelectDialog(context)
            videoMenuDialog?.isVertical = isVertical
            videoMenuDialog?.setHeight(ScreenUtils.getAppScreenWidth())
            (videoMenuDialog as VideoSelectDialog).setMenuColumn(menuColumn)
            (videoMenuDialog as VideoSelectDialog).setMenuData(list)
            (videoMenuDialog as VideoSelectDialog).setMenuClickListener(clickListener)
        }

        videoMenuDialog?.setOnDismissListener(dismissListener)
        videoMenuDialog?.show()
    }

    /**
     * 显示设置
     */
    fun showSetting(isAllowSkip: Boolean, callBack: SettingDialog.CallBack, dismissListener: DialogInterface.OnDismissListener) {
        videoMenuDialog = SettingDialog(context)
        videoMenuDialog?.isVertical = isVertical
        (videoMenuDialog as SettingDialog).isAllowSkip = isAllowSkip
        (videoMenuDialog as SettingDialog).callBack = callBack
        (videoMenuDialog as SettingDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as SettingDialog).show()
    }

    /**
     * 发送弹幕
     */
    fun showDanmakuInput(inputListener: DanamaInputDialog.OnSendDanamaListener, dismissListener: DialogInterface.OnDismissListener?) {
        bottomDialog = if (isVertical) DanamaInputVerticalDialog(context) else DanamaInputDialog(context)
        videoMenuDialog?.isVertical = isVertical
        if (isVertical) {
            (bottomDialog as DanamaInputVerticalDialog).setOnSendDanamaListener(inputListener)
        } else {
            (bottomDialog as DanamaInputDialog).setOnSendDanamaListener(inputListener)
        }
        bottomDialog?.setOnDismissListener(dismissListener)
        bottomDialog?.show()
    }

    /**
     * 屏蔽关键词
     */
    fun showWordDialog(
        forbiddenWordList: MutableList<CharSequence>,
        videoPlayer: LiteVideoPlayer,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        dismissDialog()
        videoMenuDialog = WordForbiddenDialog(context)
        videoMenuDialog?.isVertical = isVertical
        videoMenuDialog?.setHeight(ScreenUtils.getAppScreenWidth())
        (videoMenuDialog as WordForbiddenDialog).listData = forbiddenWordList
        (videoMenuDialog as WordForbiddenDialog).listener =
            object : WordForbiddenDialog.OnWordForbidden {
                override fun onAdd(word: String) {
                    forbiddenWordList.add(word)
                    videoPlayer.setForbiddenWord(forbiddenWordList)
                }

                override fun onRemove(word: CharSequence) {
                    forbiddenWordList.remove(word)
                    videoPlayer.setForbiddenWord(forbiddenWordList)
                }

            }

        (videoMenuDialog as WordForbiddenDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as WordForbiddenDialog).show()
    }

    /**
     * 弹幕设置
     */
    fun showDanmakuSetting(
        forbiddenWordList: MutableList<CharSequence>,
        forbiddenUserList: MutableList<UserInfoBean>?,
        videoPlayer: LiteVideoPlayer,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        videoMenuDialog = DanamaSettingDialog(videoPlayer.context)
        videoMenuDialog?.isVertical = isVertical
        (videoMenuDialog as DanamaSettingDialog).setDanamaController(object : IDanamaController {
            override fun onSpeedChange(speed: Float) {
                videoPlayer.setDanmakuSpeed()

            }

            override fun onFontSizeChange(fontSize: Int) {
                videoPlayer.setDanmakuTextSize()
            }

            override fun onFontBackgroundChange(transparent: Int) {
                videoPlayer.setDanmakuAlpha()
            }

            override fun onDanamaForbidden(position: Int, status: Boolean) {
                var value = -1
                when (position) {
                    0 -> value = DanmakuItemData.DANMAKU_MODE_ROLLING
                    1 -> value = DanmakuItemData.DANMAKU_MODE_CENTER_TOP
                    2 -> value = DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM
                }
                val forbidden = SPUtils.getInstance().getString(
                    Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id,
                    ""
                )
                if (status) { // 选中，添加
                    if (forbidden.isEmpty()) {
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id,
                            "$value"
                        )
                    } else {
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id,
                            "$forbidden,$value"
                        )
                    }
                } else {    // 未选中，移除
                    val array = forbidden.split(",").toMutableList()
                    array -= value.toString()
                    SPUtils.getInstance().put(
                        Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id,
                        array.joinToString(separator = ",")
                    )
                }
                videoPlayer.setForbiddenPosition()
            }

            override fun onForbiddenWordClick() {
                showWordDialog(forbiddenWordList, videoPlayer, dismissListener)
            }

            override fun onForbiddenUserClick() {
                showUserDialog(forbiddenUserList, videoPlayer, dismissListener)
            }

            override fun onDanamaListClick() {
                showDanamaListDialog(forbiddenUserList, videoPlayer, dismissListener)
            }

        })

        (videoMenuDialog as DanamaSettingDialog).setOnDismissListener { dialog ->
            if (videoMenuDialog is DanamaSettingDialog) {
                dismissListener.onDismiss(dialog)
            }
        }
        (videoMenuDialog as DanamaSettingDialog).show()
    }

    /**
     * 屏蔽用户
     */
    fun showUserDialog(
        forbiddenUserList: MutableList<UserInfoBean>?,
        videoPlayer: LiteVideoPlayer,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        dismissDialog()
        videoMenuDialog = UserForbiddenDialog(videoPlayer.context)
        videoMenuDialog?.isVertical = isVertical
        videoMenuDialog?.setHeight(ScreenUtils.getAppScreenWidth())
        (videoMenuDialog as UserForbiddenDialog).listData = forbiddenUserList
        (videoMenuDialog as UserForbiddenDialog).listener = object :
            UserForbiddenDialog.onRemoveForbidden {
            override fun onRemove(bean: UserInfoBean) {
                forbiddenUserList?.remove(bean)
                (videoMenuDialog as UserForbiddenDialog).resumeViewSize(
                    forbiddenUserList?.size ?: 0
                )
                videoPlayer.removeForbiddenUserDanmaku(bean.id.toLong())
            }

        }
        (videoMenuDialog as UserForbiddenDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as UserForbiddenDialog).show()
    }

    /**
     * 弹幕列表
     */
    fun showDanamaListDialog(
        forbiddenUserList: MutableList<UserInfoBean>?,
        videoPlayer: LiteVideoPlayer,
        dismissListener: DialogInterface.OnDismissListener
    ) {
        dismissDialog()
        videoMenuDialog = DanamaListDialog(videoPlayer.context)
        videoMenuDialog?.isVertical = isVertical
        videoMenuDialog?.setHeight(ScreenUtils.getAppScreenWidth())
        val dannmaList = videoPlayer.currentPlayer.getDanmakuList().filter { !it.isLive && !it.isAdvert }.toMutableList()
        if (forbiddenUserList != null && forbiddenUserList.size > 0 && dannmaList.size > 0) {
            var list: MutableList<BarrageBean> = ArrayList()
            dannmaList.let { list.addAll(it) }
            for (temp in forbiddenUserList) {
                list = list.filter { it.uid != temp.id }.toMutableList()
            }
            (videoMenuDialog as DanamaListDialog).listData = list
        } else {
            (videoMenuDialog as DanamaListDialog).listData = dannmaList
        }
        (videoMenuDialog as DanamaListDialog).setOnReportItemListener(object :
            DanamaListDialog.OnReportItemListener {

            override fun onReport(bean: BarrageBean) {
                showReportDialog(bean, videoPlayer, dismissListener)
            }

            override fun onForbiddenSuccess(bean: BarrageBean) {
                videoPlayer.setBottomTip(StringUtils.getString(R.string.forbidden_success))
                videoPlayer.setForbiddenUserDanmaku(bean.uid.toLong())
                EventBus.getDefault().post(bean)
            }

        })
        (videoMenuDialog as DanamaListDialog).setOnDismissListener {
            if (videoMenuDialog is DanamaListDialog) {
                dismissListener.onDismiss(it)
            }
        }
        (videoMenuDialog as DanamaListDialog).show()
    }

    /**
     * 弹幕举报
     */
    fun showReportDialog(bean: BarrageBean, videoPlayer: LiteVideoPlayer, dismissListener: DialogInterface.OnDismissListener) {
        dismissDialog()
        videoMenuDialog = ReportDialog(videoPlayer.context)
        videoMenuDialog?.isVertical = isVertical
        (videoMenuDialog as ReportDialog).setMessage(bean)
        (videoMenuDialog as ReportDialog).listener = object : ReportDialog.OnReportListener {
            override fun onReportSuccess(bean: BarrageBean) {
                videoPlayer.setBottomTip(StringUtils.getString(R.string.report_success))
            }

        }
        (videoMenuDialog as ReportDialog).setOnDismissListener(dismissListener)
        (videoMenuDialog as ReportDialog).show()
    }

    /**
     * 分享
     */
    fun showShare(upperId: Int, currentPlayVideoBean: VideoBaseBean) {
        bottomDialog = ShareBoard(context, upperId, currentPlayVideoBean, !isVertical)
        bottomDialog!!.show()
        if (isVertical) {
            (bottomDialog as ShareBoard).isGoneOtherLayout(true)
        }
    }
}