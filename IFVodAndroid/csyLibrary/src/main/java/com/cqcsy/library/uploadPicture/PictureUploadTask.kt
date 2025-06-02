package com.cqcsy.library.uploadPicture

import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.network.BaseUrl
import com.lzy.okgo.model.HttpParams

/**
 * 图片上传任务
 */
class PictureUploadTask : BaseBean() {
    /*外部必传参数*/
    var localPath: String = ""  // 本地图片路径，只能在初始化的时候赋值，其他地方不能再次赋值
    var userId: Int = 0
    var uploadUrl = BaseUrl.PICTURE_UPLOAD

    /*任务标记，用于任务传完后，发送消息到外部区分*/
    var taskTag: String? = null
    var totalTagSize: Int = 0
    var finishTagSize: Int = 0
    var progress: Float = 0f    // 当前任务进度
    var imageUrl: String? = null   // 上传后的图片地址

    /*上传任务中需要用到的字段，外部不需要*/
    var currentChunk: Int = 0
    var totalChunk: Int = 0
    var totalSize: Long = 0
    var compressPath: String = ""   // 压缩后图片路径
    var uploadPath: String = ""     // 当前上传图片路径
    var serverFileName: String = ""
    var status = PictureUploadStatus.WAITING

    var requestUrl: String? = null
    var params: HttpParams? = null
}