package com.cqcsy.lgsp.adapter

import android.graphics.Paint
import android.text.Html
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.utils.EmotionUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.utils.VoteResultUtil
import com.cqcsy.lgsp.views.widget.ExpandTextView
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONObject

/**
 * 评论适配器
 */
class CommentAdapter(data: MutableList<CommentBean>, var listener: OnClickListener?, var videoType: Int, var mediaKey: String, var replyId: Int = 0) :
    BaseMultiItemQuickAdapter<CommentBean, BaseViewHolder>(data) {

    companion object {
        val pageSize = 10

        fun setComment(commentValue: String?, item: CommentBean, commentText: ExpandTextView, width: Float) {
            if (commentValue.isNullOrEmpty()) {
                commentText.visibility = View.GONE
            } else {
                commentText.visibility = View.VISIBLE
                commentText.paint.isAntiAlias = true
                if (item.isForbidden) {
                    commentText.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    commentText.paint.flags = 0
                }
                commentText.initWidth(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(width))
                commentText.setOriginalText(Html.fromHtml(commentValue), item.isContextExpand)
            }
            commentText.setOnExpandClickListener(object : ExpandTextView.OnExpandClickListener {

                override fun onExpandStatusChange(expand: Boolean) {
                    item.isContextExpand = expand
                }

            })
            when {
                item.deleteState -> {
                    commentText.setTextColor(ColorUtils.getColor(R.color.word_color_11))
                }

                item.isForbidden -> {
                    commentText.setTextColor(ColorUtils.getColor(R.color.word_color_5))
                }

                else -> {
                    commentText.setTextColor(ColorUtils.getColor(R.color.word_color_2))
                }
            }
        }
    }

    var hotSize: Int = -1

    // 需要定位滚动到某个位置的评论id
    var scrollCommentId: Int = 0

    init {
        addItemType(0, R.layout.item_comment)
        addItemType(1, R.layout.item_vote)
    }

    override fun getItemPosition(item: CommentBean?): Int {
        val position = data.indexOfFirst { item?.replyID == it.replyID }
        return position + headerLayoutCount
    }

    fun getItemPosition(replyId: Int): Int {
        val position = data.indexOfFirst { replyId == it.replyID }
        return position + headerLayoutCount
    }

    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        var commentFabulousImage: ImageView? = null
        var commentFabulousCount: TextView? = null
        when (item.itemType) {
            0 -> {
                commentFabulousImage = holder.getView(R.id.commentFabulousImage)
                commentFabulousCount = holder.getView(R.id.commentFabulousCount)
                setCommentView(holder, item)
            }

            1 -> {
                setVoteView(holder, item)
            }
        }
        holder.itemView.setOnLongClickListener {
            listener?.onItemClick(
                3,
                item,
                commentFabulousImage,
                commentFabulousCount
            )
            return@setOnLongClickListener false
        }
    }

    private fun setCommentView(holder: BaseViewHolder, item: CommentBean) {
        if (scrollCommentId == item.replyID) {
            holder.setBackgroundColor(R.id.commentLayout, ColorUtils.getColor(R.color.white_05))
        } else {
            holder.setBackgroundColor(R.id.commentLayout, ColorUtils.getColor(R.color.transparent))
        }
        item.replierUser?.avatar?.let {
            ImageUtil.loadCircleImage(context, it, holder.getView(R.id.commentImage))
        }
        val vipGradeImage = holder.getView<ImageView>(R.id.userVipImage)
        if (item.replierUser != null) {
            if (item.replierUser!!.bigV) {
                vipGradeImage.visibility = View.VISIBLE
                vipGradeImage.setImageResource(R.mipmap.icon_big_v)
            } else {
                if (item.replierUser!!.vipLevel > 0) {
                    vipGradeImage.visibility = View.VISIBLE
                    vipGradeImage.setImageResource(VipGradeImageUtil.getVipImage(item.replierUser!!.vipLevel))
                } else {
                    vipGradeImage.visibility = View.GONE
                }
            }
            if (item.replierUser!!.vipLevel > 0) {
                holder.setTextColor(R.id.commentName, ColorUtils.getColor(R.color.word_color_vip))
            } else {
                holder.setTextColor(R.id.commentName, ColorUtils.getColor(R.color.word_color_2))
            }
        }
        val nameText = holder.getView<TextView>(R.id.commentName)
        nameText.text = item.replierUser?.nickName
        if (item.isForbidden) {
            nameText.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            nameText.paint.isAntiAlias = true
        } else {
            nameText.paint.flags = 0
            nameText.paint.isAntiAlias = true
        }
        holder.setText(R.id.commentTime, TimesUtils.friendDate(item.postTime))
        val fabulousLayout = holder.getView<LinearLayout>(R.id.commentFabulousLayout)
        val commentVipLayout = holder.getView<LinearLayout>(R.id.commentVipLayout)
        val commentText = holder.getView<ExpandTextView>(R.id.commentText)
        val commentValue = if (item.deleteState) {
            fabulousLayout.visibility = View.GONE
            StringUtils.getString(R.string.comment_deleted)
        } else {
            fabulousLayout.visibility = View.VISIBLE
            item.contxt
        }
        setComment(commentValue, item, commentText, 54f)
        holder.getView<ImageView>(R.id.commentFabulousImage).isSelected = item.likeStatus
        holder.getView<TextView>(R.id.commentFabulousCount).isSelected = item.likeStatus
        if (item.likesNumber > 0) {
            holder.setText(R.id.commentFabulousCount, NormalUtil.formatPlayCount(item.likesNumber))
        } else {
            holder.setText(R.id.commentFabulousCount, StringUtils.getString(R.string.fabulous))
        }
        val expandText = holder.getView<TextView>(R.id.comment_expand)
        if (item.repliesNumber > 0) {
            // 显示回复条数
            expandText.visibility = View.VISIBLE
            expandText.text = String.format(StringUtils.getString(R.string.all_reply), item.repliesNumber)
            expandText.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_blue_arrow_down,
                0
            )
        } else if (!item.children.isNullOrEmpty() && (item.children!!.size > 3 || holder.adapterPosition <= hotSize)) {
            // 显示收起
            expandText.visibility = View.VISIBLE
            expandText.setText(R.string.fold)
            expandText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_blue_arrow_up, 0)
        } else {
            // 没有回复
            expandText.visibility = View.GONE
        }
        expandText.setOnClickListener { view: View? ->
            if (item.repliesNumber > 0) {
                // 加载更多
                if (item.loadedComment.isNotEmpty()) {
                    val pageList = if (item.loadedComment.size <= 10) {
                        item.loadedComment
                    } else {
                        item.loadedComment.subList(0, 10)
                    }
                    item.children?.addAll(pageList)
                    item.loadedComment.removeAll(pageList)
                    item.repliesNumber = item.loadedComment.size
                    notifyItemChanged(holder.adapterPosition)
                } else {
                    if (holder.adapterPosition <= hotSize) {
                        loadHotReply(item, holder.adapterPosition)
                    } else {
                        loadReply(item, holder.adapterPosition)
                    }
                }
            } else {
                // 收起数据
                val children = ArrayList<CommentBean>()
                item.children?.let {
                    children.addAll(it)
                    item.pageIndex = 1
                    if (holder.adapterPosition <= hotSize) {
                        item.children?.clear()
                        if (item.loadedComment.isEmpty()) {
                            item.loadedComment.addAll(children)
                        }
                        item.repliesNumber += children.size
                    } else {
                        item.children = children.subList(0, 3)
                        if (item.loadedComment.isEmpty()) {
                            item.loadedComment.addAll(children.subList(3, children.size))
                        }
                        item.repliesNumber = children.size - (item.children?.size ?: 0)

                    }
                }
                notifyItemChanged(holder.adapterPosition)
                recyclerView.scrollToPosition(holder.adapterPosition)
            }
        }
        holder.getView<TextView>(R.id.commentReply).setOnClickListener { view: View? ->
            // 点击回复
            listener?.onItemClick(
                0,
                item,
                holder.getView(R.id.commentFabulousImage),
                holder.getView(R.id.commentFabulousCount)
            )
        }
        holder.getView<FrameLayout>(R.id.imageLayout).setOnClickListener { view: View? ->
            // 点击头像
            listener?.onItemClick(
                5,
                item,
                holder.getView(R.id.commentFabulousImage),
                holder.getView(R.id.commentFabulousCount)
            )
        }
        fabulousLayout.setOnClickListener { view: View? ->
            listener?.onItemClick(
                2,
                item,
                holder.getView(R.id.commentFabulousImage),
                holder.getView(R.id.commentFabulousCount)
            )
        }
        addVipView(commentVipLayout, item.vipexpression)
        setReplayList(
            holder.getView(R.id.replay_container),
            holder.getView(R.id.replay_list),
            item.children,
            item.replyID
        )
        if (holder.adapterPosition == hotSize) {
            holder.setVisible(R.id.bottomLineLayout, true)
            holder.setGone(R.id.bottomLineView, true)
        } else {
            holder.setGone(R.id.bottomLineLayout, true)
            holder.setVisible(R.id.bottomLineView, true)
        }
    }

    /**
     * 设置回复数据
     */
    private fun setReplayList(
        container: LinearLayout,
        recyclerView: RecyclerView,
        replayList: MutableList<CommentBean>?,
        parentId: Int
    ) {
        recyclerView.removeAllViews()
        if (replayList.isNullOrEmpty()) {
            container.visibility = View.GONE
        } else {
            container.visibility = View.VISIBLE
            if (recyclerView.adapter == null) {
                recyclerView.layoutManager =
                    LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
            }
            recyclerView.adapter = CommentDetailAdapter(replayList, listener, parentId)
        }
    }

    private fun setVoteView(holder: BaseViewHolder, item: CommentBean) {
        item.replierUser?.avatar?.let {
            ImageUtil.loadCircleImage(context, it, holder.getView(R.id.voteImage))
        }
        val vipGradeImage = holder.getView<ImageView>(R.id.userVipImage)
        if (item.replierUser != null && item.replierUser!!.vipLevel in 1..4) {
            vipGradeImage.visibility = View.VISIBLE
            vipGradeImage.setImageResource(VipGradeImageUtil.getVipImage(item.replierUser!!.vipLevel))
        }
        holder.setText(R.id.voteName, item.replierUser?.nickName)
        holder.setText(R.id.voteTime, TimesUtils.friendDate(item.postTime))
        holder.setText(R.id.voteTitle, item.contxt)
        holder.setText(
            R.id.voteCounts,
            StringUtils.getString(R.string.voteCount, item.participateCount)
        )
        if (item.voteStatus) {
            holder.setText(R.id.voteTips, StringUtils.getString(R.string.voted))
            holder.setGone(R.id.voteClick, true)
        } else {
            holder.setText(R.id.voteTips, StringUtils.getString(R.string.voteTips))
            holder.setGone(R.id.voteClick, false)
        }
        holder.getView<TextView>(R.id.voteClick).setOnClickListener { view: View? ->
            listener?.onItemClick(4, item, null, null)
        }
        holder.getView<FrameLayout>(R.id.voteImageLayout)
            .setOnClickListener { view: View? ->
                // 点击头像
                listener?.onItemClick(5, item, null, null)
            }
        VoteResultUtil.addVoteResultView(
            context, item, holder.getView(R.id.addOptionLayout)
        )
    }

    /**
     * 动态添加VIP表情
     */
    private fun addVipView(layout: LinearLayout, vipString: String) {
        layout.removeAllViews()
        if (vipString.isEmpty()) {
            return
        }
        val vipList = vipString.split(",")
        for (i in vipList.indices) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val view = ImageView(context)
            view.setBackgroundColor(
                ColorUtils.getColor(R.color.background_4)
            )
            if (i != 4) {
                params.rightMargin = SizeUtils.dp2px(10f)
            }
            params.topMargin = SizeUtils.dp2px(10f)
            params.height = SizeUtils.dp2px(70f)
            params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(10f) * 3
                    - SizeUtils.dp2px(12f) - SizeUtils.dp2px(54f)) / 4
            ImageUtil.loadGif(context, EmotionUtils.getVipImgByName(vipList[i]), view)
            view.layoutParams = params
            layout.addView(view)
        }
    }

    private fun loadReply(item: CommentBean, position: Int) {
        HttpRequest.cancelRequest(this)
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        params.put("replyid", item.replyID)
        params.put("page", item.pageIndex)
        params.put("size", pageSize)
        if (replyId > 0) {
            params.put("childReplyID", replyId)
        }
        HttpRequest.get(RequestUrls.REPLY_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val list = response?.optJSONArray("list")
                if (list != null && list.length() > 0) {
                    val commentList: List<CommentBean> = Gson().fromJson(
                        list.toString(),
                        object : TypeToken<List<CommentBean>>() {}.type
                    )
                    item.children?.addAll(commentList)
                    item.pageIndex++
                    item.repliesNumber = item.repliesNumber - commentList.size
                    if (item.repliesNumber < 0 || commentList.size < pageSize) {
                        item.repliesNumber = 0
                    }
                } else {
                    item.repliesNumber = 0
                }
                notifyItemChanged(position)
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    /**
     * 热门评论的二级评论
     */
    private fun loadHotReply(item: CommentBean, position: Int) {
        HttpRequest.cancelRequest(this)
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        params.put("replyid", item.replyID)
        params.put("page", item.pageIndex)
        params.put("size", pageSize)
        HttpRequest.get(RequestUrls.COMMENT_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val list = response?.optJSONArray("normal")
                if (list != null && list.length() > 0) {
                    val commentList: List<CommentBean> = Gson().fromJson(
                        list.toString(),
                        object : TypeToken<List<CommentBean>>() {}.type
                    )
                    item.children?.addAll(commentList)
                    item.pageIndex++
                    item.repliesNumber = item.repliesNumber - commentList.size
                    if (item.repliesNumber < 0 || commentList.size < pageSize) {
                        item.repliesNumber = 0
                    }
                } else {
                    item.repliesNumber = 0
                }
                notifyItemChanged(position)
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    /**
     * 点击事件接口
     * type: 0-点击回复 1-点击全部回复(详情)--去掉 2-点赞 3-长按删除 4-点击投票 5-点击头像
     */
    interface OnClickListener {
        fun onItemClick(
            type: Int,
            commentBean: CommentBean,
            zanImageView: ImageView?,
            zanTextView: TextView?
        )
    }
}