package com.cqcsy.lgsp.upper.pictures

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_picture_comment.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 所有相册底部评论区域
 */
class CommentView : LinearLayout {
    var picturesBean: PicturesBean? = null
    var isVisibilityComment = true
    var isVisibilityZan = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
        orientation = VERTICAL
        View.inflate(context, R.layout.layout_picture_comment, this)

        commentNum.setOnClickListener { onCommentNumClick() }
        commentInput.setOnClickListener { onCommentClick() }
        zanCheck.setOnClickListener { onZanClick() }
        collectContent.setOnClickListener { collectionClick() }
        setUserAvatar()
        setCommentNum(isVisibilityComment)
        setZan(isVisibilityZan)
        if (picturesBean != null) {
            setPictureInfo(picturesBean!!)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    var commentEditDialog: CommentEditDialog? = null
    private fun onCommentClick(replyID: Int = 0, replyUserID: Int = 0) {
        if (!GlobalValue.isLogin()) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        if (context !is AppCompatActivity) {
            return
        }
        commentEditDialog = CommentEditDialog("", object : CommentEditDialog.SendCommentListener {
            override fun sendComment(type: Int, inputText: String, vipList: MutableList<String>) {
                if (type == 1) {
                    commentEditDialog?.dismiss()
                    val intent = Intent(context, OpenVipActivity::class.java)
                    intent.putExtra("pathInfo", context.javaClass.simpleName)
                    context.startActivity(intent)
                    return
                }
                var vipString = ""
                if (vipList.isNotEmpty()) {
                    vipString = NormalUtil.getVipString(vipList)
                }
                releaseComment(inputText, vipString, replyID, replyUserID)
            }
        }, isEditVip = false, isPicture = true)
        commentEditDialog?.show((context as AppCompatActivity).supportFragmentManager, "commentEditDialog")
    }

    /**
     * 发布评论接口
     */
    private fun releaseComment(inputText: String, vipString: String, replyID: Int = 0, replyUserID: Int = 0) {
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean!!.token.uid)
        params.put("mediaKey", picturesBean?.mediaKey)
        params.put("ReplyID", replyID)
        params.put("ReplyUserID", replyUserID)
        params.put("Contxt", inputText)
        params.put("VIPExpression", vipString)
        HttpRequest.post(RequestUrls.RELEASE_COMMENT + "?videoType=${picturesBean?.videoType}", object : HttpCallBack<JSONObject>() {
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
                event.replyId = replyID
                event.replyUserID = replyUserID
                event.mediaKey = picturesBean?.mediaKey ?: ""
                EventBus.getDefault().post(event)
                ToastUtils.showLong(R.string.commentSuccess)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun onCommentNumClick() {
        val intent = Intent(context, PicturesCommentListActivity::class.java)
        intent.putExtra(PicturesCommentListActivity.PICTURES_MEDIA_ID, picturesBean?.mediaKey)
        intent.putExtra(PicturesCommentListActivity.PICTURES_COMMENT, picturesBean?.comments)
        intent.putExtra(PicturesCommentListActivity.PICTURES_TITLE, picturesBean?.title)
        intent.putExtra(PicturesCommentListActivity.PICTURES_TYPE, picturesBean?.videoType)
        context.startActivity(intent)
    }

    /**
     * 点赞、取消点赞
     */
    private fun onZanClick() {
        if (!GlobalValue.isLogin()) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        params.put("mediaKey", picturesBean?.mediaKey)
        picturesBean?.videoType?.let { params.put("videoType", it) }
        HttpRequest.get(RequestUrls.VIDEO_LIKES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean = Gson().fromJson(response.optString("like"), object : TypeToken<VideoLikeBean>() {}.type)
                if (videoLikeBean.selected) {
                    ImageUtil.clickAnim(context as Activity, zanCheck)
                }
                zanCheck.text = videoLikeBean.count.toString()
                val event = VideoActionResultEvent()
                event.type = 2
                event.selected = videoLikeBean.selected
                event.count = videoLikeBean.count
                picturesBean?.mediaKey?.also { event.id = it }
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }
        }, params, context)
    }

    /**
     * 收藏、取消收藏
     */
    private fun collectionClick() {
        if (!GlobalValue.isLogin()) {
            context.startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        params.put("mediaKey", picturesBean?.mediaKey)
        picturesBean?.videoType?.let { params.put("videoType", it) }
        HttpRequest.get(RequestUrls.VIDEO_COLLECTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val videoLikeBean: VideoLikeBean = Gson().fromJson(response.toString(), object : TypeToken<VideoLikeBean>() {}.type)
                picturesBean?.isCollected = videoLikeBean.selected
                val event = VideoActionResultEvent()
                event.type = 4
                event.id = picturesBean?.mediaKey ?: ""
                event.selected = videoLikeBean.selected
                event.actionType = VideoActionResultEvent.TYPE_PICTURE
                if (videoLikeBean.selected) {
                    event.action = VideoActionResultEvent.ACTION_ADD
                    ImageUtil.clickAnim(context as Activity, collectImg)
                } else {
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    fun setCommentNumberVisible(isVisibility: Boolean) {
        isVisibilityComment = isVisibility
        setCommentNum(isVisibility)
    }

    fun setZanVisible(isVisibility: Boolean) {
        isVisibilityZan = isVisibility
        setZan(isVisibility)
    }

    fun setPictures(picturesBean: PicturesBean) {
        this.picturesBean = picturesBean
        setPictureInfo(picturesBean)
    }

    private fun setCommentNum(isVisibility: Boolean) {
        if (isAttachedToWindow) {
            commentNum.visibility = if (isVisibility) View.VISIBLE else View.GONE
        }
    }

    private fun setZan(isVisibility: Boolean) {
        if (isAttachedToWindow) {
            zanCheck.visibility = if (isVisibility) View.VISIBLE else View.GONE
        }
    }

    private fun setCollect(isCollection: Boolean) {
        if (isAttachedToWindow) {
            if (isCollection) {
                collectStatus.setText(R.string.collected)
            } else {
                collectStatus.setText(R.string.collection)
            }
            collectStatus.isSelected = isCollection
            collectImg.isSelected = isCollection
        }
    }

    private fun setPictureInfo(picturesBean: PicturesBean) {
        if (isAttachedToWindow) {
            commentNum.text = picturesBean.comments.toString()
            zanCheck.text = picturesBean.likeCount.toString()
            zanCheck.isSelected = picturesBean.like
            setCollect(picturesBean.isCollected)
        }
    }

    fun setUserAvatar() {
        ImageUtil.loadCircleImage(this, GlobalValue.userInfoBean?.avatar, commentUserImage)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onZanChange(event: VideoActionResultEvent) {
        if (event.type == 2 && picturesBean != null && !event.isCommentLike) {
            if (event.id == picturesBean?.mediaKey) {
                picturesBean?.like = event.selected
            }
            picturesBean?.likeCount = event.count
            setPictureInfo(picturesBean!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        if (picturesBean != null && event.mediaKey == picturesBean!!.mediaKey) {
            picturesBean!!.comments++
            setPictureInfo(picturesBean!!)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCollectChange(event: VideoActionResultEvent) {
        if (event.type == 4 && picturesBean != null && event.id == picturesBean!!.mediaKey) {
            picturesBean!!.isCollected = event.selected
            setCollect(event.selected)
        }
    }
}