package com.cqcsy.lgsp.main.mine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.BarUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_dynamic_detail.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 动态详情页
 */
class DynamicDetailsActivity : BaseActivity() {

    companion object {
        const val TAG = "DynamicVideoDetail"

        inline fun launch(context: Context?, block: Builder.() -> Unit) =
            Builder().apply(block).build(context)

    }

    class Builder {

        /**
         * 动态视频目标索引
         */
        var videoIndex = 0

        /**
         * 动态图片目标索引
         */
        var picIndex = 0

        /**
         * 动态类型，1:动态图片 2:动态视频。同photoType
         */
        var dynamicType = -1
        var commentId: Int = 0
        var mediaKey: String = ""
        var replyId: Int = 0

        /**
         * 进入时是否显示评论，当前如果是动态小视频，则根据videoIndex来判断是否显示评论
         */
        var showComment: Boolean = false

        /**
         * 是否开启视频推荐
         */
        var openRecommend: Boolean = true

        /**
         * 是否从我的动态进入
         */
        var isFromMineDynamic: Boolean = false

        /**
         * 动态小视频集合
         */
        var dynamicVideoList: MutableList<DynamicBean>? = null

        /**
         * 来自upper主页
         */
        var fromUpperHomePage: Boolean = false

        /**
         * upper id
         */
        var upperId = 0

        fun build(context: Context?) {
            val intent = Intent(context, DynamicDetailsActivity::class.java)
            intent.putExtra("dynamic_list", dynamicVideoList as? Serializable)
            intent.putExtra("dynamic_type", dynamicType)
            intent.putExtra("index", videoIndex)
            intent.putExtra("pic_index", picIndex)
            intent.putExtra("comment_id", commentId)
            intent.putExtra("reply_id", replyId)
            intent.putExtra("show_comment", showComment)
            intent.putExtra("dynamic_id", mediaKey)
            intent.putExtra("open_recommend", openRecommend)
            intent.putExtra("is_from_mine_dynamic", isFromMineDynamic)
            intent.putExtra("from_upper_home_page", fromUpperHomePage)
            intent.putExtra("upper_id", upperId)
            context?.startActivity(intent)
        }
    }

    private var picIndex = 0
    private var currentIndex = 0
    private var dynamicType = -1
    private var commentId: Int = 0
    private var mediaKey: String? = null
    private var replyId: Int = 0
    private var upperId: Int = 0
    private var showComment: Boolean = false
    private var openRecommend: Boolean = true
    private var fromUpperHomePage: Boolean = true
    private var isFromMineDynamic: Boolean = false
    private var dynamicVideoList: MutableList<DynamicBean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.transparentStatusBar(this)
        setContentView(R.layout.activity_dynamic_detail)
        dynamicType = intent.getIntExtra("dynamic_type", -1)
        currentIndex = intent.getIntExtra("index", 0)
        picIndex = intent.getIntExtra("pic_index", 0)
        mediaKey = intent.getStringExtra("dynamic_id")
        commentId = intent.getIntExtra("comment_id", 0)
        replyId = intent.getIntExtra("reply_id", 0)
        showComment = intent.getBooleanExtra("show_comment", false)
        openRecommend = intent.getBooleanExtra("open_recommend", true)
        fromUpperHomePage = intent.getBooleanExtra("from_upper_home_page", false)
        upperId = intent.getIntExtra("upper_id", 0)
        isFromMineDynamic = intent.getBooleanExtra("is_from_mine_dynamic", true)
        dynamicVideoList =
            intent.getSerializableExtra("dynamic_list") as? MutableList<DynamicBean>
                ?: mutableListOf()
        if (dynamicType == -1) {
            getDynamicDetail()
        } else if (dynamicType == 1) {
            val dynamicBean = if (!dynamicVideoList.isNullOrEmpty()) {
                dynamicVideoList!![0]
            } else {
                null
            }
            initDynamicDetailFragment(dynamicBean)
        } else if (dynamicType == 2) {
            if (dynamicVideoList.isNullOrEmpty()) {
                getDynamicDetail()
            } else {
                initVideoDetailFragment(dynamicVideoList!!)
            }
        } else {
            Log.w(TAG, "未知的动态类型")
        }
    }

    private fun initVideoDetailFragment(videoList: MutableList<DynamicBean>) {
        if (fromUpperHomePage) {
            supportFragmentManager.commit {
                val fragment =
                    UpperDynamicVideoDetailFragment.newInstance(
                        currentIndex,
                        videoList,
                        commentId,
                        replyId,
                        showComment,
                        isFromMineDynamic,
                        upperId
                    )
                replace(R.id.fragment_container, fragment, "upper_dynamic_video_detail")
                setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            }
        } else {
            supportFragmentManager.commit {
                val fragment =
                    DynamicVideoDetailFragment.newInstance(
                        currentIndex,
                        mediaKey,
                        videoList,
                        commentId,
                        replyId,
                        showComment,
                        isFromMineDynamic,
                        openRecommend
                    )
                replace(R.id.fragment_container, fragment, "dynamic_video_detail")
                setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
            }
        }
    }

    private fun initDynamicDetailFragment(dynamicBean: DynamicBean?) {
        supportFragmentManager.commit {
            val fragment =
                DynamicDetailFragment.newInstance(
                    mediaKey,
                    dynamicBean,
                    picIndex,
                    commentId,
                    replyId,
                    showComment,
                    isFromMineDynamic,
                )
            replace(R.id.fragment_container, fragment, "dynamic_pic_detail")
            setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        }
    }

    private fun getDynamicDetail() {
        if (mediaKey.isNullOrEmpty()) {
            Log.e(TAG, "参数不完整")
        }
        loading.showProgress(getString(R.string.loading))
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        HttpRequest.post(RequestUrls.GET_DYNAMIC_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                //设置图片
                val dynamicBean = Gson().fromJson<DynamicBean>(
                    response.toString(),
                    object : TypeToken<DynamicBean>() {}.type
                )
                if (dynamicBean.photoType == 1) {
                    loading.dismissProgress()
                    initDynamicDetailFragment(dynamicBean)
                } else {
                    getPlayInfo(dynamicBean)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                loading.showFailed {
                    getDynamicDetail()
                }
            }
        }, param, this)
    }

    /**
     * 获取对应播放地址
     */
    private fun getPlayInfo(data: DynamicBean) {
        val params = HttpParams()
        params.put("mediaKey", data.smallMediaKey)
        params.put("videoType", 3)
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val clarityList = Gson().fromJson<MutableList<ClarityBean>?>(
                    response?.optJSONArray("list").toString(),
                    object : TypeToken<List<ClarityBean>>() {}.type
                )
                if (!clarityList.isNullOrEmpty()) {
                    val clarityBean = clarityList[0]
                    val mediaUrl = clarityBean.mediaUrl
                    if (!mediaUrl.isNullOrEmpty()) {
                        loading.dismissProgress()
                        data.mediaUrl = mediaUrl
                        if (dynamicVideoList == null) {
                            dynamicVideoList = mutableListOf(data)
                        } else {
                            dynamicVideoList?.clear()
                            dynamicVideoList?.add(data)
                        }
                        initVideoDetailFragment(dynamicVideoList!!)
                    }
                } else {
                    loading.showFailed {
                        getPlayInfo(data)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                loading.showFailed {
                    getPlayInfo(data)
                }
            }
        }, params, this)
    }
}