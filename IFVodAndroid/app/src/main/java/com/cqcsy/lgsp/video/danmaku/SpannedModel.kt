package com.cqcsy.lgsp.video.danmaku

import android.graphics.Bitmap
import com.cqcsy.lgsp.bean.BarrageBean

/**
 ** 2022/7/22
 ** des：图文混排
 **/

class SpannedModel(
    danmakuId: Long,
    textSize: Int,
    barrageBean: BarrageBean,
    score: Int = 0,
    danmakuStyle: Int = DanmakuStyle.DANMAKU_STYLE_SPAN,
    rank: Int = 0,
    mergedType: Int = MERGED_TYPE_NORMAL
) : BaseDanmakuItemData(danmakuId, textSize, barrageBean, score, danmakuStyle, rank, mergedType) {
    val isBigV: Boolean
        get() {
            return barrageBean.bigV
        }
    var userImage: Bitmap? = null
    val nickName: String?
        get() {
            return when (barrageBean.prefix) {
                0 -> "${barrageBean.nickName}:"
                1 -> barrageBean.nickName
                else -> null
            }
        }
    val location: String?
        get() {
            return when (barrageBean.prefix) {
                0, 2 -> barrageBean.country
                else -> null
            }
        }
}