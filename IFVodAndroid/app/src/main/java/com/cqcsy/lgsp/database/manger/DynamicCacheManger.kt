package com.cqcsy.lgsp.database.manger

import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.dao.DynamicCacheDao
import com.cqcsy.lgsp.database.impl.DynamicCacheImpl

/**
 * 动态数据库对外操作类
 */
class DynamicCacheManger private constructor() {
    private val dynamicCacheDao: DynamicCacheDao

    /**
     * 添加动态数据
     */
    fun add(bean: DynamicCacheBean): Long {
        return dynamicCacheDao.add(bean)
    }

    /**
     * 更新动态、相册记录数据
     */
    fun update(bean: DynamicCacheBean) {
        dynamicCacheDao.update(bean)
    }

    /**
     * 删除多条动态或相册记录数据
     */
    fun deleteAll() {
        dynamicCacheDao.deleteAll()
    }

    fun delete(id: Int) {
        dynamicCacheDao.delete(id)
    }

    fun select(id: Int): MutableList<DynamicCacheBean> {
        return dynamicCacheDao.select(id.toLong())
    }

    fun selectByStatus(status: Int): MutableList<DynamicCacheBean> {
        return dynamicCacheDao.select(status)
    }

    /**
     * 查询某个类型所有记录数据
     * @return
     */
    fun selectAllData(): MutableList<DynamicCacheBean> {
        return dynamicCacheDao.selectAllData()
    }

    companion object {
        private var dynamicRecordManger: DynamicCacheManger? = null

        @get:Synchronized
        val instance: DynamicCacheManger
            get() {
                if (dynamicRecordManger == null) {
                    dynamicRecordManger =
                        DynamicCacheManger()
                }
                return dynamicRecordManger!!
            }
    }

    init {
        dynamicCacheDao = DynamicCacheImpl()
    }
}