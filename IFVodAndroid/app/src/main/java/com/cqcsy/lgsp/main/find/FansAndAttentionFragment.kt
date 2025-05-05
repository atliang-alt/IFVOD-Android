package com.cqcsy.lgsp.main.find

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * 我的粉丝、我的关注fragment
 * 传参数 httpUrl:请求地址
 *
 */
class FansAndAttentionFragment : RefreshDataFragment<UpperInfoBean>() {
    private var isRefreshData: Boolean = false
    private var isFromUserCenter = false
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFromUserCenter = arguments?.getBoolean("isSelf") ?: false
        userId = arguments?.getInt("userId") ?: -1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecyclerView().isScrollOption = false
        if (isFromUserCenter) {
            emptyLargeTip.setText(R.string.no_attention)
            emptyLittleTip.setText(R.string.no_attention_tip)
        } else {
            emptyLargeTip.setText(R.string.noFansTips)
            emptyLittleTip.setText(R.string.noFansTipsLit)
        }
    }

    override fun getUrl(): String {
        return arguments?.getString("httpUrl") ?: ""
    }

    override fun onVisible() {
        super.onVisible()
        if (isRefreshData) {
            showRefresh()
            isRefreshData = false
        }
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        if (!isFromUserCenter)
            params.put("userId", userId)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_fans_attention
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<UpperInfoBean> {
        return Gson().fromJson(jsonArray.toString(), object : TypeToken<MutableList<UpperInfoBean>>() {}.type)
    }

    override fun onItemClick(position: Int, dataBean: UpperInfoBean, holder: BaseViewHolder) {
        val intent = Intent(context, UpperActivity::class.java)
        intent.putExtra(UpperActivity.UPPER_ID, dataBean.id)
        startActivity(intent)
    }

    override fun setItemView(holder: BaseViewHolder, item: UpperInfoBean, position: Int) {
        val params = if (!item.introduce.isNullOrEmpty()) {
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(101f))
        } else {
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(89f))
        }
        params.leftMargin = SizeUtils.dp2px(12f)
        params.rightMargin = SizeUtils.dp2px(12f)
        holder.getView<RelativeLayout>(R.id.itemLayout).layoutParams = params
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
        ImageUtil.loadCircleImage(requireContext(), item.avatar, holder.getView(R.id.userPhoto))
        holder.setText(R.id.nickName, item.nickName)
        val nickname = holder.getView<TextView>(R.id.nickName)
        if (item.sex == 1) {
            nickname.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_man_24,
                0
            )
        } else if (item.sex == 0) {
            nickname.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_women_24,
                0
            )
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
        val focusText = holder.getView<TextView>(R.id.attentionText)
        if (GlobalValue.isLogin() && GlobalValue.userInfoBean?.id == item.id) {
            focusText.visibility = View.GONE
        } else {
            focusText.visibility = View.VISIBLE
            if (item.focusStatus) {
                focusText.text = StringUtils.getString(R.string.followed)
                focusText.isSelected = item.focusStatus
            } else {
                focusText.text = StringUtils.getString(R.string.attention)
                focusText.isSelected = item.focusStatus
            }
        }
        focusText.setOnClickListener {
            focusHttp(item, position)
        }
    }

    private fun focusHttp(item: UpperInfoBean, position: Int) {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(context, LoginActivity::class.java))
            return
        }
        val params = HttpParams()
        params.put("userId", item.id)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                val event = VideoActionResultEvent()
                event.type = 1
                event.id = item.id.toString()
                event.userLogo = item.avatar
                event.userName = item.nickName
                var fansCount = getDataList()[position].fansCount
                if (selected) {
                    getDataList()[position].fansCount = ++fansCount
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    getDataList()[position].fansCount = --fansCount
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)
                getDataList()[position].focusStatus = selected
                refreshView()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /*    @Subscribe(threadMode = ThreadMode.MAIN)
        fun onAttentionRefreshEvent(event: VideoActionResultEvent) {
            if (event.type != 1) {
                return
            }
            // 我的关注
            if (getUrl() == RequestUrls.FIND_ATTENTION_USER) {
                isRefreshData = true
            }
        }*/


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFollowEvent(event: VideoActionResultEvent) {
        if (event.type != 1) {
            return
        }
        val adapter = mAdapter
        if (adapter != null) {
            if (event.action == VideoActionResultEvent.ACTION_REMOVE) {
                for ((i, data) in adapter.data.withIndex()) {
                    if (data.id.toString() == event.id) {
                        data.focusStatus = false
                        adapter.notifyItemChanged(i)
                        break
                    }
                }
            } else if (event.action == VideoActionResultEvent.ACTION_ADD) {
                for ((i, data) in adapter.data.withIndex()) {
                    if (data.id.toString() == event.id) {
                        data.focusStatus = true
                        adapter.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
    }
}