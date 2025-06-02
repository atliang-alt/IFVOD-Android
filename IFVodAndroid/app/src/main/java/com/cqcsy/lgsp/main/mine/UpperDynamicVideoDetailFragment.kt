package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.DynamicVideoBean
import com.cqcsy.lgsp.preload.PreloadManager2
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.fragment_dynamic_video_detail.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/22
 *
 *
 */
class UpperDynamicVideoDetailFragment : DynamicVideoDetailFragment() {

    companion object {
        private const val PAGE_SIZE = 10
        fun newInstance(
            index: Int,
            dynamicList: MutableList<DynamicBean>,
            commentId: Int = 0,
            replyId: Int = 0,
            showComment: Boolean = false,
            isFromMineDynamic: Boolean = false,
            upperId: Int
        ): UpperDynamicVideoDetailFragment {
            val args = Bundle()
            args.apply {
                putSerializable("dynamic_list", dynamicList as Serializable)
                putInt("index", index)
                putInt("comment_id", commentId)
                putInt("reply_id", replyId)
                putBoolean("show_comment", showComment)
                putBoolean("from_mine_dynamic", isFromMineDynamic)
                putInt("upper_id", upperId)
            }
            val fragment = UpperDynamicVideoDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var upperId = 0
    private var isUpRequesting = false
    private var isDownRequesting = false
    private var hasUpMore = true
    private var hasDownMore = true
    private var firstLoad = false
    private var lastIndex = -1
    override fun initArguments() {
        super.initArguments()
        upperId = arguments?.getInt("upper_id", 0) ?: 0
    }

    override fun initData() {

    }

    override fun onPageIndex(isReverseScroll: Boolean, position: Int) {
        if (lastIndex == position) {
            return
        }
        lastIndex = position
        super.onPageIndex(isReverseScroll, position)
        if (position == 0 && !firstLoad) {
            firstLoad = true
            preloadData(false)
            preloadData(true)
        }
    }

    override fun preloadData(isReverseScroll: Boolean) {
        if (dynamicList.isEmpty()) {
            return
        }
        if (isReverseScroll) {
            if (!hasUpMore) {
                return
            }
            //向前获取数据
            val dynamicBean = dynamicList[0]
            getMoreData(true, dynamicBean.mediaKey, false)
        } else {
            if (!hasDownMore) {
                return
            }
            //向后获取数据
            val dynamicBean = dynamicList[dynamicList.size - 1]
            getMoreData(false, dynamicBean.mediaKey, false)
        }
    }

    override fun onLoadMore() {
        if (dynamicList.isEmpty()) {
            return
        }
        getMoreData(false, dynamicList[dynamicList.size - 1].mediaKey)
    }

    /**
     * 获取
     */
    private fun getMoreData(isReverseScroll: Boolean, mediaKey: String?, jumpDown: Boolean = true) {
        if (isReverseScroll) {
            if (isUpRequesting) {
                return
            }
            isUpRequesting = true
        } else {
            if (isDownRequesting) {
                return
            }
            isDownRequesting = true
        }
        val params = HttpParams()
        params.put("userId", upperId)
        params.put("mediaKey", mediaKey)
        params.put("size", 10)
        params.put("videoType", 3)
        params.put("orderType", if (isReverseScroll) 1 else 2)
        HttpRequest.get(
            RequestUrls.GET_UPPER_TRENDS_VIDEO,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (isReverseScroll) {
                        isUpRequesting = false
                    } else {
                        isDownRequesting = false
                    }
                    val jsonArray = response?.optJSONArray("list")
                    val list: List<DynamicVideoBean>? = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<DynamicVideoBean>>() {}.type
                    )
                    if (!list.isNullOrEmpty()) {
                        val dynamicList = mutableListOf<DynamicBean>()
                        for ((i, bean) in list.withIndex()) {
                            val mediaUrl = bean.mediaUrl
                            val mediaKey = bean.mediaKey
                            if (!mediaUrl.isNullOrEmpty() && i != currentIndex && !mediaKey.isNullOrEmpty()) {
                                PreloadManager2.addPreloadTask(mediaKey, mediaUrl)
                            }
                            val dynamicBean = DynamicBean()
                            dynamicBean.copy(bean)
                            dynamicList.add(dynamicBean)
                        }
                        if (isReverseScroll) {
                            hasUpMore = list.size >= PAGE_SIZE
                            lastIndex += list.size
                            pagerAdapter.addData(0, dynamicList)
                        } else {
                            if (list.size < PAGE_SIZE) {
                                hasDownMore = false
                                refreshLayout.finishLoadMoreWithNoMoreData()
                            } else {
                                hasDownMore = true
                                refreshLayout.finishLoadMore()
                            }
                            pagerAdapter.addData(dynamicList)
                            if (jumpDown) {
                                viewPager.currentItem =
                                    pagerAdapter.dataList.size - dynamicList.size
                            }
                        }
                    } else {
                        if (isReverseScroll) {
                            hasUpMore = false
                        } else {
                            hasDownMore = false
                        }
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (isReverseScroll) {
                        isUpRequesting = false
                    } else {
                        isDownRequesting = false
                    }
                    refreshLayout.finishLoadMore(false)
                }
            },
            params,
            this
        )
    }
}