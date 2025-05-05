package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity

/**
 * 帮助中心
 */
class HelpCenterActivity : WebViewActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LayoutInflater.from(this).inflate(R.layout.layout_bottom_help_center, null)
        view.setOnClickListener {
            startActivity(Intent(this, FeedBackActivity::class.java))
        }
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(50f))
        mBottomContainer.addView(view, params)
    }
}