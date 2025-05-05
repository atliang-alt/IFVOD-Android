package com.cqcsy.lgsp.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.database.AddWatchRecordUtil
import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.ListToFullEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.IVideoController
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.bean.VideoItemBean
import com.cqcsy.lgsp.video.player.LiteVideoListPlayer
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.lgsp.video.player.SwitchUtil
import com.cqcsy.lgsp.video.view.DanamaInputDialog
import com.cqcsy.lgsp.video.view.IVideoDialog
import com.cqcsy.lgsp.video.view.VideoLandDialog
import com.cqcsy.lgsp.video.view.VideoPortraitDialog
import com.cqcsy.lgsp.video.view.VideoSelectDialog
import com.cqcsy.lgsp.views.dialog.ShareBoard
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * 列表播放器设置数据和控制
 */
class VideoListItemHolder(val context: Activity) {
    companion object {
        var liteVideoPlayer: WeakReference<LiteVideoListPlayer>? = null

        fun stopPlay() {
            val player = liteVideoPlayer?.get()
            player?.exitFull()
            if (player?.parent is ViewGroup) {
                (player.parent as ViewGroup).removeView(player)
            }
            player?.release()
            GSYVideoManager.releaseAllVideos()
            liteVideoPlayer = null
            player?.tag = null
        }

        fun getCurrentPlayer(): LiteVideoListPlayer? {
            return liteVideoPlayer?.get()
        }

        fun startPlay() {
            if(getCurrentPlayer()!= null) {
                getCurrentPlayer()?.startPlayLogic()
            }
        }
    }

    private var forbiddenUserList: MutableList<UserInfoBean>? = null
    private var forbiddenWordList: MutableList<CharSequence> = ArrayList()
    private var clarityList: MutableList<ClarityBean>? = null
    private var stayShow = false

    private var dialogHolder: IVideoDialog? = null
    private var mOrientationUtils: OrientationUtils? = null
    private var clickFull = false
    var isShowPlayCount = false

    fun setItemView(holder: BaseViewHolder, item: ShortVideoBean) {
        ImageUtil.loadCircleImage(context, item.headImg, holder.getView(R.id.user_image))
        val userVip = holder.getView<ImageView>(R.id.userVip)
        if (item.bigV || item.vipLevel > 0) {
            userVip.visibility = View.VISIBLE
            userVip.setImageResource(if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(item.vipLevel))
        } else {
            userVip.visibility = View.GONE
        }
        holder.setText(R.id.view_count, NormalUtil.formatPlayCount(item.playCount))
        holder.setText(R.id.user_nick_name, item.upperName)
        holder.setText(R.id.video_title, item.title)
//        holder.setText(R.id.video_publish_time, TimesUtils.friendDate(item.date))
        holder.setText(R.id.video_publish_time, TimesUtils.friendDate(item.date) + "  " + StringUtils.getString(R.string.release_short_video))
        holder.getView<ImageView>(R.id.likeImage).isSelected = item.likeStatus
        holder.getView<TextView>(R.id.like_num).isSelected = item.likeStatus
        if (item.likeCount <= 0) {
            holder.getView<TextView>(R.id.like_num).text = context.getString(R.string.fabulous)
        } else {
            holder.getView<TextView>(R.id.like_num).text = NormalUtil.formatPlayCount(item.likeCount)
        }
        if (item.comments <= 0) {
            holder.getView<TextView>(R.id.comment_num).text = context.getString(R.string.comment)
        } else {
            holder.getView<TextView>(R.id.comment_num).text = NormalUtil.formatPlayCount(item.comments)
        }
        if (isShowPlayCount) {
            holder.setGone(R.id.play_num, false)
            holder.setText(R.id.play_num, NormalUtil.formatPlayCount(item.playCount))
        } else {
            holder.setGone(R.id.play_num, true)
        }
        val btnAttention = holder.getView<Button>(R.id.btn_attention)
        val blackList = holder.getView<TextView>(R.id.blackList)
        setUserStatus(btnAttention, blackList, item)
        holder.getView<ImageView>(R.id.share).setOnClickListener {
            item.focusStatus = btnAttention.isSelected
            showShare(item)
        }
        holder.getView<ImageView>(R.id.user_image).setOnClickListener {
            onUserInfoClick(item)
        }
        holder.getView<TextView>(R.id.user_nick_name).setOnClickListener {
            onUserInfoClick(item)
        }
        holder.getView<LinearLayout>(R.id.likeLayout).setOnClickListener {
            onLikeClick(holder, item)
        }

        holder.setVisible(R.id.video_thumb, true)
        holder.setVisible(R.id.start_button, true)
        holder.setText(R.id.video_time, item.duration)
        holder.setVisible(R.id.video_time, true)
        ImageUtil.loadImage(context, item.coverImgUrl, holder.getView(R.id.video_thumb), 0)
        holder.getView<LinearLayout>(R.id.commentLayout).setOnClickListener {
            startFullPlay(item)
        }
        holder.getView<TextView>(R.id.video_title).setOnClickListener {
            startFullPlay(item)
        }
        holder.getView<ImageView>(R.id.start_button).setOnClickListener {
            startPlay(holder, item)
        }
    }

