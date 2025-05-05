package com.cqcsy.lgsp.database.dao

import com.cqcsy.lgsp.database.bean.DynamicCacheBean

interface DynamicCacheDao {
    fun add(dynamicRecordBean: DynamicCacheBean): Long
    fun update(dynamicRecordBean: DynamicCacheBean)
    fun delete(id: Int)
    fun deleteAll()
    fun select(id: Long): MutableList<DynamicCacheBean>
    fun select(status: Int): MutableList<DynamicCacheBean>
    fun selectAllData(): MutableList<DynamicCacheBean>
}