package com.cqcsy.lgsp.search

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * 搜索用户页面
 */
class SearchUserFragment : RefreshDataFragment<UpperInfoBean>() {
    private var keyword = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecyclerView().isScrollOption = false
    }

    override fun getUrl(): String {
        return RequestUrls.SEARCH_USER
    }

    override fun getParams(): HttpParams {
        keyword = arguments?.getString("keyword") ?: ""
        emptyLargeTip.text = StringUtils.getString(R.string.noSearchUser, "\"" + keyword + "\"")
        emptyLittleTip.text = ""
        val params = HttpParams()
        params.put("keyword", keyword)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_search_user
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<UpperInfoBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<UpperInfoBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: UpperInfoBean, position: Int) {
        val params = if (!item.introduce.isNullOrEmpty()) {
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(101f)
            )
        } else {
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                SizeUtils.dp2px(89f)
            )
        }
        params.leftMargin = SizeUtils.dp2px(12f)
        params.rightMargin = SizeUtils.dp2px(12f)
        holder.getView<RelativeLayout>(R.id.itemLayout).layoutParams = params
        ImageUtil.loadCircleImage(this, item.avatar, holder.getView(R.id.userPhoto))
        val userVip = holder.getView<ImageView>(R.id.userVip)
        if (item.bigV || item.vipLevel > 0) {
            userVip.visibility = View.VISIBLE
            userVip.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            userVip.visibility = View.GONE
        }
        val nameTextView = holder.getView<TextView>(R.id.nickName)
        val spannableString = SpannableString(item.nickName)
        setTextClick(nameTextView, keyword, spannableString)
        nameTextView.text = spannableString
        // 0女1男，-1没有选择
        when (item.sex) {
            1 -> {
                holder.setVisible(R.id.sexImage, true)
                holder.setImageResource(R.id.sexImage, R.mipmap.icon_sex_man_32)
            }
            0 -> {
                holder.setVisible(R.id.sexImage, true)
                holder.setImageResource(R.id.sexImage, R.mipmap.icon_sex_woman_32)
            }
            else -> {
                holder.setGone(R.id.sexImage, true)
            }
        }
        if (item.introduce.isNullOrEmpty()) {
            holder.setGone(R.id.userSign, true)
        } else {
            holder.setGone(R.id.userSign, false)
            holder.setText(R.id.userSign, item.introduce)
        }
        holder.setText(
            R.id.fansCount,
            StringUtils.getString(R.string.fansCount, NormalUtil.formatPlayCount(item.fansCount))
        )
        holder.setText(R.id.videoCount, StringUtils.getString(R.string.work_count, item.totalVideo))
        val focusText = holder.getView<TextView>(R.id.attention)
        val blackList = holder.getView<TextView>(R.id.blackList)

        if (!GlobalValue.isLogin() || GlobalValue.userInfoBean?.id == item.id) {
            focusText.isVisible = false
            blackList.isVisible = false
        } else if (item.isBlackList) {
            focusText.isVisible = false
            blackList.isVisible = true
            blackList.setOnClickListener {
                showBlackTip(item.id)
            }
        } else {
            blackList.isVisible = false
            focusText.isVisible = true
            focusText.isSelected = item.focusStatus
            focusText.text = if (item.focusStatus) {
                resources.getString(R.string.followed)
            } else {
                resources.getString(R.string.attention)
            }
            focusText.setOnClickListener {
                focusHttp(item, position)
            }
        }
    }

    private fun setTextClick(
        textView: TextView,
        clickString: String,
        name: SpannableString
    ) {
        if (name.isEmpty()) {
            textView.text = ""
            return
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        val pattern = Pattern.compile(SpanStringUtils.escapeExprSpecialWord(clickString))
        val matcher = pattern.matcher(name)
        while (matcher.find()) {
            name.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = ColorUtils.getColor(R.color.blue)
                    ds.isUnderlineText = false
                }
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        textView.text = name
    }

    override fun onItemClick(position: Int, dataBean: UpperInfoBean, holder: BaseViewHolder) {
        super.onItemClick(position, dataBean, holder)
        val intent = Intent(context, UpperActivity::class.java)
        intent.putExtra(UpperActivity.UPPER_ID, dataBean.id)
        startActivity(intent)
    }

    private fun showBlackTip(userId: Int) {
        val dialog = TipsDialog(requireContext())
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

    private fun focusHttp(bean: UpperInfoBean, position: Int) {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        params.put("userId", bean.id)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                if (selected) {
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                }
                val event = VideoActionResultEvent()

                var fansCount = getDataList()[position].fansCount
                if (selected) {
                    getDataList()[position].fansCount = ++fansCount
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    if (fansCount > 0) {
                        getDataList()[position].fansCount = --fansCount
                    }
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                event.type = 1
                event.id = bean.id.toString()
                event.userLogo = bean.avatar
                event.userName = bean.nickName
                EventBus.getDefault().post(event)
                getDataList()[position].focusStatus = selected
                refreshView()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for ((i, upperInfoBean) in getDataList().withIndex()) {
            if (upperInfoBean.id == event.uid) {
                upperInfoBean.isBlackList = event.status
                upperInfoBean.focusStatus = false
                mAdapter?.notifyItemChanged(i)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoActionResultEvent(event: VideoActionResultEvent) {
        if (event.type != 1) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> {
                getDataList().forEach {
                    if (it.id.toString() == event.id) {
                        it.focusStatus = true
                        refreshView()
                        return
                    }
                }
            }
            VideoActionResultEvent.ACTION_REMOVE -> {
                getDataList().forEach {
                    if (it.id.toString() == event.id) {
                        it.focusStatus = false
                        refreshView()
                        return
                    }
                }
            }
        }
    }
}