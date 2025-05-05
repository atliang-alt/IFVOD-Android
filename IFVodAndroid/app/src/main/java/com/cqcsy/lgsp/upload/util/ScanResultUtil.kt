package com.cqcsy.lgsp.upload.util

import android.database.Cursor

/**
 * 扫描本地数据结果处理类
 */
object ScanResultUtil {

    /**
     * 宽高比例
     * @param widthProportion
     * @param heightProportion
     * @param width
     * @return
     */
    fun getHeight(widthProportion: Int, heightProportion: Int, width: Int): Int {
        val temp = width / widthProportion
        return temp * heightProportion
    }

    fun getStringResultByKey(cursor: Cursor, key: String): String {
        return cursor.getString(cursor.getColumnIndex(key))
    }
}