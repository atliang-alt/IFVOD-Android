package com.cqcsy.lgsp.main.find

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.upper.UpperActivity.Companion.UPPER_ID
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import kotlinx.android.synthetic.main.layout_find_attention.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 发现-关注：有关注用户
 */
class AttentionFragment : NormalFragment() {
    private var attentionList: MutableList<UpperInfoBean>? = null
    private var adapter: BaseQuickAdapter<UpperInfoBean, BaseViewHolder>? = null
    private var fragmentData: AttentionDataListFragment? = null
    var navigation: NavigationBarBean? = null

    override fun getContainerView(): Int {
        return R.layout.layout_find_attention
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigation = arguments?.getSerializable("navigation") as NavigationBarBean?
        showFragment()
    }

    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        fragmentData = AttentionDataListFragment()
        val bundle = Bundle()
        bundle.putSerializable("navigation", navigation)
        fragmentData!!.arguments = bundle
        transaction.add(R.id.attentionContainer, fragmentData!!)
        transaction.commitAllowingStateLoss()
    }

    override fun initData() {
        attentionList = ArrayList()
        attentionList?.addAll(arguments?.getSerializable("attentionUserList") as MutableList<UpperInfoBean>)
    }

    override fun initView() {
        super.initView()
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = object : BaseQuickAdapter<UpperInfoBean, BaseViewHolder>(
            R.layout.item_find_attention_user,
            attentionList
        ) {
            override fun convert(holder: BaseViewHolder, item: UpperInfoBean) {
                holder.setText(R.id.userName, item.nickName)
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
                    requireContext(),
                    item.avatar,
                    holder.getView(R.id.userPhoto)
                )
            }
        }
        adapter?.setOnItemClickListener { _, _, position ->
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UPPER_ID, attentionList?.get(position)?.id)
            startActivityForResult(intent, 1001)
        }
        recyclerView.adapter = adapter
        more.setOnClickListener {
            startActivityForResult(Intent(context, MoreAttentionActivity::class.java), 1002)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val event = VideoActionResultEvent()
            event.type = 1
            event.action = VideoActionResultEvent.ACTION_REFRESH
            EventBus.getDefault().post(event)
            fragmentData?.onRefresh()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoActionResultEvent(event: VideoActionResultEvent) {
        if (event.type != 1) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> {
                val result = adapter?.data?.find { it.id == event.id.toInt() }
                if (result != null) {   // 已经在关注列表，不处理
                    return
                }
                val infoBean = UpperInfoBean()
                infoBean.id = event.id.toInt()
                infoBean.avatar = event.userLogo
                infoBean.nickName = event.userName
                attentionList?.add(infoBean)
                adapter?.notifyDataSetChanged()
            }

            VideoActionResultEvent.ACTION_REMOVE -> {
                attentionList?.forEach {
                    if (it.id.toString() == event.id) {
                        attentionList?.remove(it)
                        adapter?.notifyDataSetChanged()
                        return
                    }
                }
            }
        }
    }
}