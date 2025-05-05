package com.cqcsy.lgsp.database.manger

import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.dao.UploadCacheDao
import com.cqcsy.lgsp.database.impl.UploadCacheImpl

/**
 * 上传视频本地缓存数据库对外操作类
 */
class UploadCacheManger private constructor() {
    private val uploadCacheDao: UploadCacheDao
    /**
     * 添加上传视频本地缓存
     * @param uploadCacheBean
     */
    fun add(uploadCacheBean: UploadCacheBean) {
        uploadCacheDao.add(uploadCacheBean)
    }

    /**
     * 更新上传视频本地缓存
     * @param uploadCacheBean
     */
    fun update(uploadCacheBean: UploadCacheBean) {
        uploadCacheDao.update(uploadCacheBean)
    }

    /**
     * 删除上传视频本地缓存
     * @param parentPath 视频原始路径
     */
    fun delete(parentPath: String) {
        uploadCacheDao.delete(parentPath)
    }

    /**
     * 删除所有上传视频本地缓存
     */
    fun delete() {
        uploadCacheDao.delete()
    }

    /**
     * 查询上传视频本地缓存
     * @param parentPath 视频本地路径
     */
    fun select(parentPath: String): UploadCacheBean? {
        return uploadCacheDao.select(parentPath)
    }

    /**
     * 查询已上传或未上传的视频本地缓存
     * @param status
     */
    fun select(status: Int): MutableList<UploadCacheBean> {
        return uploadCacheDao.select(status)
    }

    /**
     * 查询已上传或未上传的视频本地缓存
     */
    fun select(): MutableList<UploadCacheBean> {
        return uploadCacheDao.select()
    }

    companion object {
        private var uploadCacheManger: UploadCacheManger? = null
        @get:Synchronized
        val instance: UploadCacheManger
            get() {
                if (uploadCacheManger == null) {
                    uploadCacheManger =
                        UploadCacheManger()
                }
                return uploadCacheManger!!
            }
    }

    init {
        uploadCacheDao = UploadCacheImpl()
    }
}