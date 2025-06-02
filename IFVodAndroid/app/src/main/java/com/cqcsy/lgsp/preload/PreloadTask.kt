package com.cqcsy.lgsp.preload

import com.google.android.exoplayer2.offline.Downloader
import java.io.IOException
import java.util.concurrent.ExecutorService

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/17
 *
 *
 */
class PreloadTask(val position: Int, val url: String, private val downloader: Downloader) :
    Thread(), Downloader.ProgressListener {

    /**
     * 是否被取消
     */
    private var isCanceled = false

    /**
     * 是否正在预加载
     */
    private var isExecuted = false

    /**
     * 是否加载完成
     */
    var isLoadFinished: Boolean = false

    override fun run() {
        try {
            if (!isCanceled) {
                try {
                    downloader.download { contentLength, bytesDownloaded, percentDownloaded ->
                        if (percentDownloaded == 100f) {
                            isLoadFinished = true
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            isExecuted = false
            isCanceled = false
        } catch (e: InterruptedException) {
            currentThread().interrupt()
        }
    }

    /**
     * 将预加载任务提交到线程池，准备执行
     */
    fun executeOn(executorService: ExecutorService) {
        if (isExecuted) return
        isCanceled = false
        isExecuted = true
        executorService.submit(this)
    }

    /**
     * 取消预加载任务
     */
    fun cancel() {
        if (!isCanceled) {
            isCanceled = true
            downloader.cancel()
            interrupt()
        }
    }

    override fun onProgress(contentLength: Long, bytesDownloaded: Long, percentDownloaded: Float) {

    }
}