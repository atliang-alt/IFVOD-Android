package com.cqcsy.lgsp.adapter

import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.utils.EmotionUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.views.widget.ExpandTextView
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.utils.ImageUtil
import java.util.regex.Pattern

/**
 * 评论详情页适配器
 */
class CommentDetailAdapter(
    data: MutableList<CommentBean>,
    var listener: CommentAdapter.OnClickListener?,
    val parentId: Int
) :
    BaseQuickAdapter<CommentBean, BaseViewHolder>(R.layout.item_comment_detail, data) {

    fun getItemPosition(replyId: Int): Int {
        val position = data.indexOfFirst { replyId == it.replyID }
        return position + headerLayoutCount
    }

    override fun convert(holder: BaseViewHolder, item: CommentBean) {
        item.replierUser?.avatar?.let {
            ImageUtil.loadCircleImage(
                context,
                it,
                holder.getView(R.id.commentImage)
            )
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
        if (item.isForbidden) {
            nameText.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            nameText.paint.isAntiAlias = true
        } else {
            nameText.paint.flags = 0
            nameText.paint.isAntiAlias = true
        }
        holder.setText(R.id.commentTime, TimesUtils.friendDate(item.postTime))
        val textView = holder.getView<ExpandTextView>(R.id.commentText)
        val replayUser = holder.getView<TextView>(R.id.replay_user)
        replayUser.text = item.respondentUser?.nickName
        if (parentId == item.oldReplyID) {
            nameText.maxEms = 16
            holder.setGone(R.id.replay_to_user, true)
            replayUser.visibility = View.GONE
        } else {
            nameText.maxEms = 5
            holder.setGone(R.id.replay_to_user, false)
            replayUser.visibility = View.VISIBLE
        }
        nameText.text = item.replierUser?.nickName
        val commentVipLayout = holder.getView<LinearLayout>(R.id.commentVipLayout)
        val fabulousLayout = holder.getView<LinearLayout>(R.id.commentFabulousLayout)
        val commentValue = if (item.deleteState) {
            fabulousLayout.visibility = View.GONE
            commentVipLayout.visibility = View.GONE
            StringUtils.getString(R.string.comment_deleted)
        } else {
            fabulousLayout.visibility = View.VISIBLE
            commentVipLayout.visibility = View.VISIBLE
            item.contxt
        }
        CommentAdapter.setComment(commentValue, item, textView, 84f)
        // 显示点赞操作
        holder.setVisible(R.id.commentFabulousLayout, true)
        val fabulousImage = holder.getView<ImageView>(R.id.commentFabulousImage)
        fabulousImage.isSelected = item.likeStatus
        holder.getView<TextView>(R.id.commentFabulousCount).isSelected = item.likeStatus
        if (item.likesNumber > 0) {
            holder.setText(
                R.id.commentFabulousCount,
                NormalUtil.formatPlayCount(item.likesNumber)
            )
        } else {
            holder.setText(
                R.id.commentFabulousCount, StringUtils.getString(R.string.fabulous)
            )
        }
        fabulousImage.setOnClickListener { view: View? ->
            fabulousImage.isSelected = item.likeStatus
            if (item.likesNumber > 0) {
                holder.setText(
                    R.id.commentFabulousCount,
                    NormalUtil.formatPlayCount(item.likesNumber)
                )
            } else {
                holder.setText(
                    R.id.commentFabulousCount,
                    StringUtils.getString(R.string.fabulous)
                )
            }
            listener?.onItemClick(2, item, fabulousImage, holder.getView(R.id.commentFabulousCount))
        }
        holder.getView<TextView>(R.id.commentReply).setOnClickListener { view: View? ->
            // 点击回复
            listener?.onItemClick(0, item, fabulousImage, holder.getView(R.id.commentFabulousCount))
        }
        holder.itemView.setOnLongClickListener { view: View? ->
            listener?.onItemClick(3, item, fabulousImage, holder.getView(R.id.commentFabulousCount))
            return@setOnLongClickListener false
        }
        holder.getView<FrameLayout>(R.id.imageLayout)
            .setOnClickListener { view: View? ->
                // 点击头像
                listener?.onItemClick(5, item, fabulousImage, holder.getView(R.id.commentFabulousCount))
            }
        addVipView(holder.getView(R.id.commentVipLayout), item.vipexpression)
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
            params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(10f) * 3
                    - SizeUtils.dp2px(12f) - SizeUtils.dp2px(54f)) / 4
            params.height = SizeUtils.dp2px(70f)
            params.rightMargin = SizeUtils.dp2px(10f)
            ImageUtil.loadGif(context, EmotionUtils.getVipImgByName(vipList[i]), view)
            view.layoutParams = params
            layout.addView(view)
        }
    }

    private fun getTextClick(
        textView: TextView,
        clickString: String,
        spanString: SpannableString,
        position: Int
    ): SpannableString {
        if (spanString.isNullOrEmpty()) {
            return spanString
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        val pattern = Pattern.compile(SpanStringUtils.escapeExprSpecialWord(clickString))
        val matcher = pattern.matcher(spanString)
        while (matcher.find()) {
            spanString.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 点击人名回复
//                    listener?.onItemClick(3, null, position, getItem(position))
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ColorUtils.getColor(R.color.blue)
                    ds.isUnderlineText = false
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        return spanString
    }

//    /**
//     * 点击时间接口
//     * type: 0-点击回复 1--点赞 2-长按删除 3-点击文本中人名回复 4-点击头像
//     */
//    interface OnClickListener {
//        fun onItemClick(type: Int, holder: BaseViewHolder?, position: Int, item: CommentBean)
//    }
}