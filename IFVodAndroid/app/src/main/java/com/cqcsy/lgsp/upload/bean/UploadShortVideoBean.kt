package com.cqcsy.lgsp.upload.bean

import com.cqcsy.library.base.BaseBean

/**
 * 已上传的小视频列表数据Bean
 */
class UploadShortVideoBean: BaseBean() {
    // 数据ID
    var mediaId: Int = 0
    // 媒体播放链接
    var mediaUrl: String = ""
    // 封面图片地址
    var coverImgUrl: String = ""
    // 标题
    var title: String = ""
    // 短视频长度
    var duration: String = ""
    // 短视频播放量
    var playCount: Int = 0
    // 点赞数量
    var likeCount: String = ""
    // 短视频更新时间
    var date: String = ""
    // 发布状态 0:已发布、1:审核中、2:不通过
    var status: Int = 0
    // 如果是不通过状态，不通过原因描述
    var reason: String = ""
    // 是否热播
    var isHot: Boolean = false
}