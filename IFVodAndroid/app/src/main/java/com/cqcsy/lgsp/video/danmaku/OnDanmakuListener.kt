package com.cqcsy.lgsp.video.danmaku

import com.kuaishou.akdanmaku.data.DanmakuItem

/**
 ** 2022/9/1
 ** des：弹幕点击
 **/

interface OnDanmakuListener {

    fun onDanmakuClickListener(x: Float, y: Float, item: MutableList<DanmakuItem>)
}