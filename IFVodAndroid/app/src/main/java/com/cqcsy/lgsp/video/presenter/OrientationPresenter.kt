package com.cqcsy.lgsp.video.presenter

import android.hardware.SensorManager
import android.view.OrientationEventListener
import com.cqcsy.lgsp.video.player.LiteVideoPlayer

/**
 * 屏幕旋转监听
 */
class OrientationPresenter(val player: LiteVideoPlayer) {
    private var mOrientationEventListener: OrientationEventListener? = null

    init {
        initOrientationEventListener()
    }

    private fun initOrientationEventListener() {
        var currentOrientation = 0
        mOrientationEventListener =
            object : OrientationEventListener(player.context, SensorManager.SENSOR_DELAY_NORMAL) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) {
                        return  // 手机平放时，检测不到有效的角度
                    } // 只检测是否有四个角度的改变
                    val temp = when (orientation) {
                        in 30..99 -> { // 90度：手机顺时针旋转90度横屏（home建在左侧）
                            90
                        }
                        in 230..310 -> { // 手机顺时针旋转270度横屏，（home键在右侧）
                            270
                        }
                        else -> {
                            0
                        }
                    }
                    if (currentOrientation != temp && temp != 0) {
                        currentOrientation = temp
//                        player.setViewPadding(currentOrientation)
                    }
                }

            }
        mOrientationEventListener!!.disable()
    }

    fun setOrientationEnable(isPlay: Boolean) {
        if (isPlay && mOrientationEventListener?.canDetectOrientation() == true) {
            mOrientationEventListener?.enable()
        } else {
            mOrientationEventListener?.disable()
        }
    }

    fun disable() {
        mOrientationEventListener?.disable()
    }

    fun destroy() {
        mOrientationEventListener?.disable()
        mOrientationEventListener = null
    }
}