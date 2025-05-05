package com.cqcsy.lgsp.search

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.SearchResultAdapter
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.SearchResultBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.download.DownloadUtil
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.video.AnthologyActivity
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.lgsp.video.bean.ClarityBean
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_search_result.*
import org.json.JSONObject

/**
 * 搜索视频结果页
 */
class SearchVideoFragment : RefreshFragment() {
    var keyword = ""
    private val anthologyCode = 1001

    // 小视频集合
    private var shortVideoList: MutableList<ShortVideoBean> = ArrayList()

    private var searchResultAdapter: SearchResultAdapter? = null

    override fun getRefreshChild(): Int {
        return R.layout.layout_search_result
    }

    override fun initData() {
        super.initData()
        keyword = arguments?.getString("keyword") ?: ""
        getSearchResult()
    }

    override fun initView() {
        super.initView()
        setEnableRefresh(false)
        setEnableLoadMore(true)
        emptyLargeTip.text = StringUtils.getString(R.string.searchNoData)
        emptyLittleTip.text = StringUtils.getString(R.string.searchNoDataTips)
        shortVideoRecycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        shortVideoRecycle.addItemDecoration(XLinearBuilder(requireContext()).setSpacing(20f).build())
        searchResultRecycle.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        shortVideoRecycle.adapter = object : BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(
            R.layout.item_short_video,
            shortVideoList
        ) {
            override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
                item.coverImgUrl?.let {
                    ImageUtil.loadImage(context, it, holder.getView(R.id.shortVideoImage))
                }
                holder.setText(R.id.shortVideoTitle, item.title)
                holder.setText(R.id.shortVideoUserName, item.upperName)
                holder.setText(R.id.times, item.duration)
                holder.setText(R.id.shortVideoPlayCount, NormalUtil.formatPlayCount(item.playCount))
                holder.setText(R.id.shortVideoUpdateTime, TimesUtils.friendDate(item.date))
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    startVideoPlayVertical(item)
                }
            }
        }
        searchResultAdapter = SearchResultAdapter(searchItemClick)
        searchResultRecycle.adapter = searchResultAdapter
    }


    private val searchItemClick = object : SearchResultAdapter.OnClickListener {
        override fun onItemClick(type: Int, view: View?, searchResultBean: SearchResultBean) {
            when (type) {
                // 立即播放
                0 -> startVideoPlayVertical(searchResultBean)
                1 -> {
                    // 具体选集点击
                    startVideoPlayVertical(searchResultBean)
                }

                2 -> {
                    // 进入选集页
                    startAnthologyActivity(searchResultBean, false)
                }

                3 -> {
                    // 下载
                    if (!GlobalValue.isLogin()) {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        return
                    }
                    if (!GlobalValue.isVipUser()) {
                        val intent = Intent(context, OpenVipActivity::class.java)
                        intent.putExtra("pathInfo", requireActivity().javaClass.simpleName)
                        startActivity(intent)
                        return
                    }
                    if (searchResultBean.videoType == Constant.VIDEO_VARIETY
                        || searchResultBean.videoType == Constant.VIDEO_TELEPLAY
                        || (searchResultBean.episodes?.size ?: 0) > 1
                    ) {
                        // 进入选集页
                        startAnthologyActivity(searchResultBean, true)
                    } else {
                        getClarity(searchResultBean)
                    }
                }
            }
        }
    }

    /**
     * 跳转选集页
     */
    fun startAnthologyActivity(searchResultBean: SearchResultBean, isDownLoad: Boolean) {
        val intent = Intent(requireContext(), AnthologyActivity::class.java)
        intent.putExtra("mediaKey", searchResultBean.mediaKey)
        intent.putExtra("videoType", searchResultBean.videoType)
        intent.putExtra("episodeTitle", searchResultBean.episodeTitle)
        intent.putExtra("lang", searchResultBean.lang)
        if (isDownLoad) {
            intent.putExtra("pageAction", 1)
            intent.putExtra("coverImage", searchResultBean.coverImgUrl)
        }
        startActivityForResult(intent, anthologyCode)
    }

    /**
     * 跳转竖屏播放页
     */
    fun startVideoPlayVertical(videoBaseBean: VideoBaseBean) {
        val intent = Intent(context, VideoPlayVerticalActivity::class.java)
        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, videoBaseBean)
        startActivity(intent)
    }

    /**
     * 网络请求搜索结果数据
     */
    private fun getSearchResult() {
        showProgress()
        // 需要清除数据刷新页面，防止再次请求返回空数据页面有切换现象
        searchResultAdapter?.clear()
        shortVideoList.clear()
        shortVideoTitle.visibility = View.GONE
        shortVideoRecycle.adapter?.notifyDataSetChanged()
        page = 1
        val params = HttpParams()
        params.put("SearchCriteria", keyword)
        HttpRequest.post(RequestUrls.SEARCH_RESULT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray != null && jsonArray.length() > 0) {
                    val list: List<SearchResultBean> = Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<SearchResultBean>>() {}.type
                    )
                    searchResultAdapter?.addData(list)
                }
                getShortVideoData()
                hideKeyboard(shortVideoRecycle)
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (isSafe()) {
                    dismissProgress()
                    showEmpty()
                    hideKeyboard(shortVideoRecycle)
                }
            }
        }, params, this)
    }

    override fun onLoadMore() {
        getShortVideoData()
    }

    /**
     * 获取小视频数据
     */
    private fun getShortVideoData() {
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        params.put("SearchCriteria", keyword)
        HttpRequest.post(RequestUrls.SHORT_VIDEO_FILTER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (shortVideoList.isEmpty()) {
                        shortVideoTitle.visibility = View.GONE
                        if (searchResultAdapter?.data.isNullOrEmpty()) {
                            showEmpty()
                        }
                    }
                    finishLoadMoreWithNoMoreData()
                    return
                }
                if (page == 1) {
                    shortVideoList.clear()
                }
                val list: List<ShortVideoBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<ShortVideoBean>>() {}.type
                )
                shortVideoTitle.visibility = View.VISIBLE
                shortVideoList.addAll(list)
                shortVideoRecycle.adapter?.notifyDataSetChanged()
                if (list.isNullOrEmpty()) {
                    finishLoadMoreWithNoMoreData()
                } else {
                    page += 1
                    finishLoadMore()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgress()
                finishLoadMore()
                shortVideoTitle?.visibility = View.GONE
                if (searchResultAdapter?.data.isNullOrEmpty() && page == 1) {
                    showEmpty()
                }
            }
        }, params, this)
    }

    /**
     * 获取清晰度
     */
    private fun getClarity(searchResultBean: SearchResultBean) {
        val params = HttpParams()
        params.put("mediaKey", searchResultBean.mediaKey)
        params.put("videoId", searchResultBean.uniqueID)
        params.put("videoType", searchResultBean.videoType)
        HttpRequest.get(RequestUrls.VIDEO_PLAY_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    ToastUtils.showLong(R.string.on_video_source)
                    return
                }
                val list = Gson().fromJson<MutableList<ClarityBean>>(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<ClarityBean>>() {}.type
                )
                if (!list.isNullOrEmpty()) {
                    DownloadUtil.showSelectQuality(
                        requireContext(),
                        list,
                        searchResultBean
                    )
                } else {
                    ToastUtils.showLong(R.string.on_video_source)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    /**
     * 隐藏软键盘
     */
    private fun hideKeyboard(view: View) {
        val manager: InputMethodManager = view.context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == anthologyCode) {
                val videoBaseBean = data?.getSerializableExtra("anthologyBean") as VideoBaseBean
                startVideoPlayVertical(videoBaseBean)
            }
        }
    }
}