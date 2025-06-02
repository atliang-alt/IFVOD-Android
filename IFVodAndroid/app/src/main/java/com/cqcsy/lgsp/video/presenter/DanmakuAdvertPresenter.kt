package com.cqcsy.lgsp.video.presenter

import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.BarrageBean
import kotlin.math.min

/**
 * 作者：wangjianxiong
 * 创建时间：2023/4/7
 *
 *
 */
class DanmakuAdvertPresenter {

    /**
     * 是否准备完毕
     */
    var isPrepared: Boolean = false

    /**
     * 是否装载完毕
     */
    var loadCompleted: Boolean = false

    /**
     * 广告数据
     */
    var danmakuAdList: MutableList<AdvertBean>? = null
    private val timeNodes: MutableList<Long> = mutableListOf()

    companion object {
        const val MAX_COUNT = 15
        const val TIME_NODE = MAX_COUNT * 60 * 1000
    }

    /**
     * @param duration 视频时长
     * 如果当前视频准备完毕，开始解析数据，转换为实际显示的弹幕数据
     */
    fun getAdBarrageList(duration: Long): MutableList<BarrageBean>? {
        val adList = danmakuAdList
        if (adList.isNullOrEmpty()) {
            return null
        }
        if (duration <= 0) {
            return null
        }
        if (!isPrepared) {
            return null
        }
        val list = mutableListOf<BarrageBean>()
        val count = min(adList.size, MAX_COUNT)
        val timeCount = duration / TIME_NODE
        timeNodes.clear()
        for (i in 0 until timeCount) {
            for (j in 0 until count) {
                val item = adList[j]
                val array = item.title?.split("#")
                var name = ""
                var title = ""
                var buttonDesc = ""
                if (!array.isNullOrEmpty()) {
                    array.forEachIndexed { z, s ->
                        //防止解析时发生异常
                        when (z) {
                            0 -> {
                                name = s
                            }
                            1 -> {
                                title = s
                            }
                            2 -> {
                                buttonDesc = s
                            }
                        }
                    }
                }
                val node = MAX_COUNT - adList.size + j + 1
                val seconds = (i * MAX_COUNT + node) * 60
                timeNodes.add(seconds)
                val bean = BarrageBean().apply {
                    color = "#ffffff"
                    contxt = title
                    nickName = name
                    guid = item.bannerId.toString()
                    position = 0
                    type = 2
                    second = seconds.toDouble()
                    guid = "${item.bannerId}_${i}_$j"
                    advertButtonDesc = buttonDesc
                    isDrawCoin = item.isDrawCoin
                    isAdvert = true
                    avatar = item.showURL
                    advertId = item.bannerId
                    advertCallback = item.viewURL
                    advertLinkUrl = item.linkURL
                }
                list.add(bean)
            }
        }
        return list
    }

    fun reset() {
        isPrepared = false
        loadCompleted = false
        danmakuAdList = null
    }

    fun refreshTimeNodes(duration: Int) {
        if (!isPrepared) {
            return
        }
        val adList = danmakuAdList
        if (adList.isNullOrEmpty()) {
            return
        }
        timeNodes.clear()
        val count = min(adList.size, MAX_COUNT)
        val timeCount = duration / TIME_NODE
        for (i in 0 until timeCount) {
            for (j in 0 until count) {
                val node = MAX_COUNT - adList.size + j + 1
                val second = (i * MAX_COUNT + node) * 60L
                timeNodes.add(second)
            }
        }
    }

    /**
     * 进行时间对比
     * @param mills 当前视频进度
     */
    fun compare(mills: Int): Boolean {
        if (timeNodes.isEmpty()) {
            return false
        }
        for (time in timeNodes) {
            if (mills / 1000L == time) {
                return true
            }
        }
        return false
    }
}