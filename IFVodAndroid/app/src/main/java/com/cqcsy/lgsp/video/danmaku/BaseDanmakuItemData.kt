package com.cqcsy.lgsp.video.danmaku

import android.graphics.Color
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.kuaishou.akdanmaku.data.DanmakuItemData

/**
 * 作者：wangjianxiong
 * 创建时间：2023/2/9
 *
 *
 */
open class BaseDanmakuItemData(
    danmakuId: Long,
    textSize: Int,
    var barrageBean: BarrageBean,
    score: Int = 0,
    danmakuStyle: Int = DanmakuStyle.DANMAKU_STYLE_COMMON,
    rank: Int = 0,
    mergedType: Int = MERGED_TYPE_NORMAL,
) : DanmakuItemData(
    danmakuId,
    (barrageBean.second * 1000).toLong(),
    barrageBean.contxt,
    when (barrageBean.position) {
        0 -> DANMAKU_MODE_ROLLING
        1 -> DANMAKU_MODE_CENTER_TOP
        2 -> DANMAKU_MODE_CENTER_BOTTOM
        else -> DANMAKU_MODE_ROLLING
    },
    textSize,
    if (!NormalUtil.isColor(barrageBean.color))
        Color.parseColor("#ffffff")
    else
        Color.parseColor(barrageBean.color),
    score,
    danmakuStyle,
    rank,
    barrageBean.uid.toLong(),
    mergedType
)