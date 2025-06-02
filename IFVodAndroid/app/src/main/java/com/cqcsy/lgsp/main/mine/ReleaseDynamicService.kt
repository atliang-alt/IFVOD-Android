package com.cqcsy.lgsp.main.mine

import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.event.ReleaseDynamicEvent
import com.cqcsy.lgsp.event.UploadEvent
import com.cqcsy.lgsp.event.UploadListenerEvent
import com.cqcsy.lgsp.upload.UploadService
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.base.BaseService
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.CachePathUtils
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hw.videoprocessor.VideoProcessor
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/26
 *
 * 动态发布服务
 */
class ReleaseDynamicService : BaseService() {

    companion object {
        const val DYNAMIC_INFO = "dynamicInfo"
        const val TAG = "ReleaseDynamicService"

        fun start(context: Context, dynamicData: DynamicCacheBean) {
            val intent = Intent(context, ReleaseDynamicService::class.java)
            dynamicData.videoPath = NormalUtil.getAbsolutePath(dynamicData.videoPath) ?: dynamicData.videoPath
            intent.putExtra(DYNAMIC_INFO, dynamicData)
            context.startService(intent)
        }


        fun stop(context: Context) {
            val intent = Intent(context, ReleaseDynamicService::class.java)
            context.stopService(intent)
            val upload = Intent(context, UploadService::class.java)
            context.stopService(upload)
        }
    }

