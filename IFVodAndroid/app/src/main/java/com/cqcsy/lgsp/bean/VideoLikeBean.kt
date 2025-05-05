package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 视频点赞、不喜欢、收藏的数据Bean
 */
class VideoLikeBean: BaseBean() {
    // 点赞数量
    var count: Int = 0
    // 点赞状态
    var selected: Boolean = false
}