package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MineCommentListBean
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.pictures.PicturesCommentListActivity
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshListActivity
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray

/**
 * 我的 -- 评论/回复
 */
class CommentAndReplyActivity : RefreshListActivity<MineCommentListBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.commentAndReply)
        emptyLargeTip.text = StringUtils.getString(R.string.noReplyTips)
        emptyLittleTip.text = StringUtils.getString(R.string.noReplyTipsLit)
        setEnableLoadMore(true)
    }

    override fun getUrl(): String {
        return RequestUrls.GET_MESSAGE_LIST
    }

    override fun getItemLayout(): Int {
        return R.layout.item_comment_and_reply
    }

    override fun getHttpParams(): HttpParams {
        val params = HttpParams()
        params.put("MsgType", 2)
        return params
    }

    override fun onItemClick(position: Int, dataBean: MineCommentListBean) {
        if (dataBean.isUnAvailable) {
            ToastUtils.showLong(dataBean.originMessage)
            return
        }
        when (dataBean.videoType) { // 1 剧集  3  小视频  7 相册  8  动态
            1, 3 -> { // 剧集、小视频
                val intent = Intent(this, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, dataBean.mediaKey)
                intent.putExtra(VideoBaseActivity.VIDEO_TYPE, dataBean.videoType)
                intent.putExtra(VideoBaseActivity.COMMENT_ID, dataBean.commentID)
                intent.putExtra(VideoBaseActivity.REPLY_ID, dataBean.originCommentID)
                startActivity(intent)
            }

            8 -> { // 动态
                DynamicDetailsActivity.launch(this) {
                    mediaKey = dataBean.mediaKey
                    commentId = dataBean.commentID
                    replyId = dataBean.originCommentID
                    showComment = true
                }
            }

            7 -> { // 相册
                val intent = Intent(this, PicturesCommentListActivity::class.java)
                intent.putExtra(PicturesCommentListActivity.PICTURES_MEDIA_ID, dataBean.mediaKey)
                intent.putExtra(PicturesCommentListActivity.PICTURES_COMMENT, dataBean.comments)
                intent.putExtra(PicturesCommentListActivity.PICTURES_TITLE, dataBean.originMessage)
                intent.putExtra(PicturesCommentListActivity.PICTURES_TYPE, dataBean.videoType)
                intent.putExtra(VideoBaseActivity.COMMENT_ID, dataBean.commentID)
                intent.putExtra(VideoBaseActivity.REPLY_ID, dataBean.originCommentID)
                startActivity(intent)
            }
        }
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MineCommentListBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<MineCommentListBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: MineCommentListBean, position: Int) {
        ImageUtil.loadCircleImage(
            this@CommentAndReplyActivity,
            item.avatar,
            holder.getView(R.id.userPhoto)
        )
        holder.getView<FrameLayout>(R.id.imageLayout).setOnClickListener {
            val intent = Intent(this, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.userId)
            startActivity(intent)
        }
        val nickName = holder.getView<TextView>(R.id.nickName)
        val userVipImage = holder.getView<ImageView>(R.id.userVipImage)
        if (item.bigV) {
            userVipImage.visibility = View.VISIBLE
            userVipImage.setImageResource(R.mipmap.icon_big_v)
        } else {
            if (item.vipLevel > 0) {
                userVipImage.visibility = View.VISIBLE
                userVipImage.setImageResource(VipGradeImageUtil.getVipImage(item.vipLevel))
            } else {
                userVipImage.visibility = View.GONE
            }
        }
        if (item.vipLevel > 0) {
            nickName.setTextColor(ColorUtils.getColor(R.color.orange))
        } else {
            nickName.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        }
        nickName.text = item.nickName
        holder.setText(R.id.time, TimesUtils.friendDate(item.updateTime))
        val replyContent = holder.getView<TextView>(R.id.replyContent)
        replyContent.text = SpanStringUtils.getEmotionContent(
            this, 14f, item.replyMessage
        )
        replyContent.maxLines = 2
        if (item.isForbidden) {
            replyContent.setTextColor(ColorUtils.getColor(R.color.word_color_5))
            replyContent.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            replyContent.paint.isAntiAlias = true
            nickName.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            nickName.paint.isAntiAlias = true
        } else {
            replyContent.paint.flags = 0
            replyContent.paint.isAntiAlias = true
            nickName.paint.flags = 0
            nickName.paint.isAntiAlias = true
            replyContent.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        }
        setTypeText(holder, item)
    }

    private fun setTypeText(holder: BaseViewHolder, item: MineCommentListBean) {
        val commentTxt = holder.getView<TextView>(R.id.comment)
        val tipsTxt = holder.getView<TextView>(R.id.tips)
        when (item.businessType) {
            0, 1 -> { //剧集、小视频
                commentTxt.text = item.originMessage
                tipsTxt.text = StringUtils.getString(R.string.comment_your_tips, resources.getString(R.string.searchVideo))
            }

            2 -> { // 动态
                commentTxt.text = item.originMessage
                tipsTxt.text = StringUtils.getString(R.string.comment_your_tips, resources.getString(R.string.dynamic))
            }

            3 -> { // 相册
                commentTxt.text = item.originMessage
                tipsTxt.text = StringUtils.getString(R.string.comment_your_tips, resources.getString(R.string.album))
            }

            else -> {
                commentTxt.text = SpanStringUtils.getEmotionContent(this, 12f, item.originMessage)
                tipsTxt.text = StringUtils.getString(R.string.replyYourComment)
            }
        }
    }
}