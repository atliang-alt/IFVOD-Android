package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.text.Html
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshListActivity
import com.cqcsy.lgsp.bean.MessageListBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.utils.TimesUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONArray

/**
 * 我的 -- 系统消息列表
 */
class SystemMessageActivity : RefreshListActivity<MessageListBean>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.systemMessage)
        emptyLargeTip.text = StringUtils.getString(R.string.noMessageTips)
        emptyLittleTip.text = StringUtils.getString(R.string.noMessageTipsLit)
    }

    override fun getUrl(): String {
        return RequestUrls.GET_MESSAGE_LIST
    }

    override fun getHttpParams(): HttpParams {
        val params = HttpParams()
        params.put("MsgType", 0)
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_system_message
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<MessageListBean> {
        return Gson().fromJson(jsonArray.toString(), object : TypeToken<List<MessageListBean>>() {}.type)
    }

    override fun onItemClick(position: Int, dataBean: MessageListBean) {
        super.onItemClick(position, dataBean)
        if (!dataBean.isRead) {
            dataBean.isRead = true
            refreshView(position)
        }
        val intent = Intent(this, SystemDetailsActivity::class.java)
        intent.putExtra("content", dataBean)
        startActivity(intent)
    }

    override fun setItemView(holder: BaseViewHolder, item: MessageListBean, position: Int) {
        holder.setText(R.id.content, Html.fromHtml(item.content))
        holder.setText(R.id.time, TimesUtils.friendDate(item.updateTime))
        if (item.isRead) {
            holder.setVisible(R.id.readImage, false)
        } else {
            holder.setVisible(R.id.readImage, true)
        }
    }
}