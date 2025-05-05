package com.cqcsy.lgsp.video.danmaku

import android.graphics.Bitmap
import com.cqcsy.lgsp.bean.BarrageBean

/**
 ** 2022/7/22
 ** des：图文混排
 **/

class AdvertModel(
    danmakuId: Long,
    textSize: Int,
    barrageBean: BarrageBean,
    score: Int = 0,
    danmakuStyle: Int = DanmakuStyle.DANMAKU_STYLE_AD,
    rank: Int = 1,
    mergedType: Int = MERGED_TYPE_NORMAL
) : BaseDanmakuItemData(danmakuId, textSize, barrageBean, score, danmakuStyle, rank, mergedType) {
    var userImage: Bitmap? = null
    val nickName: String?
        get() {
            return barrageBean.nickName
        }

    val buttonDesc: String
        get() {
            return barrageBean.advertButtonDesc
        }

    val callback: String?
        get() {
            return barrageBean.advertCallback
        }
    val linkUrl: String?
        get() {
            return barrageBean.advertLinkUrl
        }
    val isReceived: Boolean
        get() {
            return barrageBean.isDrawCoin
        }
    val advertId: Int
        get() {
            return barrageBean.advertId
        }
}