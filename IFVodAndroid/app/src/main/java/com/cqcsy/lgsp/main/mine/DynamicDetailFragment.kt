package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.set
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.*
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.mine.DynamicDetailFragment.OnImageLoadListener
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.GlideApp
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.fragment_dynamic_detail.*
import kotlinx.android.synthetic.main.layout_save_pic_dialog.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

/**
 * 作者：wangjianxiong
 * 创建时间：2022/8/18
 *
 *
 */
class DynamicDetailFragment : BaseFragment() {
    companion object {

        /**
         * 动态详情图片比例阈值
         */
        const val DYNAMIC_DETAIL_IMAGE_RATIO_THRESHOLD = 0.7

        @JvmStatic
        fun newInstance(
            mediaKey: String?,
            dynamicBean: DynamicBean? = null,
            position: Int = 0,
            commentId: Int = 0,
            replyId: Int = 0,
            showComment: Boolean = false,
            isFromMineDynamic: Boolean = false
        ): DynamicDetailFragment {
            val args = Bundle()
            args.putSerializable("dynamic_bean", dynamicBean)
            args.putString("dynamic_id", mediaKey)
            args.putInt("start_position", position)
            args.putInt("reply_id", replyId)
            args.putInt("comment_id", commentId)
            args.putBoolean("show_comment", showComment)
            args.putBoolean("from_mine_dynamic", isFromMineDynamic)
            val fragment = DynamicDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mediaKey: String? = null
    private var commentId: Int = 0
    private var replyId: Int = 0
    private var index: Int = 0
    private var isMyself: Boolean = false
    private var isFromMineDynamic: Boolean = false
    private var dynamicData: DynamicBean? = null
    private var commentEditDialog: CommentEditDialog? = null
    private var isMaskGone = false
    private var showComment = false
    private lateinit var pagerAdapter: PicturePagerAdapter

    /**
     * 用于存储长图标识
     */
    private val longImg = SparseBooleanArray()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dynamic_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initParams()
        init()
    }

    private fun initParams() {
        mediaKey = arguments?.getString("dynamic_id")
        commentId = arguments?.getInt("comment_id", 0) ?: 0
        replyId = arguments?.getInt("reply_id", 0) ?: 0
        showComment = arguments?.getBoolean("show_comment", false) ?: false
        index = arguments?.getInt("start_position", 0) ?: 0
        isFromMineDynamic = arguments?.getBoolean("from_mine_dynamic", false) ?: false
        dynamicData = arguments?.getSerializable("dynamic_bean") as? DynamicBean
    }

    private fun init() {
        ImageUtil.loadCircleImage(this, GlobalValue.userInfoBean?.avatar, commentUserImage)
        back.setOnClickListener { activity?.finish() }
        title_container.updateLayoutParams<FrameLayout.LayoutParams> {
            topMargin = BarUtils.getStatusBarHeight()
        }
        initPagerAdapter()
        val dynamicBean = dynamicData
        if (dynamicBean == null) {
            loading_view.showProgress(getString(R.string.loading))
            bottom_container.isVisible = false
        } else {
            bottom_container.isVisible = true
            fillData(dynamicBean, false)
        }
        getDetail(false)
        addHot(mediaKey)
    }

    private fun initPagerAdapter() {
        pagerAdapter = PicturePagerAdapter()
        pagerAdapter.onImageLoadListener =
            OnImageLoadListener { position, isLongImage ->
                setLongImageFlag(position, isLongImage)
            }
        image_gallery.setPageTransformer(null)
        image_gallery.adapter = pagerAdapter
        image_gallery.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                index = position
                tv_gallery_index.text =
                    String.format("%d/%d", position + 1, dynamicData?.trendsDetails?.size ?: 0)
                tv_image_flag.isVisible = longImg[position]
            }
        })
        image_gallery.setCurrentItem(index, false)
    }

    private fun getDetail(showLoading: Boolean) {
        if (showLoading) {
            loading_view.showProgress()
        }
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.GET_DYNAMIC_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                loading_view.dismissProgress()
                val dynamicBean = Gson().fromJson<DynamicBean>(
                    response.toString(),
                    object : TypeToken<DynamicBean>() {}.type
                )
                dynamicData = dynamicBean
                bottom_container.isVisible = true
                fillData(dynamicBean)
                //一些不可重复的操作放在此处
                dynamicData?.let {
                    LabelUtil.cacheDynamicLabel(it.label, it.uid)
                    addRecord(it)
                    if (showComment) {
                        showCommentDialog(it.mediaKey ?: "", it.comments)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                loading_view.showFailed {
                    getDetail(true)
                }
            }
        }, param, this)
    }

    private fun fillData(data: DynamicBean, updateImages: Boolean = true) {
        isMyself = GlobalValue.userInfoBean?.id == data.uid
        showByUserStatus()
        if (updateImages) {
            val trendsDetails = data.trendsDetails
            if (!trendsDetails.isNullOrEmpty()) {
                tv_gallery_index.isVisible = true
                tv_gallery_index.text =
                    String.format("%d/%d", index + 1, trendsDetails.size)
                pagerAdapter.setList(trendsDetails)
                //compareAndUpdate(trendsDetails)
            } else {
                tv_gallery_index.isVisible = false
            }
        }

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
        comment_container.setOnClickListener {
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

    private fun compareAndUpdate(newImageList: MutableList<ImageBean>) {
        if (pagerAdapter.data.size == newImageList.size) {
            var same = true
            for ((i, item) in pagerAdapter.data.withIndex()) {
                val newItem = newImageList[i]
                if (item.imgPath != newItem.imgPath) {
                    same = false
                    break
                }
            }
            if (same) {
                return
            }
        }
        pagerAdapter.setList(newImageList)
    }

    private fun showByUserStatus() {
        if (isFromMineDynamic) {
            user_action.isVisible = true
            user_action.setOnClickListener { showUserAction() }
        } else {
            user_action.isVisible = false
        }
    }

    private fun showExpandAnim() {
        tv_expand.isVisible = false
        tv_fold.isVisible = true
        expand_text.movementMethod = ScrollingMovementMethod.getInstance()
        content_container.transitionToEnd()
    }

    private fun showFoldAnim() {
        tv_expand.isVisible = true
        tv_fold.isVisible = false
        expand_text.movementMethod = null
        content_container.transitionToStart()
    }

    private fun showCommentDialog(mediaKey: String, commentCount: Int) {
        DynamicCommentDialog.show(childFragmentManager, commentId, replyId, mediaKey, commentCount, true, dynamicData?.videoType)
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

    override fun onLoginOut() {
        ImageUtil.loadCircleImage(this, GlobalValue.userInfoBean?.avatar, commentUserImage)
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

    /**
     * 点击弹出评论输入框
     * commentBean 回复当前对象的数据bean,不是回复别人则为null
     * replyName 被回复的名字，不是回复则为空
     */
    private fun showCommentInput() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        commentEditDialog = CommentEditDialog(null, object : CommentEditDialog.SendCommentListener {
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
                releaseComment(inputText, vipString)
            }
        }, isEditVip = false, isPicture = true)
        commentEditDialog?.show(childFragmentManager, "commentEditDialog")
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
                    ImageUtil.clickAnim(activity, collectImg)

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
                    ImageUtil.clickAnim(activity, zanImg)
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

    /**
     * 跳转开通VIP页
     */
    private fun startOpenVip() {
        val intent = Intent(context, OpenVipActivity::class.java)
        intent.putExtra("pathInfo", requireActivity().javaClass.simpleName)
        startActivity(intent)
    }

    /**
     * 发布评论接口
     */
    private fun releaseComment(inputText: String, vipString: String) {
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean!!.token.uid)
        params.put("mediaKey", dynamicData?.mediaKey ?: "")
        params.put("Contxt", inputText)
        params.put("VIPExpression", vipString)
        HttpRequest.post(RequestUrls.RELEASE_COMMENT + "?videoType=${dynamicData?.videoType}", object : HttpCallBack<JSONObject>() {
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
                event.mediaKey = dynamicData?.mediaKey ?: ""
                EventBus.getDefault().post(event)
                ToastUtils.showLong(R.string.commentSuccess)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
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
                tv_follow.isChecked = selected
                if (selected) {
                    tv_follow.text = resources.getString(R.string.followed)
                } else {
                    tv_follow.text = resources.getString(R.string.attention)
                }
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
                setUserStatus(false, status)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }

    private fun addHot(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.PICTURES_HOT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {

            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params)
    }

    private fun addRecord(bean: DynamicBean) {
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
        dynamicRecordBean.type = 0
        DynamicRecordManger.instance.add(dynamicRecordBean)
    }

    /**
     * 长按保存图片弹框
     */
    private fun showSavePic(imgPath: String) {
        if (context == null) return
        val dialog = object : BottomBaseDialog(requireContext()) {}
        val contentView = View.inflate(context, R.layout.layout_save_pic_dialog, null)
        contentView.cancel.setOnClickListener {
            dialog.dismiss()
        }
        contentView.savePic.setOnClickListener {
            GlideApp.with(this).downloadOnly().load(imgPath).into(object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    ImageUtil.saveImage(requireContext(), resource.absolutePath)
                    ToastUtils.showLong(R.string.saveSuccess)
                    dialog.dismiss()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    dialog.dismiss()
                }
            })
        }
        dialog.setContentView(contentView)
        if (isSafe())
            dialog.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        if (dynamicData != null && event.mediaKey == dynamicData!!.mediaKey) {
            setComment(++dynamicData!!.comments)
        }
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(context, LoginActivity::class.java))
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
            val intent = Intent(context, ReleaseDynamicActivity::class.java)
            intent.putExtra("dynamicBean", dynamicData)
            startActivityForResult(intent, 1111)
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

    private fun deleteHttp() {
        val param = HttpParams()
        param.put("mediaKey", dynamicData?.mediaKey)
        HttpRequest.post(RequestUrls.DELETE_ALBUM, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val event = DynamicEvent()
                event.action = DynamicEvent.DYNAMIC_REMOVE
                event.dynamicBean = dynamicData
                EventBus.getDefault().post(event)
                ToastUtils.showLong(R.string.deleteSuccess)
                requireActivity().finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, param, this)
    }

    private fun setLongImageFlag(position: Int, isLongImage: Boolean) {
        longImg[position] = isLongImage
        if (index == position) {
            tv_image_flag.isVisible = isLongImage
        } else {
            tv_image_flag.isVisible = longImg[index]
        }
    }

    private fun showGoneAnim() {
        isMaskGone = if (isMaskGone) {
            title_container.animate()
                .translationY(-(title_container.height + BarUtils.getStatusBarHeight()).toFloat())
                .translationY(0f).setDuration(200).start()
            bottom_container.animate().translationY(bottom_container.height.toFloat())
                .translationY(0f).setDuration(200).start()
            false
        } else {
            title_container.animate()
                .translationY(-(title_container.height + BarUtils.getStatusBarHeight()).toFloat())
                .setDuration(200).start()
            bottom_container.animate().translationY(bottom_container.height.toFloat())
                .setDuration(200).start()
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1111) {
                getDetail(false)
            }
        }
    }

    inner class PicturePagerAdapter :
        BaseQuickAdapter<ImageBean, BaseViewHolder>(R.layout.item_dynamic_picture) {
        var onImageLoadListener: OnImageLoadListener? = null

        override fun convert(holder: BaseViewHolder, item: ImageBean) {
            val pictureImage = holder.getView<SubsamplingScaleImageView>(R.id.pictureImage)
            val pictureGif = holder.getView<ImageView>(R.id.pictureGif)
            val progress = holder.getView<LinearLayout>(R.id.progress)
            val ratio = item.ratioValue
            pictureImage.minScale = 1.0f
            pictureImage.maxScale = 4.0f
            pictureImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            pictureImage.updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (ratio >= DYNAMIC_DETAIL_IMAGE_RATIO_THRESHOLD) {
                    //顶部状态栏高度+标题栏高度
                    topMargin = BarUtils.getStatusBarHeight() + SizeUtils.dp2px(56f)
                    this.bottomToBottom = -1
                    this.dimensionRatio = "3:4"
                } else {
                    topMargin = 0
                    this.bottomToBottom = 0
                    this.dimensionRatio = null
                }
            }
            pictureImage.setOnClickListener {
                showGoneAnim()
            }
            pictureImage.setOnLongClickListener {
                val url = ImageUtil.formatUrl(item.imgPath)
                if (!url.isNullOrEmpty()) {
                    showSavePic(url)
                }
                return@setOnLongClickListener true
            }
            val imageUrl = ImageUtil.formatUrl(item.imgPath) ?: ""
            ImageUtil.downloadOnly(this@DynamicDetailFragment, imageUrl, object :
                CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    progress.isVisible = false
                    if (imageUrl.contains(".gif", true)) {
                        pictureGif.visibility = View.VISIBLE
                        pictureImage.visibility = View.GONE
                        ImageUtil.loadGif(
                            this@DynamicDetailFragment,
                            resource.absolutePath,
                            pictureGif,
                            ImageView.ScaleType.FIT_CENTER,
                            isCache = true
                        )
                    } else {
                        pictureGif.visibility = View.GONE
                        pictureImage.visibility = View.VISIBLE
                        val option = BitmapFactory.Options()
                        option.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(resource.absolutePath, option)
                        if (option.outHeight > ScreenUtils.getAppScreenHeight()) {
                            pictureImage.setDoubleTapZoomScale(ScreenUtils.getScreenWidth() / option.outWidth.toFloat())
                            pictureImage.minScale =
                                ScreenUtils.getAppScreenHeight() / option.outHeight.toFloat()
                            val position = holder.adapterPosition
                            if (ImageUtil.isLongImage(option.outWidth, option.outHeight)) {
                                //是长图
                                onImageLoadListener?.onImageLoadSuccess(position, true)
                                pictureImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                            } else {
                                onImageLoadListener?.onImageLoadSuccess(position, false)
                            }
                        }
                        pictureImage.setImage(ImageSource.uri(resource.absolutePath))
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    progress.isVisible = false
                    pictureImage.setImage(ImageSource.resource(R.mipmap.image_default))
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
        }

    }

    fun interface OnImageLoadListener {
        fun onImageLoadSuccess(position: Int, isLongImage: Boolean)
    }
}