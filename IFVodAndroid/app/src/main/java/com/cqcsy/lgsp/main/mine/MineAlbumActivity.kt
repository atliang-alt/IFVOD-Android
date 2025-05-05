package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.AlbumListAdapter
import com.cqcsy.library.base.refresh.RefreshActivity
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.uploadPicture.UploadTaskFinishEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_album_list_header.view.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 我的相册
 */
class MineAlbumActivity : RefreshActivity() {
    private var albumData: MutableList<PicturesBean> = ArrayList()
    private var mAdapter: AlbumListAdapter? = null

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onChildAttach() {
        setHeaderTitle(R.string.mineAlbum)
        initView()
        getHttpData(true)
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
    }

    override fun onRefresh() {
        super.onRefresh()
        reset(false)
    }

    private fun reset(isShow: Boolean) {
        OkGo.getInstance().cancelTag(this)
        albumData.clear()
        mAdapter?.notifyDataSetChanged()
        page = 1
        getHttpData(isShow)
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getHttpData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(task: PictureUploadTask) {
        if (!isPaused && task.status == PictureUploadStatus.ERROR) {
            mAdapter?.setUpload(task)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveImageEvent(event: UploadTaskFinishEvent) {
        if (event.task != null) {
            if (event.isSuccess && event.response != null) {
                val bean = Gson().fromJson(
                    event.response.toString(), ImageBean::class.java
                )
                addDataCount(bean)
            } else {
                mAdapter?.setUpload(event.task!!)
            }
        }
    }

    /**
     * 上传成功后添加数据
     */
    private fun addDataCount(bean: ImageBean) {
        for (it in albumData) {
            if (it.mediaKey == bean.mediaKey) {
                it.photoCount++
                if (it.coverPath.isNullOrEmpty()) {
                    it.coverPath = bean.imgPath
                }
                break
            }
        }
        mAdapter?.notifyDataSetChanged()
    }

    private fun initView() {
        setEnableRefresh(true)
        setEnableLoadMore(true)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.addItemDecoration(
            XGridBuilder(this).setVLineSpacing(12f).setHLineSpacing(15f).setIncludeEdge(true).build()
        )
        mAdapter = AlbumListAdapter(this, albumData)
        setHeaderView()
        recyclerView.adapter = mAdapter
    }

    private fun setHeaderView() {
        val headView = View.inflate(this, R.layout.layout_album_list_header, null)
        headView.picture_cover.setBackgroundResource(R.mipmap.icon_new_album)
        headView.setOnClickListener {
            val intent = Intent(this, CreateAlbumActivity::class.java)
            startActivity(intent)
        }
        mAdapter?.addHeadView(headView)
    }

    private fun getHttpData(isShow: Boolean = false) {
        if (isShow) {
            showProgress()
        }
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(
            RequestUrls.MINE_PICTURES, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgress()
                    if (page == 1) {
                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        finishLoadMoreWithNoMoreData()
                        return
                    }
                    val list = Gson().fromJson<MutableList<PicturesBean>>(
                        jsonArray.toString(),
                        object : TypeToken<List<PicturesBean>>() {}.type
                    )
                    albumData.addAll(list)
                    mAdapter?.notifyDataSetChanged()
                    if (list.size >= size) {
                        page += 1
                        finishLoadMore()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page == 1) {
                        finishRefresh()
                        showFailed { reset(true) }
                    } else {
                        errorLoadMore()
                    }
                }
            }, params, this
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshEvent(refreshEvent: AlbumRefreshEvent) {
        reset(false)
    }
}