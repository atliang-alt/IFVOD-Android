package com.cqcsy.library.bean

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.utils.GlobalValue

class ChatMessageBean : BaseBean(), MultiItemEntity {
    var context: String = ""
    var fromUid: Int = 0    // 发送者
    var avatar: String = ""
    var sendTime: String = ""
    var nickname: String = ""
    var toUid: Int = 0
    var id: Int = 0
    var messageType: Int = 1 // 1表示文字 8表示图片
    var pictureStatus = PictureUploadStatus.WAITING // 图片上传状态
    var localPath: String? = null
    var type: Int = 0    //  0：普通消息  1：客服消息
    var isRemind = 0   // 1提醒  0不提醒

    var messageCount: Int = 1

    override val itemType: Int
        get() = if (fromUid == GlobalValue.userInfoBean?.id) 1 else 2
}