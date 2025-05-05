package com.cqcsy.lgsp.database.dao

import com.cqcsy.lgsp.database.bean.DynamicRecordBean

interface DynamicRecordDao {
    fun add(dynamicRecordBean: DynamicRecordBean)
    fun update(dynamicRecordBean: DynamicRecordBean)
    fun delete(list: MutableList<String>)
    fun deleteType(type: Int)
    fun selectAllData(type: Int): MutableList<DynamicRecordBean>
    fun selectAll(): MutableList<DynamicRecordBean>
}