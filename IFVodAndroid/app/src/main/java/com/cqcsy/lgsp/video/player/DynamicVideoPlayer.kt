package com.cqcsy.lgsp.video.player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.R
import com.cqcsy.library.GlideApp
import com.cqcsy.library.utils.ImageUtil
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge

/**
 * 动态视频播放器
 */
class DynamicVideoPlayer(context: Context, attrs: AttributeSet) :
    GSYVideoPlayer(context, attrs) {

    private var dynamicBottomContainer: FrameLayout? = null
    private var failedContainer: LinearLayout? = null
    private var durationProgressContainer: LinearLayout? = null
    private var progressContainer: FrameLayout? = null
    private var total2: TextView? = null
    private var cover: ImageView? = null
    private val seekRunnable = {
        mProgressBar?.isVisible = false
        mBottomProgressBar?.isVisible = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun init(context: Context?) {
        super.init(context)
        dynamicBottomContainer = findViewById(R.id.dynamic_bottom_container)
        progressContainer = findViewById(R.id.progress_container)
        failedContainer = findViewById(R.id.failedContainer)
        durationProgressContainer = findViewById(R.id.duration_progress_container)
        cover = findViewById(R.id.coverImage)
        total2 = findViewById(R.id.total2)
        findViewById<Button>(R.id.btn_retry).setOnClickListener {
            startPlayLogic()
        }
        progressContainer?.let {
            it.setOnTouchListener { v, event ->
                setViewShowState(mProgressBar, View.VISIBLE)
                setViewShowState(mBottomProgressBar, View.INVISIBLE)
                mProgressBar.onTouchEvent(event)
                return@setOnTouchListener true
            }
        }
        mTextureViewContainer.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
    }

    fun setCover(coverUrl: String) {
        cover?.let {
            GlideApp.with(this).load(ImageUtil.formatUrl(coverUrl)).into(it)
        }
    }

    fun addDynamicBottomView(view: View) {
        dynamicBottomContainer?.removeAllViews()
        dynamicBottomContainer?.addView(view)
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_dynamic_video_player
    }

    override fun startPlayLogic() {
        prepareVideo()
        startDismissControlViewTimer()
    }

    override fun showWifiDialog() {

    }

    override fun showProgressDialog(
        deltaX: Float,
        seekTime: String?,
        seekTimePosition: Long,
        totalTime: String?,
        totalTimeDuration: Long
    ) {

    }

    override fun dismissProgressDialog() {

    }

    override fun showVolumeDialog(deltaY: Float, volumePercent: Int) {

    }

    override fun dismissVolumeDialog() {

    }

    override fun showBrightnessDialog(percent: Float) {
    }

    override fun dismissBrightnessDialog() {
    }

    override fun onClickUiToggle(e: MotionEvent?) {
        clickStartIcon()
    }

    override fun hideAllWidget() {
    }

    override fun changeUiToNormal() {
        setViewShowState(cover, View.VISIBLE)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.GONE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun changeUiToPreparingShow() {
        setViewShowState(cover, View.VISIBLE)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.VISIBLE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun changeUiToPlayingShow() {
//        setViewShowState(cover, View.GONE)
        hideView(cover)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.GONE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun changeUiToPauseShow() {
        updateStartImage()
//        setViewShowState(cover, View.GONE)
        hideView(cover)
        setViewShowState(mStartButton, View.VISIBLE)
        setViewShowState(mLoadingProgressBar, View.GONE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun changeUiToError() {
//        setViewShowState(cover, View.GONE)
        hideView(cover)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.GONE)
        setViewShowState(failedContainer, View.VISIBLE)
    }

    override fun changeUiToCompleteShow() {
//        setViewShowState(cover, View.GONE)
        hideView(cover)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.GONE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun changeUiToPlayingBufferingShow() {
//        setViewShowState(cover, View.GONE)
        hideView(cover)
        setViewShowState(mStartButton, View.GONE)
        setViewShowState(mLoadingProgressBar, View.VISIBLE)
        setViewShowState(failedContainer, View.GONE)
    }

    override fun setProgressAndTime(
        progress: Int,
        secProgress: Int,
        currentTime: Long,
        totalTime: Long,
        forceChange: Boolean
    ) {
        super.setProgressAndTime(progress, secProgress, currentTime, totalTime, forceChange)
        total2?.text = CommonUtil.stringForTime(totalTime)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        super.onStartTrackingTouch(seekBar)
        handler.removeCallbacks(seekRunnable)
        dynamicBottomContainer?.isVisible = false
        durationProgressContainer?.isVisible = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        super.onStopTrackingTouch(seekBar)
        dynamicBottomContainer?.isVisible = true
        durationProgressContainer?.isVisible = false
        handler.postDelayed(seekRunnable, 4000)
    }

    /**
     * 定义开始按键显示
     */
    private fun updateStartImage() {
        val imageView = mStartButton as ImageView
        if (mCurrentState == CURRENT_STATE_PAUSE) {
            imageView.setImageResource(R.drawable.video_click_play_selector)
        }
    }

    private fun showView(view: View?) {
        if (view == null || (view.isVisible && view.alpha == 1.0f)) {
            return
        }
        view.clearAnimation()
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(800).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {

            }
        })?.start()
    }

    private fun hideView(view: View?) {
        if (view == null || !view.isVisible) {
            return
        }
        view.clearAnimation()
        view.animate()?.alpha(0f)?.setDuration(500)?.setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })?.start()
    }

    override fun getGSYVideoManager(): GSYVideoViewBridge {
        DynamicVideoManager.instance(context).initContext(Utils.getApp())
        return DynamicVideoManager.instance(context)
    }
}