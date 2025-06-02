package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.FansBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 我的 -- 粉丝页
 */
class MineFansActivity : RefreshActivity() {
    private var fansData: MutableList<FansBean> = ArrayList()

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onChildAttach() {
        setHeaderTitle(R.string.fansRecord)
        emptyLargeTip.text = StringUtils.getString(R.string.noFansTips)
        emptyLittleTip.text = StringUtils.getString(R.string.noFansTipsLit)
        initData()
        initView()
    }

    private fun initData() {
        getData()
    }

    private fun initView() {
        setEnableLoadMore(true)
        setEnableRefresh(false)
        recyclerView.isScrollOption = false
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = object : BaseQuickAdapter<FansBean, BaseViewHolder>(
            R.layout.item_mine_fans,
            fansData
        ) {
            override fun convert(holder: BaseViewHolder, item: FansBean) {
                holder.setText(R.id.nickName, item.nickName)
                val nickName = holder.getView<TextView>(R.id.nickName)
                if (item.sex == 1) {
                    nickName.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.mipmap.icon_man_24,
                        0
                    )
                } else if (item.sex == 0) {
                    nickName.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.mipmap.icon_women_24,
                        0
                    )
                }
                holder.setText(
                    R.id.time,
                    TimeUtils.date2String(TimesUtils.formatDate(item.updateTime), "yyyy-MM-dd")
                )
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
                ImageUtil.loadCircleImage(
                    this@MineFansActivity,
                    item.avatar,
                    holder.getView(R.id.userPhoto)
                )
                val focusText = holder.getView<TextView>(R.id.attentionText)
                val blackList = holder.getView<TextView>(R.id.blackList)
                if (item.isBlackList) {
                    focusText.isVisible = false
                    blackList.isVisible = true
                    blackList.setOnClickListener {
                        showBlackTip(item.userId)
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
                        focusHttp(item, getItemPosition(item))
                    }
                }
            }
        }
        adapter.setOnItemClickListener { _, _, position ->
            val intent = Intent(this, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, fansData[position].userId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getData()
    }

    private fun getData() {
        val params = HttpParams()
        params.put("MsgType", 3)
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.GET_MESSAGE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (page == 1) {
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list: MutableList<FansBean> =
                    Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<FansBean>>() {}.type
                    )
                fansData.addAll(list)
                recyclerView.adapter?.notifyDataSetChanged()
                if (list.size >= size) {
                    page += 1
                    finishLoadMore()
                } else {
                    finishLoadMoreWithNoMoreData()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    page = 1
                    fansData.clear()
                    getData()
                }
            }
        }, params, this)
    }

    private fun focusHttp(item: FansBean, position: Int) {
        val params = HttpParams()
        params.put("userId", item.userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                val event = VideoActionResultEvent()
                event.id = item.userId.toString()
                event.type = 1
                event.userLogo = item.avatar
                event.userName = item.nickName
                if (selected) {
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)
                fansData[position].focusStatus = selected
                recyclerView.adapter?.notifyItemChanged(position)

            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun showBlackTip(userId: Int) {
        val dialog = TipsDialog(this)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for ((i, data) in fansData.withIndex()) {
            if (data.userId == event.uid) {
                data.isBlackList = event.status
                data.focusStatus = false
                recyclerView.adapter?.notifyItemChanged(i)
            }
        }

    }
}