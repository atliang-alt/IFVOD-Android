package com.cqcsy.lgsp.video.viewModel

import android.text.Html
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.bean.VideoDetailsBean
import com.cqcsy.lgsp.bean.VideoLikeBean
import com.cqcsy.lgsp.bean.VideoRatingBean
import com.cqcsy.lgsp.bean.net.VideoIntroductionNetBean
import com.cqcsy.lgsp.event.BarrageEvent
import com.cqcsy.lgsp.event.BarrageType
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.video.bean.LanguageBean
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

/**
 ** 2023/12/5
 ** des：视频相关
 **/

class VideoViewModel : ViewModel() {
    val mGetAdCoin: MutableLiveData<Pair<Long, BarrageBean>> by lazy { MutableLiveData() }

    val mForbiddenUserList: MutableLiveData<MutableList<UserInfoBean>?> by lazy { MutableLiveData() }

    val mForbiddenWordList: MutableLiveData<MutableList<CharSequence>> by lazy { MutableLiveData() }

    val mBarrageList: MutableLiveData<MutableList<BarrageBean>> by lazy { MutableLiveData() }

    val mEpisodeList: MutableLiveData<Pair<Boolean, MutableList<VideoBaseBean>>> by lazy { MutableLiveData() }

    val mClarityList: MutableLiveData<MutableList<ClarityBean>> by lazy { MutableLiveData() }

    val mPlayClarity: MutableLiveData<ClarityBean> by lazy { MutableLiveData() }

    val mLanguageList: MutableLiveData<MutableList<LanguageBean>?> by lazy { MutableLiveData() }

    val mVideoInfo: MutableLiveData<VideoIntroductionNetBean?> by lazy { MutableLiveData() }

    val mAdBanner: MutableLiveData<MutableList<AdvertBean>?> by lazy { MutableLiveData() }

    val mAdPause: MutableLiveData<MutableList<AdvertBean>?> by lazy { MutableLiveData() }

    val mAdInsert: MutableLiveData<MutableList<AdvertBean>?> by lazy { MutableLiveData() }

    val mAdDanmaku: MutableLiveData<MutableList<AdvertBean>?> by lazy { MutableLiveData() }

    val mLikeDanmaku: MutableLiveData<Pair<Long, BarrageBean>> by lazy { MutableLiveData() }

    val mCoinSkipAd: MutableLiveData<AtomicBoolean> by lazy { MutableLiveData() }

    val mRecommendShort: MutableLiveData<MutableList<ShortVideoBean>?> by lazy { MutableLiveData() }

    val mRecommendVideo: MutableLiveData<MutableList<VideoBaseBean>?> by lazy { MutableLiveData() }

    val mResolutionList: MutableLiveData<MutableList<ClarityBean>?> by lazy { MutableLiveData() }

    val mSelectedVideoBean: MutableLiveData<VideoBaseBean> by lazy { MutableLiveData() }

    val mFeedBackUrl: MutableLiveData<String> by lazy { MutableLiveData() }

    val mVideoRating: MutableLiveData<VideoRatingBean> by lazy { MutableLiveData() }

    val mVideoLike: MutableLiveData<VideoLikeBean> by lazy { MutableLiveData() }

    val mVideoDislike: MutableLiveData<JSONObject> by lazy { MutableLiveData() }

    val mFocusState: MutableLiveData<AtomicBoolean> by lazy { MutableLiveData() }

    val mOnlineNumber: MutableLiveData<Int> by lazy { MutableLiveData() }

