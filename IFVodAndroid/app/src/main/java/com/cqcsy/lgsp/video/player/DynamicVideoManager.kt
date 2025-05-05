package com.cqcsy.lgsp.video.player

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.cqcsy.lgsp.R
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 ** 2023/9/21
 ** des：
 **/

class DynamicVideoManager(var managerTag: Any?) : GSYVideoBaseManager() {

    val SMALL_ID: Int = R.id.small_id

    val FULLSCREEN_ID: Int = R.id.full_id

    var TAG = "DynamicVideoManager"

    companion object {


        private val mMgrCache: ConcurrentMap<Any, WeakReference<DynamicVideoManager>> = ConcurrentHashMap()

        /**
         * 单例管理器
         */
        @Synchronized
        fun instance(tag: Any?): DynamicVideoManager {
            var videoManager: DynamicVideoManager? = if(tag == null && mMgrCache.isNotEmpty()) {
                mMgrCache[mMgrCache.keys.toMutableList()[0]]?.get()
            } else {
                mMgrCache[tag]?.get()
            }
            if (videoManager == null) {
                videoManager = DynamicVideoManager(tag)
                mMgrCache[tag] = WeakReference(videoManager)
            }
            return videoManager
        }
    }

    init {
        init()
    }

    /**
     * 同步创建一个临时管理器
     */
//    @Synchronized
//    fun tmpInstance(listener: GSYMediaPlayerListener?): DynamicVideoManager {
//        val dynamicVideoManager = DynamicVideoManager(tag)
//        dynamicVideoManager.bufferPoint = videoManager!!.bufferPoint
//        dynamicVideoManager.optionModelList = videoManager!!.optionModelList
//        dynamicVideoManager.playTag = videoManager!!.playTag
//        dynamicVideoManager.currentVideoWidth = videoManager!!.currentVideoWidth
//        dynamicVideoManager.currentVideoHeight = videoManager!!.currentVideoHeight
//        dynamicVideoManager.context = videoManager!!.context
//        dynamicVideoManager.lastState = videoManager!!.lastState
//        dynamicVideoManager.playPosition = videoManager!!.playPosition
//        dynamicVideoManager.timeOut = videoManager!!.timeOut
//        dynamicVideoManager.needMute = videoManager!!.needMute
//        dynamicVideoManager.needTimeOutOther = videoManager!!.needTimeOutOther
//        dynamicVideoManager.setListener(listener)
//        return dynamicVideoManager
//    }

    /**
     * 替换管理器
     */
    @Synchronized
    fun changeManager(dynamicVideoManager: DynamicVideoManager?) {
        mMgrCache[managerTag] = WeakReference(dynamicVideoManager)
    }

    /**
     * 退出全屏，主要用于返回键
     *
     * @return 返回是否全屏
     */
    fun backFromWindowFull(context: Context?): Boolean {
        var backFrom = false
        val vp = CommonUtil.scanForActivity(context).findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val oldF = vp.findViewById<View>(FULLSCREEN_ID)
        if (oldF != null) {
            backFrom = true
            CommonUtil.hideNavKey(context)
            if (instance(managerTag).lastListener() != null) {
                instance(managerTag).lastListener().onBackFullscreen()
            }
        }
        return backFrom
    }

    /**
     * 页面销毁了记得调用是否所有的video
     */
    fun releaseAllVideos() {
        if (instance(managerTag).listener() != null) {
            instance(managerTag).listener().onCompletion()
        }
        instance(managerTag).releaseMediaPlayer()
    }


    /**
     * 暂停播放
     */
    fun onPause() {
        if (instance(managerTag).listener() != null) {
            instance(managerTag).listener().onVideoPause()
        }
    }

    /**
     * 恢复播放
     */
    fun onResume() {
        if (instance(managerTag).listener() != null) {
            instance(managerTag).listener().onVideoResume()
        }
    }


    /**
     * 恢复暂停状态
     *
     * @param seek 是否产生seek动作,直播设置为false
     */
    fun onResume(seek: Boolean) {
        if (instance(managerTag).listener() != null) {
            instance(managerTag).listener().onVideoResume(seek)
        }
    }

    /**
     * 当前是否全屏状态
     *
     * @return 当前是否全屏状态， true代表是。
     */
    fun isFullState(activity: Activity?): Boolean {
        val vp = CommonUtil.scanForActivity(activity).findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
        val full = vp.findViewById<View>(FULLSCREEN_ID)
        var gsyVideoPlayer: GSYVideoPlayer? = null
        if (full != null) {
            gsyVideoPlayer = full as GSYVideoPlayer
        }
        return gsyVideoPlayer != null
    }

}