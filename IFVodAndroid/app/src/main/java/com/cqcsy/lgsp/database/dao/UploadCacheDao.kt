package com.cqcsy.lgsp.database.dao

import com.cqcsy.lgsp.database.bean.UploadCacheBean

/**
 * 上传视频缓存到本地数据
 */
interface UploadCacheDao {
    fun add(uploadCacheBean: UploadCacheBean)
    fun update(uploadCacheBean: UploadCacheBean)
    fun delete(parentPath: String)
    fun delete()
    fun select(parentPath: String): UploadCacheBean?
    fun select(status: Int): MutableList<UploadCacheBean>
    fun select(): MutableList<UploadCacheBean>
}