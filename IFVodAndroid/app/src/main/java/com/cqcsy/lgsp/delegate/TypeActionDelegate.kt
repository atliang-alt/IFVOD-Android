package com.cqcsy.lgsp.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.net.HomeNetBean
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.lgsp.event.RefreshTypeEvent
import com.cqcsy.lgsp.event.TabChangeEvent
import org.greenrobot.eventbus.EventBus

/**
 ** 2022/12/8
 ** des：分类下面的查看更多、换一换
 **/

class TypeActionDelegate : FullDelegate<HomeNetBean, TypeActionViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): TypeActionViewHolder {
        val view = inflater.inflate(R.layout.layout_type_action, parent, false)
        return TypeActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TypeActionViewHolder, item: HomeNetBean) {
        holder.moreContainer.isVisible = item.isMore
        holder.moreText.text = StringUtils.getString(R.string.more_type, item.name)
        holder.refreshContainer.isVisible = item.needRefresh
        holder.typeDivider.isVisible = item.isMore && item.needRefresh
        holder.moreContainer.setOnClickListener {
            val event = TabChangeEvent(0, item.type)
            EventBus.getDefault().post(event)
        }
        holder.refreshContainer.setOnClickListener {
            val event = RefreshTypeEvent()
            event.type = item.type
            event.imageView = holder.refreshImage
            EventBus.getDefault().post(event)
        }
    }
}

class TypeActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val moreContainer: View = view.findViewById(R.id.more_container)
    val refreshContainer: View = view.findViewById(R.id.refresh_container)
    val typeDivider: View = view.findViewById(R.id.type_divider)
    val moreText: TextView = view.findViewById(R.id.more_text)
    val refreshImage: ImageView = view.findViewById(R.id.refresh_image)
}