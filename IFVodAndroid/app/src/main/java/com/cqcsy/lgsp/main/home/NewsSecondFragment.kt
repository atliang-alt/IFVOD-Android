package com.cqcsy.lgsp.main.home

import com.cqcsy.lgsp.base.BaseVideoListFragment
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.BlackListEvent
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 小视频二级分类播放列表页
 */
class NewsSecondFragment: BaseVideoListFragment() {

    override fun getUrl(): String {
        return RequestUrls.HOME_SHORT_VIDEO_SECOND
    }

    override fun getParams(): HttpParams {
        // 二级分类分类ID
        val subId = arguments?.getString("subId") ?: ""
        // 首页导航ID
        val titleId = arguments?.getString("titleId") ?: ""
        val params = HttpParams()
        params.put("titleid", titleId)
        params.put("Tags", subId)
        return params
    }

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
}