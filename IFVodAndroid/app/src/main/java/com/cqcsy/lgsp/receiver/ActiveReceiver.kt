package com.cqcsy.lgsp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cqcsy.lgsp.main.ActivePresenter

/**
 ** 2023/1/10
 ** des：日活时间时间监听，判断日活
 **/

class ActiveReceiver : BroadcastReceiver() {
    val TIME_TO_CHECK = 10 // 10分钟检测一次
    var lastBroadcastTime = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_TIME_TICK) {
            return
        }
        lastBroadcastTime++
        if (lastBroadcastTime >= TIME_TO_CHECK) {
            lastBroadcastTime = 0
            // 日活检查
            ActivePresenter()
        }
    }
}