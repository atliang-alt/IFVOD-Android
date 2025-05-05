package com.cqcsy.library.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.blankj.utilcode.util.Utils

class DBManger private constructor(context: Context) {
    private val dbHelper: DBHelper = DBHelper(context)

    // 操作表的对象，进行增删改查
    private var database: SQLiteDatabase?

    fun getDBHelper(): DBHelper {
        return dbHelper
    }

    fun getDatabase(): SQLiteDatabase? {
        if (database == null) {
            database = dbHelper.writableDatabase
        }
        return database
    }

    companion object {
        private var dbManger: DBManger? = null
        @JvmStatic
        @get:Synchronized
        val instance: DBManger
            get() {
                if (dbManger == null) {
                    dbManger = DBManger(Utils.getApp())
                }
                return dbManger!!
            }
    }

    init {
        database = dbHelper.writableDatabase
    }
}