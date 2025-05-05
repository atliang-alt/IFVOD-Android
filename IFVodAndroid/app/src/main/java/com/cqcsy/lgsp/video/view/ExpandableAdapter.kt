package com.cqcsy.lgsp.video.view

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.VideoGroupBean
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

/**
 * 可展开收起的Adapter。他跟普通的[GroupedListAdapter]基本是一样的。
 * 它只是利用了[GroupedRecyclerViewAdapter]的
 * 删除一组里的所有子项[GroupedRecyclerViewAdapter.notifyChildrenRemoved]} 和
 * 插入一组里的所有子项[GroupedRecyclerViewAdapter.notifyChildrenInserted]
 * 两个方法达到列表的展开和收起的效果。
 * 这种列表类似于[ExpandableListView]的效果。
 * 这里我把列表的组尾去掉是为了效果上更像ExpandableListView。
 */
class ExpandableAdapter(val context: Context, val groups: MutableList<VideoGroupBean>, var currentPlay: VideoBaseBean?, val numberOfColum: Int) :
    GroupedRecyclerViewAdapter(context) {

    private var svgaParser: SVGAParser = SVGAParser(context)

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int { //如果当前组收起，就直接返回0，否则才返回子项数。这是实现列表展开和收起的关键。
        if (!isExpand(groupPosition)) {
            return 0
        }
        val children = groups[groupPosition].itemList
        return children?.size ?: 0
    }

    override fun hasHeader(groupPosition: Int): Boolean {
        return true
    }

    override fun hasFooter(groupPosition: Int): Boolean {
        return false
    }

    override fun getHeaderLayout(viewType: Int): Int {
        return R.layout.layout_sticky_select_header
    }

    override fun getFooterLayout(viewType: Int): Int {
        return 0
    }

    override fun getChildLayout(viewType: Int): Int {
        return R.layout.layout_video_menu_item
    }

    override fun onBindHeaderViewHolder(holder: BaseViewHolder, groupPosition: Int) {
        val entity: VideoGroupBean = groups[groupPosition]
        holder.setText(R.id.header_name, entity.groupName)
        val ivState = holder.get<ImageView>(R.id.arrow)
        if (entity.isExpand) {
            ivState.rotation = 90f
        } else {
            ivState.rotation = 0f
        }
    }

    override fun onBindFooterViewHolder(holder: BaseViewHolder, groupPosition: Int) {
    }

    override fun onBindChildViewHolder(holder: BaseViewHolder, groupPosition: Int, childPosition: Int) {
        holder.get<RelativeLayout>(R.id.item_root).updateLayoutParams {
            width = RelativeLayout.LayoutParams.MATCH_PARENT
        }
        val item: VideoBaseBean = groups[groupPosition].itemList?.get(childPosition)!!
        holder.setText(R.id.item_name, item.episodeTitle)
        val name = holder.get<TextView>(R.id.item_name)
        val topImage = holder.get<ImageView>(R.id.item_top_image)
        val bottomImage = holder.get<SVGAImageView>(R.id.item_bottom_image)

        name.updateLayoutParams<RelativeLayout.LayoutParams> {
            marginStart = if (numberOfColum == 2) {
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
            if (item.uniqueID == currentPlay?.uniqueID) {
                name.isEnabled = false
                holder.itemView.setBackgroundResource(R.drawable.red10_corner_2_bg)
                bottomImage.setImageResource(R.mipmap.icon_live_error_selected)
            } else {
                name.isEnabled = true
                holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
                bottomImage.setImageResource(R.mipmap.icon_live_error_normal)
            }
        } else if (item.uniqueID == currentPlay?.uniqueID) {
            holder.itemView.setBackgroundResource(R.drawable.video_menu_item_bg_corner_2)
            name.isEnabled = true
            holder.itemView.isSelected = true
            bottomImage.visibility = View.VISIBLE
            var animName = "playing_blue.svga"
            name.isSelected = true
            if (item.isLive) {
                animName = "playing_live.svga"
            }
            svgaParser.decodeFromAssets(animName, object : SVGAParser.ParseCompletion {
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

    /**
     * 判断当前组是否展开
     *
     * @param groupPosition
     * @return
     */
    fun isExpand(groupPosition: Int): Boolean {
        val entity: VideoGroupBean = groups[groupPosition]
        return entity.isExpand
    }
    /**
     * 展开一个组
     *
     * @param groupPosition
     * @param animate
     */
    /**
     * 展开一个组
     *
     * @param groupPosition
     */
    @JvmOverloads
    fun expandGroup(groupPosition: Int, animate: Boolean = false) {
        val entity: VideoGroupBean = groups[groupPosition]
        entity.isExpand = true
        if (animate) {
            notifyChildrenInserted(groupPosition)
        } else {
            notifyDataChanged()
        }
    }
    /**
     * 收起一个组
     *
     * @param groupPosition
     * @param animate
     */
    /**
     * 收起一个组
     *
     * @param groupPosition
     */
    @JvmOverloads
    fun collapseGroup(groupPosition: Int, animate: Boolean = false) {
        val entity: VideoGroupBean = groups[groupPosition]
        entity.isExpand = false
        if (animate) {
            notifyChildrenRemoved(groupPosition)
        } else {
            notifyDataChanged()
        }
    }
}