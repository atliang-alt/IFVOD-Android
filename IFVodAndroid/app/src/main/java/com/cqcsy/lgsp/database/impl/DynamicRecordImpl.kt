package com.cqcsy.lgsp.database.impl

import android.content.ContentValues
import com.cqcsy.library.database.DBHelper
import com.cqcsy.library.database.DBManger.Companion.instance
import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.dao.DynamicRecordDao
import com.cqcsy.lgsp.utils.TimesUtils

class DynamicRecordImpl : DynamicRecordDao {
    private val sqLiteDatabase = instance.getDatabase()

    override fun add(dynamicRecordBean: DynamicRecordBean) {
        val values = ContentValues()
//        values.put("pid", dynamicRecordBean.pid)
        values.put("mediaId", dynamicRecordBean.mediaKey)
        values.put("headImg", dynamicRecordBean.headImg)
        values.put("upperName", dynamicRecordBean.upperName)
        values.put("createTime", dynamicRecordBean.createTime)
        values.put("title", dynamicRecordBean.title)
        values.put("description", dynamicRecordBean.description)
        values.put("coverPath", dynamicRecordBean.coverPath)
        values.put("trendsDetails", dynamicRecordBean.trendsDetails)
        values.put("address", dynamicRecordBean.address)
        values.put("photoCount", dynamicRecordBean.photoCount)
        values.put("comments", dynamicRecordBean.comments)
        values.put("likeCount", dynamicRecordBean.likeCount)
        values.put("uid", dynamicRecordBean.uid)
        values.put("recordTime", TimesUtils.getUTCTime())
        values.put("type", dynamicRecordBean.type)
        values.put("bigV", dynamicRecordBean.bigV)
        values.put("vipLevel", dynamicRecordBean.vipLevel)
        if (selectOneData(dynamicRecordBean.mediaKey) == null) {
            sqLiteDatabase!!.insert(DBHelper.DYNAMIC_ALBUM_RECORD, null, values)
            return
        }
        update(dynamicRecordBean)
    }

    override fun update(dynamicRecordBean: DynamicRecordBean) {
        val values = ContentValues()
//        values.put("pid", dynamicRecordBean.pid)
        values.put("mediaId", dynamicRecordBean.mediaKey)
        values.put("headImg", dynamicRecordBean.headImg)
        values.put("upperName", dynamicRecordBean.upperName)
        values.put("createTime", dynamicRecordBean.createTime)
        values.put("title", dynamicRecordBean.title)
        values.put("description", dynamicRecordBean.description)
        values.put("coverPath", dynamicRecordBean.coverPath)
        values.put("trendsDetails", dynamicRecordBean.trendsDetails)
        values.put("address", dynamicRecordBean.address)
        values.put("photoCount", dynamicRecordBean.photoCount)
        values.put("comments", dynamicRecordBean.comments)
        values.put("likeCount", dynamicRecordBean.likeCount)
        values.put("uid", dynamicRecordBean.uid)
        values.put("recordTime", TimesUtils.getUTCTime())
        values.put("type", dynamicRecordBean.type)
        values.put("bigV", dynamicRecordBean.bigV)
        values.put("vipLevel", dynamicRecordBean.vipLevel)
        val where = "mediaId=?"
        val whereValue = arrayOf(dynamicRecordBean.mediaKey)
        sqLiteDatabase!!.update(DBHelper.DYNAMIC_ALBUM_RECORD, values, where, whereValue)
    }

    override fun delete(list: MutableList<String>) {
        for (i in list.indices) {
            val where = "pid=?"
            val whereValue = arrayOf(list[i])
            sqLiteDatabase!!.delete(DBHelper.DYNAMIC_ALBUM_RECORD, where, whereValue)
        }
    }

    override fun deleteType(type: Int) {
        val where = "type=?"
        val whereValue = arrayOf(type.toString())
        sqLiteDatabase!!.delete(DBHelper.DYNAMIC_ALBUM_RECORD, where, whereValue)
    }

