package com.cqcsy.lgsp.video.presenter

import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.VideoBaseActivity.Companion.AD_TIP_BEFORE_TIME
import com.cqcsy.lgsp.video.bean.AdvertPlayPointBean
import com.cqcsy.lgsp.video.player.LiteVideoAdPlayer
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.GSYVideoADManager
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.request.RequestCall
import okhttp3.Call
import org.greenrobot.eventbus.EventBus

/**
 * 广告控制器
 */
class AdvertPresenter(val player: LiteVideoPlayer, val adPlayer: LiteVideoAdPlayer) {
    private val mErrorShowTime = 20L

    private var adPointList: ArrayList<AdvertPlayPointBean>? = null
    private var pauseAdvertList: MutableList<AdvertBean>? = null
    private var insertAdvertList: MutableList<AdvertBean>? = null

    private var nextPlayAdvertBean: AdvertBean? = null

    private var countDownTime = -1

    fun advertCheck(currentSecond: Long, totalTime: Long, isFilterAds: Boolean) {
        computePoint(totalTime, isFilterAds)
        val nextPoint = getNextAdPoint(currentSecond)
        if (nextPoint == null) {
            if (!GlobalValue.isVipUser() && countDownTime > 0) {
                player.hideTopTip()
            }
            countDownTime = -1
            return
        }
        if (GlobalValue.isVipUser()) {
            countDownTime = -1
            nextPoint.advertBean = AdvertBean()
            vipSkipAdTip()
            return
        }
        if (countDownTime < 0) {
            countDownTime = AD_TIP_BEFORE_TIME
        }
        if (countDownTime > 0) {
            player.setTopTip(
                StringUtils.getString(R.string.ad_start_tip, countDownTime),
                false
            )
        } else if (countDownTime == 0) {
            playInsertAd(nextPoint)
        }
        countDownTime--
    }

    fun vipSkipAdTip() {
        player.setTopTip(
            StringUtils.getString(R.string.vip_skip_ad),
            true, colorRes = R.color.word_color_vip
        )
        callbackAd()
    }

    /**
     * 获取当前需要的广告点
     */
    private fun getNextAdPoint(currentSecond: Long): AdvertPlayPointBean? {
        if (adPointList == null || adPointList?.size == 0) {
            return null
        }
        if (adPointList?.size == 1) {
            val bean = adPointList!![0]
            if (bean.advertBean == null && (currentSecond >= bean.playTime || bean.playTime - currentSecond <= AD_TIP_BEFORE_TIME)) {
                return bean
            }
        } else if (adPointList?.size == 2) {
            val first = adPointList!![0]
            val second = adPointList!![1]
            if (currentSecond < second.playTime && first.advertBean == null && (first.playTime - currentSecond <= AD_TIP_BEFORE_TIME || currentSecond >= first.playTime)) {
                return first
            } else if (second.advertBean == null && (second.playTime - currentSecond <= AD_TIP_BEFORE_TIME || currentSecond >= second.playTime)) {
                return second
            }
        }
        return null
    }

    /**
     *  计算广告播放点
     */
    private fun computePoint(totalTime: Long, isFilterAds: Boolean) {
        //    1. 如果时间小于15分钟，不出广告。
        //    2. 小于70分钟的，播放一次广告，在总时间的1/2
        //    3. 最多播放2次广告。分别1/3的总时间
        if (adPointList == null) {
            adPointList = ArrayList()
        }
        if (totalTime == 0L || adPointList!!.size > 0 || isFilterAds) {
            return
        }
        val minute = totalTime / 1000 / 60
        if (minute in 15..70) {
            val bean = AdvertPlayPointBean()
            bean.playTime = totalTime / 1000 / 2
            adPointList!!.add(bean)
        } else if (minute > 70) {
            val first = AdvertPlayPointBean()
            first.playTime = totalTime / 1000 / 3
            adPointList!!.add(first)

            val second = AdvertPlayPointBean()
            second.playTime = totalTime / 1000 / 3 * 2
            adPointList!!.add(second)
        }
        getNextAd()
    }

    private fun getNextAd() {
        if (GlobalValue.isVipUser() || adPointList == null || adPointList!!.size == 0) {
            return
        }
        for (point in adPointList!!) {
            if (point.advertBean == null) {
                getNextInsertAdvert(null)
                break
            }
        }
    }

