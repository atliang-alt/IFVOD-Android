package com.cqcsy.lgsp.event

/**
 * 1：关注
 * 2：点赞
 * 3：踩
 * 4：收藏
 */
class VideoActionResultEvent {
    companion object {
        const val ACTION_ADD = 100
        const val ACTION_REMOVE = 101
        const val ACTION_ADD_FINISH = 102
        const val ACTION_REFRESH = 103

        const val TYPE_PICTURE = 200
        const val TYPE_DYNAMIC = 201
        const val TYPE_EPISODE = 202
        const val TYPE_SHORT = 203
    }

    var type = 0
    var count = 0
    var selected = false
    var id = "" // 关注就是userId，其他就是mediaKey,评论id，相册id，动态id

    // 是否是评论点赞
    var isCommentLike: Boolean = false

    // 关注使用字段
    var userName: String = ""
    var userLogo: String = ""
    var action = ACTION_ADD
    var actionType = TYPE_PICTURE
}