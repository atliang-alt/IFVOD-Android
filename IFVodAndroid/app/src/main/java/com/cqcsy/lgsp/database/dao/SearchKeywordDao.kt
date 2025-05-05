package com.cqcsy.lgsp.database.dao

import com.cqcsy.lgsp.database.bean.SearchKeywordBean

interface SearchKeywordDao {
    fun add(searchKeywordBean: SearchKeywordBean)
    fun update(searchKeywordBean: SearchKeywordBean)
    fun delete()
    fun select(keyword: String): SearchKeywordBean?
    fun select(): MutableList<SearchKeywordBean>
}