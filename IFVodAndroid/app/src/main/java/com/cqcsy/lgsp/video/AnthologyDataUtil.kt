package com.cqcsy.lgsp.video

import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.lgsp.video.bean.VideoGroupBean
import java.util.regex.Pattern

/**
 * 选集数据处理类
 */
object AnthologyDataUtil {
    const val total = 50
    const val other = "其他"

    /**
     * 获取分组后的数据集合
     * videoType: 资源类型
     * list: 选集数据
     * episodeId: 需要定位到的选集ID
     */
    fun getGroupData(videoType: Int, list: MutableList<VideoBaseBean>, uniqueId: Int = 0): MutableList<VideoGroupBean> {
        val groupList: MutableList<VideoGroupBean> = ArrayList()
        if (videoType == Constant.VIDEO_TELEPLAY) {
            // 电视剧
            getTeleplay(groupList, list, uniqueId)
        } else {
            // 综艺、记录片、体育
            getYearData(groupList, list, uniqueId)
        }
        groupList.forEach {
            it.isExpand = (it.itemList?.indexOfFirst { item -> item.uniqueID == uniqueId } ?: -1) >= 0
        }
        return groupList
    }

    /**
     * 电视剧的数据处理
     */
    private fun getTeleplay(groupList: MutableList<VideoGroupBean>, list: MutableList<VideoBaseBean>, uniqueId: Int) {
        val titleCount = if (list.size % total == 0) {
            list.size / total
        } else {
            list.size / total + 1
        }
        for (i in 0 until titleCount) {
            val videoGroupBean = VideoGroupBean()
            val index = i * total
            var end = index + total
            if (end > list.size) {
                end = list.size
            }
            val subList = list.subList(index, end)
            var indexTitle = subList[0].episodeTitle ?: ""
            var endTitle = subList[subList.size - 1].episodeTitle ?: ""
            if (indexTitle.isNotEmpty() && indexTitle.indexOf("-") != -1) {
                indexTitle = indexTitle.substring(0, indexTitle.indexOf("-"))
            }
            if (endTitle.isNotEmpty() && endTitle.indexOf("-") != -1) {
                endTitle = endTitle.substring(endTitle.indexOf("-") + 1)
            }
            val key = "$indexTitle - $endTitle" + "集"
            videoGroupBean.groupName = key
            videoGroupBean.itemList = subList

            groupList.add(videoGroupBean)
        }
    }

    /**
     * 综艺、记录片、体育的数据处理
     */
    private fun getYearData(groupList: MutableList<VideoGroupBean>, list: MutableList<VideoBaseBean>, uniqueId: Int) {
        val otherList: MutableList<VideoBaseBean> = ArrayList()
        val otherVideoGroupBean = VideoGroupBean()
        if (list.size < 50) {
            val videoGroupBean = VideoGroupBean()
            videoGroupBean.itemList = list
            groupList.add(videoGroupBean)
            return
        }
        for (i in list.indices) {
            val videoBaseBean = list[i]
            val key = getYear(videoBaseBean.episodeTitle)
            if (key == other) {
                otherList.add(videoBaseBean)
                continue
            }
            if (groupList.filter { it.groupName == key }.isNullOrEmpty()) {
                val videoGroupBean = VideoGroupBean()
                val sonList: MutableList<VideoBaseBean> = ArrayList()
                sonList.add(videoBaseBean)
                videoGroupBean.itemList = sonList
                videoGroupBean.groupName = key
                groupList.add(videoGroupBean)
            } else {
                val videoGroupBean = VideoGroupBean()
                videoGroupBean.groupName = key
                for (j in groupList.indices) {
                    if (groupList[j].groupName == key) {
                        val sonList = groupList[j].itemList
                        sonList?.add(videoBaseBean)
                        videoGroupBean.itemList = sonList
                        groupList[j] = videoGroupBean
                        break
                    }
                }
            }
        }
        if (otherList.isNotEmpty()) {
            otherVideoGroupBean.groupName = other
            otherVideoGroupBean.itemList = otherList
            groupList.add(otherVideoGroupBean)
        }
    }

    private fun getYear(title: String?): String {
        if (title.isNullOrEmpty()) {
            return ""
        }
        val format = "\\d{8}"
        val numPat = Pattern.compile(format)
        val numbMat = numPat.matcher(title)
        return if (numbMat.find()) {
            numbMat.group().substring(0, 4)
        } else {
            other
        }
    }
}