package com.cqcsy.lgsp.event

import com.cqcsy.lgsp.database.bean.UploadCacheBean

/**
 * 上传监听事件
 */
class UploadListenerEvent {
    var event: String = ""
    var uploadCacheBean: UploadCacheBean

    companion object {
        const val onFinish = "onFinish"
        const val onProgress = "onProgress"
        const val onError = "onError"
        const val onPause = "onPause"
        const val onStart = "onStart"
        const val onTipsDialog = "onTipsDialog"
    }

    constructor(event: String, uploadCacheBean: UploadCacheBean) {
        this.event = event
        this.uploadCacheBean = uploadCacheBean
    }
}