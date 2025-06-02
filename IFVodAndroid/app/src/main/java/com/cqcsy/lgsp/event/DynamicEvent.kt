package com.cqcsy.lgsp.event

import com.cqcsy.lgsp.bean.DynamicBean

/**
 * 动态事件
 */
class DynamicEvent {
    companion object {
        const val DYNAMIC_ADD = 100
        const val DYNAMIC_REMOVE = 101
        const val DYNAMIC_UPDATE = 102
    }
    var dynamicBean: DynamicBean? = null
    var action = DYNAMIC_ADD
    var mediaKey: String = ""
}