package com.cqcsy.lgsp.database.dao

import com.cqcsy.lgsp.database.bean.WatchRecordBean

interface WatchRecordDao {
    fun add(watchRecordBean: WatchRecordBean)
    fun update(watchRecordBean: WatchRecordBean)
    fun delete(list: MutableList<String>, uid: Int)
    fun deleteAll(videoType: Int)
    fun deleteNotShortVideo()
    fun selectAllData(uid: Int): MutableList<WatchRecordBean>
    fun selectAllData(uid: Int, status: Int): MutableList<WatchRecordBean>
}