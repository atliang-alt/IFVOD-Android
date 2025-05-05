package com.cqcsy.lgsp.database.bean

import com.danikula.videocache.file.Md5FileNameGenerator
import java.io.Serializable

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/26
 *
 *
 */
class DynamicCacheBean : Serializable {
    /**
     * 主键id
     */
    var id: Int = -1

    /**
     * 动态id
     */
    var dynamicId: Int = 0

    var userId: Int = 0

    var status: Int = 1

    /**
     * 视频封面本地路径
     */
    var coverPath: String = ""

    /**
     * 封面比例
     */
    var ratio: String = ""

    /**
     * 视频封面链接
     */
    var coverUrl: String = ""

    /**
     * 动态视频原路径
     */
    var videoPath: String = ""

    /**
     * 动态视频压缩路径
     */
    var videoCompressPath: String = ""

    /**
     * 视频大小
     */
    var videoSize: Long = 0

    /**
     * 视频id
     */
    var videoId: Int = 0

    /**
     * 动态描述
     */
    var description: String = ""

    /**
     * 动态创建时间
     */
    var createTime: String = ""

    /**
     * 动态类型1：图片 2：视频
     */
    var dynamicType: Int = 1

    /**
     * 发布进度
     */
    var progress: Int = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var labels: String = ""
    var address: String = ""
    var detailedAddress: String = ""

    val localDynamicKey: String
        get() {
            return Md5FileNameGenerator().generate("$userId$id$coverPath$videoPath$createTime")
        }

    /**
     * 动态图片集合
     */
    var trendsList: String = ""
    override fun toString(): String {
        return "DynamicCacheBean(id=$id, userId=$userId, status=$status, coverPath='$coverPath', videoPath='$videoPath', videoSize=$videoSize, videoId=$videoId, description='$description', createTime='$createTime', dynamicType=$dynamicType, progress=$progress, latitude=$latitude, longitude=$longitude, labels='$labels', address='$address', detailedAddress='$detailedAddress', trendsList='$trendsList')"
    }

    val imageRatioValue: Float
        get() {
            if (ratio.isEmpty()) {
                return 0f
            }
            val ratio = ratio
            return try {
                val str = ratio.split(":")
                val imageWidth = str[0].toInt().toFloat()
                val imageHeight = str[1].toInt().toFloat()
                imageWidth / imageHeight
            } catch (e: Exception) {
                1f
            }
        }
}