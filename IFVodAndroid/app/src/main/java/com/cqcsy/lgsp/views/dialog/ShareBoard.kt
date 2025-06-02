package com.cqcsy.lgsp.views.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.ShareCountEvent
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_full_video_share.*
import kotlinx.android.synthetic.main.layout_share_base.*
import kotlinx.android.synthetic.main.layout_share_board.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 分享
 */
class ShareBoard(context: Context, uid: Int, private var videoBean: VideoBaseBean, private val isFull: Boolean = false) : BottomBaseDialog(context),
    View.OnClickListener {
    private var userId = uid
    var listener: OnCollectionListener? = null

    interface OnCollectionListener {
        // type = 0 收藏 type = 1 关注
        fun onChange(type: Int, isSelected: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_share_board)

        setDialog()
        if (isFull) {
            title.visibility = View.GONE
            baseShareLayout.visibility = View.GONE
            fullShareLayout.visibility = View.VISIBLE
            line.visibility = View.GONE
            share_cancel.visibility = View.GONE
            wechat_full.setOnClickListener(this)
            weibo_full.setOnClickListener(this)
            qq_full.setOnClickListener(this)
            facebook_full.setOnClickListener(this)
            twitter_full.setOnClickListener(this)
            copy_full.setOnClickListener(this)
            isGoneOtherLayout(true)
        } else {
            shareLayout.setBackgroundColor(ColorUtils.getColor(R.color.background_6))
            baseShareLayout.visibility = View.VISIBLE
            title.visibility = View.VISIBLE
            fullShareLayout.visibility = View.GONE
            line.visibility = View.VISIBLE
            share_cancel.visibility = View.VISIBLE
            share_cancel.setOnClickListener(this)
            attention_content.setOnClickListener(this)
            collection_content.setOnClickListener(this)
            report_content.setOnClickListener(this)
            unlike_content.setOnClickListener(this)
            wechat_base.setOnClickListener(this)
            weibo_base.setOnClickListener(this)
            qq_base.setOnClickListener(this)
            facebook_base.setOnClickListener(this)
            twitter_base.setOnClickListener(this)
            copy_base.setOnClickListener(this)
        }
    }

    /**
     * 设置关注、收藏状态
     */
    fun setAttentionAndCollection(shortVideoBean: ShortVideoBean) {
        if (shortVideoBean.focusStatus) {
            attentionImage.isSelected = shortVideoBean.focusStatus
            attentionText.text = StringUtils.getString(R.string.followed)
        }
        if (shortVideoBean.favorites.selected) {
            collectionImage.isSelected = shortVideoBean.favorites.selected
            collectionText.text = StringUtils.getString(R.string.collected)
        }
    }

    /**
     * 是否隐藏分享的其他功能布局
     */
    fun isGoneOtherLayout(isGone: Boolean) {
        if (isGone) {
            otherLayout.visibility = View.GONE
        } else {
            otherLayout.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.share_cancel -> dismiss()
            R.id.attention_content -> {
                onAttentionClick(attentionImage)
            }

            R.id.collection_content -> {
                videoCollectionClick(collectionImage)
            }

            R.id.report_content -> {
            }

            R.id.unlike_content,
            R.id.wechat_base,
            R.id.wechat_full,
            R.id.weibo_base,
            R.id.weibo_full,
            R.id.qq_base,
            R.id.qq_full,
            R.id.facebook_base,
            R.id.facebook_full,
            R.id.twitter_full,
            R.id.twitter_base -> {
                showCopySuccess(v.tag.toString())
            }

            R.id.copy_base,
            R.id.copy_full -> {
                NormalUtil.copyText(
                    StringUtils.getString(
                        R.string.share_board,
                        videoBean.title,
                        GlobalValue.downloadH5Address
                    )
                )
                ToastUtils.showLong(R.string.copy_success)
                updateTask()
                addShareCount()
                dismiss()
            }
        }
    }

    private fun showCopySuccess(platform: String) {
        val shareContent = StringUtils.getString(
            R.string.share_board,
            "<" + videoBean.title + ">",
            GlobalValue.downloadH5Address
        )
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.share)
        dialog.setMsg(shareContent)
        dialog.setRightListener(R.string.copy) {
            updateTask()
            addShareCount()
            ToastUtils.showLong(context.getString(R.string.share_copy_success, platform))
            NormalUtil.copyText(shareContent)
            dialog.dismiss()
        }
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.show()
        dismiss()
    }

    /**
     * 点击分享增加次数统计
     */
    private fun addShareCount() {
        val params = HttpParams()
        params.put("mediaKey", videoBean.mediaKey)
        HttpRequest.post(RequestUrls.ADD_SHARE_COUNT, object : HttpCallBack<Int>() {
            override fun onSuccess(response: Int?) {
                EventBus.getDefault().post(ShareCountEvent(response ?: 0))
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    /**
     * 分享成功后更新任务
     * UserActionType=21(分享),12(签到),18(上传视频)
     */
    private fun updateTask() {
        val params = HttpParams()
        params.put("UserActionType", "21")
        HttpRequest.post(RequestUrls.UPD_TASK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params, tag = this)
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
                dismiss()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }

    /**
     * 关注点击
     */
    private fun onAttentionClick(imageView: ImageView) {
        if (!GlobalValue.checkLogin()) return
        if (videoBean is ShortVideoBean && (videoBean as ShortVideoBean).isBlackList) {
            showBlackTip(context, userId)
            return
        }
        val params = HttpParams()
        params.put("userId", userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                attentionImage.isSelected = selected
                listener?.onChange(1, selected)
//                val event = VideoActionResultEvent()
//                event.id = userId.toString()
//                event.userLogo = ""
//                event.userName = videoBean.upperName ?: ""
//                event.type = 4
//                if (selected) {
//                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
//                    event.action = VideoActionResultEvent.ACTION_ADD
//                    attentionText.text = StringUtils.getString(R.string.followed)
//                } else {
//                    attentionText.text = StringUtils.getString(R.string.attention)
//                    event.action = VideoActionResultEvent.ACTION_REMOVE
//                }
//                EventBus.getDefault().post(event)
                dismiss()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 视频收藏、取消收藏
     */
    private fun videoCollectionClick(imageView: ImageView) {
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("mediaKey", videoBean.mediaKey)
        params.put("videoType", videoBean.videoType)
        HttpRequest.get(RequestUrls.VIDEO_COLLECTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                collectionImage.isSelected = selected
                listener?.onChange(0, selected)
                if (selected) {
                    //ImageUtil.clickAnim(mActivity, imageView)
                    collectionText.text = StringUtils.getString(R.string.collected)
                } else {
                    collectionText.text = StringUtils.getString(R.string.collection)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }
}