    private fun selectOneData(mediaKey: String): DynamicRecordBean? {
        var dynamicRecordBean: DynamicRecordBean? = null
        sqLiteDatabase!!.rawQuery(
            "select * from ${DBHelper.DYNAMIC_ALBUM_RECORD} where mediaId=?",
            arrayOf(mediaKey)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                dynamicRecordBean = DynamicRecordBean()
//                dynamicRecordBean?.pid = pid
                dynamicRecordBean?.headImg = cursor.getString(cursor.getColumnIndex("headImg"))
                dynamicRecordBean?.mediaKey = cursor.getString(cursor.getColumnIndex("mediaId"))
                dynamicRecordBean?.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                dynamicRecordBean?.createTime =
                    cursor.getString(cursor.getColumnIndex("createTime"))
                dynamicRecordBean?.title = cursor.getString(cursor.getColumnIndex("title"))
                dynamicRecordBean?.description =
                    cursor.getString(cursor.getColumnIndex("description"))
                dynamicRecordBean?.coverPath = cursor.getString(cursor.getColumnIndex("coverPath"))
                dynamicRecordBean?.trendsDetails =
                    cursor.getString(cursor.getColumnIndex("trendsDetails"))
                dynamicRecordBean?.address = cursor.getString(cursor.getColumnIndex("address"))
                dynamicRecordBean?.photoCount = cursor.getInt(cursor.getColumnIndex("photoCount"))
                dynamicRecordBean?.comments = cursor.getInt(cursor.getColumnIndex("comments"))
                dynamicRecordBean?.likeCount = cursor.getInt(cursor.getColumnIndex("likeCount"))
                dynamicRecordBean?.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                dynamicRecordBean?.recordTime =
                    cursor.getString(cursor.getColumnIndex("recordTime"))
                dynamicRecordBean?.type = cursor.getInt(cursor.getColumnIndex("type"))
                dynamicRecordBean?.bigV = cursor.getInt(cursor.getColumnIndex("bigV")) == 1
                dynamicRecordBean?.vipLevel = cursor.getInt(cursor.getColumnIndex("vipLevel"))
            }
        }
        return dynamicRecordBean
    }

    override fun selectAllData(type: Int): MutableList<DynamicRecordBean> {
        val list: MutableList<DynamicRecordBean> = ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from ${DBHelper.DYNAMIC_ALBUM_RECORD} where type=? order by recordTime desc",
            arrayOf(type.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val dynamicRecordBean = DynamicRecordBean()
//                dynamicRecordBean.pid = cursor.getInt(cursor.getColumnIndex("pid"))
                dynamicRecordBean.headImg = cursor.getString(cursor.getColumnIndex("headImg"))
                dynamicRecordBean.mediaKey = cursor.getString(cursor.getColumnIndex("mediaId"))
                dynamicRecordBean.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                dynamicRecordBean.createTime = cursor.getString(cursor.getColumnIndex("createTime"))
                dynamicRecordBean.title = cursor.getString(cursor.getColumnIndex("title"))
                dynamicRecordBean.description =
                    cursor.getString(cursor.getColumnIndex("description"))
                dynamicRecordBean.coverPath = cursor.getString(cursor.getColumnIndex("coverPath"))
                dynamicRecordBean.trendsDetails =
                    cursor.getString(cursor.getColumnIndex("trendsDetails"))
                dynamicRecordBean.address = cursor.getString(cursor.getColumnIndex("address"))
                dynamicRecordBean.photoCount = cursor.getInt(cursor.getColumnIndex("photoCount"))
                dynamicRecordBean.comments = cursor.getInt(cursor.getColumnIndex("comments"))
                dynamicRecordBean.likeCount = cursor.getInt(cursor.getColumnIndex("likeCount"))
                dynamicRecordBean.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                dynamicRecordBean.recordTime = cursor.getString(cursor.getColumnIndex("recordTime"))
                dynamicRecordBean.bigV = cursor.getInt(cursor.getColumnIndex("bigV")) == 1
                dynamicRecordBean.vipLevel = cursor.getInt(cursor.getColumnIndex("vipLevel"))
                dynamicRecordBean.type = cursor.getInt(cursor.getColumnIndex("type"))
                list.add(dynamicRecordBean)
            }
        }
        return list
    }

    override fun selectAll(): MutableList<DynamicRecordBean> {
        val list: MutableList<DynamicRecordBean> = ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from ${DBHelper.DYNAMIC_ALBUM_RECORD} order by recordTime desc",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val dynamicRecordBean = DynamicRecordBean()
//                dynamicRecordBean.pid = cursor.getInt(cursor.getColumnIndex("pid"))
                dynamicRecordBean.headImg = cursor.getString(cursor.getColumnIndex("headImg"))
                dynamicRecordBean.mediaKey = cursor.getString(cursor.getColumnIndex("mediaId"))
                dynamicRecordBean.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                dynamicRecordBean.createTime = cursor.getString(cursor.getColumnIndex("createTime"))
                dynamicRecordBean.title = cursor.getString(cursor.getColumnIndex("title"))
                dynamicRecordBean.description =
                    cursor.getString(cursor.getColumnIndex("description"))
                dynamicRecordBean.coverPath = cursor.getString(cursor.getColumnIndex("coverPath"))
                dynamicRecordBean.trendsDetails =
                    cursor.getString(cursor.getColumnIndex("trendsDetails"))
                dynamicRecordBean.address = cursor.getString(cursor.getColumnIndex("address"))
                dynamicRecordBean.photoCount = cursor.getInt(cursor.getColumnIndex("photoCount"))
                dynamicRecordBean.comments = cursor.getInt(cursor.getColumnIndex("comments"))
                dynamicRecordBean.likeCount = cursor.getInt(cursor.getColumnIndex("likeCount"))
                dynamicRecordBean.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                dynamicRecordBean.recordTime = cursor.getString(cursor.getColumnIndex("recordTime"))
                dynamicRecordBean.bigV = cursor.getInt(cursor.getColumnIndex("bigV")) == 1
                dynamicRecordBean.vipLevel = cursor.getInt(cursor.getColumnIndex("vipLevel"))
                dynamicRecordBean.type = cursor.getInt(cursor.getColumnIndex("type"))
                list.add(dynamicRecordBean)
            }
        }
        return list
    }
}