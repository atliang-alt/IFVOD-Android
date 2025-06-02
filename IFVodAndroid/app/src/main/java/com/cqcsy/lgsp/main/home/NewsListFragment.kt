package com.cqcsy.lgsp.main.home

import android.os.Bundle
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.BaseVideoListFragment
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.library.network.BaseUrl
import com.cqcsy.library.utils.Constant
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 首页分类新闻、娱乐、华人等小视频类型的Fragment
 * 显示样式为视频播放列表
 */
class NewsListFragment : BaseVideoListFragment() {
    // 首页导航分类ID
    private var navigation: NavigationBarBean? = null
    private var isFromFind = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation = arguments?.getSerializable("navigation") as NavigationBarBean?
        isFromFind = arguments?.getBoolean("isFromFind", false) ?: false
    }

    override fun getUrl(): String {
        return if (isFromFind) BaseUrl.BASE_URL + navigation?.url else RequestUrls.HOME_RELATION_VIDEOS
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        if (isFromFind) {
            params.put("categoryId", navigation?.categoryId)
        } else {
            params.put("titleid", navigation?.categoryId)
        }
        params.put("slabel", LabelUtil.getAllLabels(Constant.KEY_SHORT_VIDEO_LABELS))
        params.put("userid", SPUtils.getInstance().getInt(Constant.KEY_LAST_SHORT_UPPER_ID))
        return params
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTabClickRefresh(event: TabClickRefreshEvent) {
        if ((event.position == R.id.button_home || event.position == R.id.button_find) && mIsFragmentVisible && getDataList().isNotEmpty()) {
            refreshRecyclerView.scrollToTop(refreshLayout)
        }
    }
//
//    override fun checkShow(addSize: Int) {
//        if (getDataList().isEmpty()) {
//            onDataEmpty()
//            showEmpty()
//        } else {
//            if (addSize == 0) {
//                finishLoadMoreWithNoMoreData()
//            } else {
//                finishLoadMore()
//            }
//            if (addSize > 0) {
//                mAdapter?.notifyDataSetChanged()
//            }
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        for ((i, data) in getDataList().withIndex()) {
            if (data.userId == event.uid) {
                //不管是拉黑还是取消拉黑，关注状态都会被重置
                data.focusStatus = false
                data.isBlackList = event.status
                mAdapter?.notifyItemChanged(i)
            }
        }
    }

    override fun onLogin() {
        super.onLogin()
        if (isFromFind) {
            onRefresh()
        }
    }
}