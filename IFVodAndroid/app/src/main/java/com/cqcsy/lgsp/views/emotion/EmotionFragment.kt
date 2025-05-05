package com.cqcsy.lgsp.views.emotion

import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.EmotionGridViewAdapter
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.lgsp.utils.EmotionUtils
import com.cqcsy.lgsp.utils.EmotionItemClickUtils
import kotlinx.android.synthetic.main.layout_emotion_fragment.*
import java.util.*

/**
 * 普通表情页面fragment
 */
class EmotionFragment : NormalFragment() {
    override fun getContainerView(): Int {
        return R.layout.layout_emotion_fragment
    }

    override fun initView() {
        initEmotion()
        initListener()
    }

    /**
     * 初始化表情面板
     * 思路：获取表情的总数，按每行存放7个表情，动态计算出每个表情所占的宽度大小（包含间距），
     * 而每个表情的高与宽应该是相等的，这里我们约定只存放4行
     * 每个面板最多存放7*3=21个表情，再减去一个删除键，即每个面板包含21个表情
     * 根据表情总数，循环创建多个容量为20的List，存放表情，对于大小不满21进行特殊
     * 处理即可。
     */
    private fun initEmotion() {
        // 获取屏幕宽度
        val screenWidth: Int = ScreenUtils.getScreenWidth()
        // item的宽度和高度
        val itemWidth = SizeUtils.dp2px(32f)
        val emotionViews: MutableList<GridView> = ArrayList()
        var emotionNames: MutableList<String> =
            ArrayList()
        // 遍历所有的表情的key
        for (emojiName in EmotionUtils.emotionMap.keys) {
            emotionNames.add(emojiName)
            // 每20个表情作为一组,同时添加到ViewPager对应的view集合中
            if (emotionNames.size == 20) {
                val gv: GridView =
                    createEmotionGridView(emotionNames, screenWidth, itemWidth)
                emotionViews.add(gv)
                // 添加完一组表情,重新创建一个表情名字集合
                emotionNames = ArrayList()
            }
        }
        // 判断最后是否有不足27个表情的剩余情况
        if (emotionNames.size > 0) {
            val gv: GridView =
                createEmotionGridView(emotionNames, screenWidth, itemWidth)
            emotionViews.add(gv)
        }
        //初始化指示器
        emotionIndicatorView.initIndicator(emotionViews.size)
        // 将多个GridView添加显示到ViewPager中
        emotionPager.adapter = object : PagerAdapter() {
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
        emotionPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            var oldPagerPos = 0
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                emotionIndicatorView.playByStartPointToNext(oldPagerPos, position)
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
        gv.numColumns = 7
        gv.setPadding(
            SizeUtils.dp2px(20f),
            SizeUtils.dp2px(15f),
            SizeUtils.dp2px(20f),
            0
        )
        gv.horizontalSpacing = SizeUtils.dp2px(19f)
        gv.verticalSpacing = SizeUtils.dp2px(15f)
        //设置GridView的宽高
        val params = ViewGroup.LayoutParams(gvWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        gv.layoutParams = params
        // 给GridView设置表情图片
        val adapter =
            EmotionGridViewAdapter(requireContext(), emotionNames, itemWidth)
        gv.adapter = adapter
        //设置全局点击事件
        gv.onItemClickListener = EmotionItemClickUtils.instance.getOnItemClickListener(
            requireActivity()
        )
        return gv
    }
}