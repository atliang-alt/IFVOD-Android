package com.cqcsy.lgsp.delegate.util

import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ItemTitleBean
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.bean.net.HomeNetBean
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.Constant

/**
 * 首页delegate添加数据
 */
object HomeDelegateUtils {

    @Synchronized
    fun addDelegateItem(
        dataList: MutableList<Any>,
        data: Any,
        isNeedTitle: Boolean = true,
        titleAction: ((type: Any) -> Unit)? = null
    ) {
        if (data is MutableList<*> && data.isNotEmpty() && data[0] is HomeNetBean) {
            addHomeItem(dataList, data as MutableList<HomeNetBean>, isNeedTitle, titleAction)
        }
    }

    private fun addHomeItem(
        dataList: MutableList<Any>,
        data: MutableList<HomeNetBean>,
        isNeedTitle: Boolean = true,
        titleAction: ((type: Any) -> Unit)? = null
    ) {
        data.forEach {
            when (it.type) {
                Constant.ADVERTISEMENT_TYPE -> {
                    // 广告
                    if (!it.bannerList.isNullOrEmpty()) {
                        dataList.add(it.bannerList!![0])
                    }
                }

                Constant.BANNER_TYPE -> {
                    // Banner
                    if (!it.bannerList.isNullOrEmpty() || !it.list.isNullOrEmpty()) {
                        val listWrapper = ListWrapper()
                        if (!it.bannerList.isNullOrEmpty()) {
                            listWrapper.type = it.type
                            it.bannerList?.removeAll { advert ->
                                if (!advert.playtime.isNullOrEmpty() && !advert.endtime.isNullOrEmpty()) {
                                    val end = TimesUtils.utc2Local(advert.endtime!!, "yyyy-MM-dd HH:mm:ss")
                                    val diffTime = TimeUtils.getTimeSpanByNow(end, TimeConstants.MIN)
                                    if (diffTime <= 0) {
                                        return@removeAll true
                                    }
                                }
                                false
                            }
                            listWrapper.data = it.bannerList
                        } else {
                            listWrapper.type = it.subID
                            listWrapper.data = it.list
                        }
                        dataList.add(listWrapper)
                    }
                }

                Constant.POPULAR_TYPE -> {
                    // 热门
                    if (!it.list.isNullOrEmpty()) {
                        if (isNeedTitle) {
                            val actionEnable = titleAction != null && !it.subID.isNullOrEmpty()
                            var action: (() -> Unit?)? = null
                            if (actionEnable) {
                                action = { titleAction?.invoke(it.name) }
                            }
                            val itemTitle = ItemTitleBean(
                                if (actionEnable) it.subID else it.type,
                                it.name, action
                            )
                            if (!itemTitle.itemName.isNullOrEmpty()) {
                                dataList.add(itemTitle)
                            }
                        }
                        dataList.addAll(it.list!!)
                    }
                }

                Constant.MOVIE_TYPE,
                Constant.COMIC_TYPE,
                Constant.TELEPLAY_TYPE,
                Constant.DOCUMENTARY_TYPE,
                Constant.SPORTS_TYPE,
                Constant.VARIETY_TYPE -> {
                    if (!it.list.isNullOrEmpty()) {
                        if (isNeedTitle) {
                            val itemTitle = ItemTitleBean(it.type, it.name) {
                                titleAction?.invoke(it.type)
                            }
                            if (!itemTitle.itemName.isNullOrEmpty()) {
                                dataList.add(itemTitle)
                            }
                            if (it.list!!.size > 4) {
                                val banner = it.list!![0]
                                banner.isFull = true
                                dataList.add(banner)
                                it.list!!.removeAt(0)
                            }
                        }
                        dataList.addAll(it.list!!)
                    }
                }

                Constant.PAGE_FILTER -> {
                    if (!it.filterList.isNullOrEmpty()) {
//                        val listWrapper = ListWrapper()
//                        listWrapper.type = it.type
//                        listWrapper.data = it.filterList
                        dataList.add(it.filterList!!)
                    }
                }
            }
            if (it.needRefresh || it.isMore) {
                dataList.add(it)
            }
        }
    }

    /**
     * 返回重置数据起始位置
     */
    @Synchronized
    fun resetTypeData(
        type: Int,
        dataList: MutableList<Any>,
        data: MutableList<MovieModuleBean>
    ): Int {
        var position = dataList.indexOfFirst { it is ItemTitleBean && it.type == type }
        if (position == -1) {
            return -1
        }
        val fullItem = dataList.find { it is MovieModuleBean && it.type == type && it.isFull }
        if (fullItem != null) {
            position++
        }
        dataList.removeAll { it is MovieModuleBean && it.type == type && !it.isFull }
        dataList.addAll(position + 1, data)
        return position
    }

    /**
     * 返回添加起始位置
     */
    @Synchronized
    fun addFollowing(
        dataList: MutableList<Any>,
        data: MutableList<MovieModuleBean>,
        titleAction: (() -> Unit)? = null
    ): Int {
        var position =
            dataList.indexOfLast { it is MovieModuleBean && it.type == Constant.POPULAR_TYPE }
        if (position == -1) {
            return -1
        }
        // 判断是否已经添加，添加了则需要移除
//        val exsitPosition = dataList.indexOfLast { it is ListWrapper && it.type == Constant.FOLLOWING_TYPE }
//        if (exsitPosition != -1) {
//            dataList.removeAt(exsitPosition)
//        }
//        val nextPosition = position + 1
//        if (nextPosition < dataList.size) {
//            val nextItem = dataList[nextPosition]
//            if (nextItem is ListWrapper && nextItem.type == Constant.FOLLOWING_TYPE) {
//                dataList.removeAt(nextPosition)
//            }
//        }
        position++  // 下一个位置开始添加
        val title = ItemTitleBean(
            Constant.FOLLOWING_TYPE,
            StringUtils.getString(R.string.historic_records),
            titleAction
        )
        dataList.add(position, title)
        val listWrapper = ListWrapper()
        listWrapper.type = Constant.FOLLOWING_TYPE
        listWrapper.data = data
        dataList.add(position + 1, listWrapper)
        return position
    }
}