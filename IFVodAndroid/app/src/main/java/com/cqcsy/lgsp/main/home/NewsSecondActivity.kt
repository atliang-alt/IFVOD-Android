package com.cqcsy.lgsp.main.home

import android.os.Bundle
import android.view.KeyEvent
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager

/**
 * 新闻、华人、游戏等小视频的二级标题页面
 */
class NewsSecondActivity : NormalActivity() {
    private var titleId: String = ""
    private var subId: String = ""
    private var subTitle: String = ""

    override fun getContainerView(): Int {
        return R.layout.activity_news_second
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        subTitle = intent.getStringExtra("subTitle") ?: ""
        subId = intent.getStringExtra("subId") ?: ""
        titleId = intent.getStringExtra("titleId") ?: ""
    }

    private fun initView() {
        setHeaderTitle(subTitle)
        val bundle = Bundle()
        bundle.putString("titleId", titleId)
        bundle.putString("subId", subId)
        val fragment = NewsSecondFragment()
        fragment.arguments = bundle
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commitAllowingStateLoss()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && GSYVideoManager.backFromWindowFull(this)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}