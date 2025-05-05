package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.utils.Constant
import kotlinx.android.synthetic.main.layout_video_setting.*

/**
 * 设置
 */
class SettingDialog(context: Context) : VideoMenuDialog(context) {
    var callBack: CallBack? = null
    var isAllowSkip = true

    interface CallBack {
        fun onDownload()
        fun onScreenShareClick()
        fun onAutoSkipHeaderAndTail(state: Boolean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_video_setting)

        skipTail.visibility = if (isAllowSkip) View.VISIBLE else View.GONE
        skipState.isChecked = SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_SKIP, true)

        download.setOnClickListener {
            callBack?.onDownload()
        }
        shareScreen.setOnClickListener {
            callBack?.onScreenShareClick()
        }
        skipState.setOnClickListener {
            callBack?.onAutoSkipHeaderAndTail(skipState.isChecked)
        }
    }
}