package com.cqcsy.lgsp.views.emotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.VipEmotionGridViewAdapter
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.lgsp.utils.EmotionItemClickUtils
import com.cqcsy.lgsp.utils.EmotionUtils
import kotlinx.android.synthetic.main.layout_vip_emotion_tab_fragment.*

/**
 * Vip表情包 TabLayout子页面Fragment
 */
class VipEmotionTabFragment : BaseFragment() {
    var title = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_vip_emotion_tab_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = arguments?.getString("title") ?: ""
        initView()
    }

    private fun initView() {
        initEmotion()
        initListener()
    }

    /**
     * 初始化表情面板
     */
    private fun initEmotion() {
        // 获取屏幕宽度
        val screenWidth: Int = ScreenUtils.getScreenWidth()
        // item的宽度和高度
        val itemWidth = SizeUtils.dp2px(60f)
        val emotionViews: MutableList<GridView> = ArrayList()
        var emotionUrl: MutableList<String> = ArrayList()
        // 遍历所有的表情的key
        for (emojiName in EmotionUtils.vipEmotionMap[title]!!.keys) {
            emotionUrl.add(emojiName)
            // 每10个表情作为一组,同时添加到ViewPager对应的view集合中
            if (emotionUrl.size == 10) {
                val gv: GridView =
                    createEmotionGridView(emotionUrl, screenWidth, itemWidth)
                emotionViews.add(gv)
                // 添加完一组表情,重新创建一个表情名字集合
                emotionUrl = ArrayList()
            }
        }
        // 判断最后是否有不足10个表情的剩余情况
        if (emotionUrl.size > 0) {
            val gv: GridView =
                createEmotionGridView(emotionUrl, screenWidth, itemWidth)
            emotionViews.add(gv)
        }
        //初始化指示器
        vipEmotionIndicatorView.initIndicator(emotionViews.size)
        // 将多个GridView添加显示到ViewPager中
        vipEmotionTabPager.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view == `object`
            }

            override fun getCount(): Int {
                return emotionViews.size
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                container.addView(emotionViews[position])
                return emotionViews[position]
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(emotionViews[position])
            }
        }
    }

    private fun initListener() {
        vipEmotionTabPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var oldPagerPos = 0
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                vipEmotionIndicatorView.playByStartPointToNext(oldPagerPos, position)
                oldPagerPos = position
            }

        })
    }

    /**
     * 创建显示表情的GridView
     */
    private fun createEmotionGridView(
        emotionNames: MutableList<String>, gvWidth: Int, itemWidth: Int
    ): GridView { // 创建GridView
        val gv = GridView(activity)
        //设置点击背景透明
        gv.setSelector(R.color.transparent)
        //设置7列
        gv.numColumns = 5
        gv.setPadding(
            SizeUtils.dp2px(12f),
            SizeUtils.dp2px(15f),
            SizeUtils.dp2px(12f),
            0
        )
        gv.horizontalSpacing = SizeUtils.dp2px(13f)
        gv.verticalSpacing = SizeUtils.dp2px(10f)
        //设置GridView的宽高
        val params = ViewGroup.LayoutParams(gvWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        gv.layoutParams = params
        // 给GridView设置表情图片
        val adapter =
            VipEmotionGridViewAdapter(requireContext(), emotionNames, itemWidth)
        gv.adapter = adapter
        //设置全局点击事件
        gv.onItemClickListener = EmotionItemClickUtils.instance.getOnItemClickListener(
            requireContext()
        )
        return gv
    }
}