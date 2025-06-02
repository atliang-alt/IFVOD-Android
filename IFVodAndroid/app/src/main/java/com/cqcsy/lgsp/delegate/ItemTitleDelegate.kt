package com.cqcsy.lgsp.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ItemTitleBean
import com.cqcsy.lgsp.delegate.util.FullDelegate

/**
 ** 2022/12/6
 ** des：每一项title，可点击或不能点击
 **/

class ItemTitleDelegate : FullDelegate<ItemTitleBean, ItemTitleViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ItemTitleViewHolder {
        val view = inflater.inflate(R.layout.layout_item_title, parent, false)
        return ItemTitleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemTitleViewHolder, item: ItemTitleBean) {
        holder.name.text = item.itemName
        holder.itemView.setOnClickListener { item.action?.invoke() }
        if (item.action == null) {
            holder.arrow.visibility = View.GONE
        } else {
            holder.arrow.visibility = View.VISIBLE
        }
    }
}

class ItemTitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.item_name)
    val arrow: ImageView = view.findViewById(R.id.item_arrow)
}