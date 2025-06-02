package com.cqcsy.lgsp.main.find

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.base.VideoListItemHolder
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.home.RecommendMultiAdapter.Companion.TYPE_DYNAMIC
import com.cqcsy.lgsp.main.home.RecommendMultiAdapter.Companion.TYPE_PICTURES
import com.cqcsy.lgsp.main.home.RecommendMultiAdapter.Companion.TYPE_SHORT
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.pictures.PictureListActivity
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.DynamicUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 发现-关注 混合列表适配器，包含小视频、动态、相册
 */
class AttentionListAdapter(val activity: Activity, data: MutableList<RecommendMultiBean>) :
    BaseMultiItemQuickAdapter<RecommendMultiBean, BaseViewHolder>(data) {

    init {
        addItemType(TYPE_SHORT, R.layout.layout_video_list_item)
        addItemType(TYPE_DYNAMIC, R.layout.layout_attention_dynamic)
        addItemType(TYPE_PICTURES, R.layout.layout_attention_pictures)
    }

    override fun convert(holder: BaseViewHolder, item: RecommendMultiBean) {
        when (item.itemType) {
            TYPE_SHORT -> setShort(holder, item)
            TYPE_DYNAMIC -> setDynamic(holder, item)
            TYPE_PICTURES -> setPictures(holder, item)
        }
    }

    private fun setShort(holder: BaseViewHolder, item: RecommendMultiBean) {
        val videoListItemHolder = VideoListItemHolder(activity)
        val short = ShortVideoBean()
        short.videoType = item.videoType
        short.headImg = item.headImg ?: ""
        short.upperName = item.upperName
        short.title = item.title
        short.mediaUrl = item.mediaUrl
        short.mediaKey = item.mediaKey
        short.likeCount = item.likeCount
        short.likeStatus = item.like
        short.comments = item.comments
        short.duration = item.duration ?: ""
        short.playCount = item.viewCount
        short.focusStatus = item.focusStatus
        short.coverImgUrl = item.coverImgUrl
        short.userId = item.userId
        short.date = item.date ?: ""
        short.watchingProgress = item.watchingProgress
        short.bigV = item.bigV
        short.vipLevel = item.vipLevel
        short.cidMapper = item.contentType
        short.playCount = item.viewCount
        short.isBlackList = item.isBlackList
        short.favorites = item.favorites ?: VideoLikeBean()
        videoListItemHolder.setItemView(holder, short)
    }

    private fun setDynamic(holder: BaseViewHolder, item: RecommendMultiBean) {
        ImageUtil.loadCircleImage(activity, item.headImg, holder.getView(R.id.user_image))
        val userVip = holder.getView<ImageView>(R.id.userVip)
        setVipLevel(userVip, item)
        holder.getView<LinearLayout>(R.id.user_info).setOnClickListener { upperInfoClick(item.userId) }
        val blackView = holder.getView<TextView>(R.id.blackList)
        val focus = holder.getView<Button>(R.id.btn_attention)
        setBtnAttention(focus, blackView, item)
        holder.setText(R.id.user_nick_name, item.upperName)
        holder.setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        holder.setText(R.id.item_des, item.date?.let { TimesUtils.friendDate(it) + "  " } + StringUtils.getString(R.string.release_new_dynamic))
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_des, true)
        } else {
            holder.setGone(R.id.dynamic_des, false)
            holder.setText(R.id.dynamic_des, Html.fromHtml(item.description))
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.getView<ImageView>(R.id.like_image).isSelected = item.like
        holder.getView<TextView>(R.id.like_num).isSelected = item.like
        if (item.comments > 0) {
            holder.setText(R.id.comment_num, NormalUtil.formatPlayCount(item.comments))
        } else {
            holder.setText(R.id.comment_num, StringUtils.getString(R.string.comment))
        }
        if (item.likeCount > 0) {
            holder.setText(R.id.like_num, NormalUtil.formatPlayCount(item.likeCount))
        } else {
            holder.setText(R.id.like_num, StringUtils.getString(R.string.fabulous))
        }
        holder.getView<LinearLayout>(R.id.commentLayout).setOnClickListener {
            val bean = DynamicBean().copy(item)
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey
                dynamicType = item.photoType
                videoIndex = 0
                dynamicVideoList = mutableListOf(bean)
                showComment = true
            }
        }
        holder.getView<LinearLayout>(R.id.likeLayout).setOnClickListener {
            onLikeClick(holder, item)
        }
        holder.itemView.setOnClickListener {
            val dataList = mutableListOf(DynamicBean().copy(item))
            DynamicDetailsActivity.launch(context) {
                mediaKey = item.mediaKey
                dynamicType = item.photoType
                dynamicVideoList = dataList
                openRecommend = true
            }
        }
        val imageContainer = holder.getView<LinearLayout>(R.id.imageContainer)
        imageContainer.removeAllViews()
        if (item.photoType == 2) {
            val dynamicVideo = LayoutInflater.from(context).inflate(R.layout.layout_dynamic_video_item, null)
            val videoCover = dynamicVideo.findViewById<ImageView>(R.id.iv_video_cover)
            val size = DynamicUtils.getCoverSize(item.imageRatioValue, DynamicUtils.getCellWidth())
            ImageUtil.loadImage(
                context,
                item.coverImgUrl,
                videoCover,
                corner = 2,
                imageWidth = size.width,
                imageHeight = size.height,
                scaleType = ImageView.ScaleType.CENTER_CROP
            )
            imageContainer.addView(dynamicVideo, LinearLayout.LayoutParams(size.width, size.height))
        } else {
            DynamicUtils.addDynamicImages(context, imageContainer, item.details, item.photoCount)
        }
    }

    private fun setPictures(holder: BaseViewHolder, item: RecommendMultiBean) {
        ImageUtil.loadCircleImage(activity, item.headImg, holder.getView(R.id.user_image))
        val userVip = holder.getView<ImageView>(R.id.userVip)
        setVipLevel(userVip, item)
        val blackView = holder.getView<TextView>(R.id.blackList)
        val focus = holder.getView<Button>(R.id.btn_attention)
        setBtnAttention(focus, blackView, item)
        holder.setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        ImageUtil.loadImage(
            activity,
            item.coverImgUrl,
            holder.getView(R.id.picture_cover),
            defaultImage = R.mipmap.pictures_cover_default
        )
        holder.getView<LinearLayout>(R.id.user_info)
            .setOnClickListener { upperInfoClick(item.userId) }
        holder.setText(R.id.user_nick_name, item.upperName)
        holder.setText(
            R.id.item_des,
            item.date?.let { TimesUtils.friendDate(it) + "  " } + StringUtils.getString(R.string.upload_new_pic))
        holder.setText(R.id.picture_size, item.photoCount.toString())
        holder.setText(R.id.picture_name, item.title)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.picture_des, true)
        } else {
            holder.setVisible(R.id.picture_des, true)
            holder.setText(R.id.picture_des, item.description)
        }
        if (item.comments > 0) {
            holder.setText(R.id.comment_num, NormalUtil.formatPlayCount(item.comments))
        } else {
            holder.setText(R.id.comment_num, StringUtils.getString(R.string.comment))
        }
        if (item.likeCount > 0) {
            holder.setText(R.id.like_num, NormalUtil.formatPlayCount(item.likeCount))
        } else {
            holder.setText(R.id.like_num, StringUtils.getString(R.string.fabulous))
        }
        holder.getView<ImageView>(R.id.like_image).isSelected = item.like
        holder.getView<TextView>(R.id.like_num).isSelected = item.like

        holder.getView<LinearLayout>(R.id.commentLayout).setOnClickListener {
            pictureDetail(item)
        }
        holder.getView<LinearLayout>(R.id.likeLayout).setOnClickListener {
            onLikeClick(holder, item)
        }

        holder.itemView.setOnClickListener {
            pictureDetail(item)
        }

    }

    private fun setBtnAttention(attentionBtn: Button, blackListView: TextView, item: RecommendMultiBean) {
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
                    context.startActivity(Intent(context, LoginActivity::class.java))
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
                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
            }
        }
    }

    private fun pictureDetail(item: RecommendMultiBean) {
        val intent = Intent(context, PictureListActivity::class.java)
        intent.putExtra(UpperPicturesFragment.PICTURES_PID, item.mediaKey)
        intent.putExtra(UpperPicturesFragment.PICTURES_TITLE, item.title ?: "")
        context.startActivity(intent)
    }

    private fun setVipLevel(imageView: ImageView, item: RecommendMultiBean) {
        if (item.bigV || item.vipLevel > 0) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            imageView.visibility = View.GONE
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
     * 点赞点击
     */
    private fun onLikeClick(holder: BaseViewHolder, bean: RecommendMultiBean) {
        if (!GlobalValue.isLogin()) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        params.put("mediaKey", bean.mediaKey)
        HttpRequest.get(RequestUrls.VIDEO_LIKES + "?videoType=${bean.videoType}", object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean = Gson().fromJson(response.optString("like"), object : TypeToken<VideoLikeBean>() {}.type)
                if (videoLikeBean.selected) {
                    ImageUtil.clickAnim(activity, holder.getView(R.id.like_image))
                }
                holder.getView<ImageView>(R.id.like_image).isSelected = videoLikeBean.selected
                holder.getView<TextView>(R.id.like_num).isSelected = videoLikeBean.selected
                if (videoLikeBean.count <= 0) {
                    holder.getView<TextView>(R.id.like_num).text = StringUtils.getString(R.string.fabulous)
                } else {
                    holder.getView<TextView>(R.id.like_num).text = NormalUtil.formatPlayCount(videoLikeBean.count)
                }
                bean.like = videoLikeBean.selected
                bean.likeCount = videoLikeBean.count
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 用户信息点击
     */
    private fun upperInfoClick(userId: Int) {
        val intent = Intent(context, UpperActivity::class.java)
        intent.putExtra(UpperActivity.UPPER_ID, userId)
        context.startActivity(intent)
    }

    /**
     * 关注点击
     */
    private fun onAttentionClick(bean: RecommendMultiBean) {
        val params = HttpParams()
        params.put("userId", bean.userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                val event = VideoActionResultEvent()
                if (selected) {
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                event.type = 1
                event.id = bean.userId.toString()
                event.userLogo = bean.headImg ?: ""
                event.userName = bean.upperName ?: ""
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

}