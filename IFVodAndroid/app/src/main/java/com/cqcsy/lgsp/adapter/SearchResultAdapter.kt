package com.cqcsy.lgsp.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.SearchResultBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import kotlinx.android.synthetic.main.search_result_teleplay_total.view.*

/**
 * 搜索结果适配器
 */
class SearchResultAdapter(var listener: OnClickListener) :
    BaseQuickAdapter<SearchResultBean, BaseViewHolder>(R.layout.item_search_result, ArrayList()) {

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseViewHolder, item: SearchResultBean) {
        // 电影、电视剧、综艺布局
        item.coverImgUrl?.let {
            ImageUtil.loadImage(context, it, holder.getView(R.id.searchResultImage))
        }
        holder.setText(R.id.searchResultTitle, item.title)
        holder.setText(R.id.searchResultYear, TimesUtils.getYear(item.postTime))
        holder.setText(R.id.searchResultType, item.mediaType)
        val stringBuffer = StringBuffer()
        if (!item.contentType.isNullOrEmpty()) {
            stringBuffer.append(item.contentType?.replace(",", "·"))
        }
        if (!item.regional.isNullOrEmpty()) {
            stringBuffer.append("·" + item.regional)
        }
        if (!item.lang.isNullOrEmpty()) {
            stringBuffer.append("·" + item.lang)
        }
        holder.setText(R.id.searchResultClassify, stringBuffer)
        val actor =
            StringUtils.getString(R.string.actor) + "：" + if (item.actor.isNullOrEmpty()) {
                StringUtils.getString(R.string.unknown)
            } else {
                NormalUtil.formatActorName(item.actor)
            }
        holder.setText(R.id.searchResultActor, actor)
        if (item.updateCount > 0) {
            holder.setVisible(R.id.searchResultUpdateCount, true)
            if (item.videoType == Constant.VIDEO_MOVIE) {
                holder.setText(R.id.searchResultUpdateCount, StringUtils.getString(R.string.newTips))
            } else {
                holder.setText(R.id.searchResultUpdateCount, item.updateCount.toString())
            }
            holder.setBackgroundResource(R.id.searchResultUpdateCount, R.drawable.red_rectangle_bg)
        } else {
            holder.setVisible(R.id.searchResultUpdateCount, false)
        }
        // 是否显示VIP按钮
//        if (item.isVip) {
//            holder.setVisible(R.id.searchResultVipImage, true)
//        } else {
//            holder.setVisible(R.id.searchResultVipImage, false)
//        }
        holder.setGone(R.id.download_content, item.videoType >= Constant.VIDEO_LIVE)
        if (item.videoType == Constant.VIDEO_MOVIE) {
            // 电影没有集数，有导演
            holder.setGone(R.id.searchResultMore, true)
            holder.setGone(R.id.searchResultTotalLayout, true)
            holder.setGone(R.id.searchResultDirector, false)
            val director =
                StringUtils.getString(R.string.director) + "：" + if (item.director.isNullOrEmpty()) {
                    StringUtils.getString(R.string.unknown)
                } else {
                    item.director
                }
            holder.setText(R.id.searchResultDirector, director)
        } else {
            holder.setGone(R.id.searchResultTotalLayout, false)
            holder.setGone(R.id.searchResultDirector, true)
            if (!item.episodes.isNullOrEmpty()) {
                addTotalLayout(item, item.episodes!!, item.videoType, holder)
            }
        }
        holder.getView<TextView>(R.id.videoPlayer).setOnClickListener { view: View? ->
            listener.onItemClick(0, view, item)
        }
        holder.itemView.setOnClickListener { view: View? ->
            listener.onItemClick(0, view, item)
        }
        holder.getView<LinearLayout>(R.id.searchResultMore).setOnClickListener { view: View? ->
            listener.onItemClick(2, view, item)
        }
        holder.getView<View>(R.id.download_content).setOnClickListener(object : ClickUtils.OnDebouncingClickListener() {
            override fun onDebouncingClick(view: View) {
                listener.onItemClick(3, view, item)
            }
        })
    }

    /**
     * 动态添加期数或集数布局
     */
    private fun addTotalLayout(searchResultBean: SearchResultBean, data: MutableList<VideoBaseBean>, type: Int, holder: BaseViewHolder) {
        val linearLayout: LinearLayout = holder.getView(R.id.searchResultTotalLayout)
        linearLayout.removeAllViews()
        val inflater = LayoutInflater.from(context)
        var viewType = type
        if (viewType == Constant.VIDEO_TELEPLAY) {
            val bean = data.filter { (it.title?.length ?: 0) > 5 }
            // 电视剧类型，选集标题长度大于5，显示样式为综艺样式布局
            viewType = if (bean.isNullOrEmpty()) {
                Constant.VIDEO_TELEPLAY
            } else {
                Constant.VIDEO_VARIETY
            }
        }
        if (viewType == Constant.VIDEO_TELEPLAY) {
            // 电视剧
            val itemWidth = (ScreenUtils.getScreenWidth()
                    - SizeUtils.dp2px(12f) * 2
                    - SizeUtils.dp2px(10f) * 5) / 6
            val sonLayout = LinearLayout(context)
            for (i in data.indices) {
                val lp = LinearLayout.LayoutParams(itemWidth, SizeUtils.dp2px(50f))
                val view = inflater.inflate(R.layout.search_result_teleplay_total, linearLayout, false)
                view.searchResultTotalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                view.searchResultTotalText.text = data[i].title
                lp.topMargin = SizeUtils.dp2px(5f)
                if (i != data.size) {
                    lp.rightMargin = SizeUtils.dp2px(10f)
                }
                if (data[i].isLast) {
                    view.findViewById<ImageView>(R.id.isNew).visibility = View.VISIBLE
                } else {
                    view.findViewById<ImageView>(R.id.isNew).visibility = View.GONE
                }
                view.layoutParams = lp
                view.setOnClickListener {
                    if (data[i].episodeId == -1) {
                        // 更多选集点击
                        listener.onItemClick(2, view, searchResultBean)
                    } else {
                        searchResultBean.mediaUrl = data[i].mediaUrl
                        searchResultBean.episodeId = data[i].episodeId
                        searchResultBean.uniqueID = data[i].uniqueID
                        searchResultBean.episodeKey = data[i].episodeKey
                        searchResultBean.episodeTitle = data[i].episodeTitle
                        listener.onItemClick(1, view, searchResultBean)
                    }
                }
                sonLayout.addView(view)
            }
            linearLayout.addView(sonLayout)
            // 电视剧没有查看更多布局
            holder.setGone(R.id.searchResultMore, true)
            return
        }
        // 电视剧类型但是显示综艺样式，需要移除两个数据
        if (data.size >= 6) {
            data.removeAt(2)
            data.removeAt(4)
        }
        // 综艺、纪录片
        var sonLayout: LinearLayout? = null
        for (i in data.indices) {
            val itemWidth = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(12f) * 2 - SizeUtils.dp2px(5f)) / 2
            val lp = LinearLayout.LayoutParams(itemWidth, SizeUtils.dp2px(37f))
            val view = inflater.inflate(R.layout.search_result_teleplay_total, linearLayout, false)
            view.searchResultTotalText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            view.searchResultTotalText.text = data[i].title
            if (i != data.size) {
                lp.rightMargin = SizeUtils.dp2px(5f)
            }
            lp.topMargin = SizeUtils.dp2px(5f)
            view.layoutParams = lp
            view.setOnClickListener {
                searchResultBean.mediaUrl = data[i].mediaUrl
                searchResultBean.episodeId = data[i].episodeId
                searchResultBean.uniqueID = data[i].uniqueID
                searchResultBean.episodeKey = data[i].episodeKey
                searchResultBean.episodeTitle = data[i].episodeTitle
                listener.onItemClick(1, view, searchResultBean)
            }
            if (data[i].isLast) {
                view.findViewById<ImageView>(R.id.isNew).visibility = View.VISIBLE
            } else {
                view.findViewById<ImageView>(R.id.isNew).visibility = View.GONE
            }
            if (i % 2 == 0) {
                sonLayout = LinearLayout(context)
                // 先添加横向布局layout
                sonLayout.addView(view)
            } else {
                // 先添加横向布局layout
                sonLayout?.addView(view)
                linearLayout.addView(sonLayout)
            }
            // 数据是基数需要单独添加最后一个
            if (data.size % 2 != 0 && i == (data.size - 1)) {
                linearLayout.addView(sonLayout)
            }
        }
        holder.setGone(R.id.searchResultMore, false)
    }

    /**
     * 点击事件接口
     * type 0:播放点击 1:具体的选集点击 2:所有选集点击或查看更多 3:下载点击
     */
    interface OnClickListener {
        fun onItemClick(type: Int, view: View?, searchResultBean: SearchResultBean)
    }
}