    private fun playInsertAd(pointBean: AdvertPlayPointBean) {
        player.hideTopTip()
        if (nextPlayAdvertBean == null) {
            getNextInsertAdvert(pointBean, true)
        } else {
            playAd(nextPlayAdvertBean!!, pointBean)
        }
    }

    /**
     * 广告展示回调
     */
    fun showCallBack(backUrl: String) {
        HttpRequest.get(backUrl, object : HttpCallBack<String>() {
            override fun onSuccess(response: String?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        })
    }

    //    private var proxyCacheServer: HttpProxyCacheServer? = null
    private var requestCall: RequestCall? = null
    private var isCacheFinish = false

    private fun prepareAd(advertBean: AdvertBean) {
//        if (proxyCacheServer == null) {
//            proxyCacheServer = ProxyCacheManager.instance().newProxy(Utils.getApp())
//        }
        isCacheFinish = false
        val encodeUrl = NormalUtil.urlEncode(advertBean.showURL)
//        if (proxyCacheServer!!.isCached(encodeUrl)) {
//            println("prepareAd-----clear")
        GSYVideoADManager.instance().clearCache(Utils.getApp(), null, encodeUrl)
//        }
        try {
            requestCall = OkHttpUtils.get().url(encodeUrl).build()
            requestCall?.execute(object : MemoryCallBack() {
                override fun onError(call: Call?, e: java.lang.Exception?, id: Int) {
                    GSYVideoADManager.instance().clearCache(Utils.getApp(), null, encodeUrl)
                    isCacheFinish = false
                    stopPrepare()
                }

                override fun onResponse(response: Boolean?, id: Int) {
                    isCacheFinish = response ?: false
                    stopPrepare()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            isCacheFinish = false
            GSYVideoADManager.instance().clearCache(Utils.getApp(), null, encodeUrl)
            stopPrepare()
        }
    }

    fun stopPrepare() {
        try {
            if (requestCall != null) {
                requestCall?.cancel()
                requestCall = null
            }
//            if (proxyCacheServer != null) {
//                proxyCacheServer?.shutdown()
//            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAd(advertBean: AdvertBean, pointBean: AdvertPlayPointBean) {
        if (advertBean.showURL.isEmpty()) {
            showAdError(pointBean)
            return
        }
        val encodeUrl = NormalUtil.urlEncode(advertBean.showURL)
        player.currentPlayer.onVideoPause()
        stopPrepare()
        if (!isCacheFinish) {
            GSYVideoADManager.instance().clearCache(Utils.getApp(), null, encodeUrl)
        }
        (adPlayer.currentPlayer as LiteVideoAdPlayer).totalTime = nextPlayAdvertBean!!.piDuration.toLong()
        (adPlayer.currentPlayer as LiteVideoAdPlayer).currentAd = advertBean
        adPlayer.currentPlayer.setUp(encodeUrl, true, null)
        pointBean.advertBean = nextPlayAdvertBean
        EventBus.getDefault().post(AdvertActionEvent(AdvertAction.ACTION_PLAY))
        advertBean.viewURL?.let { showCallBack(it) }
    }

    private fun showAdError(pointBean: AdvertPlayPointBean) {
        if (pointBean.advertBean == null) {
            pointBean.advertBean = AdvertBean()
        }
        player.currentPlayer.onVideoPause()
        stopPrepare()
        adPlayer.totalTime = mErrorShowTime
        adPlayer.currentPlayer.setUp("", true, null)
        adPlayer.visibility = View.VISIBLE
        EventBus.getDefault().post(AdvertActionEvent(AdvertAction.ACTION_PLAY))
    }

    fun skipPlayAd() {
        if (adPlayer.currentPlayer.isIfCurrentIsFullscreen) {
            adPlayer.removeFullWindowViewOnly()
            if (!player.currentPlayer.isIfCurrentIsFullscreen) {
                EventBus.getDefault().post(AdvertActionEvent(AdvertAction.ACTION_FULL))
                player.saveBeforeFullSystemUiVisibility =
                    adPlayer.saveBeforeFullSystemUiVisibility
            }
        }
        stopAdPlay()
        player.currentPlayer.startAfterPrepared()
        getNextAd()
    }

    fun stopAdPlay() {
        nextPlayAdvertBean = null
        adPlayer.onVideoReset()
        GSYVideoADManager.instance().stop()
        GSYVideoADManager.releaseAllVideos()
        adPlayer.visibility = View.GONE
        adPlayer.currentPlayer.visibility = View.GONE
    }

    fun reset() {
        adPointList?.clear()
//        pauseAdvertList?.clear()
        nextPlayAdvertBean = null
        countDownTime = -1
    }

//    private fun getVideoAds(pointBean: AdvertPlayPointBean?, needPlay: Boolean = false) {
//        val params = HttpParams()
//        params.put("type", 2)
//        params.put("region", NormalUtil.getAreaCode())
//        HttpRequest.post(RequestUrls.GET_ADS, object : HttpCallBack<JSONObject>() {
//            override fun onSuccess(response: JSONObject?) {
//                val jsonArray = response?.optJSONArray("list")
//                if (jsonArray == null || jsonArray.length() == 0) {
////                    skipPlayAd()
//                    if (pointBean != null) {
//                        showAdError(pointBean)
//                    }
//                    return
//                }
//                val listAd = Gson().fromJson<List<AdvertBean>>(
//                    jsonArray.toString(),
//                    object : TypeToken<List<AdvertBean>>() {}.type
//                )
//                val index = listAd.indices.random()
//                nextPlayAdvertBean = listAd[index]
//                prepareAd(nextPlayAdvertBean!!)
//                if (needPlay && pointBean != null) {
//                    playAd(nextPlayAdvertBean!!, pointBean)
//                }
//            }
//
//            override fun onError(response: String?, errorMsg: String?) {
////                skipPlayAd()
//                if (pointBean != null) {
//                    showAdError(pointBean)
//                }
//            }
//        }, params, this)
//    }

//    fun getPauseAds() {
//        if (GlobalValue.isVipUser()) {
//            return
//        }
//        if (!pauseAdvertList.isNullOrEmpty()) {
//            randomAdvert()
//            return
//        }
//        val params = HttpParams()
//        params.put("type", 3)
//        params.put("region", NormalUtil.getAreaCode())
//        HttpRequest.post(RequestUrls.GET_ADS, object : HttpCallBack<JSONObject>() {
//            override fun onSuccess(response: JSONObject?) {
//                val listAd = Gson().fromJson<MutableList<AdvertBean>>(
//                    response?.optJSONArray("list").toString(),
//                    object : TypeToken<MutableList<AdvertBean>>() {}.type
//                )
//                if (listAd != null && listAd.isNotEmpty()) {
//                    pauseAdvertList = listAd
//                    randomAdvert()
//                }
//            }
//
//            override fun onError(response: String?, errorMsg: String?) {
//
//            }
//        }, params, this)
//    }

    private fun isAdvertEnable(): Boolean {
        return !GlobalValue.isVipUser()
    }

    fun setInsertAdvert(adList: MutableList<AdvertBean>?) {
        if (!isAdvertEnable() || adList.isNullOrEmpty()) return
        if (insertAdvertList == null) {
            insertAdvertList = ArrayList()
        }
        insertAdvertList?.clear()
        insertAdvertList?.addAll(adList)
    }

    private fun getNextInsertAdvert(pointBean: AdvertPlayPointBean?, needPlay: Boolean = false) {
        if (insertAdvertList.isNullOrEmpty()) return
        val index = insertAdvertList!!.indices.random()
        nextPlayAdvertBean = insertAdvertList!![index]
        prepareAd(nextPlayAdvertBean!!)
        if (needPlay && pointBean != null) {
            playAd(nextPlayAdvertBean!!, pointBean)
        }
    }


    fun setPauseAdvert(adList: MutableList<AdvertBean>?) {
        if (adList.isNullOrEmpty()) return
        if (pauseAdvertList == null) {
            pauseAdvertList = ArrayList()
        }
        pauseAdvertList?.clear()
        pauseAdvertList?.addAll(adList)
    }

    fun showPauseAdvert() {
        if (pauseAdvertList.isNullOrEmpty()) return
        val advertBean = pauseAdvertList?.get((pauseAdvertList!!.indices).random())
        if (advertBean != null && player.isPaused) {
            player.setPauseAdvert(advertBean)
            advertBean.viewURL?.let { showCallBack(it) }
        }
    }

    private fun callbackAd() {
        val params = HttpParams()
        params.put("s", 3)
        GlobalValue.userInfoBean?.id?.let { params.put("uid", it) }
        params.put("gid", GlobalValue.userInfoBean?.token!!.gid)
        HttpRequest.get(RequestUrls.AD_CONTROL, object : HttpCallBack<String>() {
            override fun onSuccess(response: String?) {

            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }
}