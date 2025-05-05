package com.cqcsy.lgsp.video.presenter

import android.net.Uri
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.library.decode.DecryptStream
import com.cqcsy.library.decode.HttpServer
import com.lzy.okgo.db.DownloadManager
import com.lzy.okgo.model.Progress
import java.io.File
import java.net.URLEncoder

/**
 * 离线视频操作
 */
class OfflinePresenter(var videoBaseBean: VideoBaseBean?) {

    private var httpServer: HttpServer? = null


    /**
     * 判断当前播放视频是否离线下载播放的视频
     */
    fun isOfflineVideo(): Boolean {
        if (videoBaseBean == null || !videoBaseBean?.filePath.isNullOrEmpty()) {
            return true
        }
        return false
    }

    fun getOfflineUri(): String? {
        if (isOfflineVideo()) {
            val task = DownloadManager.getInstance().get(videoBaseBean?.episodeKey)
            if (task != null && task.status == Progress.FINISH && File(task.filePath).exists()) {
                httpServer = HttpServer.instance
                httpServer?.start(DecryptStream(), 8080)
                return Uri.parse(
                    httpServer?.httpAddr + "/?path=" + URLEncoder.encode(
                        task.filePath,
                        "UTF-8"
                    )
                ).toString()
            }
        }
        return null
    }

    private fun checkDownloadLocalFile(episodeKey: String): String? {
        val progress = DownloadManager.getInstance().get(episodeKey)
        if (progress?.status == Progress.FINISH) {
            return progress.filePath
        }
        return null
    }

    fun reset() {
        videoBaseBean?.filePath = ""
        httpServer?.stop()
    }
}