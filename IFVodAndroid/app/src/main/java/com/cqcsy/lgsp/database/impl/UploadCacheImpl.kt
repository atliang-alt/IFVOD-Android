package com.cqcsy.lgsp.database.impl

import android.content.ContentValues
import com.cqcsy.library.database.DBManger
import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.dao.UploadCacheDao

class UploadCacheImpl : UploadCacheDao {
    private val sqLiteDatabase = DBManger.instance!!.getDatabase()
    private val TABLE_NAME = "uploadVideoCache"

    override fun add(uploadCacheBean: UploadCacheBean) {
        val values = ContentValues()
        values.put("lid", uploadCacheBean.lid)
        values.put("vid", uploadCacheBean.vid)
        values.put("chunk", uploadCacheBean.chunk)
        values.put("chunks", uploadCacheBean.chunks)
        values.put("serviceId", uploadCacheBean.serviceId)
        values.put("uploadUrl", uploadCacheBean.uploadUrl)
        values.put("fileId", uploadCacheBean.fileId)
        values.put("fileName", uploadCacheBean.fileName)
        values.put("title", uploadCacheBean.title)
        values.put("context", uploadCacheBean.context)
        values.put("path", uploadCacheBean.path)
        values.put("videoSize", uploadCacheBean.videoSize)
        values.put("progress", uploadCacheBean.progress)
        values.put("speed", uploadCacheBean.speed)
        values.put("cutStart", uploadCacheBean.cutStart)
        values.put("cutFileName", uploadCacheBean.cutFileName)
        values.put("imagePath", uploadCacheBean.imagePath)
        values.put("imageBase", uploadCacheBean.imageBase)
        values.put("cid", uploadCacheBean.cid)
        values.put("labels", uploadCacheBean.labels)
        values.put("status", uploadCacheBean.status)
        if (select(uploadCacheBean.path) == null) {
            sqLiteDatabase!!.insert(TABLE_NAME, null, values)
            return
        }
        update(uploadCacheBean)
    }

    override fun update(uploadCacheBean: UploadCacheBean) {
        val values = ContentValues()
        values.put("lid", uploadCacheBean.lid)
        values.put("vid", uploadCacheBean.vid)
        values.put("chunk", uploadCacheBean.chunk)
        values.put("chunks", uploadCacheBean.chunks)
        values.put("serviceId", uploadCacheBean.serviceId)
        values.put("uploadUrl", uploadCacheBean.uploadUrl)
        values.put("fileId", uploadCacheBean.fileId)
        values.put("fileName", uploadCacheBean.fileName)
        values.put("title", uploadCacheBean.title)
        values.put("context", uploadCacheBean.context)
        values.put("path", uploadCacheBean.path)
        values.put("videoSize", uploadCacheBean.videoSize)
        values.put("progress", uploadCacheBean.progress)
        values.put("speed", uploadCacheBean.speed)
        values.put("cutStart", uploadCacheBean.cutStart)
        values.put("cutFileName", uploadCacheBean.cutFileName)
        values.put("imagePath", uploadCacheBean.imagePath)
        values.put("imageBase", uploadCacheBean.imageBase)
        values.put("cid", uploadCacheBean.cid)
        values.put("labels", uploadCacheBean.labels)
        values.put("status", uploadCacheBean.status)
        val where = "path=?"
        val whereValue = arrayOf(uploadCacheBean.path)
        sqLiteDatabase!!.update(TABLE_NAME, values, where, whereValue)
    }

    override fun delete(parentPath: String) {
        val where = "path=?"
        val whereValue = arrayOf(parentPath)
        sqLiteDatabase!!.delete(TABLE_NAME, where, whereValue)
    }

    override fun delete() {
        sqLiteDatabase!!.delete(TABLE_NAME, null, null)
    }

