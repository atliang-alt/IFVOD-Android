package com.cqcsy.lgsp.database

import com.cqcsy.lgsp.database.bean.WatchRecordBean
import com.cqcsy.lgsp.database.manger.WatchRecordManger
import com.cqcsy.lgsp.event.AddRecordSuccess
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 添加观看到数据库
 * 请求接口上传观看记录数据
 */
object AddWatchRecordUtil {

    fun addRecord(watchRecordBean: WatchRecordBean) {
        if (GlobalValue.isLogin()) {
            uploadData(watchRecordBean)
        } else {
            addData(watchRecordBean)
        }
    }

    /**
     * 添加到数据库
     */
    private fun addData(watchRecordBean: WatchRecordBean) {
        WatchRecordManger.instance.add(watchRecordBean)
    }

    /**
     * 上传观看记录
     */
    private fun uploadData(watchRecordBean: WatchRecordBean) {
        val params = HttpParams()
        params.put("mediaKey", watchRecordBean.mediaKey)
        params.put("videoType", watchRecordBean.videoType)
        params.put("videoId", watchRecordBean.uniqueID)
        params.put("watchingProgress", watchRecordBean.watchTime)
        params.put("totalTime", watchRecordBean.time)
        params.put(
            "title",
            if (watchRecordBean.videoType == Constant.VIDEO_MOVIE) watchRecordBean.title else watchRecordBean.episodeTitle
        )
        if (watchRecordBean.videoType == Constant.VIDEO_SHORT) {
            params.put("isNormalVideo", 0)
        } else {
            params.put("isNormalVideo", 1)
        }
        HttpRequest.get(RequestUrls.ADD_RECORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                EventBus.getDefault().post(AddRecordSuccess())
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params)
    }
}