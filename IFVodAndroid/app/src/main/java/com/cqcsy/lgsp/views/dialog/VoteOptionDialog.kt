package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.bean.VoteOptionBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.VoteResultUtil
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_vote_option_dialog.*
import org.json.JSONObject

/**
 * 投票选项Dialog
 * allVoteCount: 参与投票的总人数
 * voteOptionData: 每个投票选项数据
 * isMoreSelect: 是否多选
 */
class VoteOptionDialog(
    var mContext: Context,
    var commentBean: CommentBean,
    var videoType: Int,
    var listener: OnVoteListener
) : Dialog(mContext, R.style.dialog_style) {
    private var selectIds: MutableList<String> = ArrayList()

    // 单选记录选中位置
    private var selectPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vote_option_dialog)
        voteCounts.text = StringUtils.getString(R.string.voteCount, commentBean.participateCount)
        voteTitle.text = commentBean.contxt
        if (commentBean.voteType != 2) {
            moreChoiceTips.visibility = View.GONE
            selectIds.add(commentBean.voteItem[0].id.toString())
        }
        recyclerOptionView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setAdapter()
        vote.setOnClickListener {
            if (selectIds.isEmpty()) {
                ToastUtils.showLong(R.string.voteEmptyTips)
                return@setOnClickListener
            }
            vote.isEnabled = false
            voteHttp(appendIds(selectIds))
        }
        cancel.setOnClickListener {
            dismiss()
        }
        close.setOnClickListener {
            dismiss()
        }
    }

    private fun setAdapter() {
        recyclerOptionView.adapter = object : BaseQuickAdapter<VoteOptionBean, BaseViewHolder>(
            R.layout.item_vote_option_checkbox,
            commentBean.voteItem
        ) {
            override fun convert(holder: BaseViewHolder, item: VoteOptionBean) {
                val position = getItemPosition(item)
                holder.setText(R.id.itemText, item.option)
                val imageView = holder.getView<ImageView>(R.id.itemCheck)
                if (commentBean.voteType == 2) {
                    holder.setImageResource(R.id.itemCheck, R.drawable.check_box_normal_selector)
                } else {
                    holder.setImageResource(R.id.itemCheck, R.drawable.check_box_spot_selector)
                    imageView.isSelected = selectPosition == position
                }
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    val voteId = commentBean.voteItem[position].id.toString()
                    if (commentBean.voteType == 2) {
                        imageView.isSelected = !imageView.isSelected
                        notifyDataSetChanged()
                        if (imageView.isSelected) {
                            selectIds.add(voteId)
                        } else {
                            selectIds.remove(voteId)
                        }
                    } else {
                        notifyItemChanged(selectPosition)
                        selectPosition = position
                        notifyItemChanged(selectPosition)
                        selectIds.clear()
                        selectIds.add(voteId)
                    }
                }
            }
        }
    }

    /**
     * 投票请求
     */
    private fun voteHttp(selectIds: String) {
        val params = HttpParams()
        params.put("IDS", selectIds)
        params.put("videoType", videoType)
        HttpRequest.get(RequestUrls.VOTE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                commentBean.participateCount = commentBean.participateCount + 1
                commentBean.voteStatus = true
                for (i in commentBean.voteItem.indices) {
                    if (selectIds.contains(commentBean.voteItem[i].id.toString())) {
                        commentBean.voteItem[i].count += 1
                    }
                }
                listener.onVoteClick(commentBean)
                showVoteResult()
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (errorMsg != null) {
                    showVoteFailed(errorMsg)
                    dismiss()
                }
                vote.isEnabled = true
            }
        }, params, this)
    }

    private fun showVoteResult() {
        dialogTitle.text = StringUtils.getString(R.string.votedTitle)
        voteTips.text = StringUtils.getString(R.string.voted)
        voteCounts.text = StringUtils.getString(R.string.voteCount, commentBean.participateCount)
        moreChoiceTips.visibility = View.GONE
        recyclerOptionView.visibility = View.GONE
        voteClickLayout.visibility = View.GONE
        addResultLayout.visibility = View.VISIBLE
        close.visibility = View.VISIBLE
        VoteResultUtil.addVoteResultView(mContext, commentBean, addResultLayout)
    }

    /**
     * 拼接投票选项ID
     */
    private fun appendIds(selectList: MutableList<String>): String {
        var selectIds = ""
        for (i in selectList.indices) {
            selectIds = if (i == 0) {
                selectList[i]
            } else {
                selectIds + "," + selectList[i]
            }
        }
        return selectIds
    }

    /**
     * 投票失败dialog
     */
    private fun showVoteFailed(errorMsg: String) {
        val tipsDialog = TipsDialog(mContext)
        tipsDialog.setDialogTitle(R.string.voteFailed)
        tipsDialog.setMsg(errorMsg)
        tipsDialog.setLeftListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * 点击投票接口
     */
    interface OnVoteListener {
        fun onVoteClick(commentBean: CommentBean)
    }
}