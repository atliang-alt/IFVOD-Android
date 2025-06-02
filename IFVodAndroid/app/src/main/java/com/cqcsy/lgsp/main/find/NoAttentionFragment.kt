package com.cqcsy.lgsp.main.find

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_no_attemtion_head.view.*
import kotlinx.android.synthetic.main.layout_no_attention.*
import kotlinx.android.synthetic.main.layout_no_attention.recyclerLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 发现-关注：没有关注用户
 */
class NoAttentionFragment : RefreshFragment() {
    private var adapter: BaseQuickAdapter<UpperInfoBean, BaseViewHolder>? = null
    private var dataList: MutableList<UpperInfoBean> = ArrayList()
    private var selectList: MutableList<UpperInfoBean> = ArrayList()
    private var headView: View? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var firstVisibleItem = 0

    override fun getRefreshChild(): Int {
        return R.layout.layout_no_attention
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerLayout.itemAnimator = null
        recyclerLayout.isScrollOption = false
    }

    override fun onResume() {
        super.onResume()
        selectList.clear()
        if (headView != null) {
            headView!!.attentionLayout.visibility = View.GONE
            headView!!.noAttentionLayout.visibility = View.VISIBLE
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        getRecommendUser(false)
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getRecommendUser(false)
    }

    override fun initData() {
        super.initData()
        getRecommendUser(true)
    }

    override fun initView() {
        super.initView()
        setEnableRefresh(true)
        setEnableLoadMore(true)
        linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerLayout.layoutManager = linearLayoutManager
        adapter = object : BaseQuickAdapter<UpperInfoBean, BaseViewHolder>(
            R.layout.item_fans_attention,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: UpperInfoBean) {
                val position = getItemPosition(item)
                ImageUtil.loadCircleImage(
                    this@NoAttentionFragment,
                    item.avatar,
                    holder.getView(R.id.userPhoto)
                )
                holder.setText(R.id.userSign, item.introduce)
                val userVip = holder.getView<ImageView>(R.id.userVip)
                val nickname = holder.getView<TextView>(R.id.nickName)
                nickname.text = item.nickName
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
                holder.setText(
                    R.id.fansCount,
                    StringUtils.getString(
                        R.string.fansCount,
                        NormalUtil.formatPlayCount(item.fansCount)
                    )
                )
                holder.setText(
                    R.id.videoCount,
                    StringUtils.getString(R.string.work_count, item.totalVideo)
                )
                val focusText = holder.getView<TextView>(R.id.attentionText)
                if (item.focusStatus) {
                    focusText.text = StringUtils.getString(R.string.followed)
                    focusText.isSelected = item.focusStatus
                } else {
                    focusText.text = StringUtils.getString(R.string.attention)
                    focusText.isSelected = item.focusStatus
                }
                focusText.setOnClickListener {
                    focusHttp(item, position)
                }
                holder.getView<RelativeLayout>(R.id.itemLayout).setOnClickListener {
                    val intent = Intent(context, UpperActivity::class.java)
                    intent.putExtra(UpperActivity.UPPER_ID, item.id)
                    startActivity(intent)
                }
            }
        }
        addHeadView()
        recyclerLayout.adapter = adapter
        recyclerLayout.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItem = linearLayoutManager!!.findFirstVisibleItemPosition()
                if (firstVisibleItem >= 1 && selectList.size > 0) {
                    attentionLayout.visibility = View.VISIBLE
                } else {
                    attentionLayout.visibility = View.GONE
                }
            }
        })
        attentionLayout.setOnClickListener {
            if (selectList.size > 0) {
                val event = VideoActionResultEvent()
                event.type = 1
                event.action = VideoActionResultEvent.ACTION_ADD_FINISH
                EventBus.getDefault().post(event)
            }
        }
    }

    private fun addHeadView() {
        headView = View.inflate(requireContext(), R.layout.layout_no_attemtion_head, null)
        headView!!.lookVideo.setOnClickListener {
            val event = VideoActionResultEvent()
            event.type = 1
            event.action = VideoActionResultEvent.ACTION_ADD_FINISH
            EventBus.getDefault().post(event)
        }
        adapter?.addHeaderView(headView!!)
    }

    private fun getRecommendUser(isShowView: Boolean) {
        if (isShowView) {
            showProgress()
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.FIND_RECOMMEND_USER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (page == 1) {
                    dataList.clear()
                    dismissProgress()
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (dataList.isEmpty()) {
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val upperInfoList = Gson().fromJson<MutableList<UpperInfoBean>>(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<UpperInfoBean>>() {}.type
                )
                dataList.addAll(upperInfoList)
                adapter?.notifyDataSetChanged()
                if (upperInfoList.isNullOrEmpty()) {
                    finishLoadMoreWithNoMoreData()
                } else {
                    page += 1
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (page == 1) {
                    finishRefresh()
                    showFailed {
                        getRecommendUser(true)
                    }
                } else {
                    errorLoadMore()
                }
            }
        }, params, this)
    }

    private fun focusHttp(item: UpperInfoBean, position: Int) {
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
                var fansCount = dataList[position].fansCount
                if (selected) {
                    dataList[position].fansCount = ++fansCount
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    if (fansCount > 0) {
                        dataList[position].fansCount = --fansCount
                    }
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)
                dataList[position].focusStatus = selected
                // 由于添加了head,更新position需要+1
                adapter?.notifyItemChanged(position + 1)
                if (selected) {
                    selectList.add(dataList[position])
                } else {
                    selectList.remove(dataList[position])
                }
                if (selectList.size > 0) {
                    headView!!.attentionLayout.visibility = View.VISIBLE
                    headView!!.noAttentionLayout.visibility = View.GONE
                    headView!!.attentionCount.text =
                        StringUtils.getString(R.string.attentionCount, selectList.size)
                    counts.text = StringUtils.getString(R.string.attentionCount, selectList.size)
                    if (firstVisibleItem >= 1) {
                        attentionLayout.visibility = View.VISIBLE
                    } else {
                        attentionLayout.visibility = View.GONE
                    }
                } else {
                    attentionLayout.visibility = View.GONE
                    headView!!.attentionLayout.visibility = View.GONE
                    headView!!.noAttentionLayout.visibility = View.VISIBLE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if (event.position == R.id.button_find && mIsFragmentVisible && dataList.isNotEmpty()) {
            recyclerLayout.scrollToTop(refreshLayout)
        }
    }
}