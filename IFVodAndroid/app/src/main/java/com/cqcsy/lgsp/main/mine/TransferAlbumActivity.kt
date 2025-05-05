package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshActivity
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_transfer_album_header.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 转移到某个相册
 */
class TransferAlbumActivity : RefreshActivity() {
    private var albumData: MutableList<PicturesBean>? = null
    private var adapter: BaseQuickAdapter<PicturesBean, BaseViewHolder>? = null
    private var ids: String = ""
    private var oldMediaKey: String? = null
    private var createAlbumCode = 1001

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onChildAttach() {
        setHeaderTitle(R.string.transferTo)
        initData()
        initView()
        getHttpData()
    }

    override fun onRefresh() {
        super.onRefresh()
        albumData?.clear()
        adapter?.notifyDataSetChanged()
        page = 1
        getHttpData()
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getHttpData()
    }

    private fun initData() {
        ids = intent.getStringExtra("ids") ?: ""
        oldMediaKey = intent.getStringExtra("oldMediaKey")
        albumData = ArrayList()
    }

    private fun initView() {
        setEnableRefresh(true)
        setEnableLoadMore(true)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = object :
            BaseQuickAdapter<PicturesBean, BaseViewHolder>(
                R.layout.layout_transfer_album_item,
                albumData
            ) {
            override fun convert(holder: BaseViewHolder, item: PicturesBean) {
                ImageUtil.loadImage(
                    this@TransferAlbumActivity,
                    item.coverPath,
                    holder.getView(R.id.picture_cover),
                    defaultImage = 0, scaleType = ImageView.ScaleType.CENTER
                )
                holder.setText(R.id.picture_name, item.title)
                holder.setText(
                    R.id.picture_info, StringUtils.getString(R.string.photoCount, item.photoCount)
                )
                val selectImg = holder.getView<ImageView>(R.id.selectImg)
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    selectImg.visibility = View.VISIBLE
                    transferAlbum(item.mediaKey)
                }
            }
        }
        addHerder()
        recyclerView.adapter = adapter
    }

    private fun addHerder() {
        val headView = View.inflate(this, R.layout.layout_transfer_album_header, null)
        headView.headerLayout.setOnClickListener {
            val intent = Intent(this, CreateAlbumActivity::class.java)
            intent.putExtra("isMove", true)
            startActivityForResult(intent, createAlbumCode)
        }
        adapter?.addHeaderView(headView)
    }

    private fun getHttpData() {
        val params = HttpParams()
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.MINE_PICTURES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (page == 1) {
                    finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (albumData.isNullOrEmpty()) {
                        showEmpty()
                    } else {
                        finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list = Gson().fromJson<MutableList<PicturesBean>>(jsonArray.toString(), object : TypeToken<List<PicturesBean>>() {}.type)
                list.forEach {
                    if (it.mediaKey != oldMediaKey) {
                        albumData?.add(it)
                    }
                }
                adapter?.notifyDataSetChanged()
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
                } else {
                    errorLoadMore()
                }
            }
        }, params, this)
    }

    private fun transferAlbum(mediaKey: String) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("oldMediaKey", oldMediaKey)
        params.put("ids", ids)
        HttpRequest.post(RequestUrls.MOVE_PICTURES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                EventBus.getDefault().post(AlbumRefreshEvent())
                ToastUtils.showLong(R.string.transferSuccess)
                setResult(RESULT_OK)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == createAlbumCode) {
                val serializable = data?.getSerializableExtra("picturesBean")
                if (serializable != null) {
                    val bean = serializable as PicturesBean
                    albumData?.add(bean)
                    adapter?.notifyDataSetChanged()
                    transferAlbum(bean.mediaKey)
                }
            }
        }
    }
}