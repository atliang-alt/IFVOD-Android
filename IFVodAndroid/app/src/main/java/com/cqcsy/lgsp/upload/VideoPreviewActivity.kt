package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.library.base.NormalActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_video_preview.*

/**
 * 本地视频选择预览
 */
class VideoPreviewActivity : NormalActivity() {
    private var localMediaBean: LocalMediaBean? = null
    private val shortVideoCode = 1001

    override fun getContainerView(): Int {
        return R.layout.activity_video_preview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.selectVideo)
        if (intent.getSerializableExtra("LocalMediaBean") != null) {
            localMediaBean = intent.getSerializableExtra("LocalMediaBean") as LocalMediaBean
        }
        initView()
    }

    private fun initView() {
        if (localMediaBean == null) {
            return
        }
        videoPlayer.setUp(localMediaBean!!.path, false, "")
        //隐藏title
        videoPlayer.titleTextView.visibility = View.GONE
        //隐藏返回键
        videoPlayer.backButton.visibility = View.GONE
        //隐藏全屏按键功能
        videoPlayer.fullscreenButton.visibility = View.GONE
        videoPlayer.isLooping = true
        videoPlayer.startPlayLogic()
        next.setOnClickListener {
            val intent = Intent(this, ShortVideoInfoActivity::class.java)
            intent.putExtra(ShortVideoInfoActivity.FORM_TYPE, 0)
            intent.putExtra(ShortVideoInfoActivity.LOCAL_BEAN, localMediaBean)
            startActivityForResult(intent, shortVideoCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == shortVideoCode) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
    }

    override fun onBackPressed() {
        //释放所有
        videoPlayer.setVideoAllCallBack(null)
        super.onBackPressed()
    }
}