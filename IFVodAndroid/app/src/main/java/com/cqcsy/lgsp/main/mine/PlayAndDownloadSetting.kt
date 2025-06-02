package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.Constant
import kotlinx.android.synthetic.main.activity_play_and_download.*

/**
 * 播放下载设置
 */
class PlayAndDownloadSetting : NormalActivity(), View.OnClickListener {

    override fun getContainerView(): Int {
        return R.layout.activity_play_and_download
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.play_download_setting)
        switchAutoSkip.isChecked = SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_SKIP, true)
//        switchAutoPlayNext.isChecked =
//            SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_PLAY_NEXT, true)
//        autoPlayNoWifi.isChecked =
//            SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_PLAY_MOBILE_NET, false)
        autoDownloadNoWifi.isChecked =
            SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_DOWNLOAD_MOBILE_NET, false)
        autoUploadNoWifi.isChecked =
            SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, false)

        switchAutoSkip.setOnClickListener(this)
        switchAutoPlayNext.setOnClickListener(this)
        autoPlayNoWifi.setOnClickListener(this)
        autoDownloadNoWifi.setOnClickListener(this)
        autoUploadNoWifi.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.switchAutoSkip->SPUtils.getInstance().put(Constant.KEY_AUTO_SKIP, switchAutoSkip.isChecked)
//            R.id.switchAutoPlayNext->SPUtils.getInstance().put(Constant.KEY_AUTO_PLAY_NEXT, switchAutoPlayNext.isChecked)
//            R.id.autoPlayNoWifi->SPUtils.getInstance().put(Constant.KEY_AUTO_PLAY_MOBILE_NET, autoPlayNoWifi.isChecked)
            R.id.autoDownloadNoWifi->SPUtils.getInstance().put(Constant.KEY_AUTO_DOWNLOAD_MOBILE_NET, autoDownloadNoWifi.isChecked)
            R.id.autoUploadNoWifi->SPUtils.getInstance().put(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, autoUploadNoWifi.isChecked)
        }
    }
}