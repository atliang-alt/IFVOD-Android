package com.cqcsy.lgsp.database.bean

import com.cqcsy.library.base.BaseBean

/**
 * 动态、相册数据库对象
 */
class DynamicRecordBean: BaseBean() {
//     动态、相册id 统一使用mediakey
//    var pid: Int = 0
    // 动态、相册mediaKey
    var mediaKey: String = ""
    // 用户头像
    var headImg: String = ""
    // 用户昵称
    var upperName: String = ""
    // 创建时间
    var createTime: String = ""
    // 标题
    var title: String = ""
    // 描述
    var description: String = ""
    // 封面图
    var coverPath: String = ""
    // 动态图片集合
    var trendsDetails: String = ""
    // 动态显示地址
    var address: String = ""
    // 图片数量
    var photoCount: Int = 0
    // 评论数量
    var comments: Int = 0
    // 点赞数量
    var likeCount: Int = 0
    // 用户ID
    var uid: Int = 0
    // 记录时间
    var recordTime: String = ""
    // 类型 0:动态 , 1:相册,2 动态视频
    var type: Int = 0

    // 是否是大V
    var bigV: Boolean = false

    // VIP等级
    var vipLevel: Int = 0
}