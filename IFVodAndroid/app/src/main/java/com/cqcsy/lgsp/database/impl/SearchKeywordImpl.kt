package com.cqcsy.lgsp.database.impl

import android.content.ContentValues
import com.cqcsy.library.database.DBManger
import com.cqcsy.lgsp.database.bean.SearchKeywordBean
import com.cqcsy.lgsp.database.dao.SearchKeywordDao

class SearchKeywordImpl : SearchKeywordDao {
    private val sqLiteDatabase = DBManger.instance!!.getDatabase()
    private val TABLE_NAME = "searchkeyword"
    override fun add(searchKeywordBean: SearchKeywordBean) {
        val values = ContentValues()
        values.put("keyword", searchKeywordBean.keyword)
        values.put("time", searchKeywordBean.time)
        values.put("uid", searchKeywordBean.uid)
        if (select(searchKeywordBean.keyword) == null) {
            sqLiteDatabase!!.insert(TABLE_NAME, null, values)
            return
        }
        update(searchKeywordBean)
    }

    override fun update(searchKeywordBean: SearchKeywordBean) {
        val values = ContentValues()
        values.put("keyword", searchKeywordBean.keyword)
        values.put("time", searchKeywordBean.time)
        values.put("uid", searchKeywordBean.uid)
        val where = "keyword=?"
        val whereValue = arrayOf(searchKeywordBean.keyword)
        sqLiteDatabase!!.update(TABLE_NAME, values, where, whereValue)
    }

    override fun delete() {
        sqLiteDatabase!!.delete(TABLE_NAME, null, null)
    }

    override fun select(keyword: String): SearchKeywordBean? {
        var searchKeywordBean: SearchKeywordBean? = null
        sqLiteDatabase!!.rawQuery(
            "select * from searchkeyword where keyword=?",
            arrayOf(keyword)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                searchKeywordBean = SearchKeywordBean()
                searchKeywordBean!!.keyword = cursor.getString(cursor.getColumnIndex("keyword"))
                searchKeywordBean!!.time = cursor.getString(cursor.getColumnIndex("time"))
                searchKeywordBean!!.uid = cursor.getString(cursor.getColumnIndex("uid"))
            }
        }
        return searchKeywordBean
    }

    override fun select(): MutableList<SearchKeywordBean> {
        val searchKeywordBeanList: MutableList<SearchKeywordBean> = ArrayList()
        sqLiteDatabase!!.rawQuery("select * from searchkeyword order by time desc limit 0,20", null)
            .use { cursor ->
                while (cursor.moveToNext()) {
                    val searchKeywordBean = SearchKeywordBean()
                    searchKeywordBean.keyword = cursor.getString(cursor.getColumnIndex("keyword"))
                    searchKeywordBean.time = cursor.getString(cursor.getColumnIndex("time"))
                    searchKeywordBean.uid = cursor.getString(cursor.getColumnIndex("uid"))
                    searchKeywordBeanList.add(searchKeywordBean)
                }
            }
        return searchKeywordBeanList
    }
}