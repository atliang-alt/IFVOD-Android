package com.cqcsy.lgsp.video.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.event.BarrageEvent
import com.cqcsy.lgsp.event.BarrageType
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.library.utils.GlobalValue
import com.littlejerk.rvdivider.builder.XLinearBuilder
import kotlinx.android.synthetic.main.layout_video_chat.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 直播聊天页面
 */
class VideoChatFragment : BaseFragment() {
    private var adapter: BaseQuickAdapter<BarrageBean, BaseViewHolder>? = null
    private var dataList: MutableList<BarrageBean> = ArrayList()

    var commentEditDialog: CommentEditDialog? = null
    private val mVideoModel: VideoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_video_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserve()
        initView()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun initObserve() {
        mVideoModel.mPlayClarity.observe(this) {
            val title = if (it?.isLive == true) it.episodeTitle else it.title
            videoTitle.text = title
        }
        mVideoModel.mBarrageList.observe(this) {
            adapter?.setNewInstance(it)
            adapter?.notifyItemChanged(0, it.size)
        }
        mVideoModel.mOnlineNumber.observe(this) {
            setOnlineNumber(it)
        }
    }

    private fun initView() {
        chatRecycler.layoutManager = LinearLayoutManager(context)
        chatRecycler.addItemDecoration(XLinearBuilder(context).setSpacing(10f).build())
        adapter = object : BaseQuickAdapter<BarrageBean, BaseViewHolder>(R.layout.item_video_chat, dataList) {
            override fun convert(holder: BaseViewHolder, item: BarrageBean) {
                val textView = holder.getView<TextView>(R.id.chatText)
                textView.text = SpanStringUtils.getVideoChatText(textView, context, item)
            }
        }
        chatRecycler.adapter = adapter
        chatEdit.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                startActivity(Intent(context, LoginActivity::class.java))
                return@setOnClickListener
            }
            commentEditDialog = CommentEditDialog("", object : CommentEditDialog.SendCommentListener {
                override fun sendComment(type: Int, inputText: String, vipList: MutableList<String>) {
                    mVideoModel.mPlayClarity.value?.apply {
                        mVideoModel.sendBarrage(mediaKey, uniqueID, videoType, inputText, type = 1)
                    }
                }
            }, false)
            commentEditDialog?.show(childFragmentManager, "commentEditDialog")
        }
    }

    private fun addDataRefresh(bean: BarrageBean) {
        dataList.forEach {
            if (it.guid == bean.guid) {
                return
            }
        }
        adapter?.addData(bean)
        val size = adapter?.itemCount ?: 0
        adapter?.notifyDataSetChanged()
        if (chatRecycler.scrollState == 0 && size > 0) {
            chatRecycler.scrollToPosition(size - 1)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvent(event: BarrageEvent) {
        when (event.eventType) {
            BarrageType.EVENT_ONLINE -> if (event.onlineNumber >= 0) {
                setOnlineNumber(event.onlineNumber)
            }

            BarrageType.EVENT_CHAT, BarrageType.EVENT_BARRAGE -> if (event.message != null && event.message?.uid != GlobalValue.userInfoBean?.id) {
                addDataRefresh(event.message!!)
            }

            BarrageType.EVENT_LOCAL -> {
                if (event.message != null) {
                    addDataRefresh(event.message!!)
                }
            }
        }
    }

    private fun setOnlineNumber(number: Int) {
        lookCount.text = StringUtils.getString(R.string.people, number)
    }
}