    private fun startFullPlay(item: ShortVideoBean) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, item)
        val player = liteVideoPlayer?.get()
        if (player != null && player.isInPlayingState) {
            EventBus.getDefault().post(ListToFullEvent())
            player.setSwitchCache(true)
            player.setSwitchTitle(item.title!!)
            player.setSwitchUrl(NormalUtil.urlEncode(item.mediaUrl))
            SwitchUtil.savePlayState(player)
            player.setVideoAllCallBack(null)
            player.controllerI = null
            player.seekOnStart = 0
            // 这里指定了共享的视图元素
            val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                context,
                player,
                VideoBaseActivity.OPTION_VIEW
            )
            ActivityCompat.startActivity(context, intent, options.toBundle())
        } else {
            context.startActivity(intent)
        }
    }

    private fun startPlay(holder: BaseViewHolder, item: ShortVideoBean) {
        if (liteVideoPlayer?.get() != null) {
            stopPlay()
        }
        val player = LiteVideoListPlayer(holder.itemView.context)
        player.tag = holder.absoluteAdapterPosition
        liteVideoPlayer = WeakReference(player)
        player.controllerI = object : IVideoController {
            override fun onShareClick() {
                val bottomDialog = ShareBoard(context, item.userId, item, !player.getIsVerticalVideo())
                bottomDialog.show()
                bottomDialog.isGoneOtherLayout(true)
            }

            override fun onSpeedClick() {
                showSpeed(player)
            }

            override fun onDanamaInputClick() {
                if (GlobalValue.isLogin()) {
                    dialogHolder?.showDanmakuInput(object : DanamaInputDialog.OnSendDanamaListener {
                        override fun onSend(input: String) {
                            sendBarrage(input, item, player)
                        }

                        override fun onOpenVip() {
                            dialogHolder?.dismissDialog()
                            buyVip(player)
                        }
                    }, null)
                } else {
                    startLogin()
                }
            }

            override fun onDanamaSettingClick() {
                if (GlobalValue.isLogin()) {
                    showDanmakuSetting(player)
                } else {
                    startLogin()
                }
            }

            override fun isVip(): Boolean {
                return GlobalValue.isVipUser()
            }

            override fun isStayShow(): Boolean {
                return stayShow || dialogHolder?.isShowingDialog() == true
            }

            override fun exitFullScreen() {
                exitFull()
            }

            override fun isAllowQuality(): Boolean {
                return (clarityList?.size ?: 0) > 1
            }

            override fun getQuality(): String {
                return item.resolutionDes ?: StringUtils.getString(R.string.video_quality)
            }

            override fun onClarityClick() {
                showClarityDialog(item)
            }

            override fun isShortVideo(): Boolean {
                return true
            }
        }
        GSYVideoOptionBuilder()
            .setFullHideActionBar(true)
            .setFullHideStatusBar(true)
            .setIsTouchWiget(false)
            .setIsTouchWigetFull(true)
            .setDismissControlTime(5000)
            .setLockLand(false)
            .setOnlyRotateLand(false)
            .setRotateViewAuto(false)
            .setRotateWithSystem(false)
            .setShowFullAnimation(false) //打开动画
            .setShowDragProgressTextOnSeekBar(true)
            .setNeedLockFull(true)
            .setReleaseWhenLossAudio(false)
            .setSeekRatio(3f)
            .setVideoTitle(item.title)
            .setCacheWithPlay(true)
            .setAutoFullWithSize(true)  // 是否根据视频尺寸自动判断竖屏还是横屏
            .setPlayTag(BaseVideoListFragment::class.java.simpleName)
            .setPlayPosition(holder.adapterPosition)
            .setVideoAllCallBack(object : GSYSampleCallBack() {
                override fun onStartPrepared(url: String?, vararg objects: Any?) {
                    super.onStartPrepared(url, *objects)
                    if (objects[1] is LiteVideoListPlayer) {
                        val listPlayer = objects[1] as LiteVideoListPlayer
                        setOrientationUtils(listPlayer)
                    }
                    holder.setGone(R.id.video_thumb, true)
                    holder.setGone(R.id.video_time, true)
                    holder.setGone(R.id.start_button, true)
                    if (GlobalValue.isLogin()) {
                        getForbiddenUserList(player)
                        getForbiddenWordList(player)
                    }
                }

                override fun onPrepared(url: String?, vararg objects: Any?) {
                    super.onPrepared(url, *objects)
                    playRecord(item.mediaKey)
                    getBarrageList(item, player)
                    if (objects[1] is LiteVideoListPlayer) {
                        val listPlayer = objects[1] as LiteVideoListPlayer
                        dialogHolder = if (listPlayer.getIsVerticalVideo()) VideoPortraitDialog(context) else VideoLandDialog(context)
                        item.time = (listPlayer.duration / 1000).toString()
                    }
                    addRecord(item)
                }

                override fun onAutoComplete(url: String?, vararg objects: Any?) {
                    super.onAutoComplete(url, *objects)
                    item.watchingProgress = 0
                    player.seekOnStart = 0
                    hideDialog(player)
                    player.setUp(null, false, null)
                    holder.setVisible(R.id.video_thumb, true)
                    holder.setVisible(R.id.start_button, true)
                    holder.setVisible(R.id.video_time, true)
                    if (!player.isIfCurrentIsFullscreen) {
                        mOrientationUtils = null
                    }
                    dialogHolder = null
                }

                override fun onComplete(url: String?, vararg objects: Any?) {
                    super.onComplete(url, *objects)
                    if (objects[2] != null) {
                        val time = objects[2] as Long
                        if (time > 0) {
                            item.watchingProgress = time / 1000
                            player.seekOnStart = time
                        }
                    }
                    holder.setVisible(R.id.video_thumb, true)
                    holder.setVisible(R.id.start_button, true)
                    holder.setVisible(R.id.video_time, true)
                    mOrientationUtils = null
                    dialogHolder = null
                }

                override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                    super.onEnterFullscreen(url, *objects)
                    player.currentPlayer.titleTextView.text = objects[0] as String?
                }

                override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                    super.onQuitFullscreen(url, *objects)
                    exitFull()
                }
            }).build(player)
        holder.getView<FrameLayout>(R.id.videoContainer).addView(player, 0)
        player.seekOnStart = item.watchingProgress * 1000L
        if (!item.mediaUrl.isNullOrEmpty()) {
            player.setUp(item.mediaUrl, true, item.title)
            player.startPlayLogic()
        } else {
            getPlayInfo(player, item)
        }
    }

    private fun startLogin() {
        if(liteVideoPlayer?.get()?.isIfCurrentIsFullscreen == true) {
            exitFull()
        }
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    private fun showClarityDialog(video: ShortVideoBean) {
        dialogHolder?.showClarity(video.episodeId, clarityList!!, false, object : VideoSelectDialog.OnMenuClickListener {

            override fun onItemClick(item: VideoItemBean) {
                val clarityBean = clarityList!![item.id]
                if (clarityBean.episodeId == video.episodeId) {
                    return
                }
                val player = liteVideoPlayer?.get()
                player?.let { startPlay(it, video, clarityBean, it.currentPositionWhenPlaying) }
            }

        }
        ) { }
    }

    private fun addRecord(item: ShortVideoBean) {
        val watchRecordBean = WatchRecordBean()
        val uid = if (GlobalValue.isLogin()) {
            GlobalValue.userInfoBean?.token!!.uid
        } else {
            0
        }
        watchRecordBean.uid = uid
        watchRecordBean.videoType = item.videoType
        watchRecordBean.mediaKey = item.mediaKey
        watchRecordBean.title = item.title
        watchRecordBean.episodeId = item.episodeId
        watchRecordBean.uniqueID = item.uniqueID
        watchRecordBean.episodeTitle = item.episodeTitle
        watchRecordBean.coverImgUrl = item.coverImgUrl.toString()
        watchRecordBean.upperName = item.upperName
        watchRecordBean.mediaUrl = item.mediaUrl
        watchRecordBean.watchTime = item.watchingProgress.toString()
        watchRecordBean.duration = item.duration
        watchRecordBean.time = item.time
        watchRecordBean.cidMapper = item.cidMapper.toString()
        watchRecordBean.regional = item.regional.toString()
        watchRecordBean.lang = item.lang
        watchRecordBean.status = 0
        AddWatchRecordUtil.addRecord(watchRecordBean)
        item.cidMapper?.let { LabelUtil.addLabels(it, item.userId, Constant.KEY_SHORT_VIDEO_LABELS) }
    }

    private fun setOrientationUtils(player: LiteVideoListPlayer) {
        //外部辅助的旋转，帮助全屏
        mOrientationUtils = OrientationUtils(context, player, null)
        mOrientationUtils?.isEnable = false
        if (player.fullscreenButton != null) {
            player.fullscreenButton.setOnClickListener {
                clickFull = true
                showFull(player)
            }
        }
        player.setBackFromFullScreenListener {
            exitFull()
        }
    }

    private fun showShare(item: ShortVideoBean) {
        val share = ShareBoard(context, item.userId, item)
        share.listener = object : ShareBoard.OnCollectionListener {
            override fun onChange(type: Int, isSelected: Boolean) {
                if (type == 1) {
                    attentionEvent(item, isSelected)
                } else {
                    val bean = VideoLikeBean()
                    bean.selected = isSelected
                    item.favorites = bean
                }
            }

        }
        share.show()
        share.setAttentionAndCollection(item)
    }

    fun hideDialog(player: LiteVideoListPlayer) {
        stayShow = false
        dialogHolder?.dismissDialog()
        player.startDismiss()
    }

    fun showSpeed(player: LiteVideoListPlayer) {
        stayShow = true
        dialogHolder?.showSpeed(player.getCurrentSpeed(), object : VideoSelectDialog.OnMenuClickListener {
            override fun onItemClick(item: VideoItemBean) {
                if (!item.enbale) {
                    buyVip(player)
                    return
                }
                var speed = 1.0f
                when (item.id) {
                    0 -> speed = 2.0f
                    1 -> speed = 1.5f
                    2 -> speed = 1.25f
                    3 -> speed = 1.0f
                    4 -> speed = 0.75f
                    5 -> speed = 0.5f
                }
                player.currentPlayer.setSpeed(speed, true)
                val speedText = if (item.id == 3) {
                    StringUtils.getString(R.string.video_speed)
                } else {
                    item.text ?: ""
                }
                player.setSpeedText(speedText)
                player.setBottomTip(StringUtils.getString(R.string.speed_change_tip, speed.toString()))
            }

            override fun onOpenVipClick() {
                buyVip(player)
            }
        }) { hideDialog(player) }
    }

    private fun showDanmakuSetting(videoPlayer: LiteVideoListPlayer) {
        stayShow = true
        dialogHolder?.showDanmakuSetting(forbiddenWordList, forbiddenUserList, videoPlayer) { hideDialog(videoPlayer) }
    }

    /**
     * 获取弹幕列表
     */
    private fun getBarrageList(bean: ShortVideoBean, player: LiteVideoPlayer) {
        val params = HttpParams()
        params.put("mediaKey", bean.mediaKey)
        params.put("videoId", bean.uniqueID)
        params.put("videoType", 3)
        HttpRequest.get(RequestUrls.GET_BARRAGE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    player.setDanmaKuData(ArrayList())
                    return
                }
                val barrageList: MutableList<BarrageBean> =
                    Gson().fromJson(jsonArray.toString(), object : TypeToken<MutableList<BarrageBean>>() {}.type)
                player.setDanmaKuData(barrageList)
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }


    /**
     * 获取屏蔽用户列表
     */
    private fun getForbiddenUserList(videoPlayer: LiteVideoPlayer) {
        forbiddenUserList = null
        HttpRequest.get(RequestUrls.FORBIDDEN_USER_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                forbiddenUserList = Gson().fromJson(
                    response?.optJSONArray("list")?.toString(),
                    object : TypeToken<MutableList<UserInfoBean>>() {}.type
                )
                if (forbiddenUserList != null) {
                    val ids: MutableList<Long> = ArrayList()
                    for (temp in forbiddenUserList!!) {
                        ids.add(temp.id.toLong())
                    }
                    videoPlayer.setForbiddenUserDanmaku(ids)
                } else {
                    forbiddenUserList = ArrayList()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }

        }, tag = this)
    }

    /**
     * 获取屏蔽关键词列表
     */
    private fun getForbiddenWordList(videoPlayer: LiteVideoPlayer) {
        forbiddenWordList.clear()
        HttpRequest.get(RequestUrls.GET_FORBIDDEN_WORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                forbiddenWordList = Gson().fromJson(
                    response?.optJSONArray("list")?.toString(),
                    object : TypeToken<MutableList<String>>() {}.type
                )
                if (forbiddenWordList != null) {
                    videoPlayer.setForbiddenWord(forbiddenWordList)
                } else {
                    forbiddenUserList = ArrayList()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }

        }, tag = this)
    }

    /**
     * 发送弹幕
     */
    private fun sendBarrage(content: String, shortVideoBean: ShortVideoBean, videoPlayer: LiteVideoPlayer) {
        val color = SPUtils.getInstance().getString(Constant.KEY_SEND_DANAMA_COLOR + GlobalValue.userInfoBean?.id, "#ffffff")
        val position = SPUtils.getInstance().getInt(Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id, 0)
        val params = HttpParams()
        params.put("Contxt", content)
        params.put("Color", color)
        params.put("Position", position)
        params.put("videoType", 3)
        params.put("Second", videoPlayer.currentPlayer.currentPositionWhenPlaying / 1000)
        params.put("mediaKey", shortVideoBean.mediaKey)
        params.put("videoId", shortVideoBean.uniqueID)
        HttpRequest.get(RequestUrls.SEND_BARRAGE, object : HttpCallBack<JSONArray>() {
            override fun onSuccess(response: JSONArray?) {
                if (response == null) {
                    return
                }
                val list = Gson().fromJson<MutableList<BarrageBean>>(response.toString(), object : TypeToken<MutableList<BarrageBean>>() {}.type)
                if (list.size > 0) {
                    videoPlayer.addDanmaku(list[0])
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 播放次数
     */
    fun playRecord(mediaKey: String) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.PLAY_RECORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    /**
     * 用户信息点击
     */
    private fun onUserInfoClick(bean: ShortVideoBean) {
        val intent = Intent(context, UpperActivity::class.java)
        intent.putExtra(UpperActivity.UPPER_ID, bean.userId)
        context.startActivity(intent)
    }

    private fun setUserStatus(attentionBtn: Button, blackListView: TextView, item: ShortVideoBean) {
        val isFocus = item.focusStatus
        val inBlackList = item.isBlackList
        val isMySelf = GlobalValue.userInfoBean?.id == item.userId
        if (isMySelf) {
            attentionBtn.isVisible = false
            blackListView.isVisible = false
        } else if (inBlackList) {
            blackListView.isVisible = true
            attentionBtn.isVisible = false
            ClickUtils.applyGlobalDebouncing(blackListView) {
                if (GlobalValue.isLogin()) {
                    showBlackTip(blackListView.context, item.userId)
                } else {
                    startLogin()
                }
            }
        } else {
            blackListView.isVisible = false
            attentionBtn.isVisible = true
            attentionBtn.isSelected = isFocus && GlobalValue.isLogin()
            if (attentionBtn.isSelected) {
                attentionBtn.setText(R.string.followed)
            } else {
                attentionBtn.setText(R.string.attention)
            }
            ClickUtils.applyGlobalDebouncing(attentionBtn) {
                if (GlobalValue.isLogin()) {
                    onAttentionClick(item)
                } else {
                    startLogin()
                }
            }
        }
    }

    private fun showBlackTip(context: Context, userId: Int) {
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.blacklist_remove)
        dialog.setMsg(R.string.in_black_list_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.ensure) {
            dialog.dismiss()
            removeBlackList(userId)
        }
        dialog.show()
    }

    /**
     * 取消拉黑
     */
    private fun removeBlackList(uid: Int) {
        val params = HttpParams()
        params.put("uid", uid)
        params.put("status", true)
        HttpRequest.post(RequestUrls.CHAT_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val status = response.optBoolean("status")
                EventBus.getDefault().post(BlackListEvent(uid, status))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }

    /**
     * 关注点击
     */
    private fun onAttentionClick(bean: ShortVideoBean) {
        val params = HttpParams()
        params.put("userId", bean.userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                attentionEvent(bean, selected)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 点赞点击
     */
    private fun onLikeClick(holder: BaseViewHolder, bean: ShortVideoBean) {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        val params = HttpParams()
        params.put("mediaKey", bean.mediaKey)
        params.put("videoType", 3)
        HttpRequest.get(RequestUrls.VIDEO_LIKES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean = Gson().fromJson(response.optString("like"), object : TypeToken<VideoLikeBean>() {}.type)
                if (videoLikeBean.selected) {
                    ImageUtil.clickAnim(context, holder.getView(R.id.likeImage))
                }
                holder.getView<ImageView>(R.id.likeImage).isSelected = videoLikeBean.selected
                holder.getView<TextView>(R.id.like_num).isSelected = videoLikeBean.selected
                if (videoLikeBean.count <= 0) {
                    holder.getView<TextView>(R.id.like_num).text = StringUtils.getString(R.string.fabulous)
                } else {
                    holder.getView<TextView>(R.id.like_num).text = NormalUtil.formatPlayCount(videoLikeBean.count)
                }
                bean.likeStatus = videoLikeBean.selected
                bean.likeCount = videoLikeBean.count
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 获取播放地址
     */
    private fun getPlayInfo(videoPlayer: LiteVideoListPlayer, item: ShortVideoBean) {
        val params = HttpParams()
        params.put("mediaKey", item.mediaKey)
        params.put("videoType", 3)
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (getCurrentPlayer() != videoPlayer) return
                if (response == null || response.length() == 0 || (response.optJSONArray("list")?.length() ?: 0) == 0) {
                    ToastUtils.showLong(R.string.on_video_source)
                    videoPlayer.currentPlayer.changeUiToError()
                    return
                }
                clarityList = Gson().fromJson<MutableList<ClarityBean>>(
                    response!!.optJSONArray("list").toString(),
                    object : TypeToken<MutableList<ClarityBean>>() {}.type
                )
                if (!clarityList.isNullOrEmpty()) {
                    for (temp in clarityList!!) {
                        if (temp.isDefault) {
                            startPlay(videoPlayer, item, temp, 0)
                            break
                        }
                    }
                } else {
                    ToastUtils.showLong(R.string.on_video_source)
                    videoPlayer.currentPlayer.changeUiToError()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                videoPlayer.currentPlayer.changeUiToError()
            }
        }, params, this)
    }

    private fun startPlay(videoPlayer: LiteVideoListPlayer, item: ShortVideoBean, clarityBean: ClarityBean, seekTime: Long = 0) {
        clarityBean.setValueToBase(item)
        item.mediaUrl = NormalUtil.urlEncode(clarityBean.mediaUrl)
        videoPlayer.currentPlayer.seekOnStart = seekTime
        videoPlayer.currentPlayer.setUp(item.mediaUrl, true, item.title)
        videoPlayer.currentPlayer.startPlayLogic()
    }

    fun buyVip(listVideo: LiteVideoListPlayer) {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        if (listVideo.currentPlayer.isIfCurrentIsFullscreen) {
            listVideo.onBackFullscreen()
        }
        context.startActivity(Intent(context, OpenVipActivity::class.java))
    }

    private fun showFull(player: LiteVideoListPlayer) {
        if (mOrientationUtils?.isLand != 1) {
            //直接横屏
            mOrientationUtils?.resolveByClick()
        }
        //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
        player.startWindowFullscreen(context, true, true)
    }

    private fun exitFull() {
        mOrientationUtils?.backToProtVideo()
        GSYVideoManager.backFromWindowFull(context)
        if (liteVideoPlayer?.get()?.isInPlayingState == false) {
            mOrientationUtils = null
        }
    }

    /**
     * 关注事件
     */
    fun attentionEvent(bean: ShortVideoBean, selected: Boolean) {
        if (selected) {
            ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
        }
        bean.focusStatus = selected
        val event = VideoActionResultEvent()
        event.action = if (selected) VideoActionResultEvent.ACTION_ADD else VideoActionResultEvent.ACTION_REMOVE
        event.type = 1
        event.id = bean.userId.toString()
        event.userLogo = bean.headImg
        event.userName = bean.upperName ?: ""
        EventBus.getDefault().post(event)
    }

}