package com.cqcsy.lgsp.database.impl

import android.content.ContentValues
import android.database.Cursor
import com.cqcsy.library.database.DBManger.Companion.instance
import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.database.dao.WatchRecordDao
import com.cqcsy.lgsp.utils.TimesUtils
import java.util.*

class WatchRecordImpl : WatchRecordDao {
    private val sqLiteDatabase = instance!!.getDatabase()
    private val TABLE_NAME = "watchrecord"
    override fun add(watchRecordBean: WatchRecordBean) {
        val values = ContentValues()
        values.put("uid", watchRecordBean.uid)
        values.put("videoType", watchRecordBean.videoType)
        values.put("mediaId", watchRecordBean.mediaKey)
        values.put("uniqueID", watchRecordBean.uniqueID)
        values.put("title", watchRecordBean.title)
        values.put("cidMapper", watchRecordBean.cidMapper)
        values.put("regional", watchRecordBean.regional)
        values.put("lang", watchRecordBean.lang)
        values.put("episodeId", watchRecordBean.episodeId)
        values.put("episodeTitle", watchRecordBean.episodeTitle)
        values.put("coverImgUrl", watchRecordBean.coverImgUrl)
        values.put("upperName", watchRecordBean.upperName)
        values.put("mediaUrl", watchRecordBean.mediaUrl)
        values.put("watchTime", watchRecordBean.watchTime)
        values.put("duration", watchRecordBean.duration)
        values.put("time", watchRecordBean.time)
        values.put("status", watchRecordBean.status)
        values.put("contentType", watchRecordBean.contentType)
        values.put("recordTime", TimesUtils.getUTCTime())
        if (selectOneData(watchRecordBean.mediaKey, watchRecordBean.uid) == null) {
            sqLiteDatabase!!.insert(TABLE_NAME, null, values)
            return
        }
        update(watchRecordBean)
    }

    override fun update(watchRecordBean: WatchRecordBean) {
        val values = ContentValues()
        values.put("uid", watchRecordBean.uid)
        values.put("videoType", watchRecordBean.videoType)
        values.put("mediaId", watchRecordBean.mediaKey)
        values.put("uniqueID", watchRecordBean.uniqueID)
        values.put("title", watchRecordBean.title)
        values.put("cidMapper", watchRecordBean.cidMapper)
        values.put("regional", watchRecordBean.regional)
        values.put("lang", watchRecordBean.lang)
        values.put("episodeId", watchRecordBean.episodeId)
        values.put("episodeTitle", watchRecordBean.episodeTitle)
        values.put("coverImgUrl", watchRecordBean.coverImgUrl)
        values.put("upperName", watchRecordBean.upperName)
        values.put("mediaUrl", watchRecordBean.mediaUrl)
        values.put("watchTime", watchRecordBean.watchTime)
        values.put("duration", watchRecordBean.duration)
        values.put("time", watchRecordBean.time)
        values.put("status", watchRecordBean.status)
        values.put("contentType", watchRecordBean.contentType)
        values.put("recordTime", TimesUtils.getUTCTime())
        val where = "mediaId=? and uid=?"
        val whereValue = arrayOf(watchRecordBean.mediaKey, watchRecordBean.uid.toString())
        sqLiteDatabase!!.update(TABLE_NAME, values, where, whereValue)
    }

    override fun delete(list: MutableList<String>, uid: Int) {
        for (i in list.indices) {
            val where = "mediaId=? and uid=?"
            val whereValue = arrayOf(list[i], uid.toString())
            sqLiteDatabase!!.delete(TABLE_NAME, where, whereValue)
        }
    }

    override fun deleteAll(videoType: Int) {
        val where = "videoType=?"
        val whereValue = arrayOf(videoType.toString())
        sqLiteDatabase!!.delete(TABLE_NAME, where, whereValue)
    }

    override fun deleteNotShortVideo() {
        val where = "videoType<3"
        sqLiteDatabase!!.delete(TABLE_NAME, where, null)
    }

    private fun selectOneData(mediaKey: String, uid: Int): WatchRecordBean? {
        var watchRecordBean: WatchRecordBean? = null
        sqLiteDatabase!!.rawQuery(
            "select * from watchrecord where mediaId=? and uid=?",
            arrayOf(mediaKey, uid.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                watchRecordBean = WatchRecordBean()
                watchRecordBean?.mediaKey = mediaKey
                watchRecordBean?.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                watchRecordBean?.videoType = cursor.getInt(cursor.getColumnIndex("videoType"))
                watchRecordBean?.uniqueID = cursor.getInt(cursor.getColumnIndex("uniqueID"))
                watchRecordBean?.title = cursor.getString(cursor.getColumnIndex("title"))
                watchRecordBean?.episodeId = cursor.getInt(cursor.getColumnIndex("episodeId"))
                watchRecordBean?.episodeTitle =
                    cursor.getString(cursor.getColumnIndex("episodeTitle"))
                watchRecordBean?.coverImgUrl =
                    cursor.getString(cursor.getColumnIndex("coverImgUrl"))
                watchRecordBean?.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                watchRecordBean?.mediaUrl = cursor.getString(cursor.getColumnIndex("mediaUrl"))
                watchRecordBean?.watchTime = cursor.getString(cursor.getColumnIndex("watchTime"))
                watchRecordBean?.duration = cursor.getString(cursor.getColumnIndex("duration"))
                watchRecordBean?.time = cursor.getString(cursor.getColumnIndex("time"))
                watchRecordBean?.status = cursor.getInt(cursor.getColumnIndex("status"))
                watchRecordBean?.contentType =
                    cursor.getString(cursor.getColumnIndex("contentType"))
                watchRecordBean?.recordTime = cursor.getString(cursor.getColumnIndex("recordTime"))
                watchRecordBean?.cidMapper = getCursorString("cidMapper", cursor)
                watchRecordBean?.regional = getCursorString("regional", cursor)
                watchRecordBean?.lang = getCursorString("lang", cursor)
            }
        }
        return watchRecordBean
    }

