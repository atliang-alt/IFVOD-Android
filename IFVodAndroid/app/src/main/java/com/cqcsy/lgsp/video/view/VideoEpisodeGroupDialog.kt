package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.VideoGroupBean
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import kotlinx.android.synthetic.main.layout_dialog_episode_group.*

/**
 ** 2023/12/25
 ** des：竖屏全屏，分组选集
 **/

class VideoEpisodeGroupDialog(
    context: Context,
    val current: VideoBaseBean,
    val groupData: MutableList<VideoGroupBean>,
    val listener: VideoStickySelectDialog.OnItemSelectListener
) : VideoMenuDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dialog_episode_group)
        setTabs()
    }

    private fun setTabs() {
        var selectTab = -1
        groupData.forEach {
            if (selectTab == -1) {
                val isCurrent = it.itemList?.indexOfFirst { item -> item.uniqueID == current.uniqueID } != -1
                if (isCurrent) {
                    selectTab = groupData.indexOf(it)
                }
            }
            group_tab.addTab(group_tab.newTab().setText(it.groupName).setTag(it.itemList))
        }
        group_tab.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.tag is MutableList<*> && (tab.tag as MutableList<*>)[0] is VideoBaseBean) {
                    setGroupData(tab.tag as MutableList<VideoBaseBean>)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        if (selectTab == -1) selectTab = 0
        group_tab.getTabAt(selectTab)?.let {
            group_tab.selectTab(it)
            setGroupData(it.tag as MutableList<VideoBaseBean>)
        }
        group_tab.post {
            group_tab.setScrollPosition(selectTab, 0f, true)
        }
    }

    private fun setGroupData(itemList: MutableList<VideoBaseBean>) {
        var groupAdapter = group_recycler.adapter
        if (groupAdapter == null) {
            val layoutManager = GridLayoutManager(context, getMenuColumn())
            group_recycler.layoutManager = layoutManager
            group_recycler.addItemDecoration(XGridBuilder(context).setVLineSpacing(10f).setHLineSpacing(10f).setIncludeEdge(true).build())
            groupAdapter = object : BaseQuickAdapter<VideoBaseBean, BaseViewHolder>(R.layout.layout_video_menu_item, itemList) {
                override fun convert(holder: BaseViewHolder, item: VideoBaseBean) {
                    holder.getView<RelativeLayout>(R.id.item_root).updateLayoutParams {
                        width = RelativeLayout.LayoutParams.MATCH_PARENT
                    }
                    holder.setText(R.id.item_name, item.episodeTitle)
                    val name = holder.getView<TextView>(R.id.item_name)
                    val topImage = holder.getView<ImageView>(R.id.item_top_image)
                    val bottomImage = holder.getView<SVGAImageView>(R.id.item_bottom_image)

                    name.updateLayoutParams<RelativeLayout.LayoutParams> {
                        marginStart = if (getMenuColumn() == 2) {
                            addRule(RelativeLayout.ALIGN_PARENT_START, R.id.item_name)
                            SizeUtils.dp2px(15f)
                        } else {
                            addRule(RelativeLayout.CENTER_IN_PARENT, R.id.item_name)
                            0
                        }
                    }
                    if (item.isLast) {
                        topImage.visibility = View.VISIBLE
                    } else {
                        topImage.visibility = View.GONE
                    }
                    if (item.maintainStatus && item.isLive) {
                        bottomImage.isVisible = true
                        name.isSelected = false
                        if (item.uniqueID == current.uniqueID) {
                            name.isEnabled = false
                            holder.itemView.setBackgroundResource(R.drawable.red10_corner_2_bg)
                            bottomImage.setImageResource(R.mipmap.icon_live_error_selected)
                        } else {
                            name.isEnabled = true
                            holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                            bottomImage.setImageResource(R.mipmap.icon_live_error_normal)
                        }
                    } else if (item.uniqueID == current.uniqueID) {
                        holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                        holder.itemView.isSelected = true
                        name.isEnabled = true
                        bottomImage.visibility = View.VISIBLE
                        var animName = "playing_blue.svga"
                        name.isSelected = true
                        if (item.isLive) {
                            animName = "playing_live.svga"
                        }
                        SVGAParser(context).decodeFromAssets(animName, object : SVGAParser.ParseCompletion {
                            override fun onComplete(videoItem: SVGAVideoEntity) {
                                bottomImage.setImageDrawable(SVGADrawable(videoItem))
                                bottomImage.startAnimation()
                            }

                            override fun onError() {

                            }

                        })
                    } else {
                        holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                        name.isEnabled = true
                        holder.itemView.isSelected = false
                        bottomImage.visibility = View.GONE
                        name.isSelected = false
                    }
                }

            }
            groupAdapter.setOnItemClickListener { adapter, view, position ->
                dismiss()
                val data = adapter.getItem(position) as VideoBaseBean
                listener.onItemSelect(data)
            }
            group_recycler.adapter = groupAdapter
        } else {
            (groupAdapter as BaseQuickAdapter<VideoBaseBean, BaseViewHolder>).setNewInstance(itemList)
        }

        var playIndex = itemList.indexOfFirst { it.uniqueID == current.uniqueID }
        if (playIndex == -1) {
            playIndex = 0
        }
        group_recycler.post { group_recycler.scrollToPosition(playIndex) }
    }
}