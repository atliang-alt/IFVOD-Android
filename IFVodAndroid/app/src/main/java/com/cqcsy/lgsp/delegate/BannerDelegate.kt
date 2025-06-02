package com.cqcsy.lgsp.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.banner.BannerViewAdapter
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.delegate.util.FullDelegate
import com.cqcsy.lgsp.delegate.util.ListWrapper
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.Constant
import com.youth.banner.Banner
import com.youth.banner.indicator.RectangleIndicator
import com.youth.banner.listener.OnPageChangeListener

/**
 ** 2022/12/6
 ** des：banner
 **/

class BannerDelegate(val owner: LifecycleOwner) : FullDelegate<ListWrapper, ImageHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ImageHolder {
        val view = inflater.inflate(R.layout.layout_banner, parent, false)
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, item: ListWrapper) {
        if (item.data.isNullOrEmpty() || (item.data!![0] !is AdvertBean && item.data!![0] !is MovieModuleBean)) {
            return
        }
        if (item.data!![0] is AdvertBean) {
            setAdvertInfo(holder, item.data as MutableList<AdvertBean>)
        } else {
            setVideoInfo(holder, item.data as MutableList<MovieModuleBean>)
        }
    }

    private fun setAdvertInfo(holder: ImageHolder, list: MutableList<AdvertBean>) {
        holder.banner.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val item = list[position]
                setInfo(holder, item)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })//添加切换监听
        if (holder.banner.tag == list) {
            holder.banner.start()
            return
        }
        val adapter = BannerViewAdapter(list, holder.itemView.context)
        holder.banner.addBannerLifecycleObserver(owner)
        holder.banner.setIndicator(RectangleIndicator(holder.itemView.context), false)
        holder.indicator.removeAllViews()
        holder.indicator.addView(holder.banner.indicator.indicatorView)
        holder.banner.setLoopTime(Constant.DELAY_TIME)
        holder.banner.setAdapter(adapter)
        setInfo(holder, list[0])
        val position = if (holder.banner.tag is Int) holder.banner.tag as Int else 1
        holder.banner.currentItem = position
        holder.banner.tag = list
    }

    private fun setInfo(holder: ImageHolder, item: AdvertBean) {
        holder.title.text = item.title
        if (item.playtime.isNullOrEmpty()) {
            holder.bannerTime.isVisible = false
        } else {
            val start = TimesUtils.utc2Local(item.playtime!!, "yyyy年MM月dd日 HH:mm")
//            val end = item.endtime?.let { TimesUtils.utc2Local(it, "yyyy年MM月dd日 HH:mm") }
            holder.bannerTime.isVisible = true
            holder.bannerTime.text = StringUtils.getString(R.string.live_start_time, start)
        }
    }

    private fun setVideoInfo(holder: ImageHolder, list: MutableList<MovieModuleBean>) {
        val data = ArrayList<AdvertBean>()
        for (item in list) {
            val advertBean = AdvertBean()
            advertBean.showURL = item.coverImgUrl.toString()
            advertBean.mediaItem = item
            advertBean.title = item.title
            data.add(advertBean)
        }
        setAdvertInfo(holder, data)
    }
}

class ImageHolder(view: View) : RecyclerView.ViewHolder(view) {
    val banner: Banner<AdvertBean, BannerViewAdapter> = view.findViewById(R.id.banner)
    val indicator: LinearLayout = view.findViewById(R.id.banner_indicator)
    val title: TextView = view.findViewById(R.id.banner_title)
    val bannerTime: TextView = view.findViewById(R.id.banner_time)
}