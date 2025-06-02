package com.cqcsy.lgsp.main.mine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.preload.PreloadManager2
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.player.DynamicVideoManager
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.fragment_dynamic_detail.*
import kotlinx.android.synthetic.main.fragment_dynamic_video.*
import kotlinx.android.synthetic.main.fragment_dynamic_video.collectImg
import kotlinx.android.synthetic.main.fragment_dynamic_video.collectStatus
import kotlinx.android.synthetic.main.fragment_dynamic_video.collect_container
import kotlinx.android.synthetic.main.fragment_dynamic_video.commentEdit
import kotlinx.android.synthetic.main.fragment_dynamic_video.commentUserImage
import kotlinx.android.synthetic.main.fragment_dynamic_video.like_container
import kotlinx.android.synthetic.main.fragment_dynamic_video.title_container
import kotlinx.android.synthetic.main.fragment_dynamic_video.tv_comment_count
import kotlinx.android.synthetic.main.fragment_dynamic_video.user_action
import kotlinx.android.synthetic.main.fragment_dynamic_video.zanCount
import kotlinx.android.synthetic.main.fragment_dynamic_video.zanImg
import kotlinx.android.synthetic.main.fragment_mine_dynamic.*
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.*
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.action_container
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.avatar_container
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.blackList
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.content_container
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.expand_text
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.fold_text
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.iv_avatar
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tag_group
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_expand
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_fold
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_follow
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_location
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_time
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.tv_user_name
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.userVipImage
import kotlinx.android.synthetic.main.layout_video_bottom.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/13
 *
 * 动态视频详情
 */
class DynamicVideoFragment : BaseFragment() {

    var mediaKey: String? = ""
        private set
    private var commentId: Int = 0
    private var replyId: Int = 0
    private var showComment: Boolean = false
    private var isMyself: Boolean = false
    private var dynamicData: DynamicBean? = null
    private var commentEditDialog: CommentEditDialog? = null
    private var bottomView: View? = null
    private var isFromMineDynamic: Boolean = false
    private var isAddHot = false
    private var isAddRecord = false
    private var isRefreshData = false
    private var playFlag = false
    private var labels: String? = null
    private var cacheLabels = false

