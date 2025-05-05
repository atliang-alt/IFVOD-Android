package com.cqcsy.library.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.library.R

object NotificationBuilder {

    fun builderForegroundNotification(service: Service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = AppUtils.getAppPackageName()
            val manager: NotificationManager =
                service.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                channelId,
                StringUtils.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_NONE
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
            manager.createNotificationChannel(channel)
            val notificationBuilder = NotificationCompat.Builder(service, channelId)
            notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
            service.startForeground(100, notificationBuilder.build())
        }
    }
}