    private var dynamicCacheBean: DynamicCacheBean? = null
    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dynamicCacheBean = intent?.getSerializableExtra(DYNAMIC_INFO) as? DynamicCacheBean
        this.dynamicCacheBean = dynamicCacheBean
        if (dynamicCacheBean?.dynamicType == 1) {
            // TODO: 发布动态图片
        } else if (dynamicCacheBean?.dynamicType == 2) {
            startPublishVideo()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startPublishVideo() {
        val dynamicCacheBean = dynamicCacheBean ?: return
        val list = DynamicCacheManger.instance.select(dynamicCacheBean.id)
        if (list.isEmpty()) {
            val id = DynamicCacheManger.instance.add(dynamicCacheBean)
            this.dynamicCacheBean?.id = id.toInt()
        } else {
            dynamicCacheBean.createTime = TimesUtils.getUTCTime()
            dynamicCacheBean.status = DynamicReleaseStatus.RELEASING
            DynamicCacheManger.instance.update(dynamicCacheBean)
        }
        sendDynamicEvent(DynamicReleaseStatus.RELEASING)
        if (dynamicCacheBean.coverUrl.isNotEmpty()) {
            uploadVideo()
        } else {
            uploadImages(dynamicCacheBean.coverPath)
        }
    }

    private fun uploadImages(imageStr: String) {
        val split = imageStr.split(",")
        if (split.isEmpty()) {
            return
        }
        val list: MutableList<PictureUploadTask> = ArrayList()
        split.forEach {
            if (it.isNotEmpty()) {
                val taskBean = PictureUploadTask()
                taskBean.taskTag = dynamicCacheBean?.id?.toString()
                taskBean.localPath = it
                taskBean.userId = GlobalValue.userInfoBean?.id ?: 0
                list.add(taskBean)
            }
        }
        PictureUploadManager.uploadImage(list)
    }

    /**
     * 上传视频
     */
    private fun uploadVideo() {
        val dynamicData = dynamicCacheBean ?: return
        if (dynamicData.videoId > 0) {
            releaseDynamic(dynamicData.videoId)
            return
        }
        if (dynamicData.videoCompressPath.isNotEmpty() && FileUtils.isFileExists(dynamicData.videoCompressPath)) {
            startUploadService(dynamicData)
            return
        }
        var fileExtension = FileUtils.getFileExtension(dynamicData.videoPath)
        if (fileExtension.isEmpty()) {
            fileExtension = "mp4"
        }
        val compressPath =
            CachePathUtils.getDynamicCompressVideoPath("dynamic_video_${System.currentTimeMillis()}.$fileExtension")
        thread {
            try {
                VideoProcessor.processor(this)
                    .input(dynamicData.videoPath)
                    .output(compressPath)
                    .process()
                dynamicData.videoCompressPath = compressPath
                DynamicCacheManger.instance.update(dynamicData)
                ThreadUtils.runOnUiThread {
                    startUploadService(dynamicData)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                ThreadUtils.runOnUiThread {
                    startUploadService(dynamicData)
                }
            }
        }
    }

    private fun startUploadService(dynamicData: DynamicCacheBean) {
        val upload = UploadCacheBean()
        upload.context = dynamicData.description
        upload.path = dynamicData.videoPath
        upload.imagePath = dynamicData.coverPath
        upload.videoSize = dynamicData.videoSize
        upload.cid = "0,7"
        upload.status = Constant.UPLOAD_WAIT
        val intent = Intent(this, UploadService::class.java)
        intent.putExtra(UploadService.UPLOAD_INFO, upload)
        startService(this, intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadImageEvent(task: PictureUploadTask) {
        if (task.taskTag != dynamicCacheBean?.id?.toString()) {
            return
        }
        when (task.status) {
            PictureUploadStatus.FINISH -> {
                dynamicCacheBean?.let {
                    it.coverUrl = task.imageUrl ?: ""
                    DynamicCacheManger.instance.update(it)
                }
                uploadVideo()
            }

            PictureUploadStatus.ERROR -> {
                if ((task.totalTagSize - task.finishTagSize) == 0) {
                    dynamicCacheBean?.let {
                        sendDynamicEvent(DynamicReleaseStatus.RELEASE_FAIL)
                        it.status = DynamicReleaseStatus.RELEASE_FAIL
                        DynamicCacheManger.instance.update(it)
                    }
                    stopSelf()
                }
            }

            else -> {}
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(uploadEvent: UploadEvent) {
        if (!uploadEvent.event) {
            onTaskError()
        }
    }

    private fun onTaskError() {
        dynamicCacheBean?.let {
            it.status = DynamicReleaseStatus.RELEASE_FAIL
            sendDynamicEvent(DynamicReleaseStatus.RELEASE_FAIL)
            DynamicCacheManger.instance.update(it)
        }
        stopSelf()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadVideoEvent(uploadEvent: UploadListenerEvent) {
        if (uploadEvent.uploadCacheBean.path != dynamicCacheBean?.videoPath) {
            return
        }
        when (uploadEvent.event) {
            UploadListenerEvent.onFinish -> {
                dynamicCacheBean?.let {
                    it.videoId = uploadEvent.uploadCacheBean.lid
                    DynamicCacheManger.instance.update(it)
                }
                releaseDynamic(uploadEvent.uploadCacheBean.lid)
            }

            UploadListenerEvent.onError -> {
                onTaskError()
            }

            UploadListenerEvent.onProgress -> {
                dynamicCacheBean?.let {
                    val bean = uploadEvent.uploadCacheBean
                    val progress = ((bean.progress * 100f) / bean.videoSize).toInt()
                    it.progress = progress
                    DynamicCacheManger.instance.update(it)
                }
            }
        }
    }

    private fun sendDynamicEvent(status: Int) {
        val event = ReleaseDynamicEvent()
        event.action = status
        EventBus.getDefault().post(event)
    }

    private fun releaseDynamic(videoId: Int? = null) {
        val dynamicData = dynamicCacheBean ?: return
        val param = HttpParams()
        param.put("id", dynamicData.dynamicId)
        if (videoId != null) {
            param.put("videoId", videoId)
        }
        param.put("description", dynamicData.description)
        param.put("latitude", dynamicData.latitude)
        param.put("longitude", dynamicData.longitude)
        param.put("trendsDetails", dynamicData.coverUrl)
        param.put("address", dynamicData.address)
        param.put("detailedAddress", dynamicData.detailedAddress)
        param.put("key", dynamicData.localDynamicKey)
        if (dynamicData.labels.isNotEmpty()) {
            param.put("label", dynamicData.labels)
        }
        HttpRequest.post(RequestUrls.RELEASE_DYNAMIC, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    ToastUtils.showLong(R.string.releaseFail)
                    return
                }
                val bean = Gson().fromJson<DynamicBean>(
                    response.toString(),
                    object : TypeToken<DynamicBean>() {}.type
                )
                dynamicCacheBean?.let {
                    DynamicCacheManger.instance.delete(it.id)
                    if (it.videoCompressPath.isNotEmpty()) {
                        FileUtils.delete(it.videoCompressPath)
                    }
                }
                val event = DynamicEvent()
                event.dynamicBean = bean
                if (dynamicData.dynamicId != 0) {
                    event.action = DynamicEvent.DYNAMIC_UPDATE
                } else {
                    event.action = DynamicEvent.DYNAMIC_ADD
                }
                EventBus.getDefault().post(event)
                sendDynamicEvent(DynamicReleaseStatus.RELEASE_SUCCESS)
                stopSelf()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dynamicCacheBean?.let {
                    sendDynamicEvent(DynamicReleaseStatus.RELEASE_FAIL)
                    it.status = DynamicReleaseStatus.RELEASE_FAIL
                    DynamicCacheManger.instance.update(it)
                }
                stopSelf()
            }
        }, param, TAG)
    }

    override fun onDestroy() {
        super.onDestroy()
        OkGo.getInstance().cancelTag(TAG)
        EventBus.getDefault().unregister(this)
    }
}