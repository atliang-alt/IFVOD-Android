package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.text.Html
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.MessageListBean
import kotlinx.android.synthetic.main.activity_system_message_details.*

/**
 * 系统消息详情页
 */
class SystemDetailsActivity : NormalActivity() {
    private var messageListBean: MessageListBean? = null
    override fun getContainerView(): Int {
        return R.layout.activity_system_message_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.messageDetails)
        messageListBean = intent.getSerializableExtra("content") as MessageListBean
        contentText.text = Html.fromHtml(messageListBean?.content)
    }
}