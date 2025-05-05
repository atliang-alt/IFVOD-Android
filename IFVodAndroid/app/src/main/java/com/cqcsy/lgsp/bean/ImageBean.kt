package com.cqcsy.lgsp.bean

import com.cqcsy.library.base.BaseBean
import com.cqcsy.library.utils.ImageUtil

/**
 * 图片对象
 */
class ImageBean : BaseBean() {
    // 图片id
    var id: Int = 0
    var refID: Int = 0

    // 相册id
    var mediaKey: String? = null

    // 相册标题
    var title: String = ""

    // 状态
    var deleteFlag: Int = 0

    // 创建时间
    var createTime: String = ""

    // 图片地址
    var imgPath: String? = null

    /**
     * 图片比例
     */
    var ratio: String? = null

    val ratioValue: Double
        get() {
            val ratio = ratio ?: return 0.0
            return try {
                val str = ratio.split(":")
                val imageWidth = str[0].toInt()
                val imageHeight = str[1].toInt()
                imageWidth / imageHeight.toDouble()
            } catch (e: Exception) {
                0.0
            }
        }

    val isLongImage: Boolean
        get() {
            return if (ratio != null) {
                try {
                    val str = ratio!!.split(":")
                    val imageWidth = str[0].toInt()
                    val imageHeight = str[1].toInt()
                    ImageUtil.isLongImage(imageWidth, imageHeight)
                } catch (e: Exception) {
                    false
                }
            } else {
                false
            }
        }
}