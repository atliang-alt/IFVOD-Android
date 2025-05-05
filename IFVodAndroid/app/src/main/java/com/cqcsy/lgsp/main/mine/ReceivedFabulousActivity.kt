package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MineLikeListBean
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
 * 我的 -- 收到赞
 */
class ReceivedFabulousActivity : RefreshListActivity<MineLikeListBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.receivedFabulous)
        emptyLargeTip.text = StringUtils.getString(R.string.noLikeTips)
        emptyLittleTip.text = StringUtils.getString(R.string.noLikeTipsLit)
    }

    override fun getUrl(): String {
        return RequestUrls.GET_MESSAGE_LIST
    }

    override fun getHttpParams(): HttpParams {
        val params = HttpParams()
        params.put("MsgType", 4)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_comment_and_reply
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MineLikeListBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<MineLikeListBean>>() {}.type
        )
    }

    override fun onItemClick(position: Int, dataBean: MineLikeListBean) {
        if (dataBean.isUnAvailable) {
            ToastUtils.showLong(dataBean.context)
            return
        }
        when (dataBean.videoType) {// 1 剧集  3  小视频  7 相册  8  动态
            1, 3 -> { // 剧集、小视频
                val intent = Intent(this, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, dataBean.mediaKey)
                intent.putExtra(VideoBaseActivity.VIDEO_TYPE, dataBean.videoType)
                intent.putExtra(VideoBaseActivity.COMMENT_ID, dataBean.parentCommentID)
                intent.putExtra(VideoBaseActivity.REPLY_ID, dataBean.originCommentID)
//                intent.putExtra(VideoCommentFragment.SHOW_INPUT, false)
                startActivity(intent)
            }
            8 -> { // 动态
                DynamicDetailsActivity.launch(this) {
                    mediaKey = dataBean.mediaKey
                    commentId = dataBean.parentCommentID
                    replyId = dataBean.originCommentID
                    showComment = false
                    isFromMineDynamic = true
                    fromUpperHomePage = false
                    openRecommend = false
                }
            }
            7 -> { // 相册
                val intent = Intent(this, AlbumDetailsActivity::class.java)
                intent.putExtra(AlbumDetailsActivity.ALBUM_ID, dataBean.mediaKey)
                startActivity(intent)
            }
        }
    }

    override fun setItemView(holder: BaseViewHolder, item: MineLikeListBean, position: Int) {
        holder.setGone(R.id.replyContent, true)
        ImageUtil.loadCircleImage(
            this@ReceivedFabulousActivity,
            item.avatar,
            holder.getView(R.id.userPhoto)
        )
        holder.getView<FrameLayout>(R.id.imageLayout).setOnClickListener {
            val intent = Intent(this, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.userId)
            startActivity(intent)
        }
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
        val nickName = holder.getView<TextView>(R.id.nickName)
        if (item.vipLevel > 0) {
            nickName.setTextColor(ColorUtils.getColor(R.color.orange))
        } else {
            nickName.setTextColor(ColorUtils.getColor(R.color.word_color_2))
        }
        nickName.text = item.nickName
        holder.setText(
            R.id.time,
            TimesUtils.friendDate(item.updateTime)
        )
        setTypeText(holder, item)
    }

    private fun setTypeText(holder: BaseViewHolder, item: MineLikeListBean) {
        val commentTxt = holder.getView<TextView>(R.id.comment)
        val tipsTxt = holder.getView<TextView>(R.id.tips)
        when (item.businessType) {
            0, 1 -> { //剧集、小视频
                commentTxt.text = item.context
                tipsTxt.text =
                    StringUtils.getString(
                        R.string.zan_your_tips,
                        resources.getString(R.string.searchVideo)
                    )
            }
            2 -> { // 动态
                commentTxt.text = item.context
                tipsTxt.text = StringUtils.getString(
                    R.string.zan_your_tips,
                    resources.getString(R.string.dynamic)
                )
            }
            3 -> { // 相册
                commentTxt.text = item.context
                tipsTxt.text = StringUtils.getString(
                    R.string.zan_your_tips,
                    resources.getString(R.string.album)
                )
            }
            else -> {
                commentTxt.text = SpanStringUtils.getEmotionContent(this, 12f, item.context)
                tipsTxt.text = StringUtils.getString(R.string.fabulousYourComment)
            }
        }
    }

    override fun showEmpty() {
        super.showEmpty()
        emptyLittleTip.isVisible = true
    }
}