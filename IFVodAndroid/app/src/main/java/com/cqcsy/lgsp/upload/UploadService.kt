package com.cqcsy.lgsp.upload

import android.content.Intent
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.manger.UploadCacheManger
import com.cqcsy.lgsp.event.UploadEvent
import com.cqcsy.lgsp.event.UploadListenerEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upload.util.UploadFileUtil
import com.cqcsy.lgsp.upload.util.UploadMgr
import com.cqcsy.library.base.BaseService
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Progress
import com.lzy.okgo.model.Response
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File

/**
 * 上传视频服务Service
 */
class UploadService : BaseService(), NetworkUtils.OnNetworkStatusChangedListener {
    companion object {
        const val UPLOAD_INFO = "uploadInfo"
        const val START_UPLOAD = "startUpload"
        const val TAG = "UploadService"
        const val UPLOAD_TAG = "uploading"
    }

    val blockSize = 2 * 1024 * 1024

    override fun onCreate() {
        super.onCreate()
        NetworkUtils.registerNetworkStatusChangedListener(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getSerializableExtra(UPLOAD_INFO) != null) {
            val uploadInfoBean = intent.getSerializableExtra(UPLOAD_INFO) as UploadCacheBean
            getServiceId(uploadInfoBean)
        } else if (intent?.getStringExtra(START_UPLOAD) != null) {
            if (intent.getStringExtra(START_UPLOAD)!!.isNotEmpty()) {
                val uploadCacheBean = UploadCacheManger.instance.select(intent.getStringExtra(START_UPLOAD)!!)
                if (uploadCacheBean != null) {
                    uploadCacheBean.status = Constant.UPLOADING
                    UploadCacheManger.instance.update(uploadCacheBean)
                    checkUploadFile(uploadCacheBean)
                } else {
                    stopSelf()
                }
            } else {
                restoreAll()
            }
        } else {
            restoreAll()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 获取上传服务地址
     */
    private fun getServiceId(uploadInfoBean: UploadCacheBean) {
        if (!GlobalValue.isLogin()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            return
        }
        HttpRequest.post(RequestUrls.GET_UPLOAD_SERVICE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                uploadInfoBean.serviceId = response.optInt("id")
                uploadInfoBean.uploadUrl = response.optString("endpoint")
                createFileHttp(uploadInfoBean)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                EventBus.getDefault().post(UploadEvent(false))
                stopSelf()
            }

        }, tag = TAG)
    }

