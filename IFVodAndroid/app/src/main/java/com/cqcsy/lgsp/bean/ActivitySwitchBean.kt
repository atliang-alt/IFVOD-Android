package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean

/**
 * 全局需要获取控制的开关数据bean
 */
class ActivitySwitchBean : BaseBean() {
    var status: Boolean = false
    var key: String = ""
    var content: Any? = null
}