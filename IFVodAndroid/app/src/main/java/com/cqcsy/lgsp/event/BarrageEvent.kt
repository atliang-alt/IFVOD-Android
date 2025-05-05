package com.cqcsy.lgsp.event

import com.cqcsy.lgsp.bean.BarrageBean

enum class BarrageType {
    EVENT_BARRAGE, EVENT_CHAT, EVENT_ONLINE, EVENT_LOCAL
}

/**
 * 弹幕、直播聊天、数量更新事件
 */
class BarrageEvent {
    var eventType = BarrageType.EVENT_BARRAGE
    var message: BarrageBean? = null
    var onlineNumber = -1
}