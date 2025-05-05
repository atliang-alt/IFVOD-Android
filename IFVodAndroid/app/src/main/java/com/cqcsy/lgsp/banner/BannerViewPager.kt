package com.cqcsy.lgsp.banner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.library.utils.Constant
import com.youth.banner.Banner
import com.youth.banner.indicator.RectangleIndicator
import com.youth.banner.listener.OnPageChangeListener
import kotlinx.android.synthetic.main.layout_banner_view.view.*
import kotlinx.android.synthetic.main.layout_introduction.adBanner

@SuppressLint("ViewConstructor")
class BannerViewPager(val mContext: Context, val bannerData: MutableList<AdvertBean>) :
    FrameLayout(mContext), OnPageChangeListener {
    private var mAdapter: BannerViewAdapter? = null

    init {
        initView()
    }

    /**
     * 初始化View
     */
    private fun initView() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_banner_view, this, true)
        val tempBanner: Banner<AdvertBean, BannerViewAdapter> = banner as Banner<AdvertBean, BannerViewAdapter>
        mAdapter = BannerViewAdapter(bannerData, mContext)
        tempBanner.setLoopTime(Constant.DELAY_TIME)
        tempBanner.setIndicator(RectangleIndicator(context), false)
        bannerIndicator.addView(tempBanner.indicator.indicatorView)
        if (bannerData.isNotEmpty()) {
            bannerTitle.text = bannerData[0].title
        } else {
            titleLayout.setBackgroundColor(
                ColorUtils.getColor(R.color.transparent)
            )
        }
        tempBanner.addOnPageChangeListener(this)//添加切换监听
        tempBanner.setAdapter(mAdapter)
    }

    /**
     * 停止滚动
     */
    fun stopCycle() {
        banner.stop()
    }

    /**
     * 停止滚动
     */
    fun startCycle() {
        banner.start()
    }

    /**
     * 设置当前位置
     */
    fun setCurrentItem(position: Int) {
        banner.currentItem = position
    }

    /**
     * 设置banner生命周期
     */
    fun setBannerLifecycle(owner: LifecycleOwner) {
        banner.addBannerLifecycleObserver(owner)
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        bannerTitle.text = bannerData[position].title
    }

}