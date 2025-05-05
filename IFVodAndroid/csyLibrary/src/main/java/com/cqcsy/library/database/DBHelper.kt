package com.cqcsy.library.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper internal constructor(context: Context?) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        // 创建观看记录表
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + WATCH_RECORD
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, uid INTEGER, videoType INTEGER,"
                    + " mediaId NVARCHAR, uniqueID INTEGER, title NVARCHAR, episodeId INTEGER, episodeTitle NVARCHAR,"
                    + " coverImgUrl NVARCHAR, upperName NVARCHAR, mediaUrl NVARCHAR, watchTime NVARCHAR,"
                    + " duration NVARCHAR, time NVARCHAR, status INTEGER, recordTime NVARCHAR, contentType NVARCHAR,"
                    + " cidMapper NVARCHAR, regional NVARCHAR, lang NVARCHAR)"
        )
        // 创建视频切片上传表
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + VIDEO_UPLOAD_LOG
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, title NVARCHAR, parentPath NVARCHAR,"
                    + " videoSize NVARCHAR, cutPath NVARCHAR, cutStart NVARCHAR, cutEnd NVARCHAR,"
                    + " imagePath NVARCHAR, classify NVARCHAR, tags NVARCHAR, status NVARCHAR)"
        )
        // 创建搜索历史词表
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + SEARCH_KEYWORD
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, keyword NVARCHAR, time NVARCHAR, uid NVARCHAR)"
        )
        // 上传本地视频缓存表
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + UPLOAD_VIDEO_CACHE
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, lid NVARCHAR, vid NVARCHAR, chunk NVARCHAR,"
                    + " chunks NVARCHAR, serviceId NVARCHAR, uploadUrl NVARCHAR, title NVARCHAR, context NVARCHAR, path NVARCHAR,"
                    + " videoSize NVARCHAR, cutStart NVARCHAR, cutFileName NVARCHAR, imagePath NVARCHAR, speed NVARCHAR,"
                    + " fileId NVARCHAR, fileName NVARCHAR, imageBase NVARCHAR, cid NVARCHAR, labels NVARCHAR,"
                    + " progress NVARCHAR, status NVARCHAR)"
        )

        createDynamicAndAlbumRecord(db)
        createDynamicCache(db)

        createMusicRecord(db)
        createSheetRecord(db)
        createPlayList(db)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        updateWatchRecord(db)
        createDynamicAndAlbumRecord(db)
        createDynamicCache(db)
        updateDynamicAndAlbumRecord(db)

        createMusicRecord(db)
        createSheetRecord(db)
        createPlayList(db)
    }

    private fun updateDynamicAndAlbumRecord(db: SQLiteDatabase) {
        // 增加观看记录表字段
        if (!isColumnExit(db, DYNAMIC_ALBUM_RECORD, "bigV")) {
            db.execSQL("alter table $DYNAMIC_ALBUM_RECORD add column bigV BOOLEAN")
        }
        if (!isColumnExit(db, DYNAMIC_ALBUM_RECORD, "vipLevel")) {
            db.execSQL("alter table $DYNAMIC_ALBUM_RECORD add column vipLevel INTEGER")
        }
    }

    private fun updateWatchRecord(db: SQLiteDatabase) {
        // 增加观看记录表字段
        if (!isColumnExit(db, WATCH_RECORD, "cidMapper")) {
            db.execSQL("alter table $WATCH_RECORD add column cidMapper NVARCHAR")
        }
        if (!isColumnExit(db, WATCH_RECORD, "regional")) {
            db.execSQL("alter table $WATCH_RECORD add column regional NVARCHAR")
        }
        if (!isColumnExit(db, WATCH_RECORD, "lang")) {
            db.execSQL("alter table $WATCH_RECORD add column lang NVARCHAR")
        }
    }

    // 创建动态、相册记录
    private fun createDynamicAndAlbumRecord(db: SQLiteDatabase) {
        if (isTableExist(db, DYNAMIC_ALBUM_RECORD)) {
            return
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DYNAMIC_ALBUM_RECORD
                    + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, pid INTEGER, mediaId NVARCHAR, headImg NVARCHAR,"
                    + " upperName NVARCHAR, createTime NVARCHAR, title NVARCHAR, description NVARCHAR,"
                    + " coverPath NVARCHAR, trendsDetails NVARCHAR, address NVARCHAR, photoCount INTEGER,"
                    + " comments INTEGER, likeCount INTEGER, uid INTEGER, recordTime NVARCHAR, type INTEGER, bigV BOOLEAN, vipLevel INTEGER)"
        )
    }

    /**
     * 创建动态缓存表
     */
    private fun createDynamicCache(db: SQLiteDatabase) {
        if (isTableExist(db, DYNAMIC_CACHE)) {
            return
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + DYNAMIC_CACHE
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT, dynamicId NVARCHAR,userId NVARCHAR, status NVARCHAR, coverPath NVARCHAR,coverUrl NVARCHAR,ratio NVARCHAR,"
                    + " videoPath NVARCHAR, videoCompressPath NVARCHAR, videoSize NVARCHAR,videoId NVARCHAR, description NVARCHAR, createTime NVARCHAR, dynamicType NVARCHAR, progress NVARCHAR,"
                    + " latitude NVARCHAR, longitude NVARCHAR, labels NVARCHAR, address NVARCHAR, detailedAddress NVARCHAR,"
                    + " trendsList NVARCHAR)"
        )
    }

 

    /**
     * 未登录本地歌单播放记录
     */
    private fun createSheetRecord(db: SQLiteDatabase) {
        if (isTableExist(db, SHEET_RECORD)) {
            return
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + SHEET_RECORD
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT, sheetKey NVARCHAR,name NVARCHAR, songImage NVARCHAR, addTime DATETIME)"
        )
    }

    /**
     * 播放列表
     */
    private fun createPlayList(db: SQLiteDatabase) {
        if (isTableExist(db, MUSIC_PLAY_LIST)) {
            return
        }
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + MUSIC_PLAY_LIST
                    + " (id INTEGER PRIMARY KEY AUTOINCREMENT, mediaKey NVARCHAR, musicName NVARCHAR, songKey NVARCHAR, singerKey NVARCHAR, singerImage NVARCHAR, singerName NVARCHAR, songType INTEGER,"
                    + " isCollected BOOLEAN, orderId INTEGER, addTime DATETIME, isPlaying BOOLEAN)"
        )
    }

    /**
     * 判断某列是否存在，不存在需要升级添加字段
     */
    private fun isColumnExit(db: SQLiteDatabase, table: String, column: String): Boolean {
        var result = false
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery("SELECT * FROM $table LIMIT 0", null)
            result = cursor != null && (cursor.getColumnIndex(column) != -1)
        } catch (e: Exception) {
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return result
    }

    /**
     * 判断表是否存在
     */
    private fun isTableExist(db: SQLiteDatabase, tableName: String): Boolean {
        var result = false
        var cursor: Cursor? = null
        try {
            if (db.isOpen) {
                val sql = "select count(*) as c from sqlite_master where type ='table' and name ='" + tableName.trim() + "' "
                cursor = db.rawQuery(sql, null)
                if (cursor.moveToNext()) {
                    val count = cursor.getInt(0)
                    if (count > 0) {
                        result = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cursor?.close()
        return result
    }

    companion object {
        // 数据库名字
        private const val DATABASE_NAME = "history_cache.db"

        // 数据库版本号
        private const val DATABASE_VERSION = 9

        // 观看记录表
        val WATCH_RECORD = "watchrecord"

        // 动态、相册记录表
        val DYNAMIC_ALBUM_RECORD = "dynamicrecord"

        // 视频切片上传表
        val VIDEO_UPLOAD_LOG = "videouploadlog"

        // 搜索历史词表
        val SEARCH_KEYWORD = "searchkeyword"

        // 上传本地视频缓存表
        val UPLOAD_VIDEO_CACHE = "uploadVideoCache"

        // 发布动态缓存表
        val DYNAMIC_CACHE = "dynamicCache"

 
    }
}