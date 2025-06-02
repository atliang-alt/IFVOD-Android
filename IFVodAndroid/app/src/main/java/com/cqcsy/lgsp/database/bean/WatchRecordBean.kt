package com.cqcsy.lgsp.database.bean

import com.cqcsy.library.base.BaseBean

class WatchRecordBean: BaseBean() {
    // 数据库ID
    var id: Int = 0
    // 用户ID
    var uid: Int = 0
    // 资源类型
    var videoType: Int = 0
    // 资源ID
    var mediaKey: String = ""
    // 标题名字
    var title: String? = ""
    // 二级标签 地区、语言、类型等
    var contentType: String = ""

    // 类型
    var cidMapper: String = ""

    //地区
    var regional: String = ""

    // 语言
    var lang: String? = ""

    // 子集ID
    var episodeId: Int = 0
    // 子集名字
    var episodeTitle: String? = ""
    // 图片地址
    var coverImgUrl: String = ""
    // 作者
    var upperName: String? = ""

    // 播放地址
    var mediaUrl: String? = ""
    // 观看时间
    var watchTime: String = ""
    // 资源总长度
    var duration: String = ""
    // 资源总长度 单位整型秒
    var time: String = ""
    // 同步状态 0:未同步 1:已同步
    var status: Int = 0
    // 添加的时间 不需要设置，添加数据库时自动获取当前时间
    var recordTime: String = ""
    // 某个视频ID，不区分清晰度
    var uniqueID: Int = 0
    // 添加视频记录地址
    var playRecordUrl: String = ""
}