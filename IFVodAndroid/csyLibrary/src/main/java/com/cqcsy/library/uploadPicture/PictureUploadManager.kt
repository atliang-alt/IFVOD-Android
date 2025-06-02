package com.cqcsy.library.uploadPicture

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.library.compress.Luban
import com.cqcsy.library.network.BaseUrl
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.CachePathUtils
import com.cqcsy.library.utils.ImageUtil
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import com.lzy.okgo.model.Progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.*
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.LinkedTransferQueue

/**
 * 图片上传
 */
object PictureUploadManager {
    private val taskQueue: Queue<PictureUploadTask> = LinkedTransferQueue()
    private const val BLOCK_SIZE = 2 * 1024 * 1024
    private var mCurrentTask: PictureUploadTask? = null
    private const val IMAGE_KEY = "imageKey"

    /**
     * 获取文件绝对路径
     */
    fun getAbsolutePath(path: String?): String? {
        var result = path
        try {
            val uri = Uri.parse(path)
            if (DeviceUtils.getSDKVersionCode() >= Build.VERSION_CODES.Q && uri.scheme == "content") {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = Utils.getApp().contentResolver.query(uri, proj, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    result = cursor.getString(index)
                }
                cursor?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    @Synchronized
    fun uploadImage(task: PictureUploadTask) {
        CoroutineScope(Dispatchers.IO).launch {
            task.localPath = getAbsolutePath(task.localPath) ?: task.localPath
            taskQueue.add(task)
            computeTagSize(task)
            startNextTask()
        }
    }

    @Synchronized
    fun uploadImage(taskList: MutableList<PictureUploadTask>) {
        CoroutineScope(Dispatchers.IO).launch {
            taskList.forEach {
                it.localPath = getAbsolutePath(it.localPath) ?: it.localPath
            }
            taskQueue.addAll(taskList)
            computeTagSize(taskList)
            startNextTask()
        }
    }

    /**
     * 根据tag移除任务
     */
    fun removeTaskByTag(tag: String?) {
        if (tag.isNullOrEmpty()) return
        taskQueue.removeAll { it.taskTag == tag }
        if (mCurrentTask?.taskTag == tag) {
            OkGo.getInstance().cancelTag(tag)
            mCurrentTask = null
            startNextTask()
        }
    }

    fun removeTaskByTags(tags: List<String>) {
        taskQueue.removeAll { tags.contains(it.taskTag) }
        if (tags.contains(mCurrentTask?.taskTag)) {
            OkGo.getInstance().cancelTag(mCurrentTask?.taskTag)
            mCurrentTask = null
            startNextTask()
        }
    }

    /**
     * 根据tag获取任务信息
     */
    fun getTaskInfoByTag(tag: String?): PictureUploadTask? {
        if (tag.isNullOrEmpty()) return null
        if (mCurrentTask?.taskTag == tag) {
            return mCurrentTask
        }
        taskQueue.forEach {
            if (it.taskTag == tag) {
                return it
            }
        }
        return null
    }

    /**
     * 根据tag获取任务信息集合
     */
    fun getTaskListByTag(tag: String?): MutableList<PictureUploadTask> {
        val list: MutableList<PictureUploadTask> = ArrayList()
        if (tag.isNullOrEmpty()) return list
        if (mCurrentTask?.taskTag == tag) {
            list.add(mCurrentTask!!)
        }
        taskQueue.forEach {
            if (it.taskTag == tag) {
                list.add(it)
                LogUtils.e(it)
                LogUtils.e(it.status)
            }
        }
        return list
    }

    /**
     * 设置离开页面后，图片上传完成，需要额外发送的请求，仅支持post
     * 其中params中必须包含 IMAGE_KEY:XXXX;用于图片地址key
     * @see PictureUploadManager#IMAGE_KEY
     */
    fun setTaskBackgroundRequest(tag: String, requestUrl: String, params: HttpParams) {
        if (!params.urlParamsMap.containsKey(IMAGE_KEY) || params.urlParamsMap[IMAGE_KEY] == null || params.urlParamsMap[IMAGE_KEY]!!.size == 0) {
            return
        }
        if (mCurrentTask != null && mCurrentTask!!.taskTag == tag) {
            mCurrentTask!!.requestUrl = requestUrl
            mCurrentTask!!.params = params
        }
        taskQueue.forEach {
            if (it.taskTag == tag) {
                it.requestUrl = requestUrl
                it.params = params
            }
        }
    }

    fun removeTaskBackgroundRequest(tag: String) {
        if (mCurrentTask != null && mCurrentTask!!.taskTag == tag) {
            mCurrentTask!!.requestUrl = null
            mCurrentTask!!.params = null
        }
        taskQueue.forEach {
            if (it.taskTag == tag) {
                it.requestUrl = null
                it.params = null
            }
        }
    }

    /**
     * 生成服务器文件名
     */
    fun generateServerFileName(localPath: String): String {
        var extension = FileUtils.getFileExtension(localPath)
        if (extension == "webp") {
            extension = "jpg"
        }
        return EncryptUtils.encryptMD5ToString(localPath + System.currentTimeMillis()) + ".$extension"
    }

    private fun computeTagSize(addList: MutableList<PictureUploadTask>) {
        addList.forEach {
            computeTagSize(it)
        }
    }

    private fun computeTagSize(task: PictureUploadTask) {
        if (mCurrentTask != null && mCurrentTask?.taskTag == task.taskTag) {
            mCurrentTask!!.totalTagSize++
        }
        val taskList = taskQueue.filter { it.taskTag == task.taskTag }.sortedByDescending { it.totalTagSize }
        if (taskList.isNotEmpty()) {
            val maxSize = taskList[0].totalTagSize
            taskList.forEach {
                it.totalTagSize = maxSize + 1
            }
        }
    }

    private fun startNextTask() {
        if (mCurrentTask == null && taskQueue.isNotEmpty()) {
            mCurrentTask = taskQueue.poll()
            startUpload(mCurrentTask!!)
        }
    }

    private fun startUpload(task: PictureUploadTask) {
        if (task.localPath.isEmpty()) {
            sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
            return
        }
        if (task.localPath.endsWith(".gif", true)) {
            task.uploadPath = task.localPath
            task.compressPath = task.localPath
        } else {
            try {
                val imageFiles = Luban.with(Utils.getApp())
                    .load(task.localPath)
                    .ignoreBy(200)
                    .setTargetDir(CachePathUtils.getImageCachePath())
                    .filter { path -> path.isNotEmpty() }.get()
                if (imageFiles.size > 0) {
                    var path = imageFiles[0].absolutePath
                    if (path.endsWith(".webp", true)) {
                        path = ImageUtil.formatImage(path, Bitmap.CompressFormat.JPEG)
                    }
                    task.uploadPath = path
                    task.compressPath = path
                } else {
                    sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
                return
            }
        }
        val fileSize = FileUtils.getLength(task.compressPath)
        task.totalSize = fileSize
        if (task.serverFileName.isEmpty())
            task.serverFileName = generateServerFileName(task.localPath)
        task.currentChunk = 0
        task.totalChunk = (fileSize / BLOCK_SIZE).toInt() + if (fileSize % BLOCK_SIZE > 0) 1 else 0
        if (task.totalChunk > 1) {
            getChunk(task)
        } else {
            uploadImageToServer(task)
        }
    }

    private fun getChunk(task: PictureUploadTask) {
        val file =
            File(CachePathUtils.getImageCachePath() + File.separator + task.serverFileName)
        file.delete()
        val tempFile = getCutFile(
            (task.currentChunk * BLOCK_SIZE).toLong(),
            task.compressPath,
            file.absolutePath,
            BLOCK_SIZE
        )
        if (tempFile != null) {
            task.uploadPath = tempFile.absolutePath
            uploadImageToServer(task)
        } else {
            sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
        }
    }

    private fun uploadImageToServer(task: PictureUploadTask) {
        sendPictureUploadEvent(task, PictureUploadStatus.LOADING)
        val params = HttpParams()
        val file = File(task.uploadPath)
        params.put("file", file, URLEncoder.encode(file.name, "UTF-8"))
        HttpRequest.post(
            task.uploadUrl + "?chunk=${task.currentChunk}&totalChunk=${task.totalChunk}",
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    deleteUploadFile(task)
                    if (task.currentChunk == (task.totalChunk - 1)) {
                        val imageUrl = response?.optString("filepath")
                        if (imageUrl.isNullOrEmpty()) {
                            sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
                        } else {
                            task.imageUrl = imageUrl
                            sendPictureUploadEvent(task, PictureUploadStatus.FINISH)
                        }
                    } else {
                        task.currentChunk++
                        getChunk(task)
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (!errorMsg.isNullOrEmpty()) ToastUtils.showShort(errorMsg)
                    deleteUploadFile(task)
                    sendPictureUploadEvent(task, PictureUploadStatus.ERROR)
                }

                override fun onProgress(progress: Progress) {
                    if (task.totalChunk == 1) {
                        task.progress = progress.fraction
                    } else {
                        task.progress =
                            ((task.currentChunk - 1) * BLOCK_SIZE + progress.currentSize).toFloat() / task.totalSize
                    }
                    sendPictureUploadEvent(task, PictureUploadStatus.LOADING)
                }
            },
            params,
            task.taskTag ?: BaseUrl.PICTURE_UPLOAD
        )
    }

    private fun deleteUploadFile(task: PictureUploadTask) {
        if (task.localPath != task.uploadPath) {
            FileUtils.delete(task.uploadPath)
        }
        if (task.localPath != task.compressPath && (task.totalChunk - 1) == task.currentChunk) {
            FileUtils.delete(task.compressPath)
        }
    }

    private fun sendPictureUploadEvent(
        task: PictureUploadTask,
        status: PictureUploadStatus,
    ) {
        task.status = status
        if (status == PictureUploadStatus.FINISH) {
            task.finishTagSize++
            taskQueue.forEach {
                if (it.taskTag == task.taskTag) {
                    it.finishTagSize = task.finishTagSize
                }
            }
            EventBus.getDefault().post(task)
            if (status == PictureUploadStatus.FINISH) {
                backgroundRequest(task)
            }
            mCurrentTask = null
            startNextTask()
        } else {
            EventBus.getDefault().post(task)
        }
    }

    private fun backgroundRequest(task: PictureUploadTask) {
        if (!task.imageUrl.isNullOrEmpty() && task.status == PictureUploadStatus.FINISH
            && task.requestUrl != null && task.params != null
        ) {
            val key = task.params!!.urlParamsMap[IMAGE_KEY]!![0]
            task.params!!.put(key, task.imageUrl!!)
            HttpRequest.post(
                url = task.requestUrl!!,
                params = task.params!!,
                tag = task.requestUrl!!,
                callBack = object : HttpCallBack<JSONObject>() {
                    override fun onSuccess(response: JSONObject?) {
                        val event = UploadTaskFinishEvent()
                        event.isSuccess = true
                        event.task = task
                        event.response = response
                        EventBus.getDefault().post(event)
                    }

                    override fun onError(response: String?, errorMsg: String?) {
                        val event = UploadTaskFinishEvent()
                        event.isSuccess = false
                        event.task = task
                        EventBus.getDefault().post(event)
                    }

                }
            )
        }
    }

    private fun getBlock(offset: Long, file: File, blockSize: Int): ByteArray? {
        val result = ByteArray(blockSize)
        var accessFile: RandomAccessFile? = null
        try {
            accessFile = RandomAccessFile(file, "r")
            accessFile.seek(offset)
            return when (val readSize = accessFile.read(result)) {
                -1 -> {
                    null
                }

                blockSize -> {
                    result
                }

                else -> {
                    val tmpByte = ByteArray(readSize)
                    System.arraycopy(result, 0, tmpByte, 0, readSize)
                    tmpByte
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close()
                } catch (e1: IOException) {
                }
            }
        }
        return null
    }

    private fun getCutFile(
        offset: Long,
        filePath: String,
        cutFileName: String,
        blockSize: Int
    ): File? {
        val byteArray = getBlock(offset, File(filePath), blockSize)
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        var file: File? = null
        try {
            if (byteArray == null) {
                return file
            }
            val dir = File(cutFileName).parentFile
            if (dir != null && !dir.exists() && dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(cutFileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(byteArray)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            try {
                fos?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return file
    }
}