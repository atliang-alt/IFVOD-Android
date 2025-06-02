package com.cqcsy.lgsp.database.impl

import android.content.ContentValues
import com.cqcsy.library.database.DBHelper
import com.cqcsy.library.database.DBManger
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.dao.DynamicCacheDao

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/26
 *
 *
 */
class DynamicCacheImpl : DynamicCacheDao {
    private val sqLiteDatabase = DBManger.instance.getDatabase()

    override fun add(dynamicRecordBean: DynamicCacheBean): Long {
        val values = ContentValues()
        values.put("dynamicId", dynamicRecordBean.dynamicId)
        values.put("userId", dynamicRecordBean.userId)
        values.put("status", dynamicRecordBean.status)
        values.put("coverPath", dynamicRecordBean.coverPath)
        values.put("coverUrl", dynamicRecordBean.coverUrl)
        values.put("ratio", dynamicRecordBean.ratio)
        values.put("videoPath", dynamicRecordBean.videoPath)
        values.put("videoCompressPath", dynamicRecordBean.videoCompressPath)
        values.put("createTime", dynamicRecordBean.createTime)
        values.put("dynamicType", dynamicRecordBean.dynamicType)
        values.put("description", dynamicRecordBean.description)
        values.put("progress", dynamicRecordBean.progress)
        values.put("address", dynamicRecordBean.address)
        values.put("latitude", dynamicRecordBean.latitude)
        values.put("longitude", dynamicRecordBean.longitude)
        values.put("videoSize", dynamicRecordBean.videoSize)
        values.put("videoId", dynamicRecordBean.videoId)
        values.put("detailedAddress", dynamicRecordBean.detailedAddress)
        values.put("trendsList", dynamicRecordBean.trendsList)
        values.put("labels", dynamicRecordBean.labels)
        return sqLiteDatabase!!.insert(DBHelper.DYNAMIC_CACHE, null, values)
    }

    override fun update(dynamicRecordBean: DynamicCacheBean) {
        val values = ContentValues()
        values.put("dynamicId", dynamicRecordBean.dynamicId)
        values.put("userId", dynamicRecordBean.userId)
        values.put("status", dynamicRecordBean.status)
        values.put("coverPath", dynamicRecordBean.coverPath)
        values.put("coverUrl", dynamicRecordBean.coverUrl)
        values.put("ratio", dynamicRecordBean.ratio)
        values.put("videoPath", dynamicRecordBean.videoPath)
        values.put("videoCompressPath", dynamicRecordBean.videoCompressPath)
        values.put("videoId", dynamicRecordBean.videoId)
        values.put("createTime", dynamicRecordBean.createTime)
        values.put("dynamicType", dynamicRecordBean.dynamicType)
        values.put("description", dynamicRecordBean.description)
        values.put("progress", dynamicRecordBean.progress)
        values.put("address", dynamicRecordBean.address)
        values.put("latitude", dynamicRecordBean.latitude)
        values.put("longitude", dynamicRecordBean.longitude)
        values.put("videoSize", dynamicRecordBean.videoSize)
        values.put("detailedAddress", dynamicRecordBean.detailedAddress)
        values.put("trendsList", dynamicRecordBean.trendsList)
        values.put("labels", dynamicRecordBean.labels)
        val where = "id=?"
        val whereValue = arrayOf(dynamicRecordBean.id.toString())
        sqLiteDatabase!!.update(DBHelper.DYNAMIC_CACHE, values, where, whereValue)
    }

    override fun delete(id: Int) {
        val where = "id=?"
        val whereValue = arrayOf(id.toString())
        sqLiteDatabase!!.delete(DBHelper.DYNAMIC_CACHE, where, whereValue)
    }

    override fun deleteAll() {
        sqLiteDatabase!!.delete(DBHelper.DYNAMIC_CACHE, null, null)
    }

    override fun select(id: Long): MutableList<DynamicCacheBean> {
        return select("select * from dynamicCache where id=?", arrayOf(id.toString()))
    }

    override fun select(status: Int): MutableList<DynamicCacheBean> {
        return select("select * from dynamicCache where status=?", arrayOf(status.toString()))
    }

    override fun selectAllData(): MutableList<DynamicCacheBean> {
        return select("select * from dynamicCache", null)
    }

    private fun select(
        query: String,
        selectionArgs: Array<String>?
    ): MutableList<DynamicCacheBean> {
        val list: MutableList<DynamicCacheBean> =
            ArrayList()
        sqLiteDatabase!!.rawQuery(query, selectionArgs).use { cursor ->
            while (cursor.moveToNext()) {
                val bean = DynamicCacheBean().apply {
                    this.id = cursor.getInt(cursor.getColumnIndex("id"))
                    dynamicId = cursor.getInt(cursor.getColumnIndex("dynamicId"))
                    userId = cursor.getInt(cursor.getColumnIndex("userId"))
                    status = cursor.getInt(cursor.getColumnIndex("status"))
                    coverPath = cursor.getString(cursor.getColumnIndex("coverPath"))
                    coverUrl = cursor.getString(cursor.getColumnIndex("coverUrl"))
                    ratio = cursor.getString(cursor.getColumnIndex("ratio"))
                    videoPath = cursor.getString(cursor.getColumnIndex("videoPath"))
                    videoCompressPath = cursor.getString(cursor.getColumnIndex("videoCompressPath"))
                    videoSize = cursor.getLong(cursor.getColumnIndex("videoSize"))
                    videoId = cursor.getInt(cursor.getColumnIndex("videoId"))
                    progress = cursor.getInt(cursor.getColumnIndex("progress"))
                    description = cursor.getString(cursor.getColumnIndex("description"))
                    createTime = cursor.getString(cursor.getColumnIndex("createTime"))
                    dynamicType = cursor.getInt(cursor.getColumnIndex("dynamicType"))
                    latitude = cursor.getDouble(cursor.getColumnIndex("latitude"))
                    longitude = cursor.getDouble(cursor.getColumnIndex("longitude"))
                    labels = cursor.getString(cursor.getColumnIndex("labels"))
                    address = cursor.getString(cursor.getColumnIndex("address"))
                    detailedAddress = cursor.getString(cursor.getColumnIndex("detailedAddress"))
                    trendsList = cursor.getString(cursor.getColumnIndex("trendsList"))
                }
                list.add(bean)
            }
        }
        return list
    }
}