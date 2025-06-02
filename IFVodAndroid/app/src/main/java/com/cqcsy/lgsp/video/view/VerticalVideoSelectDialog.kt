package com.cqcsy.lgsp.video.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.video.bean.VideoItemBean
import com.cqcsy.library.views.BottomBaseDialog
import kotlinx.android.synthetic.main.layout_vertical_video_normal_menu.*

/**
 * 剧集和普通列菜单
 */
class VerticalVideoSelectDialog(context: Context) : BottomBaseDialog(context) {
    var mListData: MutableList<VideoItemBean>? = null

    var listener: OnMenuClickListener? = null

    interface OnMenuClickListener {
        fun onItemClick(selectId: Int)
        fun onOpenVipClick() {}
    }

    fun setMenuClickListener(listener: OnMenuClickListener) {
        this.listener = listener
    }

    fun setMenuData(data: MutableList<VideoItemBean>) {
        mListData = data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_vertical_video_normal_menu)

        initList()
    }

    private fun initList() {
        if (mListData == null || mListData?.size == 0) {
            return
        }
        initRecyclerView()
        setAdapter()
    }

    private fun initRecyclerView() {
        menuList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        disableMenuList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    private fun setAdapter() {
        val enableList = mListData?.filter { it.enbale }
        val disableList = mListData?.filter { !it.enbale }
        if (!enableList.isNullOrEmpty()) {
            val adapter = object : BaseQuickAdapter<VideoItemBean, BaseViewHolder>(
                R.layout.layout_vertical_video_menu_item,
                enableList.toMutableList()
            ) {
                override fun convert(holder: BaseViewHolder, item: VideoItemBean) {
                    holder.setText(R.id.item_name, item.text)
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    holder.getView<RelativeLayout>(R.id.itemLayout).setOnClickListener {
                        if (listener != null) {
                            dismiss()
                            listener?.onItemClick(item.id)
                        }
                    }
                }
            }
            menuList.adapter = adapter
        }
        if (!disableList.isNullOrEmpty()) {
            disable_title_content.visibility = View.VISIBLE
            disableMenuList.visibility = View.VISIBLE
            open_vip.setOnClickListener {
                dismiss()
                listener?.onOpenVipClick()
            }
            disableMenuList.adapter = object : BaseQuickAdapter<VideoItemBean, BaseViewHolder>(
                R.layout.layout_vertical_video_menu_item,
                disableList.toMutableList()
            ) {
                override fun convert(holder: BaseViewHolder, item: VideoItemBean) {
                    val name = holder.getView<TextView>(R.id.item_name)
                    name.text = item.text
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    if (item.isVip) {
                        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_vip_tag, 0)
                    } else {
                        name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    }
                }
            }
        }
    }
}