package com.cqcsy.lgsp.bean

import com.cqcsy.lgsp.medialoader.MimeTypeUtil
import com.cqcsy.library.base.BaseBean
import com.luck.picture.lib.entity.LocalMedia

/**
 * 获取本地媒体数据bean
 * 图片、视频
 */
class LocalMediaBean : BaseBean() {
    // 数据ID
    var id: Long = 0
    var bucketId: Long = 0

    // 数据名字
    var name: String = ""

    // 照片、视频拍摄时间
    var addTime: Long = 0

    // 视频时长
    var duration: Long = 0

    // 资源本地路径地址
    var path: String = ""

    // 资源大小
    var size: Long = 0

    // 宽
    var width: Int = 0

    // 高
    var height: Int = 0

    // 图片类型
    var mimeType: String = ""

    var isChecked: Boolean = false

    var index = -1

    val isVideo: Boolean
        get() {
            return MimeTypeUtil.isVideo(mimeType)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalMediaBean

        if (id != other.id) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    fun copy(localMedia: LocalMedia): LocalMediaBean {
        id = localMedia.id
        bucketId = localMedia.bucketId
        duration = localMedia.duration
        name = localMedia.fileName
        mimeType = localMedia.mimeType
        path = localMedia.path
        size = localMedia.size
        addTime = localMedia.dateAddedTime
        width = localMedia.width
        height = localMedia.height
        return this
    }
}