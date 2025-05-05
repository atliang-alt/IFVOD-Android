package com.cqcsy.lgsp.delegate

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.net.PageFilterBean
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.lgsp.delegate.util.HorizontalRecyclerViewHolder
import com.cqcsy.lgsp.search.CategoryFilterActivity
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XLinearBuilder

/**
 ** 2022/12/6
 ** des：页面中选项（如电影、电视剧banner下面）
 **/

class PageFilterDelegate : FullDelegate<ArrayList<PageFilterBean>, HorizontalRecyclerViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): HorizontalRecyclerViewHolder {
        val view = inflater.inflate(R.layout.layout_only_recyclerview, parent, false)
        val holder = HorizontalRecyclerViewHolder(view)
        val layoutManager = LinearLayoutManager(parent.context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        holder.recyclerView.layoutManager = layoutManager
        holder.recyclerView.setPadding(SizeUtils.dp2px(12f), SizeUtils.dp2px(10f), SizeUtils.dp2px(12f), SizeUtils.dp2px(15f))
        holder.recyclerView.addItemDecoration(XLinearBuilder(parent.context).setSpacing(15f).build())
        return holder
    }

    override fun onBindViewHolder(pageHolder: HorizontalRecyclerViewHolder, item: ArrayList<PageFilterBean>) {
        if (item.isEmpty()) {
            return
        }
        val size = item.size
        val params = ConstraintLayout.LayoutParams(
            (ScreenUtils.getAppScreenWidth() - SizeUtils.dp2px((size - 1) * 15f + 24f)) / size,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        val adapter = object : BaseQuickAdapter<PageFilterBean, BaseViewHolder>(R.layout.layout_page_filter, item) {
            override fun convert(holder: BaseViewHolder, item: PageFilterBean) {
                holder.itemView.layoutParams = params
                holder.setText(R.id.item_name, item.title)
                ImageUtil.loadImage(
                    holder.itemView.context,
                    item.normalIcon,
                    holder.getView(R.id.image),
                    resize = false
                )
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            val context = pageHolder.itemView.context
            val item = adapter.getItem(position) as PageFilterBean
            val idsArray = item.ids?.split(",")
            val intent = Intent(context, CategoryFilterActivity::class.java)
            intent.putExtra("sortId", idsArray?.get(0) ?: 0)
            intent.putExtra("classifyId", idsArray?.get(0) ?: 0)
            intent.putExtra("categoryName", item.name)
            intent.putExtra("categoryId", item.type.toString())
            context.startActivity(intent)
        }
        pageHolder.recyclerView.adapter = adapter
    }

}