package com.cqcsy.lgsp.download

import android.content.Context
import android.webkit.URLUtil
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.event.GetDownloadUrlEvent
import com.cqcsy.lgsp.event.TaskCancelEvent
import com.cqcsy.library.download.server.DownloadTask
import com.cqcsy.library.download.server.OkDownload
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.callback.BaseResponse
import com.cqcsy.library.utils.GlobalValue
import com.danikula.videocache.file.Md5FileNameGenerator
import com.google.gson.Gson
import com.lzy.okgo.db.DownloadManager
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Progress
import com.lzy.okgo.request.GetRequest
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File
import java.io.Serializable

/**
 * 获取下载相关数据
 */
object DownloadMgr {
    private val MOVIE_TAG = "movie_download"
    private val maxDownload = 1
    private val MAX_DOWNLOADING = 10

    fun initFilePath() {
        OkDownload.getInstance().folder = GlobalValue.VIDEO_DOWNLOAD_PATH
        OkDownload.getInstance().threadPool.setCorePoolSize(maxDownload)
    }

    @Synchronized
    fun startDownload(context: Context, videoBaseBean: VideoBaseBean) {
        if (judgeDownloadExist(videoBaseBean)) {
            return
        }
        if (getDownloading().size >= MAX_DOWNLOADING) {
            ToastUtils.showLong(R.string.downloading_max_tip)
            return
        }
        val params = HttpParams()
        params.put("id", videoBaseBean.episodeKey)
        params.put("mediaKey", videoBaseBean.mediaKey)
        params.put("videoType", videoBaseBean.videoType)
        HttpRequest.get(RequestUrls.GET_DOWNLOAD_LINK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val downloadUrl = response?.optString("url")
                if (!URLUtil.isValidUrl(downloadUrl)) {
                    ToastUtils.showLong(R.string.download_url_error)
                    return
                }
                if (videoBaseBean.coverImgUrl.isNullOrEmpty())
                    videoBaseBean.coverImgUrl = response?.optString("image")
                if (videoBaseBean.episodeTitle.isNullOrEmpty())
                    videoBaseBean.episodeTitle = response?.optString("subTitle")
                if (videoBaseBean.title.isNullOrEmpty())
                    videoBaseBean.title = response?.optString("title")

                val downloadInfoBean = DownloadInfoBean()
                if (videoBaseBean.videoType == Constant.VIDEO_TELEPLAY || videoBaseBean.videoType == Constant.VIDEO_VARIETY) {
                    downloadInfoBean.folderName = videoBaseBean.title
                }
                downloadInfoBean.tag = videoBaseBean.episodeKey
                downloadInfoBean.downloadUrl = downloadUrl
                downloadInfoBean.episodeTitle = videoBaseBean.episodeTitle
                downloadInfoBean.mediaKey = videoBaseBean.mediaKey
                downloadInfoBean.uniqueId = videoBaseBean.uniqueID.toString()
                downloadInfoBean.extra = Gson().toJson(videoBaseBean)
                checkDownloadNet(downloadInfoBean)
            }

            override fun onError(response: String?, errorMsg: String?) {
                try {
                    val baseResponse: BaseResponse<*> =
                        GsonUtils.fromJson(response, BaseResponse::class.java)
                    if (baseResponse.ret == 7001) {  // 下载次数限制
                        ToastUtils.showLong(errorMsg)
                    } else {
                        ToastUtils.showLong(R.string.download_error)
                    }
                } catch (e: Exception) {
                    ToastUtils.showLong(R.string.download_error)
                }
            }

        }, params, context)
    }

    /**
     * 判断视频下载是否存在
     */
    fun judgeDownloadExist(
        videoBaseBean: VideoBaseBean,
        showTips: Boolean = true,
        needDeleteDownload: Boolean = true
    ): Boolean {
        val progress = DownloadManager.getInstance().get(videoBaseBean.episodeKey)
        if (progress != null && progress.status == Progress.FINISH && File(progress.filePath).exists()) {
            if (showTips)
                ToastUtils.showShort(R.string.download_exsit)
            return true
        }
        val allTask = getAllDownload()
        allTask.forEach {
            val bean = Gson().fromJson(
                it.extra1.toString(),
                VideoBaseBean::class.java
            )
            if (bean.uniqueID == videoBaseBean.uniqueID) {
                if (it.status == Progress.FINISH) {
                    ToastUtils.showLong(R.string.download_finish_exist)
                } else if (needDeleteDownload) {
                    deleteTask(it.tag, true)
                    cancelDownload(videoBaseBean.mediaKey, it.tag, showTips)
                }
                return true
            }
        }
        return false
    }

    private fun cancelDownload(mediaKey: String, tag: String, showTips: Boolean) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.CANCEL_DOWNLOAD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val event = TaskCancelEvent()
                event.taskTag = tag
                EventBus.getDefault().post(event)
                if (showTips) {
                    ToastUtils.showShort(R.string.download_cancel)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params)
    }

    /**
     * 获取正在下载数量
     */
    fun getDownloadingSize(): Int {
        return getDownloading().size
    }

    /**
     * 获取正在下载列表
     */
    fun getDownloading(): MutableList<Progress> {
        return DownloadManager.getInstance().downloading.filter { isMovieDownload(it.extra2) }.toMutableList()
    }

    /**
     * 获取所有下载记录
     */
    fun getAllDownload(): MutableList<Progress> {
        return DownloadManager.getInstance().all.filter { isMovieDownload(it.extra2) && it.extra1 != null && it.extra1.toString().isNotEmpty() }
            .toMutableList()
    }

    /**
     * 获取所有已完成下载记录
     */
    fun getDownloadFinished(): MutableList<Progress> {
        return DownloadManager.getInstance().finished.filter { isMovieDownload(it.extra2) }.toMutableList()
    }

    private fun isMovieDownload(extra2: Serializable?): Boolean {
        return extra2 == null || extra2 == MOVIE_TAG
    }

    /**
     * 删除下载记录
     */
    fun deleteTask(tag: String, isDeleteFile: Boolean = false) {
        if (OkDownload.getInstance().getTask(tag)?.remove(isDeleteFile) == null) {
            DownloadManager.getInstance().delete(tag)
        }
    }

    /**
     * 下载视频
     */
    private fun checkDownloadNet(downloadInfoBean: DownloadInfoBean) {
        val result = Utils.Consumer<Boolean> {
            val autoNetDownload =
                SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_DOWNLOAD_MOBILE_NET, false)
            if (autoNetDownload || it) {
                val progress = DownloadManager.getInstance().get(downloadInfoBean.tag)
                if (progress != null && progress.status == Progress.FINISH && File(progress.filePath).exists()) {
                    ToastUtils.showShort(R.string.download_exsit)
                    return@Consumer
                }
                if (downloadInfoBean.headers == null) {
                    downloadInfoBean.headers = HttpHeaders()
                }
                downloadInfoBean.headers!!.put("Accept-Encoding", "identity")
                checkPermission(downloadInfoBean)
            } else {
                ToastUtils.showLong(R.string.download_no_wifi_forbidden)
            }
        }
        NetworkUtils.isWifiAvailableAsync(result)
    }

    /**
     * 恢复所有未完成下载任务
     */
    fun restoreAll() {
        //从数据库中恢复数据
        val progressList = getDownloading()
        val deleteList: MutableList<Progress> = ArrayList()
        progressList.forEach {
            if (it.extra1 == null) {
                deleteList.add(it)
                deleteTask(it.tag, true)
            }
        }
        progressList.removeAll(deleteList)
        if (progressList.size > 0) {
            OkDownload.restore(progressList)
            OkDownload.getInstance().startAll()
        }
    }

    fun startDownload(
        downloadInfoBean: DownloadInfoBean? = null,
        downloadUrl: String? = null
    ) {
        if (downloadUrl != null && !downloadUrl.isNullOrEmpty()) {
            checkPermission(null, downloadUrl)
        } else if (downloadInfoBean != null) {
            checkDownloadNet(downloadInfoBean)
        }
    }

    private fun checkPermission(
        downloadInfoBean: DownloadInfoBean? = null,
        downloadUrl: String? = null
    ) {
        if (downloadInfoBean != null) {
            startDownload(downloadInfoBean)
        } else if (downloadUrl != null && !downloadUrl.isNullOrEmpty()) {
            startDownload(downloadUrl, downloadUrl)
        }
    }

    /**
     * 下载视频
     */
    private fun startDownload(downloadInfoBean: DownloadInfoBean) {
        if (downloadInfoBean.downloadUrl.isNullOrEmpty()) {
            return
        }
        val task: DownloadTask?
        if (OkDownload.getInstance().hasTask(downloadInfoBean.tag)) {
            task = OkDownload.getInstance().getTask(downloadInfoBean.tag)
            if (task.progress != null && !task.progress.filePath.isNullOrEmpty() && File(task.progress.filePath).exists()) {
                task.start()
            } else {
                task.restart()
            }
        } else {
            val request: GetRequest<File> = GetRequest(downloadInfoBean.downloadUrl)
            //        设置下载参数
            request.headers.put(downloadInfoBean.headers)
            request.params.put(downloadInfoBean.params)
            task = OkDownload.request(downloadInfoBean.tag, request)
                .fileName(downloadInfoBean.fileName).extra1(downloadInfoBean.extra).extra2(MOVIE_TAG)
            // 如果外面传了文件保存文件夹，则使用外部，不传则使用通用路径
            if (downloadInfoBean.folderName != null) {
                val fileFolder = GlobalValue.VIDEO_DOWNLOAD_PATH + File.separator + downloadInfoBean.folderName + File.separator
                if (FileUtils.createOrExistsDir(fileFolder)) {
                    task.folder(fileFolder)
                }
            }
            task.save().start()
        }
        EventBus.getDefault().post(GetDownloadUrlEvent(downloadInfoBean))
        ToastUtils.showShort(R.string.download_add)
    }

    /**
     * 传入URL下载
     */
    private fun startDownload(downloadUrl: String, tag: String) {
        if (!URLUtil.isValidUrl(downloadUrl)) {
            ToastUtils.showLong(R.string.download_url_error)
            return
        }
        if (OkDownload.getInstance().hasTask(tag)) {
            val task = OkDownload.getInstance().getTask(tag)
            if (task.progress != null && !task.progress.filePath.isNullOrEmpty() && File(task.progress.filePath).exists()) {
                task.start()
            } else {
                task.restart()
            }
        } else {
            val request: GetRequest<File> = GetRequest(downloadUrl)
            val task: DownloadTask =
                OkDownload.request(tag, request).folder(GlobalValue.APP_CACHE_PATH).fileName(
                    Md5FileNameGenerator().generate(downloadUrl) + ".apk"
                )
            task.save().start()
        }
    }

//    @SuppressLint("StaticFieldLeak")
//    fun decryptVideo(file: File) {
//        object : AsyncTask<Void, Integer, Boolean>() {
//            override fun doInBackground(vararg params: Void?): Boolean {
//                val tempFile = File(file.parent + "/" + "temp")
//                tempFile.deleteOnExit()
//                tempFile.createNewFile()
//                val out = FileOutputStream(tempFile)
//                val decryption = VideoDecryption(FileInputStream(file), file.length())
//                decryption.setDecodeKey(file.name)
//                decryption.outStream = out
//                decryption.start()
//                file.deleteOnExit()
//                tempFile.renameTo(file)
//                return true
//            }
//
//        }.execute()
//    }
}