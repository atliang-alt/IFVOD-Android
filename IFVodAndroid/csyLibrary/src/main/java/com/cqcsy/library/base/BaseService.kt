package com.cqcsy.library.base

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.cqcsy.library.utils.NotificationBuilder

abstract class BaseService : Service() {

    companion object {
        // 需要传递参数的调用这个
        fun startService(context: Context, intent: Intent) {
            ContextCompat.startForegroundService(context, intent)
        }
        // 不需要传递参数的调这个
        fun startService(context: Context, targetClass: Class<*>) {
            val intent = Intent(context, targetClass)
            startService(context, intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        NotificationBuilder.builderForegroundNotification(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        NotificationBuilder.builderForegroundNotification(this)
        return START_STICKY
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        super.onDestroy()
    }

}