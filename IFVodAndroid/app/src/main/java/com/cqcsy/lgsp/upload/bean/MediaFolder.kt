package com.cqcsy.lgsp.upload.bean

import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.library.base.BaseBean

data class MediaFolder(
    val bucketId: Long,
    val name: String?,
    var images: MutableList<LocalMediaBean>? = null,
    var cover: String? = null
) : BaseBean() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaFolder

        if (bucketId != other.bucketId) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bucketId.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}