package com.cqcsy.lgsp.video

import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.lgsp.video.viewModel.VideoViewModel
import com.cqcsy.library.utils.GlobalValue
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView


/**
 * 2024.02.04
 * des: 直播流切换逻辑判断
 */
class LiveSourceChange(
    val player: LiteVideoPlayer,
    val currentPlay: VideoBaseBean?,
    val videoViewModel: VideoViewModel,
    val clarityList: MutableList<ClarityBean>?,
    val startPlayMethod: (() -> Unit)
) {

    // 播放错误自动切换地址的缓存，如果没有了，就直接报错，否则自动切换
    private val mHandler = Handler(Looper.getMainLooper())

    private var mMediaSourceList: MutableList<ClarityBean?>? = null

    var isClarityChange = false
    private val MAX_RETRY = 5    // 最大刷新次数
    var mRetryTime = 0   // 刷新播放地址次数
    private val LIVE_TIME_OUT = 15_000L  // 直播切流或刷新时长

    init {
        resetMediaSource(currentPlay?.episodeId, currentPlay?.mediaUrl, clarityList)
        startChangeTimer()
    }

    fun reset() {
        mRetryTime = 0
        isClarityChange = false
        clearLiveTimer()
        mMediaSourceList?.clear()
    }

    fun isEnableError(): Boolean {
        return mRetryTime >= MAX_RETRY
    }

    fun sourceSize(): Int {
        return mMediaSourceList?.size ?: 0
    }

    /**
     * 获取播放地址列表或者切换清晰度后（手动或自动），需要重新刷新数据源列表，以便自动切换线路
     */
    private fun resetMediaSource(episodeId: Int?, playUrl: String?, clarityList: MutableList<ClarityBean>?) {
        if (mMediaSourceList == null) {
            mMediaSourceList = ArrayList()
        }
        mMediaSourceList?.clear()
        if (clarityList.isNullOrEmpty() || playUrl.isNullOrEmpty()) {
            return
        }
        val temp = clarityList.find { it.episodeId == episodeId }
        val first: MutableList<ClarityBean?> = ArrayList()
        val last: MutableList<ClarityBean?> = ArrayList()
        var isFirst = true // 标识是否应该放在列表前面部分first中，否则放last
        for (item in clarityList) {
            if (item.isVip && !GlobalValue.isVipUser()) {
                continue
            }
            if (item == temp) {
                isFirst = false
                continue
            }
            if (isFirst) {
                first.add(item)
            } else {
                last.add(item)
            }
        }
        mMediaSourceList!!.add(temp)
        mMediaSourceList!!.addAll(first)
        mMediaSourceList!!.addAll(last)
    }

    fun clearLiveTimer() {
        mHandler.removeCallbacksAndMessages(null)
    }

    fun startChangeTimer() {
        clearLiveTimer()

        if (mRetryTime == 0) {
            player.showLoading(false)
        }
        // 切流计时，15s超时切换，每15s重置成0
        mHandler.postDelayed(mTimerTask, LIVE_TIME_OUT)
        // 2s未播放，且不处于buffering状态，切换为buffering状态等待
        mHandler.postDelayed(mProgressTask, 2 * 1000)
    }

    private val mTimerTask = Runnable {
        mHandler.removeCallbacks(mProgressTask)
        changeLiveSource()
    }

    private val mProgressTask = Runnable {
        if (player.isPlaying) {
            player.showLoading(false)
        } else if (player.currentState != GSYVideoView.CURRENT_STATE_PLAYING_BUFFERING_START && player.currentState != GSYVideoView.CURRENT_STATE_PREPAREING) {
            player.showLoading(true)
        }
    }

    /**
     * 当直播线路故障，切换到其他线路
     */
    private fun changeLiveSource() {
        if (!mMediaSourceList.isNullOrEmpty()) {
            if (mMediaSourceList!!.size > 1) {
                val nextClarity = mMediaSourceList!![1]
                mMediaSourceList?.removeAt(1)
                currentPlay?.let { nextClarity?.setValueToBase(it) }
                startPlayMethod.invoke()
                ToastUtils.showLong(R.string.change_source)
            } else {
                refreshPlayClarity()
            }
        } else {
            refreshPlayClarity()
        }
    }

    /**
     * 没有切换线路，重新获取播放地址播放
     */
    private fun refreshPlayClarity() {
        if (currentPlay == null || mRetryTime >= MAX_RETRY) {
            player.currentPlayer.changeUiToError()
        } else {
            player.showLoading(true)
            currentPlay.let {
                mRetryTime++
                ToastUtils.showLong(StringUtils.getString(R.string.retry_tips, mRetryTime))
                videoViewModel.getPlayInfo(it.mediaKey, it.uniqueID)
            }
        }
    }

}