    /**
     * 创建文件
     */
    private fun createFileHttp(uploadInfoBean: UploadCacheBean) {
        val params = HttpParams()
        params.put("CID", uploadInfoBean.cid)
        params.put("Labels", uploadInfoBean.labels)
        params.put("Title", uploadInfoBean.title)
        params.put("ServerID", uploadInfoBean.serviceId)
        params.put("FileName", getFileName(uploadInfoBean.path))
        GlobalValue.userInfoBean?.token?.uid?.let { params.put("Uid", it) }
        params.put("Size", uploadInfoBean.videoSize)
        HttpRequest.post(RequestUrls.CREATE_UPLOAD_FILE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                uploadInfoBean.fileId = response.optString("uniqueFileName")
                uploadInfoBean.lid = response.optInt("lid")
                uploadInfoBean.vid = response.optInt("id")
                uploadInfoBean.status = Constant.UPLOADING
                val videoFile = File(uploadInfoBean.path)
                uploadInfoBean.chunks = if (videoFile.length() % blockSize == 0L) {
                    videoFile.length().toInt() / blockSize
                } else {
                    videoFile.length().toInt() / blockSize + 1
                }
                uploadInfoBean.chunk = 1
                UploadCacheManger.instance.add(uploadInfoBean)
                EventBus.getDefault().post(UploadEvent(true))
                uploadFile(uploadInfoBean, false)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                EventBus.getDefault().post(UploadEvent(false))
                stopSelf()
            }
        }, params, TAG)
    }

    /**
     * 上传文件
     */
    private fun uploadFile(uploadInfoBean: UploadCacheBean, isCheck: Boolean) {
        val cutFileName = GlobalValue.UPLOAD_CUT_FILE + getFileName(uploadInfoBean.path) + "_" + uploadInfoBean.chunk + ".mp4"
        val file: File? = if (isCheck) {
            uploadInfoBean.chunk = (uploadInfoBean.progress.toInt() / blockSize) + 1
            val size = uploadInfoBean.chunk * blockSize - uploadInfoBean.progress
            UploadFileUtil.getCutFile(
                uploadInfoBean.progress,
                uploadInfoBean.path,
                cutFileName,
                size.toInt()
            )
        } else {
            UploadFileUtil.getCutFile(
                (uploadInfoBean.chunk - 1) * blockSize.toLong(),
                uploadInfoBean.path,
                cutFileName,
                blockSize
            )
        }
        if (file == null) {
            uploadInfoBean.status = Constant.UPLOAD_ERROR
            UploadCacheManger.instance.update(uploadInfoBean)
            EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onError, uploadInfoBean))
            FileUtils.delete(cutFileName)
            return
        }
        uploadInfoBean.cutFileName = cutFileName
        uploadInfoBean.cutStart = (uploadInfoBean.chunk - 1) * blockSize.toLong()
        val params = HttpParams()
        params.put("uniqueFileID", uploadInfoBean.fileId)
        params.put("renamemode", "Format")
        params.put("prefix", "xw-")
        GlobalValue.userInfoBean?.token?.uid?.let { params.put("uid", it) }
        params.put("file", file)
        params.put("chunks", uploadInfoBean.chunks)
        params.put("chunk", uploadInfoBean.chunk)
        OkGo.post<String>(uploadInfoBean.uploadUrl).tag(UPLOAD_TAG).params(params)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    UploadCacheManger.instance.update(uploadInfoBean)
                    if (uploadInfoBean.chunk < uploadInfoBean.chunks) {
                        uploadInfoBean.chunk = uploadInfoBean.chunk + 1
                        uploadFile(uploadInfoBean, false)
                        FileUtils.delete(file)
                        return
                    }
                    if (response.body().isNotEmpty()) {
                        uploadInfoBean.fileName = response.body().toString()
                        UploadCacheManger.instance.update(uploadInfoBean)
                        saveUploadFile(uploadInfoBean)
                        FileUtils.delete(file)
                    }
                }

                override fun onFinish() {
                }

                override fun onError(response: Response<String>) {
                    uploadInfoBean.status = Constant.UPLOAD_ERROR
                    UploadCacheManger.instance.update(uploadInfoBean)
                    FileUtils.delete(file)
                    EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onError, uploadInfoBean))
                    stopSelf()
                }

                override fun uploadProgress(progress: Progress) {
                    uploadInfoBean.progress = blockSize * (uploadInfoBean.chunk - 1) + progress.currentSize
                    uploadInfoBean.speed = progress.speed
                    UploadCacheManger.instance.update(uploadInfoBean)
                    EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onProgress, uploadInfoBean))
                }
            })
    }

    /**
     * 上传完成保存文件
     */
    private fun saveUploadFile(uploadInfoBean: UploadCacheBean) {
        if (uploadInfoBean.imageBase.isNullOrEmpty() && !uploadInfoBean.imagePath.isNullOrEmpty()) {
            val file = File(uploadInfoBean.imagePath)
            if (!file.exists()) {
                UploadCacheManger.instance.delete(uploadInfoBean.path)
                EventBus.getDefault().post(UploadEvent(false))
                return
            }
            uploadInfoBean.imageBase = ImageUtil.imageToBase64(uploadInfoBean.imagePath)
        }
        val params = HttpParams()
        params.put("LID", uploadInfoBean.lid)
        params.put("ID", uploadInfoBean.vid)
        params.put("CID", uploadInfoBean.cid)
        params.put("Labels", uploadInfoBean.labels)
        params.put("Title", uploadInfoBean.title)
        params.put("Contxt", uploadInfoBean.context)
        GlobalValue.userInfoBean?.token?.uid?.let { params.put("UID", it) }
        params.put("Corver", uploadInfoBean.imageBase)
        params.put("UploadedFileName", uploadInfoBean.fileName)
        params.put("ServerID", uploadInfoBean.serviceId)
        HttpRequest.post(RequestUrls.SAVE_UPLOAD_FILE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                UploadCacheManger.instance.delete(uploadInfoBean.path)
                EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onFinish, uploadInfoBean))
                val list = UploadMgr.getUploadList()
                if (list.isNotEmpty()) {
                    checkUploadFile(list[0])
                }
                updateTask()
            }

            override fun onError(response: String?, errorMsg: String?) {
                uploadInfoBean.status = Constant.UPLOAD_ERROR
                UploadCacheManger.instance.update(uploadInfoBean)
                EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onError, uploadInfoBean))
                stopSelf()
            }

        }, params, TAG)
    }

    /**
     * 上传视频成功后更新任务
     * UserActionType=21(分享),12(签到),18(上传视频)
     */
    private fun updateTask() {
        val params = HttpParams()
        params.put("UserActionType", "18")
        HttpRequest.post(RequestUrls.UPD_TASK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                stopSelf()
            }

            override fun onError(response: String?, errorMsg: String?) {
                stopSelf()
            }

        }, params, tag = TAG)
    }

    /**
     * 断开重新上传，获取已上传的大小
     */
    private fun checkUploadFile(uploadInfoBean: UploadCacheBean) {
        if (uploadInfoBean.chunk == uploadInfoBean.chunks) {
            saveUploadFile(uploadInfoBean)
            return
        }
        val params = HttpParams()
        params.put("uniqueFileID", uploadInfoBean.fileId)
        GlobalValue.userInfoBean?.token?.uid?.let { params.put("uid", it) }
        val url = uploadInfoBean.uploadUrl + "?good=true"
        OkGo.post<String>(url).tag(uploadInfoBean.path).params(params)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    if (response.body().isNotEmpty()) {
                        LogUtils.eTag(TAG, "上一次进度：${uploadInfoBean.progress}");
                        uploadInfoBean.progress = response.body().toLong()
                        LogUtils.eTag(TAG, "最新进度：${uploadInfoBean.progress}");
                        uploadFile(uploadInfoBean, true)
                    }
                }

                override fun onError(response: Response<String>) {
                    uploadInfoBean.status = Constant.UPLOAD_ERROR
                    UploadCacheManger.instance.update(uploadInfoBean)
                    EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onError, uploadInfoBean))
                    stopSelf()
                }
            })
    }

    private fun restoreAll() {
        //从数据库中恢复数据
        val uploadList = UploadMgr.getUploadList()
        if (uploadList.isNotEmpty()) {
            val uploadCacheBean = uploadList[0]
            uploadCacheBean.status = Constant.UPLOADING
            UploadCacheManger.instance.update(uploadCacheBean)
            checkUploadFile(uploadCacheBean)
            EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onStart, uploadCacheBean))
        } else {
            stopSelf()
        }
    }

    private fun getFileName(path: String): String {
        if (path.isEmpty()) {
            return ""
        }
        var fileName = ""
        val startIndex = path.lastIndexOf("/")
        val endIndex = path.lastIndexOf(".")
        if (startIndex != -1 && endIndex != -1) {
            fileName = path.substring(startIndex + 1, endIndex)
        }
        return fileName
    }

    override fun onDestroy() {
        super.onDestroy()
        OkGo.getInstance().cancelTag(TAG)
        OkGo.getInstance().cancelTag(UPLOAD_TAG)
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        if (networkType == NetworkUtils.NetworkType.NETWORK_WIFI) {
            restoreAll()
        } else {
            val autoNetDownload = SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, false)
            if (autoNetDownload) {
                restoreAll()
            } else {
                OkGo.getInstance().cancelTag(TAG)
                EventBus.getDefault().post(UploadListenerEvent(UploadListenerEvent.onTipsDialog, UploadCacheBean()))
                stopSelf()
            }
        }
    }

    override fun onDisconnected() {
        OkGo.getInstance().cancelTag(TAG)
    }
}