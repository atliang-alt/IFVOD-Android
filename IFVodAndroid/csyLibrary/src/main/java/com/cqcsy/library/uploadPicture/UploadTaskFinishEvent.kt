package com.cqcsy.library.uploadPicture

import org.json.JSONObject

/**
 * 图片上传成功后，后面调用保存后事件
 */
class UploadTaskFinishEvent {
    var isSuccess = false
    var task: PictureUploadTask? = null
    var response: JSONObject? = null
}