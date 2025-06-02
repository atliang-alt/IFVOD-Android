package com.cqcsy.lgsp.record

/**
 * 记录监听回调
 */
interface RecordListener {
    // 位置 0剧集 1小视频 2动态 3相册
    fun <T> onLoadFinish(index: Int, list: MutableList<T>)
    fun onDataEmpty(index: Int)
    fun <T> removeData(list: MutableList<T>)
    fun <T> checkAvailable(index: Int, bean: T)
}