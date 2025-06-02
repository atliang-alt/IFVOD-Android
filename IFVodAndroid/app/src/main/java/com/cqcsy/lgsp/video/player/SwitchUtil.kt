package com.cqcsy.lgsp.video.player

import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener
import java.lang.ref.WeakReference

object SwitchUtil {

    private var sSwitchVideo: WeakReference<LiteVideoPlayer>? = null
    private var sMediaPlayerListener: GSYMediaPlayerListener? = null

    @Synchronized
    fun savePlayState(switchVideo: LiteVideoPlayer) {
        sSwitchVideo = WeakReference(switchVideo.saveState())
        sMediaPlayerListener = switchVideo
    }

    @Synchronized
    fun clonePlayState(switchVideo: LiteVideoPlayer) {
        switchVideo.cloneState(sSwitchVideo?.get())
    }

    @Synchronized
    fun release() {
        if (sMediaPlayerListener != null) {
            sMediaPlayerListener!!.onAutoCompletion()
        }
        sSwitchVideo?.get()?.release()
        sSwitchVideo = null
        sMediaPlayerListener = null
    }

    @Synchronized
    fun isRelease(): Boolean {
        return sSwitchVideo == null
    }
}