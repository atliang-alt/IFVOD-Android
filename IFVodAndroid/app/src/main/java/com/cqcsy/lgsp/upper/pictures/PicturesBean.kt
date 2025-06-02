package com.cqcsy.lgsp.upper.pictures

import com.cqcsy.library.base.BaseBean

/**
 * 相册对象
 */
class PicturesBean : BaseBean() {
    var coverPath: String? = null
    var createTime: String = ""
    var mediaKey: String = ""
    var likeCount: Int = 0
    var description: String? = null
//    var id: Int = 0
    var videoType: Int = 0
    var title: String = ""
    var uid: Int = 0
    var viewCount: Int = 0
    var sort: Int = 1
    var loadRes = 0

    // 照片总数量
    var photoCount: Int = 0

    // 评论数量
    var comments: Int = 0
    var like: Boolean = false

    // 发布者头像
    var headImg: String? = ""

    // 是否是大V
    var bigV: Boolean = false

    // VIP等级
    var vipLevel: Int = 0

    // 用户昵称
    var upperName: String? = ""

    // 是否关注当前发布者
    var focus: Boolean = false

    // 粉丝数
    var fansCount: Int = 0

    // 收藏
    var isCollected: Boolean = false

    // 标签
    var label: String? = ""
    // 是否失效
    var isUnAvailable: Boolean = false
}