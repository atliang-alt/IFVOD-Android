package com.cqcsy.library.pay.area

import android.content.Context
import com.cqcsy.library.R
import com.cqcsy.library.bean.AreaGroupBean
import com.cqcsy.library.pay.model.ProvinceBean
import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter
import com.donkingliang.groupedadapter.holder.BaseViewHolder

class ProvinceSelectAdapter(context: Context) : GroupedRecyclerViewAdapter(context) {

    var groups: MutableList<AreaGroupBean> = arrayListOf()
        private set

    fun setList(groups: MutableList<AreaGroupBean>?) {
        if (groups === this.groups) {
            return
        }
        this.groups = groups ?: arrayListOf()
        notifyDataChanged()
    }

    override fun getGroupCount(): Int {
        return groups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val children = groups[groupPosition].provinces
        return children?.size ?: 0
    }

    override fun hasHeader(groupPosition: Int): Boolean {
        return true
    }

    override fun hasFooter(groupPosition: Int): Boolean {
        return false
    }

    override fun getHeaderLayout(viewType: Int): Int {
        return R.layout.layout_area_header
    }

    override fun getFooterLayout(viewType: Int): Int {
        return 0
    }

    override fun getChildLayout(viewType: Int): Int {
        return R.layout.layout_area_item
    }

    override fun onBindHeaderViewHolder(
        holder: BaseViewHolder,
        groupPosition: Int
    ) {
        val entity: AreaGroupBean = groups[groupPosition]
        holder.setText(R.id.group_name, entity.letter)
    }

    override fun onBindFooterViewHolder(
        holder: BaseViewHolder,
        groupPosition: Int
    ) {
    }

    override fun onBindChildViewHolder(
        holder: BaseViewHolder,
        groupPosition: Int,
        childPosition: Int
    ) {
        val item: ProvinceBean = groups[groupPosition].provinces?.get(childPosition)!!
        holder.setText(R.id.area_name, item.stateName)
        holder.setVisible(R.id.area_code, true)
        holder.setText(R.id.area_code, item.stateCode)
    }
}