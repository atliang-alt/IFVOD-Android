package com.cqcsy.lgsp.preload

import android.net.Uri
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.app.ExoDataSourceHelper
import com.google.android.exoplayer2.database.DefaultDatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.offline.DefaultDownloadIndex
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import java.util.concurrent.Executors

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/22
 *
 *
 */
object PreloadManager2 {

    private var downloadManager: DownloadManager? = null

    private val listener = object : DownloadManager.Listener {
        override fun onDownloadChanged(
            downloadManager: DownloadManager,
            download: Download,
            finalException: Exception?
        ) {
            //LogUtils.e("${download.state},${download.request.id}：${download.percentDownloaded}：${download.bytesDownloaded}")
            if (download.percentDownloaded == 100f) {

            }
        }
    }

    private val tasks: LinkedHashMap<String, DownloadRequest> = LinkedHashMap()

    fun init() {
        downloadManager?.release()
        downloadManager = DownloadManager(
            Utils.getApp(), DefaultDownloadIndex(
                DefaultDatabaseProvider(
                    StandaloneDatabaseProvider(Utils.getApp())
                )
            ),
            PreloadDownloadFactory(
                ExoDataSourceHelper.getCacheDataSourceFactory(Utils.getApp(), null),
                Executors.newSingleThreadExecutor()
            )
        )
        downloadManager?.addListener(listener)
        downloadManager?.maxParallelDownloads = 10
        if (tasks.isNotEmpty()) {
            for (task in tasks) {
                downloadManager?.addDownload(task.value)
            }
            downloadManager?.removeAllDownloads()
        }
    }

    fun addPreloadTask(id: String, url: String) {
        var request = tasks[id]
        if (request == null) {
            request = createDownloadRequest(id, url)
        }
        tasks[id] = request
        downloadManager?.addDownload(request)
    }

    fun setMaxParallelDownloads(maxCount: Int) {
        downloadManager?.maxParallelDownloads = maxCount
    }

    fun removePreloadTask(id: String) {
        tasks.remove(id)
        downloadManager?.removeDownload(id)
    }

    /**
     * 开始所有预加载任务
     */
    fun resumeAllPreload() {
        downloadManager?.resumeDownloads()
    }

    /**
     * 暂停所有预加载任务
     */
    fun pauseAllPreload() {
        downloadManager?.pauseDownloads()
    }

    /**
     * 清除所有预加载任务
     */
    fun clear() {
        tasks.clear()
    }

    fun release() {
        tasks.clear()
        downloadManager?.removeListener(listener)
        downloadManager?.release()
    }

    private fun createDownloadRequest(id: String, url: String): DownloadRequest {
        return DownloadRequest.Builder(id, Uri.parse(url)).build()
    }
}