package com.cqcsy.lgsp.video.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.CommentAdapter
import com.cqcsy.lgsp.adapter.CommentDetailAdapter
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.VideoRefreshEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.viewModel.CommentViewModel
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.lgsp.views.dialog.VoteOptionDialog
import com.cqcsy.lgsp.vip.NewVoteActivity
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_album_comment_header.view.commentCount
import kotlinx.android.synthetic.main.layout_comment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * 视频评论fragment
 */
class VideoCommentFragment : BaseFragment() {
    companion object {
        const val VIDEO_TYPE = 0
        const val ALBUM_TYPE = 1
        const val DYNAMIC_TYPE = 2
        const val FORM_TYPE = "formType"
        const val SHOW_INPUT = "showInput"
        const val ENABLE_REPORT = "enable_report"
    }

    // 评论适配器
    private lateinit var commentAdapter: CommentAdapter

    // 评论
    private var commentData: MutableList<CommentBean> = ArrayList()
    private var page: Int = 1
    private var size: Int = 30
    private val voteResultCode = 1002
    private var mediaKey = ""
    private var videoType = 0

    // 热门评论数量
    private var hotCommSize = -1

    // 需要定位滚动到某个位置的评论id
    private var scrollCommentId = 0

    // 需要定位滚动到评论回复id
    private var mReplyId = 0

    // 0:视频 1:相册 2:动态
    private var formType = VIDEO_TYPE

    /**
     * 举报功能是否可用
     */
    private var enableReport = true

    private var isShowHeader = false

    // 相册评论数量
    private var commentCount = 0
    private var showInput = true
    private var headerView: View? = null

    private val mViewModel: CommentViewModel by viewModels()

    private val mVideoModel: VideoViewModel by activityViewModels()
    private var mImageViewCache: WeakReference<ImageView?>? = null
    private var mTextViewCache: WeakReference<TextView?>? = null

