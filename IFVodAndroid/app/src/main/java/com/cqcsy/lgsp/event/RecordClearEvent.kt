package com.cqcsy.lgsp.event

/**
 * 历史记录清空事件
 */
class RecordClearEvent(var type: Int) {
    companion object {
        const val TYPE_EPISODE = 100
        const val TYPE_SHORT = 101
        const val TYPE_PICTURE = 102
        const val TYPE_DYNAMIC = 103
    }
}