    override fun selectAllData(uid: Int): MutableList<WatchRecordBean> {
        val watchRecordBeanList: MutableList<WatchRecordBean> =
            ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from watchrecord where uid=? order by recordTime desc",
            arrayOf(uid.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val watchRecordBean = WatchRecordBean()
                watchRecordBean.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                watchRecordBean.videoType = cursor.getInt(cursor.getColumnIndex("videoType"))
                watchRecordBean.mediaKey = cursor.getString(cursor.getColumnIndex("mediaId"))
                watchRecordBean.uniqueID = cursor.getInt(cursor.getColumnIndex("uniqueID"))
                watchRecordBean.title = cursor.getString(cursor.getColumnIndex("title"))
                watchRecordBean.episodeId = cursor.getInt(cursor.getColumnIndex("episodeId"))
                watchRecordBean.episodeTitle =
                    cursor.getString(cursor.getColumnIndex("episodeTitle"))
                watchRecordBean.coverImgUrl = cursor.getString(cursor.getColumnIndex("coverImgUrl"))
                watchRecordBean.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                watchRecordBean.mediaUrl = cursor.getString(cursor.getColumnIndex("mediaUrl"))
                watchRecordBean.watchTime = cursor.getString(cursor.getColumnIndex("watchTime"))
                watchRecordBean.duration = cursor.getString(cursor.getColumnIndex("duration"))
                watchRecordBean.time = cursor.getString(cursor.getColumnIndex("time"))
                watchRecordBean.status = cursor.getInt(cursor.getColumnIndex("status"))
                watchRecordBean.contentType = cursor.getString(cursor.getColumnIndex("contentType"))
                watchRecordBean.recordTime = cursor.getString(cursor.getColumnIndex("recordTime"))
                watchRecordBean.cidMapper = getCursorString("cidMapper", cursor)
                watchRecordBean.regional = getCursorString("regional", cursor)
                watchRecordBean.lang = getCursorString("lang", cursor)
                watchRecordBeanList.add(watchRecordBean)
            }
        }
        return watchRecordBeanList
    }

    override fun selectAllData(uid: Int, status: Int): MutableList<WatchRecordBean> {
        val watchRecordBeanList: MutableList<WatchRecordBean> =
            ArrayList()
        sqLiteDatabase!!.rawQuery(
            "select * from watchrecord where uid=? and status=? order by recordTime desc",
            arrayOf(uid.toString(), status.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val watchRecordBean = WatchRecordBean()
                watchRecordBean.uid = cursor.getInt(cursor.getColumnIndex("uid"))
                watchRecordBean.videoType = cursor.getInt(cursor.getColumnIndex("videoType"))
                watchRecordBean.mediaKey = cursor.getString(cursor.getColumnIndex("mediaId"))
                watchRecordBean.uniqueID = cursor.getInt(cursor.getColumnIndex("uniqueID"))
                watchRecordBean.title = cursor.getString(cursor.getColumnIndex("title"))
                watchRecordBean.episodeId = cursor.getInt(cursor.getColumnIndex("episodeId"))
                watchRecordBean.episodeTitle =
                    cursor.getString(cursor.getColumnIndex("episodeTitle"))
                watchRecordBean.coverImgUrl = cursor.getString(cursor.getColumnIndex("coverImgUrl"))
                watchRecordBean.upperName = cursor.getString(cursor.getColumnIndex("upperName"))
                watchRecordBean.mediaUrl = cursor.getString(cursor.getColumnIndex("mediaUrl"))
                watchRecordBean.watchTime = cursor.getString(cursor.getColumnIndex("watchTime"))
                watchRecordBean.duration = cursor.getString(cursor.getColumnIndex("duration"))
                watchRecordBean.time = cursor.getString(cursor.getColumnIndex("time"))
                watchRecordBean.status = cursor.getInt(cursor.getColumnIndex("status"))
                watchRecordBean.contentType = cursor.getString(cursor.getColumnIndex("contentType"))
                watchRecordBean.recordTime = cursor.getString(cursor.getColumnIndex("recordTime"))
                watchRecordBean.cidMapper = getCursorString("cidMapper", cursor)
                watchRecordBean.regional = getCursorString("regional", cursor)
                watchRecordBean.lang = getCursorString("lang", cursor)
                watchRecordBeanList.add(watchRecordBean)
            }
        }
        return watchRecordBeanList
    }

    private fun getCursorString(key: String, cursor: Cursor): String {
        return when {
            cursor.getColumnIndex(key) == -1 -> {
                ""
            }
            cursor.getString(cursor.getColumnIndex(key)) == null -> {
                ""
            }
            else -> {
                cursor.getString(cursor.getColumnIndex(key))
            }
        }
    }
}