    override fun select(parentPath: String): UploadCacheBean? {
        var uploadCacheBean: UploadCacheBean? = null
        sqLiteDatabase!!.rawQuery(
            "select * from uploadVideoCache where path=?",
            arrayOf(parentPath)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                uploadCacheBean = UploadCacheBean()
                uploadCacheBean?.lid = cursor.getInt(cursor.getColumnIndex("lid"))
                uploadCacheBean?.vid = cursor.getInt(cursor.getColumnIndex("vid"))
                uploadCacheBean?.chunk = cursor.getInt(cursor.getColumnIndex("chunk"))
                uploadCacheBean?.chunks = cursor.getInt(cursor.getColumnIndex("chunks"))
                uploadCacheBean?.serviceId = cursor.getInt(cursor.getColumnIndex("serviceId"))
                uploadCacheBean?.uploadUrl = cursor.getString(cursor.getColumnIndex("uploadUrl"))
                uploadCacheBean?.fileId = cursor.getString(cursor.getColumnIndex("fileId"))
                uploadCacheBean?.fileName = cursor.getString(cursor.getColumnIndex("fileName"))
                uploadCacheBean?.title = cursor.getString(cursor.getColumnIndex("title"))
                uploadCacheBean?.context = cursor.getString(cursor.getColumnIndex("context"))
                uploadCacheBean?.path = cursor.getString(cursor.getColumnIndex("path"))
                uploadCacheBean?.videoSize = cursor.getLong(cursor.getColumnIndex("videoSize"))
                uploadCacheBean?.progress = cursor.getLong(cursor.getColumnIndex("progress"))
                uploadCacheBean?.speed = cursor.getLong(cursor.getColumnIndex("speed"))
                uploadCacheBean?.cutStart = cursor.getLong(cursor.getColumnIndex("cutStart"))
                uploadCacheBean?.cutFileName = cursor.getString(cursor.getColumnIndex("cutFileName"))
                uploadCacheBean?.imagePath = cursor.getString(cursor.getColumnIndex("imagePath"))
                uploadCacheBean?.imageBase = cursor.getString(cursor.getColumnIndex("imageBase"))
                uploadCacheBean?.cid = cursor.getString(cursor.getColumnIndex("cid"))
                uploadCacheBean?.labels = cursor.getString(cursor.getColumnIndex("labels"))
                uploadCacheBean?.status = cursor.getInt(cursor.getColumnIndex("status"))
            }
        }
        return uploadCacheBean
    }

    override fun select(status: Int): MutableList<UploadCacheBean> {
        val list: MutableList<UploadCacheBean> =
            ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from uploadVideoCache where status=?",
            arrayOf(status.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val uploadCacheBean = UploadCacheBean()
                uploadCacheBean.lid = cursor.getInt(cursor.getColumnIndex("lid"))
                uploadCacheBean.vid = cursor.getInt(cursor.getColumnIndex("vid"))
                uploadCacheBean.chunk = cursor.getInt(cursor.getColumnIndex("chunk"))
                uploadCacheBean.chunks = cursor.getInt(cursor.getColumnIndex("chunks"))
                uploadCacheBean.serviceId = cursor.getInt(cursor.getColumnIndex("serviceId"))
                uploadCacheBean.uploadUrl = cursor.getString(cursor.getColumnIndex("uploadUrl"))
                uploadCacheBean.fileId = cursor.getString(cursor.getColumnIndex("fileId"))
                uploadCacheBean.fileName = cursor.getString(cursor.getColumnIndex("fileName"))
                uploadCacheBean.title = cursor.getString(cursor.getColumnIndex("title"))
                uploadCacheBean.context = cursor.getString(cursor.getColumnIndex("context"))
                uploadCacheBean.path = cursor.getString(cursor.getColumnIndex("path"))
                uploadCacheBean.videoSize = cursor.getLong(cursor.getColumnIndex("videoSize"))
                uploadCacheBean.progress = cursor.getLong(cursor.getColumnIndex("progress"))
                uploadCacheBean.speed = cursor.getLong(cursor.getColumnIndex("speed"))
                uploadCacheBean.cutStart = cursor.getLong(cursor.getColumnIndex("cutStart"))
                uploadCacheBean.cutFileName = cursor.getString(cursor.getColumnIndex("cutFileName"))
                uploadCacheBean.imagePath = cursor.getString(cursor.getColumnIndex("imagePath"))
                uploadCacheBean.imageBase = cursor.getString(cursor.getColumnIndex("imageBase"))
                uploadCacheBean.cid = cursor.getString(cursor.getColumnIndex("cid"))
                uploadCacheBean.labels = cursor.getString(cursor.getColumnIndex("labels"))
                uploadCacheBean.status = cursor.getInt(cursor.getColumnIndex("status"))
                list.add(uploadCacheBean)
            }
        }
        return list
    }

    override fun select(): MutableList<UploadCacheBean> {
        val list: MutableList<UploadCacheBean> =
            ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from uploadVideoCache order by status asc", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val uploadCacheBean = UploadCacheBean()
                uploadCacheBean.lid = cursor.getInt(cursor.getColumnIndex("lid"))
                uploadCacheBean.vid = cursor.getInt(cursor.getColumnIndex("vid"))
                uploadCacheBean.chunk = cursor.getInt(cursor.getColumnIndex("chunk"))
                uploadCacheBean.chunks = cursor.getInt(cursor.getColumnIndex("chunks"))
                uploadCacheBean.serviceId = cursor.getInt(cursor.getColumnIndex("serviceId"))
                uploadCacheBean.uploadUrl = cursor.getString(cursor.getColumnIndex("uploadUrl"))
                uploadCacheBean.fileId = cursor.getString(cursor.getColumnIndex("fileId"))
                uploadCacheBean.fileName = cursor.getString(cursor.getColumnIndex("fileName"))
                uploadCacheBean.title = cursor.getString(cursor.getColumnIndex("title"))
                uploadCacheBean.context = cursor.getString(cursor.getColumnIndex("context"))
                uploadCacheBean.path = cursor.getString(cursor.getColumnIndex("path"))
                uploadCacheBean.videoSize = cursor.getLong(cursor.getColumnIndex("videoSize"))
                uploadCacheBean.progress = cursor.getLong(cursor.getColumnIndex("progress"))
                uploadCacheBean.speed = cursor.getLong(cursor.getColumnIndex("speed"))
                uploadCacheBean.cutStart = cursor.getLong(cursor.getColumnIndex("cutStart"))
                uploadCacheBean.cutFileName = cursor.getString(cursor.getColumnIndex("cutFileName"))
                uploadCacheBean.imagePath = cursor.getString(cursor.getColumnIndex("imagePath"))
                uploadCacheBean.imageBase = cursor.getString(cursor.getColumnIndex("imageBase"))
                uploadCacheBean.cid = cursor.getString(cursor.getColumnIndex("cid"))
                uploadCacheBean.labels = cursor.getString(cursor.getColumnIndex("labels"))
                uploadCacheBean.status = cursor.getInt(cursor.getColumnIndex("status"))
                list.add(uploadCacheBean)
            }
        }
        return list
    }
}