    /**
     * 评论相关点击监听
     */
    private val commentClick: CommentAdapter.OnClickListener =
        object : CommentAdapter.OnClickListener {
            override fun onItemClick(
                type: Int,
                commentBean: CommentBean,
                zanImageView: ImageView?,
                zanTextView: TextView?
            ) {
                when (type) {
                    // 点击回复
                    0 -> commentEditClick(null, commentBean)
                    // 评论点赞
                    2 -> {
                        mImageViewCache = WeakReference(zanImageView)
                        mTextViewCache = WeakReference(zanTextView)
                        mViewModel.commentLike(videoType, commentBean)
                    }
                    // 长按
                    3 -> {
                        if (!commentBean.deleteState && (commentBean.replierUser?.id == GlobalValue.userInfoBean?.id)) {
                            //长按删除
                            showDeleteCommentDialog(commentBean)
                        } else {
                            if (enableReport) {
                                showReportDialog(commentBean)
                            }
                        }
                    }
                    // 投票
                    4 -> {
                        if (!GlobalValue.isLogin()) {
                            startLogin()
                            return
                        }
                        showVoteOptionDialog(commentBean)
                    }
                    // 点击头像
                    5 -> {
                        val intent = Intent(context, UpperActivity::class.java)
                        intent.putExtra(UpperActivity.UPPER_ID, commentBean.replierUser?.id)
                        startActivity(intent)
                    }
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_comment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        initData()
        initView()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initObserve() {
        mViewModel.mCommentLike.observe(this) {
            refreshLike(it.first, it.second)
        }
        mViewModel.mCommentData.observe(this) {
            setCommentData(it)
        }
        mViewModel.mCommentDelete.observe(this) {
            if (it.voteItem.isNotEmpty()) {
                removeVote(it)
            } else {
                refreshDelete(it.replyID)
            }
        }
        mViewModel.mCommentRelease.observe(this) {
            val event = it.second
            if (it.first == null) {
                // 直接回复视频评论
                addData(event.commentBean)
            } else {
                refreshData(it.first!!.replyID, event.commentBean)
                event.replyId = it.first!!.replyID
                event.replyUserID = it.first!!.replierUser?.id ?: 0
            }
            commentCount += 1
            headerView?.commentCount?.text = StringUtils.getString(R.string.albumCommentCount, commentCount)
            EventBus.getDefault().post(event)
            ToastUtils.showLong(R.string.commentSuccess)
        }
    }

    private fun initData() {
        mediaKey = arguments?.getString("mediaKey") ?: ""
        isShowHeader = arguments?.getBoolean("isShowHeader") ?: false
        videoType = arguments?.getInt("videoType") ?: 0
        commentCount = arguments?.getInt("commentCount") ?: 0
        formType = arguments?.getInt(FORM_TYPE, VIDEO_TYPE) ?: VIDEO_TYPE
        enableReport = arguments?.getBoolean(ENABLE_REPORT, true) ?: true
        showInput = arguments?.getBoolean(SHOW_INPUT) ?: true
        scrollCommentId = arguments?.getInt(VideoBaseActivity.COMMENT_ID) ?: 0
        mReplyId = arguments?.getInt(VideoBaseActivity.REPLY_ID) ?: 0
    }

    private fun initView() {
        if (formType == ALBUM_TYPE || formType == DYNAMIC_TYPE) {
            sendVote.visibility = View.GONE
        }
        bottomLayout.isVisible = showInput

        initRefresh()
        commentRecycle.itemAnimator = null
        if (GlobalValue.isLogin()) {
            GlobalValue.userInfoBean!!.avatar?.let {
                ImageUtil.loadCircleImage(
                    requireContext(), it, commentUserImage
                )
            }
        }
        commentRecycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setAdapter()
        commentEdit.setOnClickListener {
            commentEditClick(null, null)
        }
        sendVote.setOnClickListener {
            sendVote()
        }
        mViewModel.getCommentList(mediaKey, videoType, page, size, scrollCommentId, mReplyId)
    }

    private fun initRefresh() {
        commentRefresh.setEnableRefresh(false)
        commentRefresh.setEnableLoadMore(true)
        commentRefresh.setEnableAutoLoadMore(true)
        commentRefresh.setEnableOverScrollBounce(true)
        commentRefresh.setEnableLoadMoreWhenContentNotFull(false)

        commentRefresh.setDisableContentWhenLoading(false)
        commentRefresh.setDisableContentWhenRefresh(false)

        commentRefresh.setOnLoadMoreListener { onLoadMore() }
    }

    private fun setAdapter() {
        commentAdapter = CommentAdapter(commentData, commentClick, videoType, mediaKey, mReplyId)
        commentRecycle.adapter = commentAdapter
        try {
            if (isShowHeader) {
                adapterAddHeader()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adapterAddHeader() {
        headerView = View.inflate(context, R.layout.layout_album_comment_header, null)
        headerView!!.commentCount.text = StringUtils.getString(R.string.albumCommentCount, commentCount)
        commentAdapter.removeAllHeaderView()
        commentAdapter.addHeaderView(headerView!!)
    }

    private fun onLoadMore() {
        mViewModel.getCommentList(mediaKey, videoType, page, size, scrollCommentId, mReplyId)
    }

    /**
     * 拉黑提示框
     */
    private fun showBlackUserDialog(commentBean: CommentBean) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.blackUser)
        tipsDialog.setMsg(R.string.blackUserTips)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.confirmBlackUser) {
            mVideoModel.forbidden(commentBean.replierUser?.id ?: 0, commentBean.isForbidden)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

    private fun setCommentData(response: JSONObject?) {
        dismissProgressView()
        if (response == null) {
            if (formType != DYNAMIC_TYPE) {
                showFailedView {
                    page = 1
                    showProgressView()
                    mViewModel.getCommentList(mediaKey, videoType, page, size, scrollCommentId, mReplyId)
                }
            }
            return
        }
        var hotListSize = 0
        commentAdapter.scrollCommentId = scrollCommentId
        if (page == 1) {
            commentData.clear()
        }
        val hotList: List<CommentBean> = Gson().fromJson(
            response.optJSONArray("hot")?.toString(),
            object : TypeToken<List<CommentBean>>() {}.type
        )
        if (hotList.isNotEmpty()) {
            hotListSize = hotList.size
            hotCommSize = hotListSize - 1
            commentAdapter.hotSize = hotCommSize
            commentData.addAll(hotList)
            commentAdapter.notifyDataSetChanged()
        }
        val normalComment = response.optJSONArray("normal")
        if (normalComment == null || normalComment.length() == 0) {
            if (commentData.isEmpty()) {
                commentRefresh.setEnableLoadMore(false)
                showEmptyView()
            } else {
                commentRefresh.finishLoadMoreWithNoMoreData()
            }
            return
        }
        val list: MutableList<CommentBean> = ArrayList()
        for (i in 0 until normalComment.length()) {
            val commentBean = Gson().fromJson(normalComment[i].toString(), CommentBean::class.java)
            if (commentBean.voteItem.isEmpty()) {
                commentBean.itemType = 0
            } else {
                commentBean.itemType = 1
            }
            list.add(commentBean)
        }
        commentData.addAll(list)
        commentAdapter.notifyDataSetChanged()
        scrollRecycler()

        if ((list.size + hotListSize) >= size) {
            page += 1
            commentRefresh.finishLoadMore()
        } else {
            commentRefresh.finishLoadMoreWithNoMoreData()
        }
    }

    /**
     * 设置滚动到具体位置
     */
    private fun scrollRecycler() {
        if (scrollCommentId > 0) {
            for (i in commentData.indices) {
                val item = commentData[i]
                if (scrollCommentId == item.replyID) {
                    commentRecycle.smoothScrollToPosition(i)
                    if (showInput && mReplyId > 0) {
                        Handler().postDelayed({
                            commentEditClick(mReplyId, item)
                        }, 200)
                    }
                    break
                }
            }
        }
    }

    /**
     * 投票dialog
     */
    fun showVoteOptionDialog(commentBean: CommentBean) {
        val voteOptionDialog = VoteOptionDialog(
            requireContext(),
            commentBean,
            videoType,
            object : VoteOptionDialog.OnVoteListener {
                override fun onVoteClick(commentBean: CommentBean) {
                    refreshVote(commentBean)
                }
            })
        voteOptionDialog.show()
    }

    private fun showDeleteCommentDialog(commentBean: CommentBean) {
        val dialog = object : BottomBaseDialog(requireContext()) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.layout_comment_action)
                findViewById<TextView>(R.id.cancel).setOnClickListener { dismiss() }
                findViewById<TextView>(R.id.delete).isVisible = true
                findViewById<TextView>(R.id.report).isVisible = false
                findViewById<TextView>(R.id.action_forbidden).isVisible = false
                findViewById<TextView>(R.id.delete).setOnClickListener {
                    dismiss()
                    mViewModel.deleteComment(videoType, commentBean)
                }
            }
        }
        dialog.show()
    }

    private fun showReportDialog(commentBean: CommentBean) {
        val dialog = object : BottomBaseDialog(requireContext()) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.layout_comment_action)
                findViewById<TextView>(R.id.cancel).setOnClickListener { dismiss() }
                findViewById<TextView>(R.id.delete).isVisible = false
                findViewById<TextView>(R.id.report).isVisible = true
                val forbidden = findViewById<TextView>(R.id.action_forbidden)
                forbidden.isVisible = true
                findViewById<TextView>(R.id.report).setOnClickListener {
                    if (!GlobalValue.isLogin()) {
                        startLogin()
                        dismiss()
                        return@setOnClickListener
                    }
                    val dialog = ReportCommentDialog(requireContext())
                    dialog.setReportContent(
                        commentBean.replyID.toString(),
                        commentBean.replierUser?.id ?: 0
                    )
                    dialog.listener = object : ReportCommentDialog.OnReportListener {
                        override fun onReportSuccess(reportId: String) {
                            showReportSuccessDialog()
                            refreshDelete(commentBean.replyID)
                        }

                    }
                    dialog.show()
                    dismiss()
                }
                forbidden.text = if (commentBean.isForbidden) {
                    context.getString(R.string.blacklist_remove)
                } else {
                    context.getString(R.string.blacklist)
                }
                forbidden.setOnClickListener {
                    if (!GlobalValue.isLogin()) {
                        startLogin()
                        dismiss()
                        return@setOnClickListener
                    }
                    if (commentBean.isForbidden) {
                        mVideoModel.forbidden(commentBean.replierUser?.id ?: 0, commentBean.isForbidden)
                    } else {
                        showBlackUserDialog(commentBean)
                    }
                    dismiss()
                }
            }
        }
        dialog.show()
    }

    /**
     * 刷新投票后页面
     */
    private fun refreshVote(commentBean: CommentBean) {
        for (i in commentData.indices) {
            if (commentBean.replyID == commentData[i].replyID) {
                notifyItemChange(i)
                break
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: VideoRefreshEvent) {
        if (event.needRefresh) {
            mediaKey = event.mediaKey
            page = 1
            if (commentData.isEmpty()) {
                showProgressView()
            }
            mViewModel.getCommentList(mediaKey, videoType, page, size, scrollCommentId, mReplyId)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        if (event.status) {
            refreshBlack(event.uid)
        }
    }

    /**
     * 回复评论成功后刷新页面
     */
    fun refreshData(replyId: Int, child: CommentBean?) {
        if (child == null) return
        for ((index, item) in commentData.withIndex()) {
            if (dealReply(item, replyId, index, child, item)) {
                return
            } else if (!item.children.isNullOrEmpty()) {
                for (c in item.children!!) {
                    if (dealReply(c, replyId, index, child, item)) return
                }
            }
        }
    }

    /**
     * @param item 当前item
     * @param replyId 回复评论ID
     * @param position 被回复的评论位置
     * @param child 回复的评论对象
     * @param replyComment 找到回复对象后，需要将评论添加到该对象的children中
     */
    private fun dealReply(
        item: CommentBean,
        replyId: Int,
        position: Int,
        child: CommentBean,
        replyComment: CommentBean
    ): Boolean {
        if (replyComment.children == null) {
            replyComment.children = ArrayList()
        }
        if (item.replyID == replyId) {
            replyComment.children!!.add(0, child)
            // 未加载更多时固定显示三条
//            if (replyComment.children!!.size > (replyComment.pageIndex - 1) * CommentAdapter.pageSize + 3) {
//                replyComment.repliesNumber++
//                replyComment.children!!.removeLast()
//            }
            if (replyComment.children!!.find { it.replyID == mReplyId } == null) {
                mReplyId = 0
                commentAdapter.replyId = 0
            }
            notifyItemChange(position)
            return true
        }
        return false
    }

    /**
     * 点赞成功后刷新页面
     */
    private fun refreshLike(commentBean: CommentBean, videoLikeBean: VideoLikeBean) {
        commentBean.likesNumber = videoLikeBean.count
        commentBean.likeStatus = videoLikeBean.selected
        val imageView: View? = mImageViewCache?.get()
        val number: View? = mTextViewCache?.get()
//        以下代码回复二级评论点赞有bug
//        if (commentBean.oldReplyID != 0) {
//            val p = commentAdapter.getItemPosition(commentBean.oldReplyID)
//            val replyList = commentAdapter.getViewByPosition(p, R.id.replay_list) as RecyclerView?
//            if (replyList?.adapter is CommentDetailAdapter) {
//                val replyAdapter = replyList.adapter as CommentDetailAdapter
//                val tempPosition = replyAdapter.getItemPosition(commentBean.replyID)
//                imageView = replyAdapter.getViewByPosition(tempPosition, R.id.commentFabulousImage)
//                number = replyAdapter.getViewByPosition(tempPosition, R.id.commentFabulousCount)
//            }
//        } else {
//            val p = commentAdapter.getItemPosition(commentBean)
//            imageView = commentAdapter.getViewByPosition(p, R.id.commentFabulousImage)
//            number = commentAdapter.getViewByPosition(p, R.id.commentFabulousCount)
//        }
        if (imageView != null) {
            if (videoLikeBean.selected) {
                var parent = parentFragment?.view
                if (parent == null) {
                    parent = view
                }
                ImageUtil.clickAnim(parent as ViewGroup?, imageView)
            }
            imageView.isSelected = videoLikeBean.selected
        }
        if (number is TextView) {
            number.isSelected = videoLikeBean.selected
            if (videoLikeBean.count > 0) {
                number.text = NormalUtil.formatPlayCount(videoLikeBean.count)
            } else {
                number.text = StringUtils.getString(R.string.fabulous)
            }
        }
        mTextViewCache = null
        mImageViewCache = null
    }

    /**
     * 点赞成功后刷新页面
     */
    fun refreshLike(replyID: Int, count: Int, likeStatus: Boolean) {
        for ((index, bean) in commentData.withIndex()) {
            if (bean.replyID == replyID) {
                bean.likesNumber = count
                bean.likeStatus = likeStatus
                notifyItemChange(index)
                break
            }
        }
    }

    private fun notifyItemChange(position: Int) {
        commentAdapter.notifyItemChanged((commentAdapter.headerLayoutCount) + position)
    }

    private fun notifyItemRemoved(position: Int) {
        commentAdapter.notifyItemRemoved((commentAdapter.headerLayoutCount) + position)
    }

    /**
     * 拉黑用户后刷新页面
     */
    fun refreshBlack(userId: Int) {
        val iterator = commentData.iterator()
        var index = 0
        while (iterator.hasNext()) {
            val bean = iterator.next()
            if (userId == bean.replierUser?.id) {
                iterator.remove()
                notifyItemRemoved(index)
                if (index <= hotCommSize) {     // 移除热门评论，分割处理
                    hotCommSize--
                    commentAdapter.hotSize = hotCommSize
                    notifyItemChange(hotCommSize)
                }
                continue
            } else if (!bean.children.isNullOrEmpty()) {
                val childrenIterator = bean.children!!.iterator()
                var needRefresh = false
                while (childrenIterator.hasNext()) {
                    val next = childrenIterator.next()
                    if (userId == next.replierUser?.id) {
                        childrenIterator.remove()
                        needRefresh = true
                    }
                }
                if (needRefresh) {
                    notifyItemChange(index)
                }
            }
            index++
        }
        if (commentData.isEmpty()) {
            showEmptyView()
        }
    }

    /**
     * 删除评论后刷新页面
     */
    private fun refreshDelete(replyID: Int) {
        for ((index, bean) in commentData.withIndex()) {
            if (dealDelete(bean, index, replyID, bean)) {
                return
            } else if (!bean.children.isNullOrEmpty()) {
                for (item in bean.children!!) {
                    if (dealDelete(item, index, replyID, bean)) {
                        return
                    }
                }
            }
        }
    }

    private fun dealDelete(item: CommentBean, position: Int, replyId: Int, parentComment: CommentBean): Boolean {
        if (item.replyID == replyId) {
            item.deleteState = true
            if (item.replyID == parentComment.replyID) {
                commentData.remove(item)
                commentAdapter.notifyItemRemoved(position)
                if (commentData.isEmpty()) {
                    showEmptyView()
                }
            } else {
                parentComment.children?.remove(item)
                notifyItemChange(position)
            }
            return true
        }
        return false
    }

    private fun removeVote(bean: CommentBean) {
        val position = commentAdapter.getItemPosition(bean)
        commentAdapter.remove(bean)
        if (position != -1) {
            commentAdapter.notifyItemRemoved(position)
        }
    }

    /**
     * 新增评论、投票发布后刷新页面
     */
    private fun addData(commentBean: CommentBean?) {
        if (commentBean == null) return
        if (commentData.isEmpty()) {
            dismissProgressView()
        }
        val index = hotCommSize + 1
        commentData.add(index, commentBean)
        commentAdapter.notifyItemInserted(index)
        Handler().postDelayed({
            if (formType == DYNAMIC_TYPE || formType == ALBUM_TYPE) {
                commentRecycle.scrollToPosition(index + 1)
            } else {
                commentRecycle.scrollToPosition(index)
            }
        }, 100)
    }

    /**
     * 发起投票
     */
    private fun sendVote() {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        if (!GlobalValue.isVipUser() && (GlobalValue.userInfoBean!!.userExtension?.currentLevel ?: 0) < 4) {
            showVipInfoFragment()
            return
        }
        val intent = Intent(context, NewVoteActivity::class.java)
        intent.putExtra("mediaKey", mediaKey)
        intent.putExtra("videoType", videoType)
        startActivityForResult(intent, voteResultCode)
    }

    /**
     * vip套餐支付页Fragment
     */
    private fun showVipInfoFragment() {
        if (isShowHeader) {
            val intent = Intent(requireContext(), OpenVipActivity::class.java)
            intent.putExtra("pathInfo", requireActivity().javaClass.simpleName)
            startActivity(intent)
            return
        }
        var vipFragment = parentFragmentManager.findFragmentByTag(VipPayFragment::class.java.simpleName)
        val transaction = parentFragmentManager.beginTransaction()
        if (vipFragment == null) {
            vipFragment = VipPayFragment()
            val bundle = Bundle()
            bundle.putString("pathInfo", requireActivity().javaClass.simpleName + "/" + mediaKey)
            vipFragment.arguments = bundle
            transaction.add(R.id.bottomContainer, vipFragment, VipPayFragment::class.java.simpleName)
        }
        transaction.show(vipFragment)
        transaction.commitNowAllowingStateLoss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == voteResultCode) {
                val commentBean = data?.getSerializableExtra("commentBean") as CommentBean
                commentBean.itemType = 1
                addData(commentBean)
            }
        }
    }

    private fun showProgressView() {
        if (isSafe()) {
            statusView.showProgress()
        }
    }

    private fun dismissProgressView() {
        if (isSafe()) {
            statusView.dismissProgress()
        }
    }

    private fun showFailedView(listener: View.OnClickListener) {
        if (isSafe()) {
            statusView.showFailed(listener)
        }
    }

    private fun showEmptyView() {
        if (isSafe()) {
            commentAdapter.notifyDataSetChanged()
            statusView.showEmpty()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = SizeUtils.dp2px(40f)
            statusView.findViewById<View>(R.id.image_empty).layoutParams = params
            statusView.findViewById<TextView>(R.id.large_tip).text =
                StringUtils.getString(R.string.commentEmpty)
            statusView.findViewById<TextView>(R.id.little_tip).text =
                StringUtils.getString(R.string.noCommentTips)
        }
    }

    private fun showReportSuccessDialog() {
        val dialog = TipsDialog(requireContext())
        dialog.setDialogTitle(R.string.received_your_report_tip)
        dialog.setMsg(R.string.report_success_tip)
        dialog.setRightListener(R.string.known) {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * 点击弹出评论输入框
     * commentBean 回复当前对象的数据bean,不是回复别人则为null
     * replyName 被回复的名字，不是回复则为空
     */
    private var commentEditDialog: CommentEditDialog? = null
    private fun commentEditClick(targetReplayId: Int?, commentBean: CommentBean?) {
        if (!GlobalValue.isLogin()) {
            startLogin()
            return
        }
        val targetComment: CommentBean? =
            if (targetReplayId == null || targetReplayId == commentBean?.replyID) {
                commentBean
            } else if (!commentBean?.children.isNullOrEmpty()) {
                var result: CommentBean? = null
                for (item in commentBean!!.children!!) {
                    if (targetReplayId == item.replyID) {
                        result = item
                        break
                    }
                }
                result
            } else {
                null
            }
        commentEditDialog = CommentEditDialog(targetComment?.replierUser?.nickName, object : CommentEditDialog.SendCommentListener {
            override fun sendComment(type: Int, inputText: String, vipList: MutableList<String>) {
                if (type == 1) {
                    commentEditDialog?.dismiss()
                    showVipInfoFragment()
                    return
                }
                var vipString = ""
                if (vipList.isNotEmpty()) {
                    vipString = NormalUtil.getVipString(vipList)
                }
                mViewModel.releaseComment(mediaKey, videoType, inputText, vipString, targetComment)
            }
        })
        commentEditDialog?.show(parentFragmentManager, "commentEditDialog")
    }
}