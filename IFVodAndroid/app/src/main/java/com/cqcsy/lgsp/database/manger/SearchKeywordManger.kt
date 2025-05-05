package com.cqcsy.lgsp.database.manger

import com.cqcsy.lgsp.database.bean.SearchKeywordBean
import com.cqcsy.lgsp.database.dao.SearchKeywordDao
import com.cqcsy.lgsp.database.impl.SearchKeywordImpl

/**
 * 搜索关键词数据库对外操作类
 */
class SearchKeywordManger private constructor() {
    private val searchKeywordDao: SearchKeywordDao

    /**
     * 添加搜索历史词
     */
    fun add(searchKeywordBean: SearchKeywordBean) {
        searchKeywordDao.add(searchKeywordBean)
    }

    /**
     * 更新搜索历史词
     */
    fun update(searchKeywordBean: SearchKeywordBean) {
        searchKeywordDao.update(searchKeywordBean)
    }

    /**
     * 清空搜索历史词
     */
    fun delete() {
        searchKeywordDao.delete()
    }

    /**
     * 查询某条搜索历史词
     */
    fun select(keyword: String): SearchKeywordBean? {
        return searchKeywordDao.select(keyword)
    }

    /**
     * 按时间排序查询前20条数据
     */
    fun select(): MutableList<SearchKeywordBean> {
        return searchKeywordDao.select()
    }

    companion object {
        private var searchKeywordManger: SearchKeywordManger? = null
        @get:Synchronized
        val instance: SearchKeywordManger
            get() {
                if (searchKeywordManger == null) {
                    searchKeywordManger =
                        SearchKeywordManger()
                }
                return searchKeywordManger!!
            }
    }

    init {
        searchKeywordDao = SearchKeywordImpl()
    }
}