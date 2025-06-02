package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.VideoGroupBean
import com.donkingliang.groupedadapter.layoutmanger.GroupedGridLayoutManager
import com.littlejerk.rvdivider.builder.XGridBuilder
import kotlinx.android.synthetic.main.layout_video_expand_menu.*


/**
 * 分组选集菜单
 */
class VideoStickySelectDialog(context: Context) : VideoMenuDialog(context) {
    var listData: MutableList<VideoGroupBean>? = null

    var listener: OnItemSelectListener? = null
    var currentPlay: VideoBaseBean? = null
    var playIndex = -1

    interface OnItemSelectListener {
        fun onItemSelect(bean: VideoBaseBean?)
    }

    fun setItemClickListener(listener: OnItemSelectListener) {
        this.listener = listener
    }

    fun setData(list: MutableList<VideoGroupBean>?) {
        listData = list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_video_expand_menu)

        initList()
    }

    private fun initList() {
        if (listData == null || listData!!.isEmpty()) {
            return
        }
        val adapter = ExpandableAdapter(context, listData!!, currentPlay, getMenuColumn())
        adapter.setOnHeaderClickListener { adapter, holder, groupPosition ->
            val expandableAdapter = adapter as ExpandableAdapter
            if (expandableAdapter.isExpand(groupPosition)) {
                expandableAdapter.collapseGroup(groupPosition)
            } else {
                expandableAdapter.expandGroup(groupPosition)
            }
        }
        adapter.setOnChildClickListener { adapter, holder, groupPosition, childPosition ->
            val item = listData!![groupPosition].itemList?.get(childPosition) as VideoBaseBean
            if (item.uniqueID == currentPlay?.uniqueID) {
                return@setOnChildClickListener
            }
            dismiss()
            listener?.onItemSelect(item)
        }
        menuList.layoutManager = GroupedGridLayoutManager(context, getMenuColumn(), adapter)
        menuList.addItemDecoration(XGridBuilder(context).setVLineSpacing(10f).setHLineSpacing(10f).build())
        menuList.adapter = adapter
        val expandIndex = listData?.indexOfFirst { it.isExpand } ?: 0
        val groupIndex = adapter.getPositionForGroup(expandIndex)
        val childIndex = listData!![expandIndex].itemList?.indexOfFirst { it.uniqueID == currentPlay?.uniqueID }
        playIndex = childIndex?.let { adapter.getPositionForChild(groupIndex, it) } ?: -1
    }

    override fun show() {
        super.show()
        if (playIndex != -1) {
            menuList.scrollToPosition(playIndex)
        }
    }
}
