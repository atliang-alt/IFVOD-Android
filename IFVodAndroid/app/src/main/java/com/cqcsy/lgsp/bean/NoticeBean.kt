package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 公告数据bean
 */
class NoticeBean : BaseBean() {
    var id = 0
    var title: String? = null
    var activityContent: String? = null
    var isPublic = false
}