    companion object {
        const val PLAY_TAG = "dynamic_video_detail"
        fun newInstance(
            data: DynamicBean,
            isFromMineDynamic: Boolean,
            showComment: Boolean,
            commentId: Int = 0,
            replyId: Int = 0,
        ): DynamicVideoFragment {
            val args = Bundle()
            args.putSerializable("dynamic_bean", data)
            args.putBoolean("is_from_mine_dynamic", isFromMineDynamic)
            args.putBoolean("show_comment", showComment)
            args.putInt("comment_id", commentId)
            args.putInt("reply_id", replyId)
            val fragment = DynamicVideoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dynamic_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isFromMineDynamic = arguments?.getBoolean("is_from_mine_dynamic") ?: false
        showComment = arguments?.getBoolean("show_comment") ?: false
        dynamicData = arguments?.getSerializable("dynamic_bean") as? DynamicBean
        commentId = arguments?.getInt("comment_id") ?: 0
        replyId = arguments?.getInt("reply_id") ?: 0
        initView()
        initVideoBottomView()
        initVideoPlayer()
        dynamicData?.let { data ->
            isMyself = GlobalValue.userInfoBean?.id == data.uid
            mediaKey = data.mediaKey
            fillData(data)
        }
        getDynamicDetail(startPlay = playFlag)
    }

    private fun initView() {
        ImageUtil.loadCircleImage(this, GlobalValue.userInfoBean?.avatar, commentUserImage)
        iv_back.setOnClickListener { activity?.finish() }
        title_container.updateLayoutParams<FrameLayout.LayoutParams> {
            topMargin = BarUtils.getStatusBarHeight()
        }
        showByUserStatus()
    }

    override fun onVisible() {
        super.onVisible()
        if (!isAddHot) {
            addHot(mediaKey)
        }
        if (!isAddRecord) {
            dynamicData?.let {
                addRecord(it)
            }
        }
    }

    private fun initVideoBottomView() {
        bottomView = layoutInflater.inflate(R.layout.layout_dynamic_video_bottom_view, null, false)
        video_player.addDynamicBottomView(bottomView!!)
        tv_follow.isVisible = !isMyself
    }

    private fun initVideoPlayer() {
        video_player.apply {
            isLockLand = true
            playTag = PLAY_TAG
            isShowFullAnimation = false
            isLooping = true
            setIsTouchWiget(false)
            isShowDragProgressTextOnSeekBar = true
        }
    }

    fun startPlay() {
        if (dynamicData == null) {
            playFlag = true
            return
        }
        val mediaUrl = dynamicData!!.mediaUrl
        if (!mediaUrl.isNullOrEmpty()) {
            DynamicVideoManager.instance(requireActivity()).releaseAllVideos()
            video_player.setGSYVideoProgressListener { _, _, currentPosition, _ ->
                val position = currentPosition / 1000
                if (position >= 5 && !cacheLabels) {
                    cacheLabels = true
                    LabelUtil.cacheDynamicLabel(dynamicData!!.label, dynamicData!!.uid)
                }
            }
            video_player.setUp(mediaUrl, true, null, "")
            video_player.startPlayLogic()
            PreloadManager2.resumeAllPreload()
        } else {
            DynamicVideoManager.instance(requireActivity()).releaseAllVideos()
            getPlayInfo(dynamicData!!.smallMediaKey ?: "", true)
        }
    }

    private fun showByUserStatus() {
        if (isFromMineDynamic) {
            user_action.isVisible = true
            user_action.setOnClickListener { showUserAction() }
        } else {
            user_action.isVisible = false
        }
    }

    private fun showUserAction() {
        val menu = LayoutInflater.from(context).inflate(R.layout.layout_manage_dynamic_menu, null)
        val popupWindow = PopupWindow(
            menu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        menu.findViewById<View>(R.id.editDynamic).setOnClickListener {
            ReleaseDynamicVideoActivity.launch(this, dynamicData, 1111)
            popupWindow.dismiss()
        }
        menu.findViewById<View>(R.id.deleteDynamic).setOnClickListener {
            deleteDialog()
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = menu
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(user_action, -130, 0)
    }

    private fun deleteDialog() {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.deleteDynamic)
        tipsDialog.setMsg(R.string.deleteDynamicTips)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            deleteHttp()
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    private fun showRemoveBlackListDialog(uid: Int) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.blacklist_remove)
        tipsDialog.setMsg(R.string.in_black_list_tip)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.sure) {
            removeBlackList(uid)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    private fun showCommentDialog(mediaKey: String, commentCount: Int) {
        DynamicCommentDialog.show(childFragmentManager, commentId, replyId, mediaKey, commentCount, true, dynamicData?.videoType)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showExpandAnim() {
        tv_expand.isVisible = false
        tv_fold.isVisible = true
        expand_text.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_UP -> {
                    v?.parent?.requestDisallowInterceptTouchEvent(true)

                }
            }
            false
        }
        expand_text.movementMethod = ScrollingMovementMethod.getInstance()
        content_container.transitionToEnd()
    }

    private fun showFoldAnim() {
        tv_expand.isVisible = true
        tv_fold.isVisible = false
        expand_text.movementMethod = null
        expand_text.setOnTouchListener(null)
        expand_text.parent.requestDisallowInterceptTouchEvent(false)
        content_container.transitionToStart()
    }

    private fun addRecord(bean: DynamicBean) {
        isAddRecord = true
        val dynamicRecordBean = DynamicRecordBean()
        dynamicRecordBean.mediaKey = bean.mediaKey ?: ""
        dynamicRecordBean.headImg = bean.headImg ?: ""
        dynamicRecordBean.upperName = bean.upperName ?: ""
        dynamicRecordBean.createTime = bean.createTime ?: ""
        dynamicRecordBean.description = bean.description ?: ""
        dynamicRecordBean.trendsDetails = Gson().toJson(bean.trendsDetails)
        dynamicRecordBean.address = bean.address ?: ""
        dynamicRecordBean.comments = bean.comments
        dynamicRecordBean.likeCount = bean.likeCount
        dynamicRecordBean.uid = bean.uid
        dynamicRecordBean.bigV = bean.bigV
        dynamicRecordBean.vipLevel = bean.vipLevel
        dynamicRecordBean.type = 2
        dynamicRecordBean.coverPath = bean.cover ?: ""
        DynamicRecordManger.instance.add(dynamicRecordBean)
    }

    private fun fillData(data: DynamicBean) {
        //设置封面
        video_player.setCover(data.cover ?: "")
        tv_follow.isVisible = GlobalValue.userInfoBean?.id != data.uid
        setUserStatus(data.focus, data.isBlackList)
        setCollect(data.isCollected)
        setComment(data.comments)
        initLikeView(data.like, data.likeCount)
        if (data.description.isNullOrEmpty()) {
            fold_text.isVisible = false
            action_container.isVisible = false
        } else {
            fold_text.isVisible = true
            val content = Html.fromHtml(data.description?.replace("\n", "<br/>"))
            fold_text.initWidth(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(24f))
            fold_text.text = content
            expand_text.text = content
            if (fold_text.isOverSize(content)) {
                action_container.isVisible = true
                tv_expand.isVisible = true
                tv_fold.isVisible = false
            }
        }
        tv_time.text = TimeUtils.date2String(
            TimesUtils.formatDate(data.createTime ?: ""),
            "yyyy-MM-dd HH:mm"
        )
        tv_user_name.text = data.upperName ?: ""
        ImageUtil.loadCircleImage(this, data.headImg, iv_avatar)
        if (data.bigV || data.vipLevel > 0) {
            userVipImage.visibility = View.VISIBLE
            userVipImage.setImageResource(
                if (data.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    data.vipLevel
                )
            )
        } else {
            userVipImage.visibility = View.GONE
        }
        if (!data.address.isNullOrEmpty()) {
            tv_location.visibility = View.VISIBLE
            tv_location.text = data.address
        } else {
            tv_location.visibility = View.GONE
        }
        labels = data.label
        addTags(data.label)
        ClickUtils.applySingleDebouncing(tv_follow) {
            if (GlobalValue.isLogin()) {
                followClick(data.uid)
            } else {
                startLogin()
            }
        }
        ClickUtils.applySingleDebouncing(blackList) {
            if (GlobalValue.isLogin()) {
                showRemoveBlackListDialog(data.uid)
            } else {
                startLogin()
            }
        }
        tv_expand.setOnClickListener {
            showExpandAnim()
        }
        tv_fold.setOnClickListener {
            showFoldAnim()
        }
        avatar_container.setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, data.uid)
            startActivity(intent)
        }
        tv_user_name.setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, data.uid)
            startActivity(intent)
        }
        commentEdit.setOnClickListener {
            showCommentInput()
        }
        collect_container.setOnClickListener {
            collectionClick()
        }
        like_container.setOnClickListener {
            dynamicLike()
        }
        tv_comment_count.setOnClickListener {
            showCommentDialog(data.mediaKey ?: "", data.comments)
        }
    }

    /**
     * 设置用户状态，关注 拉黑
     */
    private fun setUserStatus(isFollow: Boolean, isBlack: Boolean) {
        if (isMyself) {
            tv_follow.isVisible = false
            blackList.isVisible = false
        } else if (isBlack) {
            tv_follow.isVisible = false
            blackList.isVisible = true
        } else {
            blackList.isVisible = false
            tv_follow.isVisible = true
            tv_follow.isChecked = isFollow
            tv_follow.text = if (isFollow) {
                resources.getString(R.string.followed)
            } else {
                resources.getString(R.string.attention)
            }
        }
    }

    private fun setCollect(isCollected: Boolean) {
        if (isCollected) {
            collectStatus.setText(R.string.collected)
        } else {
            collectStatus.setText(R.string.collection)
        }
        collectStatus.isSelected = isCollected
        collectImg.isSelected = isCollected
    }

    private fun setComment(commentCount: Int) {
        if (commentCount > 0) {
            tv_comment_count.text = commentCount.toString()
        } else {
            tv_comment_count.text = getString(R.string.comment)
        }
    }

    private fun addHot(mediaKey: String?) {
        isAddHot = true
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.PICTURES_HOT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {

            }

            override fun onError(response: String?, errorMsg: String?) {
                isAddHot = false
            }

        }, params)
    }

    private fun dynamicLike() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        val params = HttpParams()
        params.put("mediaKey", dynamicData?.mediaKey)
        dynamicData?.videoType?.let { params.put("videoType", it) }
        HttpRequest.get(RequestUrls.VIDEO_LIKES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean =
                    Gson().fromJson(
                        response.optString("like"),
                        object : TypeToken<VideoLikeBean>() {}.type
                    )
                if (videoLikeBean.selected) {
                    ImageUtil.clickAnim(requireActivity(), zanImg)
                }
                dynamicData?.like = videoLikeBean.selected
                dynamicData?.likeCount = videoLikeBean.count
                initLikeView(videoLikeBean.selected, videoLikeBean.count)
                val event = VideoActionResultEvent()
                event.type = 2
                event.selected = videoLikeBean.selected
                event.count = videoLikeBean.count
                dynamicData?.mediaKey.let { event.id = it.toString() }
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 初始化点赞布局
     */
    private fun initLikeView(select: Boolean, count: Int) {
        zanImg.isSelected = select
        zanCount.isSelected = select
        if (count > 0) {
            zanCount.text = NormalUtil.formatPlayCount(count)
        } else {
            zanCount.text = resources.getString(R.string.fabulous)
        }
    }

    private fun addTags(label: String?) {
        tag_group.removeAllViews()
        if (!label.isNullOrEmpty()) {
            val tags = label.split(",")
            if (tags.isNotEmpty()) {
                tag_group.visibility = View.VISIBLE
                val color = ColorUtils.getColor(R.color.white_60)
                val padding = SizeUtils.dp2px(5f)
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.rightMargin = SizeUtils.dp2px(10f)
                params.bottomMargin = SizeUtils.dp2px(5f)
                tags.forEach {
                    val tagText = TextView(context)
                    tagText.setBackgroundResource(R.drawable.tag_bg)
                    tagText.text = it
                    tagText.setTextColor(color)
                    tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    tagText.setPadding(padding, 4, padding, 4)

                    tag_group.addView(tagText, params)
                }
            }
        }
    }

    /**
     * 点击弹出评论输入框
     */
    private fun showCommentInput() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        commentEditDialog =
            CommentEditDialog("", object : CommentEditDialog.SendCommentListener {
                override fun sendComment(
                    type: Int,
                    inputText: String,
                    vipList: MutableList<String>
                ) {
                    if (type == 1) {
                        commentEditDialog?.dismiss()
                        startOpenVip()
                        return
                    }
                    var vipString = ""
                    if (vipList.isNotEmpty()) {
                        vipString = NormalUtil.getVipString(vipList)
                    }
                    releaseComment(inputText, vipString, "0", "0")
                }
            }, isEditVip = false, isPicture = true)
        commentEditDialog?.show(childFragmentManager, "commentEditDialog")
    }

    private fun getDynamicDetail(fetchPlayInfo: Boolean = dynamicData?.mediaUrl.isNullOrEmpty(), startPlay: Boolean = false) {
        isRefreshData = true
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.GET_DYNAMIC_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                //设置图片
                val dynamicBean = Gson().fromJson<DynamicBean?>(
                    response.toString(),
                    object : TypeToken<DynamicBean>() {}.type
                )
                if (dynamicBean != null) {
                    dynamicData = dynamicBean
                    fillData(dynamicBean)
                    if (showComment) {
                        showCommentDialog(dynamicBean.mediaKey ?: "", dynamicBean.comments)
                    }
                    if (fetchPlayInfo) {
                        getPlayInfo(dynamicBean.smallMediaKey ?: "", startPlay)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                isRefreshData = false
            }
        }, param, this)
    }

    /**
     * 获取对应播放地址
     */
    private fun getPlayInfo(mediaKey: String, startPlay: Boolean = false) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", 3)
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val clarityList = Gson().fromJson<MutableList<ClarityBean>?>(
                    response?.optJSONArray("list").toString(),
                    object : TypeToken<List<ClarityBean>>() {}.type
                )
                if (!clarityList.isNullOrEmpty()) {
                    val clarityBean = clarityList[0]
                    val mediaUrl = clarityBean.mediaUrl
                    if (!mediaUrl.isNullOrEmpty()) {
                        dynamicData?.mediaUrl = mediaUrl
                        if (startPlay) {
                            startPlay()
                        }
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                failedContainer.isVisible = true
                ClickUtils.applySingleDebouncing(btn_retry) {
                    getPlayInfo(mediaKey, startPlay)
                }
            }
        }, params, this)
    }

    /**
     * 关注、取消关注接口
     */
    private fun followClick(userId: Int) {
        val params = HttpParams()
        params.put("userId", userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                if (selected) {
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                }
                val event = VideoActionResultEvent()
                event.action =
                    if (selected) VideoActionResultEvent.ACTION_ADD else VideoActionResultEvent.ACTION_REMOVE
                dynamicData?.uid.let { event.id = it.toString() }
                event.type = 1
                event.userLogo = dynamicData?.headImg ?: ""
                event.userName = dynamicData?.upperName ?: ""
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 收藏、取消收藏
     */
    private fun collectionClick() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        val params = HttpParams()
        params.put("mediaKey", dynamicData?.mediaKey)
        dynamicData?.videoType?.let { params.put("videoType", it) }
        HttpRequest.get(RequestUrls.VIDEO_COLLECTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean =
                    Gson().fromJson(
                        response.toString(),
                        object : TypeToken<VideoLikeBean>() {}.type
                    )
                dynamicData?.isCollected = videoLikeBean.selected
                if (videoLikeBean.selected)
                    ImageUtil.clickAnim(requireActivity(), collectImg)

                val event = VideoActionResultEvent()
                event.type = 4
                event.id = dynamicData?.mediaKey ?: ""
                event.selected = videoLikeBean.selected
                event.actionType = VideoActionResultEvent.TYPE_DYNAMIC
                if (videoLikeBean.selected) {
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)

                setCollect(videoLikeBean.selected)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun deleteHttp() {
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.DELETE_ALBUM, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val event = DynamicEvent()
                event.action = DynamicEvent.DYNAMIC_REMOVE
                event.dynamicBean = dynamicData
                EventBus.getDefault().post(event)
                ToastUtils.showLong(R.string.deleteSuccess)
                requireActivity().finish()
                /* if (parentFragment is DynamicVideoDetailFragment) {
                     (parentFragment as DynamicVideoDetailFragment).remove(dynamicId)
                 }*/
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, param, this)
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
                //移除成功
                val status = response.optBoolean("status")
                EventBus.getDefault().post(BlackListEvent(uid, status))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }

    /**
     * 发布评论接口
     */
    private fun releaseComment(inputText: String, vipString: String, replyID: String, replyUserID: String) {
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean!!.token.uid)
        params.put("mediaKey", dynamicData?.mediaKey ?: "")
        params.put("ReplyID", replyID)
        params.put("ReplyUserID", replyUserID)
        params.put("Contxt", inputText)
        params.put("VIPExpression", vipString)
        HttpRequest.post(
            RequestUrls.RELEASE_COMMENT + "?videoType=${dynamicData?.videoType}",
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (response == null) {
                        return
                    }
                    val commentBean = Gson().fromJson<CommentBean>(
                        response.toString(),
                        object : TypeToken<CommentBean>() {}.type
                    )
                    val event = CommentEvent()
                    event.commentBean = commentBean
                    event.replyId = replyID.toInt()
                    event.mediaKey = dynamicData?.mediaKey ?: ""
                    event.replyUserID = replyUserID.toInt()
                    EventBus.getDefault().post(event)
                    ToastUtils.showLong(R.string.commentSuccess)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        if (dynamicData?.uid == event.uid) {
            dynamicData?.isBlackList = event.status
            setUserStatus(false, event.status)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        if (dynamicData?.mediaKey == event.mediaKey) {
            dynamicData?.comments = (dynamicData?.comments ?: 0) + 1
            setComment(dynamicData?.comments ?: 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFocusChange(event: VideoActionResultEvent) {
        val dynamicData = dynamicData ?: return
        if (dynamicData.uid.toString() == event.id && event.type == 1) {
            if (event.action == VideoActionResultEvent.ACTION_ADD) {
                setUserStatus(isFollow = true, isBlack = false)
            } else {
                setUserStatus(isFollow = false, isBlack = false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1111) {
                getDynamicDetail()
            }
        }
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

    /**
     * 跳转开通VIP页
     */
    private fun startOpenVip() {
        val intent = Intent(context, OpenVipActivity::class.java)
        intent.putExtra("pathInfo", requireActivity().javaClass.simpleName)
        startActivity(intent)
    }
}