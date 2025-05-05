package com.cqcsy.lgsp.main.mine

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.utils.VoteResultUtil
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 我的 -- 我参与的投票、我发起的投票
 */
class PartakeVoteFragment : RefreshFragment() {
    // 0:我参与的投票 1: 我发起的投票
    private var formType = 0
    private var httpUrl = ""
    private var voteData: MutableList<CommentBean> = ArrayList()
    var adapter: BaseQuickAdapter<CommentBean, BaseViewHolder>? = null

    // 剧集请求页码
    private var dramaPage = 1

    // 小视频请求页码
    private var shortPage = 1

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun initData() {
        formType = arguments?.getInt("formType") ?: 0
        httpUrl = arguments?.getString("httpUrl") ?: ""
    }

    override fun initView() {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(context)
        adapter = object :
            BaseQuickAdapter<CommentBean, BaseViewHolder>(R.layout.item_mine_vote, voteData) {
            override fun convert(holder: BaseViewHolder, item: CommentBean) {
                holder.setText(R.id.voteTitle, item.contxt)
                holder.setText(R.id.voteTime, TimesUtils.friendDate(item.postTime))
                holder.setText(
                    R.id.voteCounts,
                    StringUtils.getString(R.string.voteCount, item.participateCount)
                )
                val voteStatus = holder.getView<TextView>(R.id.voteStatus)
                if (formType == 1) {
                    // 我发起的
                    voteStatus.text = StringUtils.getString(R.string.delete)
                    voteStatus.setTextColor(ColorUtils.getColor(R.color.yellow))
                    voteStatus.setOnClickListener {
                        deleteCommentDialog(item)
                    }
                } else {
                    // 我参与的
                    if (item.deleteState) {
                        voteStatus.visibility = View.VISIBLE
                        voteStatus.text = StringUtils.getString(R.string.voteClose)
                        voteStatus.setTextColor(ColorUtils.getColor(R.color.red))
                        holder.getView<TextView>(R.id.voteTitle)
                            .setTextColor(ColorUtils.getColor(R.color.grey))
                        holder.setBackgroundResource(R.id.voteInfoContent, R.color.transparent)
                    } else {
                        voteStatus.visibility = View.GONE
                        holder.getView<TextView>(R.id.voteTitle)
                            .setTextColor(ColorUtils.getColor(R.color.yellow))
                        holder.setBackgroundResource(
                            R.id.voteInfoContent,
                            R.drawable.vote_content_bg
                        )
                    }
                }
                VoteResultUtil.mineVoteResultView(
                    requireContext(),
                    item.voteItem,
                    holder.getView(R.id.addOptionLayout),
                    item.deleteState
                )
            }
        }
        recyclerView?.adapter = adapter
        if (voteData.isEmpty()) {
            showProgress()
        }
        getDramaData()
        getShortData()
    }

    override fun onRefresh() {
        super.onRefresh()
        reset()
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getDramaData()
        getShortData()
    }

    private fun reset() {
        dramaPage = 1
        shortPage = 1
        voteData.clear()
        adapter?.notifyDataSetChanged()
        getDramaData()
        getShortData()
    }

    /**
     * 获取投票数据
     */
    private fun getDramaData() {
        val params = HttpParams()
        params.put("page", dramaPage)
        params.put("size", size / 2)
        HttpRequest.post(httpUrl, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (voteData.isEmpty()) {
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list: MutableList<CommentBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<CommentBean>>() {}.type
                )
                voteData.addAll(list)
                if (dramaPage == 1) {
                    finishRefresh()
                }
                adapter?.notifyDataSetChanged()
                if (list.isEmpty()) {
                    finishLoadMoreWithNoMoreData()
                } else {
                    dramaPage += 1
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    reset()
                }
            }
        }, params, this)
    }

    /**
     * 获取小视频数据
     */
    private fun getShortData() {
        val params = HttpParams()
        params.put("page", shortPage)
        params.put("size", size / 2)
        HttpRequest.post("$httpUrl?videoType=3", object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                if (shortPage == 1) {
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (voteData.isEmpty()) {
                        showEmpty()
                    } else {
                        finishLoadMore()
                    }
                    return
                }
                val list: MutableList<CommentBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<CommentBean>>() {}.type
                )
                for (i in list.indices) {
                    list[i].isShortData = true
                }
                voteData.addAll(list)
                adapter?.notifyDataSetChanged()
                if (list.isNullOrEmpty()) {
                    finishLoadMoreWithNoMoreData()
                } else {
                    shortPage += 1
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    reset()
                }
            }
        }, params, this)
    }

    /**
     * 删除投票弹框
     */
    private fun deleteCommentDialog(commentBean: CommentBean) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.delete_vote)
        tipsDialog.setMsg(R.string.deleteVoteTips)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.sureDelete) {
            deleteComment(commentBean)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * 删除评论接口
     */
    private fun deleteComment(commentBean: CommentBean) {
        val params = HttpParams()
        params.put("ReplyID", commentBean.replyID)
        var url = RequestUrls.DELETE_COMMENT
        if (commentBean.isShortData) {
            url = "$url?videoType=3"
        }
        HttpRequest.post(url, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                voteData.remove(commentBean)
                if (voteData.size == 0) {
                    showEmpty()
                }
                val event = CommentEvent()
                event.replyId = commentBean.replyID
                EventBus.getDefault().post(event)
                adapter?.notifyDataSetChanged()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        if (formType == 0) {
            // 删除我发起的投票后我参与的页面刷新的UI
            for (i in voteData.indices) {
                if (voteData[i].replyID == event.replyId) {
                    voteData[i].deleteState = true
                    adapter?.notifyItemChanged(i)
                }
            }
        }
    }

    override fun showEmpty() {
        super.showEmpty()
        if (formType == 1) {
            emptyLargeTip.setText(R.string.no_vote_owner)
            emptyLittleTip.setText(R.string.no_vote_owner_tip)
        } else {
            emptyLargeTip.setText(R.string.no_vote)
            emptyLittleTip.setText(R.string.no_vote_tip)
        }
        emptyLittleTip.visibility = View.VISIBLE
    }
}