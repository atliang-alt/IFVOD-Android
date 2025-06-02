package com.cqcsy.lgsp.views

import android.content.Context
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R
import com.scwang.smartrefresh.layout.footer.ClassicsFooter

/**
 * 自定义下拉刷新footer
 */
class RefreshFooter : ClassicsFooter {

    constructor(context: Context) : super(context) {
        mTextRefreshing = resources.getString(R.string.refresh_tip)
        mTextLoading = resources.getString(R.string.refresh_tip)
        mTextNothing = resources.getString(R.string.load_tip)
        mTextRelease = resources.getString(R.string.load_release)
        mTextFinish = resources.getString(R.string.load_success)
        mTextFailed = resources.getString(R.string.load_failed)
        mTextPulling = resources.getString(R.string.load_pulling)
        setAccentColor(ColorUtils.getColor(R.color.word_color_6))
        setBackgroundColor(ColorUtils.getColor(R.color.background_1))

    }
}