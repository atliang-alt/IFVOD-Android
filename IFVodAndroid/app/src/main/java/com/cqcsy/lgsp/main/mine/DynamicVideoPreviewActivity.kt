package com.cqcsy.lgsp.main.mine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_dynamic_video_preview.*

/**
 * 本地视频选择预览
 */
class DynamicVideoPreviewActivity : NormalActivity() {
    companion object {
        @JvmStatic
        fun launch(context: Context, videoPath: String) {
            val intent = Intent(context, DynamicVideoPreviewActivity::class.java)
            intent.putExtra("video_path", videoPath)
            context.startActivity(intent)
        }
    }

    override fun getContainerView(): Int {
        return R.layout.activity_dynamic_video_preview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.previewVideo)
        val videoPath = intent.getStringExtra("video_path")
        videoPlayer.setUp(videoPath, false, "")
        //隐藏title
        videoPlayer.titleTextView.visibility = View.GONE
        //隐藏返回键
        videoPlayer.backButton.visibility = View.GONE
        //隐藏全屏按键功能
        videoPlayer.fullscreenButton.visibility = View.GONE
        videoPlayer.isLooping = true
        videoPlayer.startPlayLogic()
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
        GSYVideoManager.releaseAllVideos()
        super.onDestroy()
    }

    override fun onBackPressed() {
        //释放所有
        videoPlayer.setVideoAllCallBack(null)
        super.onBackPressed()
    }
}