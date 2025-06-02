package com.cqcsy.lgsp.event

import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.main.mine.DynamicReleaseStatus

/**
 * 动态事件
 */
class ReleaseDynamicEvent {

    var dynamicBean: DynamicCacheBean? = null
    var action = DynamicReleaseStatus.RELEASING
}