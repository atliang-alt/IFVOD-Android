package com.cqcsy.lgsp.views

import android.content.Context
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R
import com.scwang.smartrefresh.layout.header.ClassicsHeader

/**
 * 自定义下拉刷新header
 */
class RefreshHeader : ClassicsHeader {

    constructor(context: Context) : super(context) {
        mTextRefreshing = resources.getString(R.string.refresh_tip)
        mTextLoading = resources.getString(R.string.refresh_tip)

        mTextFinish = resources.getString(R.string.refresh_success)
        mTextFailed = resources.getString(R.string.refresh_failed)
        mTextRelease = resources.getString(R.string.refresh_release)
        mTextPulling = resources.getString(R.string.refresh_pulling)
        mTextSecondary = resources.getString(R.string.refresh_secondary)
        mTextUpdate = resources.getString(R.string.refresh_update)
        setAccentColor(ColorUtils.getColor(R.color.word_color_6))
        setBackgroundColor(ColorUtils.getColor(R.color.background_1))
    }
}