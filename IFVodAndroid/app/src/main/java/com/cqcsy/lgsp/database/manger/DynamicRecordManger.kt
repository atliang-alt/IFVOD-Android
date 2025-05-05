package com.cqcsy.lgsp.database.manger

import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.dao.DynamicRecordDao
import com.cqcsy.lgsp.database.impl.DynamicRecordImpl

/**
 * 动态、相册记录数据库对外操作类
 */
class DynamicRecordManger private constructor() {
    private val dynamicRecordDao: DynamicRecordDao
    /**
     * 添加动态、相册记录数据
     * @param dynamicRecordBean
     */
    fun add(dynamicRecordBean: DynamicRecordBean) {
        dynamicRecordDao.add(dynamicRecordBean)
    }

    /**
     * 更新动态、相册记录数据
     * @param dynamicRecordBean
     */
    fun update(dynamicRecordBean: DynamicRecordBean) {
        dynamicRecordDao.update(dynamicRecordBean)
    }

    /**
     * 删除多条动态或相册记录数据
     * @param list 资源ID集合
     */
    fun delete(list: MutableList<String>) {
        dynamicRecordDao.delete(list)
    }

    /**
     * 删除某个类型所有数据
     * @param uid 用户ID
     */
    fun deleteType(type: Int) {
        dynamicRecordDao.deleteType(type)
    }

    /**
     * 查询某个类型所有记录数据
     * @return
     */
    fun selectAllData(type: Int): MutableList<DynamicRecordBean> {
        return dynamicRecordDao.selectAllData(type)
    }

    /**
     * 查询所有记录数据
     * @return
     */
    fun selectAll(): MutableList<DynamicRecordBean> {
        return dynamicRecordDao.selectAll()
    }

    companion object {
        private var dynamicRecordManger: DynamicRecordManger? = null

        @get:Synchronized
        val instance: DynamicRecordManger
            get() {
                if (dynamicRecordManger == null) {
                    dynamicRecordManger =
                        DynamicRecordManger()
                }
                return dynamicRecordManger!!
            }
    }

    init {
        dynamicRecordDao = DynamicRecordImpl()
    }
}