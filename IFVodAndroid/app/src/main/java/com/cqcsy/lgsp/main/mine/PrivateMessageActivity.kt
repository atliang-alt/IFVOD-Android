package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.MessageListBean
import com.cqcsy.lgsp.upper.chat.ChatActivity
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.views.widget.SwipeRecyclerView
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshActivity
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_swipe_recyclerview.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 我的 -- 私信列表
 */
class PrivateMessageActivity : RefreshActivity(), SwipeRecyclerView.OnItemClickListener {
    private val messageList: MutableList<MessageListBean> = ArrayList()

    override fun getRefreshChild(): Int {
        return R.layout.layout_swipe_recyclerview
    }

    override fun onChildAttach() {
        setHeaderTitle(R.string.privateLetter)
        emptyLargeTip.text = StringUtils.getString(R.string.noReplyTips)
        emptyLittleTip.text = StringUtils.getString(R.string.noReplyTipsLit)
        initView()
        getMessageListData()
    }

    override fun onRightClick(view: View) {
        if (messageList.isEmpty()) {
            return
        }
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_private_message)
        tipsDialog.setMsg(R.string.clear_private_message_tip)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.clear_up) {
            tipsDialog.dismiss()
            // 接口处理
            clearMessage(allType = 1)
        }
        tipsDialog.show()
    }

    private fun initView() {
        swipeRecyclerView.setOnItemClickListener(this)
        swipeRecyclerView.layoutManager = LinearLayoutManager(this)
        setAdapter()
    }

    private fun setAdapter() {
        swipeRecyclerView.adapter = object : BaseQuickAdapter<MessageListBean, BaseViewHolder>(R.layout.item_private_message, messageList) {
            override fun convert(holder: BaseViewHolder, item: MessageListBean) {
                setItemView(holder, item)
            }
        }
    }

    override fun onRefresh() {
        page = 1
        getMessageListData()
    }

    override fun onLoadMore() {
        getMessageListData()
    }

    private fun getMessageListData() {
        if (messageList.isEmpty()) {
            showProgress()
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        params.put("MsgType", 1)
        HttpRequest.post(RequestUrls.GET_MESSAGE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                if (page == 1) {
                    messageList.clear()
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (messageList.isEmpty()) {
                        setRightTextVisible(View.GONE)
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list = Gson().fromJson<List<MessageListBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<MessageListBean>>() {}.type
                )
                messageList.addAll(list)
                swipeRecyclerView.adapter?.notifyDataSetChanged()
                setRightText(R.string.clear_up)
                if (list.size >= size) {
                    page += 1
                    finishLoadMore()
                } else {
                    finishLoadMoreWithNoMoreData()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (messageList.isEmpty()) {
                    showFailed { getMessageListData() }
                } else {
                    finishLoadMoreWithNoMoreData()
                }
            }
        }, params, this)
    }

    private fun setItemView(holder: BaseViewHolder, item: MessageListBean) {
        val textView = holder.getView<TextView>(R.id.content)
        when {
            SpanStringUtils.isVipText(item.content) -> {
                textView.setText(R.string.picture)
            }

            SpanStringUtils.hasEmoji(item.content) -> {
                textView.text = SpanStringUtils.getEmotionContent(this, 11f, item.content)
            }

            else -> {
                textView.text = Html.fromHtml(item.content.replace("\n", "<br/>"))
            }
        }
        ImageUtil.loadCircleImage(this@PrivateMessageActivity, item.avatar, holder.getView(R.id.userPhoto))
        if (item.type > 0) {
            holder.setGone(R.id.serviceTag, false)
                .setGone(R.id.userVip, true)
        } else if (item.bigV || item.vipLevel > 0) {
            holder.setGone(R.id.serviceTag, true)
                .setGone(R.id.userVip, false)
                .setImageResource(
                    R.id.userVip,
                    if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                        item.vipLevel
                    )
                )
        } else {
            holder.setGone(R.id.serviceTag, true).setGone(R.id.userVip, true)
        }
        holder.setText(R.id.nickName, item.nickName)
        holder.setText(R.id.time, TimesUtils.friendDate(item.updateTime))
        if (item.isRead) {
            holder.setVisible(R.id.readImage, false)
        } else {
            holder.setVisible(R.id.readImage, true)
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        onItemClick(position)
    }

    override fun onDeleteClick(position: Int) {
        deleteItemDialog(position)
    }

    private fun onItemClick(position: Int) {
        val item = messageList[position]
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ChatActivity.USER_ID, item.userId.toString())
        intent.putExtra(ChatActivity.NICK_NAME, item.nickName)
        intent.putExtra(ChatActivity.USER_IMAGE, item.avatar)
        intent.putExtra(ChatActivity.CHAT_TYPE, item.type)
        startActivityForResult(intent, 1000)
        messageList[position].isRead = true
        swipeRecyclerView.adapter?.notifyItemChanged(position)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            showRefresh()
        }
    }

    private fun deleteItemDialog(position: Int) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_private_message)
        tipsDialog.setMsg(R.string.delete_private_message_tip)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            // 接口处理
            clearMessage(position)
        }
        tipsDialog.show()
    }

    /**
     * all 是否清空所有 0否 1是
     * position 有值清空单个
     */
    private fun clearMessage(position: Int = -1, allType: Int = 0) {
        showProgressDialog()
        val params = HttpParams()
        if (position >= 0) {
            params.put("uid", messageList[position].userId)
        }
        params.put("all", allType)
        HttpRequest.post(RequestUrls.CLEAR_CHAT_MESSAGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (allType == 1) {
                    messageList.clear()
                } else {
                    messageList.removeAt(position)
                    swipeRecyclerView.adapter?.notifyItemRemoved(position)
                    swipeRecyclerView.resetView()
                }
                checkEmpty()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }

    private fun checkEmpty() {
        if (messageList.isEmpty()) {
            setRightTextVisible(View.GONE)
            showEmpty()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    override fun onNewMessage(message: ChatMessageBean) {
        onRefresh()
    }
}