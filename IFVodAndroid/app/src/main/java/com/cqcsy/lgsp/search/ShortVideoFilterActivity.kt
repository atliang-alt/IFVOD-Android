package com.cqcsy.lgsp.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import kotlinx.android.synthetic.main.activity_fliter_short_video.*

/**
 * 分类筛选页面 小视频
 */
class ShortVideoFilterActivity : NormalActivity() {
    var categoryId = ""
    var classifyName = ""
    var subId = ""

    private var comprehensivePosition = 0
    private var playMorePosition = 1
    private var releaseMorePosition = 2

    // 综合
    private var comprehensiveFragment: ShortVideoFilterFragment? = null

    // 播放多
    private var playMoreFragment: ShortVideoFilterFragment? = null

    // 发布多
    private var releaseMoreFragment: ShortVideoFilterFragment? = null
    override fun getContainerView(): Int {
        return R.layout.activity_fliter_short_video
    }

    override fun onViewCreate() {
        super.onViewCreate()
        initView()
        showFragment(comprehensivePosition)
    }

    private fun initView() {
        categoryId = intent.getStringExtra("categoryId") ?: ""
        classifyName = intent.getStringExtra("classifyName") ?: ""
        subId = intent.getStringExtra("subId") ?: ""
        setHeaderTitle(classifyName)
        setRightImageVisible(View.VISIBLE)
        filterComprehensive.isSelected = true
        filterComprehensive.setTextColor(ColorUtils.getColor(R.color.blue))
        filterComprehensive.setOnClickListener {
            if (!filterComprehensive.isSelected) {
                filterComprehensive.isSelected = true
                filterPlayMore.isSelected = false
                filterNewRelease.isSelected = false
                filterComprehensive.setTextColor(ColorUtils.getColor(R.color.blue))
                filterPlayMore.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                filterNewRelease.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                showFragment(comprehensivePosition)
            }
        }
        filterPlayMore.setOnClickListener {
            if (!filterPlayMore.isSelected) {
                filterComprehensive.isSelected = false
                filterPlayMore.isSelected = true
                filterNewRelease.isSelected = false
                filterComprehensive.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                filterPlayMore.setTextColor(ColorUtils.getColor(R.color.blue))
                filterNewRelease.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                showFragment(playMorePosition)
            }
        }
        filterNewRelease.setOnClickListener {
            if (!filterNewRelease.isSelected) {
                filterComprehensive.isSelected = false
                filterPlayMore.isSelected = false
                filterNewRelease.isSelected = true
                filterComprehensive.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                filterPlayMore.setTextColor(
                    ColorUtils.getColor(R.color.word_color_3)
                )
                filterNewRelease.setTextColor(ColorUtils.getColor(R.color.blue))
                showFragment(releaseMorePosition)
            }
        }
    }

    private fun showFragment(position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        hideFragment(transaction)
        val bundle = Bundle()
        bundle.putString("categoryId", categoryId)
        bundle.putString("subId", subId)
        bundle.putInt("filterId", position)
        when (position) {
            comprehensivePosition -> {
                if (comprehensiveFragment == null) {
                    comprehensiveFragment = ShortVideoFilterFragment()
                    comprehensiveFragment!!.arguments = bundle
                    transaction.add(R.id.filterContent, comprehensiveFragment!!)
                } else {
                    transaction.show(comprehensiveFragment!!)
                }
            }
            playMorePosition -> {
                if (playMoreFragment == null) {
                    playMoreFragment = ShortVideoFilterFragment()
                    playMoreFragment!!.arguments = bundle
                    transaction.add(R.id.filterContent, playMoreFragment!!)
                } else {
                    transaction.show(playMoreFragment!!)
                }
            }
            releaseMorePosition -> {
                if (releaseMoreFragment == null) {
                    releaseMoreFragment = ShortVideoFilterFragment()
                    releaseMoreFragment!!.arguments = bundle
                    transaction.add(R.id.filterContent, releaseMoreFragment!!)
                } else {
                    transaction.show(releaseMoreFragment!!)
                }
            }
        }
        transaction.commitAllowingStateLoss()
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        if (comprehensiveFragment != null) {
            transaction.hide(comprehensiveFragment!!)
        }
        if (playMoreFragment != null) {
            transaction.hide(playMoreFragment!!)
        }
        if (releaseMoreFragment != null) {
            transaction.hide(releaseMoreFragment!!)
        }
    }

    override fun onRightClick(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }
}