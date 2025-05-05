package com.cqcsy.lgsp.download

import android.app.Dialog
import android.content.Context
import android.view.View
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.views.dialog.DownloadSelectDialog
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.json.JSONObject

/**
 * 下载公共逻辑类
 */
object DownloadUtil {

    /**
     * 小视频、电影下载弹框处理
     */
    fun showSelectQuality(
        context: Context,
        clarityList: MutableList<ClarityBean>? = ArrayList(),
        videoBaseBean: VideoBaseBean
    ): Dialog? {
        if (clarityList == null || clarityList.size == 0) {
            ToastUtils.showShort(R.string.get_quality_failed)
            return null
        }
        val dialog = DownloadSelectDialog(context, videoBaseBean, clarityList)
        dialog.show()
        return dialog
    }


    /**
     * 获取对应播放地址
     */
    fun getPlayInfo(context: Context, videoBaseBean: VideoBaseBean) {
        val params = HttpParams()
        params.put("mediaKey", videoBaseBean.mediaKey)
        params.put("videoId", videoBaseBean.uniqueID)
        params.put("videoType", videoBaseBean.videoType)
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (response == null || (jsonArray?.length() ?: 0) == 0) {
                    ToastUtils.showLong(R.string.on_video_source)
                    return
                }
                val listClarity = Gson().fromJson<List<ClarityBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<ClarityBean>>() {}.type
                )
                if (!listClarity.isNullOrEmpty()) {
                    for (it in listClarity) {
                        if (it.lang == videoBaseBean.lang && it.resolution == videoBaseBean.resolution) {
                            videoBaseBean.mediaUrl = it.mediaUrl
                            videoBaseBean.mediaKey = it.mediaKey
                            videoBaseBean.title = it.title
                            videoBaseBean.episodeTitle = it.episodeTitle
                            videoBaseBean.episodeKey = it.episodeKey
                            videoBaseBean.episodeId = it.episodeId
                            videoBaseBean.resolution = it.resolution
                            videoBaseBean.resolutionDes = it.resolutionDes
                            videoBaseBean.isVip = it.isVip
                            videoBaseBean.opSecond = it.opSecond
                            videoBaseBean.epSecond = it.epSecond
                            videoBaseBean.lang = it.lang
                            videoBaseBean.videoType = it.videoType
                            DownloadMgr.startDownload(context, videoBaseBean)
                            break
                        }
                    }
                } else {
                    ToastUtils.showLong(R.string.on_video_source)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    fun showNetTip(
        context: Context,
        videoBaseBean: VideoBaseBean
    ) {
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.no_wifi_tip)
        dialog.setLeftListener(R.string.cancel, View.OnClickListener {
            dialog.dismiss()
        })
        dialog.setRightListener(R.string.continue_download, View.OnClickListener {
            dialog.dismiss()
            getPlayInfo(context, videoBaseBean)
        })
        dialog.show()
    }
}