    /**
     * 领取广告金币
     */
    fun receiveAdCoin(danmakuId: Long, data: BarrageBean) {
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean?.id ?: 0)
        params.put("ID", data.advertId)
        HttpRequest.get(RequestUrls.GET_BARRAGE_COIN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                data.isDrawCoin = !data.isDrawCoin
                data.coin = response?.optInt("count") ?: 0
                mGetAdCoin.value = Pair(danmakuId, data)
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    /**
     * 获取屏蔽用户列表
     */
    fun getForbiddenUserList() {
        HttpRequest.get(RequestUrls.FORBIDDEN_USER_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response?.optJSONArray("list") != null && (response.optJSONArray("list")?.length() ?: 0) > 0) {
                    val forbiddenUserList: MutableList<UserInfoBean> = GsonUtils.fromJson(
                        response.optJSONArray("list")?.toString(),
                        object : TypeToken<MutableList<UserInfoBean>>() {}.type
                    )
                    mForbiddenUserList.value = forbiddenUserList
                } else {
                    mForbiddenUserList.value = ArrayList()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }

        }, tag = this)
    }

    /**
     * 获取屏蔽关键词列表
     */
    fun getForbiddenWordList() {
        HttpRequest.get(RequestUrls.GET_FORBIDDEN_WORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response?.optJSONArray("list")?.length() == 0) {
                    mForbiddenWordList.value = ArrayList()
                    return
                }
                mForbiddenWordList.value =
                    GsonUtils.fromJson(response?.optJSONArray("list")?.toString(), object : TypeToken<MutableList<String>>() {}.type)
            }

            override fun onError(response: String?, errorMsg: String?) {
                mForbiddenWordList.value = ArrayList()
            }

        }, tag = this)
    }

    /**
     * 获取弹幕列表
     */
    fun getBarrageList(mediaKey: String?, videoId: Int?, videoType: Int?) {
        if (mediaKey.isNullOrEmpty() && videoId == null) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        if (videoId != null) {
            params.put("videoId", videoId)
        }
        if (videoType != null) {
            params.put("videoType", videoType)
        }
        HttpRequest.get(RequestUrls.GET_BARRAGE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                mBarrageList.value =
                    GsonUtils.fromJson(jsonArray.toString(), object : TypeToken<MutableList<BarrageBean>>() {}.type) ?: mutableListOf()
            }

            override fun onError(response: String?, errorMsg: String?) {
                LogUtils.e(errorMsg)
            }
        }, params, this)
    }


    /**
     * 获取集数、期数
     */
    fun getEpisodeInfo(mediaKey: String?, resolution: String? = null) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("resolution", resolution)
        HttpRequest.post(RequestUrls.VIDEO_CHOSE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                mEpisodeList.value = Pair(true, GsonUtils.fromJson(jsonArray.toString(), object : TypeToken<List<VideoBaseBean>>() {}.type))
            }

            override fun onError(response: String?, errorMsg: String?) {
                mEpisodeList.value = Pair(false, ArrayList())
            }
        }, params, this)
    }

    /**
     * 获取对应播放地址
     */
    fun getPlayInfo(
        mediaKey: String,
        uniqueID: Int = 0,
        videoType: Int? = null,
        needPlay: Boolean = true,
        resolution: String? = null
    ) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoId", uniqueID)
        params.put("resolution", resolution)
        params.put("liveLine", SPUtils.getInstance().getString(Constant.KEY_LIVE_LINE))
        if (videoType != null) {
            params.put("videoType", videoType)
        }
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if ((response?.optJSONArray("list")?.length() ?: 0) == 0) {
                    if (needPlay) {
                        mClarityList.value = null
                    }
                    return
                }
                val clarityList: MutableList<ClarityBean> = GsonUtils.fromJson(
                    response?.optJSONArray("list").toString(),
                    object : TypeToken<List<ClarityBean>>() {}.type
                )
                try {
                    clarityList.sortWith(compareBy({ -(it.resolution?.toInt() ?: 0) }, { it.resolutionDes }))

                    mClarityList.value = clarityList
                    if (needPlay) {
                        if (clarityList.isNotEmpty()) {
                            var playClarity: ClarityBean? = null
                            if (videoType == Constant.VIDEO_SHORT) {
                                playClarity = clarityList[0]
                            } else {
                                for (temp in clarityList) {
                                    if (temp.isDefault) {
                                        playClarity = temp
                                        break
                                    }
                                }
                            }
                            if (playClarity == null) {
                                mPlayClarity.value = null
                            } else {
                                mPlayClarity.value = playClarity
                            }
                        } else {
                            mPlayClarity.value = null
                        }
                    } else {

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (needPlay) {
                    mClarityList.value = null
                    mPlayClarity.value = null
                }
            }
        }, params, this)
    }


    fun goldPay(mediaKey: String?, uniqueID: Int, videoId: Int) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("videoId", videoId)
        HttpRequest.get(RequestUrls.GOLD_PAY_VIDEO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                mClarityList.value?.forEach {
                    it.isBoughtByCoin = true
                }
//                play(clarityList!![0].mediaUrl, resetEpisodesList = true) //自动切换到最高清晰度
                ToastUtils.showLong(Html.fromHtml(response?.optString("msg") + response?.optString("subtitle")))
                mEpisodeList.value?.second?.forEach {
                    it.isVip = false
                }
                getPlayInfo(mediaKey, uniqueID, needPlay = false)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    fun getClarityUrl(clarityBean: ClarityBean) {
        val params = HttpParams()
        params.put("mediaKey", clarityBean.mediaKey)
        params.put("videoId", clarityBean.uniqueID)
        clarityBean.resolution?.let { params.put("resolution", it) }
        HttpRequest.get(RequestUrls.GET_PLAY_ADDRESS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val mediaUrl = response?.optString("mediaUrl")
                if (!mediaUrl.isNullOrEmpty()) {
                    clarityBean.mediaUrl = mediaUrl
                    mPlayClarity.value = clarityBean
                } else {
                    ToastUtils.showLong(R.string.change_clarity_error)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(R.string.change_clarity_error)
            }
        }, params, this)
    }

    /**
     * 获取视频详情信息
     */
    fun getVideoInfo(mediaKey: String? = null, episodeKey: String? = null, videoType: Int, videoTitle: String? = null) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        if (videoType < Constant.VIDEO_LIVE) {
            params.put("videoType", videoType)
        }
        if (episodeKey != null) {
            params.put("episodeKey", episodeKey)
        }
        HttpRequest.get(RequestUrls.VIDEO_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                mVideoInfo.value =
                    GsonUtils.fromJson<VideoIntroductionNetBean>(response.toString(), object : TypeToken<VideoIntroductionNetBean>() {}.type)
                mLanguageList.value = mVideoInfo.value?.languageList
            }

            override fun onError(response: String?, errorMsg: String?) {
                val detail = VideoDetailsBean()
                detail.mediaKey = mediaKey ?: ""
                detail.episodeKey = episodeKey ?: ""
                detail.videoType = videoType
                detail.title = videoTitle
                val introductionNetBean = VideoIntroductionNetBean()
                introductionNetBean.detailInfo = detail
                mVideoInfo.value = introductionNetBean
            }
        }, params, this)
    }

    /**
     * 播放次数记录接口
     */
    fun playRecord(mediaKey: String) {
        if (mediaKey.isNotEmpty()) {
            val params = HttpParams()
            params.put("mediaKey", mediaKey)
            HttpRequest.post(RequestUrls.PLAY_RECORD, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                }

                override fun onError(response: String?, errorMsg: String?) {
                }
            }, params, this)
        }
    }

    /**
     * 设置收藏夹更新数量已读
     */
    fun updateCollectStatus(mediaKey: String?, videoType: Int) {
        if (GlobalValue.isLogin() && !mediaKey.isNullOrEmpty() && mVideoInfo.value?.favorites?.selected == true && (videoType == Constant.VIDEO_VARIETY || videoType == Constant.VIDEO_TELEPLAY)) {
            val params = HttpParams()
            params.put("id", mediaKey)
            HttpRequest.post(RequestUrls.UPDATE_COLLECT_STATUS, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                }

                override fun onError(response: String?, errorMsg: String?) {
                }
            }, params, this)
        }
    }

    /**
     * 用金币跳过广告
     */
    fun coinSkipAdHttp(uniqueId: Int) {
        val params = HttpParams()
        params.put("videoId", uniqueId)
        HttpRequest.post(
            RequestUrls.COIN_SKIP_AD, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (response == null) {
                        return
                    }
                    if (response.optBoolean("issucess")) {
                        mCoinSkipAd.value = AtomicBoolean(true)
                    }
                    ToastUtils.showLong(response.optString("msg"))
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    /**
     * 发送弹幕
     */
    fun sendBarrage(mediaKey: String?, uniqueId: Int, videoType: Int? = 0, content: String, second: Long? = null, type: Int? = null) {
        val color = SPUtils.getInstance().getString(
            Constant.KEY_SEND_DANAMA_COLOR + GlobalValue.userInfoBean?.id, "#ffffff"
        )
        val position = SPUtils.getInstance().getInt(
            Constant.KEY_SEND_DANAMA_POSITION + GlobalValue.userInfoBean?.id, 0
        )
        val params = HttpParams()
        params.put("Contxt", content)
        params.put("Color", color)
        params.put("Position", position)
        if (videoType != null) {
            params.put("videoType", videoType)
        }
        if (second != null) {
            params.put("Second", second)
        }
        if (type != null) {
            params.put("type", type)
        }
        params.put("mediaKey", mediaKey)
        params.put("videoId", uniqueId)
        HttpRequest.get(RequestUrls.SEND_BARRAGE, object : HttpCallBack<JSONArray>() {
            override fun onSuccess(response: JSONArray?) {
                if (response == null) {
                    return
                }
                val list = GsonUtils.fromJson<MutableList<BarrageBean>>(
                    response.toString(),
                    object : TypeToken<MutableList<BarrageBean>>() {}.type
                )
                if (list.size > 0) {
                    val event = BarrageEvent()
                    event.message = list[0]
                    event.eventType = BarrageType.EVENT_LOCAL
                    EventBus.getDefault().post(event)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 点赞弹幕
     */
    fun like(danmakuId: Long, data: BarrageBean) {
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("id", data.guid)
        HttpRequest.post(RequestUrls.LIKE_BARRAGE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                data.isLike = !data.isLike
                if (data.isLike) {
                    data.good += 1
                } else {
                    data.good -= 1
                }
                mLikeDanmaku.value = Pair(danmakuId, data)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    fun getAdsList(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("typeList", "2,3,4,8")
        params.put("mediaKey", mediaKey)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.post(RequestUrls.ADS_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    val pauseAd = response.optJSONArray("zanting")
                    mAdPause.value = GsonUtils.fromJson<MutableList<AdvertBean>>(
                        pauseAd?.toString() ?: "",
                        object : TypeToken<MutableList<AdvertBean>>() {}.type
                    )
                    val bannerAd = response.optJSONArray("bofang")
                    mAdBanner.value = GsonUtils.fromJson<MutableList<AdvertBean>>(
                        bannerAd?.toString() ?: "",
                        object : TypeToken<MutableList<AdvertBean>>() {}.type
                    )
                    val insertAd = response.optJSONArray("chaBo")
                    mAdInsert.value = GsonUtils.fromJson<MutableList<AdvertBean>>(
                        insertAd?.toString() ?: "",
                        object : TypeToken<MutableList<AdvertBean>>() {}.type
                    )
                    val danmakuAd = response.optJSONArray("danmu")
                    mAdDanmaku.value = GsonUtils.fromJson(
                        danmakuAd?.toString() ?: "",
                        object : TypeToken<MutableList<AdvertBean>>() {}.type
                    )
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    /**
     * 获取更多推荐小视频数据
     */
    fun getRecommendShort(mediaKey: String?, videoTitle: String?, page: Int, size: Int) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        params.put("mediaKey", mediaKey)
        params.put("SearchCriteria", videoTitle)
        HttpRequest.post(RequestUrls.SHORT_VIDEO_FILTER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    mRecommendShort.value = null
                    return
                }
                mRecommendShort.value = GsonUtils.fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<ShortVideoBean>>() {}.type
                )
            }

            override fun onError(response: String?, errorMsg: String?) {
                mRecommendShort.value = null
            }
        }, params, this)
    }

    fun getFeedBackUrl() {
        HttpRequest.get(RequestUrls.SERVER_HTML, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val url = response?.optString("playFeedback")
                if (!url.isNullOrEmpty()) {
                    SPUtils.getInstance().put(Constant.KEY_FEED_BACK, url)
                    mFeedBackUrl.value = url
                } else {
                    ToastUtils.showShort(R.string.feed_back_empty)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }
        }, tag = this)
    }

    /**
     * 获取推荐视频
     */
    fun getRecommendVideo(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.RECOMMEND_RELATED_VIDEO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if ((jsonArray?.length() ?: 0) > 0) {
                    mRecommendVideo.value = GsonUtils.fromJson(jsonArray.toString(), object : TypeToken<List<VideoBaseBean>>() {}.type)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    /**
     * 关注、取消关注接口
     */
    fun followClick(userId: Int) {
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("userId", userId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                mFocusState.value = AtomicBoolean(response.optBoolean("selected"))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 视频点赞、取消点赞
     */
    fun videoFabulous(mediaKey: String?, videoType: Int?) {
        if (mediaKey.isNullOrEmpty() || videoType == null) return
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        HttpRequest.get(RequestUrls.VIDEO_LIKES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                mVideoDislike.value = response
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 视频不喜欢
     */
    fun videoDebunkClick(mediaKey: String?, videoType: Int?) {
        if (mediaKey.isNullOrEmpty() || videoType == null) return
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        HttpRequest.get(RequestUrls.VIDEO_DISLIKES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                mVideoDislike.value = response
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 视频收藏、取消收藏
     */
    fun videoCollectionClick(mediaKey: String?, videoType: Int?) {
        if (mediaKey.isNullOrEmpty() || videoType == null) return
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("videoType", videoType)
        HttpRequest.get(RequestUrls.VIDEO_COLLECTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                mVideoLike.value = GsonUtils.fromJson(response.toString(), object : TypeToken<VideoLikeBean>() {}.type)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 获取视频评分
     */
    fun getRating(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.RATING, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                mVideoRating.value = GsonUtils.fromJson(response.toString(), object : TypeToken<VideoRatingBean>() {}.type)
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    /**
     * 拉黑/取消拉黑
     */
    fun forbidden(uid: Int?, isForbidden: Boolean = false) {
        if (uid == null) return
        if (!GlobalValue.checkLogin()) return
        val params = HttpParams()
        params.put("uid", uid)
        params.put("status", isForbidden)
        HttpRequest.post(RequestUrls.CHAT_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val status = response.optBoolean("status")
                EventBus.getDefault().post(BlackListEvent(uid, status))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }

    /**
     * 获取清晰度和语言
     */
    fun getAllResolution(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.SEARCH_LANG, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val jsonArray = response.optJSONArray("list")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list = GsonUtils.fromJson<MutableList<ClarityBean>>(
                        jsonArray.toString(),
                        object : TypeToken<MutableList<ClarityBean>>() {}.type
                    )
                    list?.sortBy { item ->
                        try {
                            item.resolution?.toInt()
                        } catch (e: Exception) {
                            0
                        }
                    }
                    mResolutionList.value = list
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }
}