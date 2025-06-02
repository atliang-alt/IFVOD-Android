package com.cqcsy.lgsp.video.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONObject

/**
 ** 2023/12/6
 ** des：评论相关
 **/

class CommentViewModel : ViewModel() {

    val mCommentLike: MutableLiveData<Pair<CommentBean, VideoLikeBean>> by lazy { MutableLiveData() }

    val mCommentDelete: MutableLiveData<CommentBean> by lazy { MutableLiveData() }

    val mCommentRelease: MutableLiveData<Pair<CommentBean?, CommentEvent>> by lazy { MutableLiveData() }

    val mCommentData: MutableLiveData<JSONObject> by lazy { MutableLiveData() }


    /**
     * 删除评论接口
     */
    fun deleteComment(videoType: Int?, commentBean: CommentBean) {
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("ReplyID", commentBean.replyID)
        HttpRequest.post(
            RequestUrls.DELETE_COMMENT + "?videoType=" + videoType,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (response == null) {
                        return
                    }
                    mCommentDelete.value = commentBean
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    /**
     * 评论点赞接口
     * isLandlord 是否是二级评论页对楼主的点赞
     */
    fun commentLike(videoType: Int?, commentBean: CommentBean) {
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("ReplyID", commentBean.replyID)
        params.put("videoType", videoType ?: 0)
        HttpRequest.get(RequestUrls.COMMENT_LIKE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val like: VideoLikeBean = GsonUtils.fromJson(response.toString(), object : TypeToken<VideoLikeBean>() {}.type)
                mCommentLike.value = Pair(commentBean, like)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 发布评论接口
     */
    fun releaseComment(mediaKey: String?, videoType: Int?, inputText: String, vipString: String, parent: CommentBean?) {
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean!!.token.uid)
        params.put("mediaKey", mediaKey)
        params.put("ReplyID", parent?.replyID.toString())
        params.put("ReplyUserID", parent?.replierUser?.id.toString())
        params.put("Contxt", inputText)
        params.put("VIPExpression", vipString)
        HttpRequest.post(
            RequestUrls.RELEASE_COMMENT + "?videoType=" + videoType,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (response == null) {
                        return
                    }
                    val commentBean = GsonUtils.fromJson<CommentBean>(
                        response.toString(),
                        object : TypeToken<CommentBean>() {}.type
                    )
                    val event = CommentEvent()
                    event.commentBean = commentBean
                    event.mediaKey = mediaKey ?: ""
                    mCommentRelease.value = Pair(parent, event)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            },
            params,
            this
        )
    }

    /**
     * 评论列表接口
     */
    fun getCommentList(mediaKey: String?, videoType: Int?, page: Int, size: Int, scrollCommentId: Int = 0, replyId: Int = 0) {
        if (mediaKey.isNullOrEmpty() || videoType == null) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        if (scrollCommentId > 0) {
            params.put("CommentID", scrollCommentId)
            if (replyId > 0) {
                params.put("childReplyID", replyId)
            }
        }
        params.put("page", page)
        params.put("size", size)
        HttpRequest.get(RequestUrls.COMMENT_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                mCommentData.value = response
            }

            override fun onError(response: String?, errorMsg: String?) {
                mCommentData.value = null
            }
        }, params, this)
    }
}