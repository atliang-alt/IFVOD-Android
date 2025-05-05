package com.cqcsy.lgsp.database.manger

import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.database.dao.WatchRecordDao
import com.cqcsy.lgsp.database.impl.WatchRecordImpl

/**
 * 观看记录数据库对外操作类
 */
class WatchRecordManger private constructor() {
    private val watchRecordDao: WatchRecordDao
    /**
     * 添加观看记录数据
     * @param watchRecordBean
     */
    fun add(watchRecordBean: WatchRecordBean) {
        watchRecordDao.add(watchRecordBean)
    }

    /**
     * 更新观看记录数据
     * @param watchRecordBean
     */
    fun update(watchRecordBean: WatchRecordBean) {
        watchRecordDao.update(watchRecordBean)
    }

    /**
     * 删除多条观看记录数据
     * @param list 资源ID集合
     * @param uid 用户ID
     */
    fun delete(list: MutableList<String>, uid: Int) {
        watchRecordDao.delete(list, uid)
    }

    /**
     * 删除多条观看记录数据
     * @param videoType 视频资源类型 3为小视频
     */
    fun deleteAll(videoType: Int) {
        watchRecordDao.deleteAll(videoType)
    }

    fun deleteNotShortVideo() {
        watchRecordDao.deleteNotShortVideo()
    }

    /**
     * 查询所有观看记录数据
     * @return
     */
    fun selectAllData(uid: Int): MutableList<WatchRecordBean> {
        return watchRecordDao.selectAllData(uid)
    }

    /**
     * 查询所有没同步服务器的观看记录数据
     * @return
     */
    fun selectAllData(uid: Int, status: Int): MutableList<WatchRecordBean> {
        return watchRecordDao.selectAllData(uid, status)
    }

    companion object {
        private var watchRecordManger: WatchRecordManger? = null
        @get:Synchronized
        val instance: WatchRecordManger
            get() {
                if (watchRecordManger == null) {
                    watchRecordManger =
                        WatchRecordManger()
                }
                return watchRecordManger!!
            }
    }

    init {
        watchRecordDao = WatchRecordImpl()
    }
}