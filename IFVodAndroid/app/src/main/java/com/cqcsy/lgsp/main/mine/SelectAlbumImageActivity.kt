package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upload.album.DownloadImageUtil
import com.cqcsy.lgsp.upper.pictures.ViewAllActivity
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_select_album_image.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.Serializable

/**
 * 从相册中选择封面图片
 */
class SelectAlbumImageActivity : BaseActivity() {
    var isManager = false
    var mediaKey: String? = null
    var pictureCount = 0
    var position = 0
    var page = 1
    var size = 30
    private var dataList: MutableList<ImageBean> = ArrayList()
    private var adapter: BaseQuickAdapter<ImageBean, BaseViewHolder>? = null
    private var selectList: MutableList<ImageBean> = ArrayList()
    private var isAllSelect = false
    private val transferCode = 1000
    private val viewImgCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_album_image)
        isManager = intent.getBooleanExtra("isManager", false)
        mediaKey = intent.getStringExtra("mediaKey")
        pictureCount = intent.getIntExtra("pictureCount", 0)
        val rightText = findViewById<TextView>(R.id.rightText)
        val headerTitle = findViewById<TextView>(R.id.headerTitle)
        val largeTip = findViewById<TextView>(R.id.large_tip)
        val littleTip = findViewById<TextView>(R.id.little_tip)
        rightText.visibility = View.VISIBLE
        initRefreshLayout()
        initView()
        if (isManager) {
            headerTitle.text = StringUtils.getString(R.string.batchManagement)
            rightText.text = StringUtils.getString(R.string.cancel)
            line.visibility = View.VISIBLE
            bottomLayout.visibility = View.VISIBLE
            page = intent.getIntExtra("page", 1)
            position = intent.getIntExtra("position", 0)
            if (intent.getSerializableExtra("list") != null) {
                dataList.addAll(intent.getSerializableExtra("list") as MutableList<ImageBean>)
            }
            adapter?.notifyDataSetChanged()
            if (!dataList.isNullOrEmpty() && position < dataList.size) {
                recyclerView.smoothScrollToPosition(position)
            }
            val isNoLoadMore = intent.getBooleanExtra("isNoLoadMore", false)
            if (isNoLoadMore) {
                refreshLayout.setEnableLoadMore(false)
            }
        } else {
            headerTitle.text = StringUtils.getString(R.string.selectFaceImage)
            rightText.text = StringUtils.getString(R.string.ensure)
        }
        largeTip.text = StringUtils.getString(R.string.noPhotoData)
        littleTip.text = StringUtils.getString(R.string.goToUpload)
        if (dataList.isNullOrEmpty()) {
            page = 1
            showProgress()
            getHttpData()
        }
    }

    /**
     * 初始化下拉刷新
     */
    private fun initRefreshLayout() {
        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(true)
        refreshLayout.setOnLoadMoreListener {
            onLoadMore()
        }
        refreshLayout.setOnRefreshListener {
            onRefresh()
        }
    }

    private fun initView() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(
            XGridBuilder(this).setHLineSpacing(5f).setVLineSpacing(5f).setIncludeEdge(true).build()
        )
        adapter = object : BaseQuickAdapter<ImageBean, BaseViewHolder>(
            R.layout.item_select_short_video,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: ImageBean) {
                val position = getItemPosition(item)
                ImageUtil.loadImage(
                    this@SelectAlbumImageActivity,
                    item.imgPath,
                    holder.getView(R.id.image)
                )
                val checkBox = holder.getView<ImageView>(R.id.checkbox)
                checkBox.isSelected = selectList.contains(item)
                holder.getView<ImageView>(R.id.image).setOnClickListener {
                    if (!isManager) {
                        val selected: MutableList<String> = ArrayList()
                        selectList.forEach {
                            if (!it.imgPath.isNullOrEmpty()) {
                                selected.add(it.imgPath!!)
                            }
                        }
                        val intent = Intent(context, ViewAllActivity::class.java)
                        intent.putExtra(PictureViewerActivity.SHOW_BOTTOM, true)
                        intent.putExtra(PictureViewerActivity.AUTO_HIDE, false)
                        intent.putExtra(ViewAllActivity.SHOW_DATA, dataList as Serializable)
                        intent.putExtra(ViewAllActivity.SHOW_SELECT, selected as Serializable)
                        intent.putExtra(PictureViewerActivity.SHOW_INDEX, position)
                        intent.putExtra(PictureViewerActivity.SHOW_COUNTS, pictureCount)
                        intent.putExtra(ViewAllActivity.PID, mediaKey)
                        startActivityForResult(intent, viewImgCode)
                    }
                }
                checkBox.setOnClickListener {
                    if (isManager) {
                        if (checkBox.isSelected) {
                            checkBox.isSelected = false
                            selectList.remove(item)
                        } else {
                            checkBox.isSelected = true
                            selectList.add(item)
                        }
                        if (selectList.size == dataList.size) {
                            allSelectImg.isSelected = true
                            isAllSelect = true
                        } else {
                            allSelectImg.isSelected = false
                            isAllSelect = false
                        }
                        if (isManager) {
                            refreshBottomView()
                        }
                    } else {
                        selectList.clear()
                        selectList.add(item)
                        notifyDataSetChanged()
                    }
                }
            }
        }
        recyclerView.adapter = adapter
    }

    private fun refreshBottomView() {
        if (selectList.isNullOrEmpty()) {
            allSelectImg.isSelected = false
            isAllSelect = false
            download.setTextColor(ColorUtils.getColor(R.color.word_color_5))
            transfer.setTextColor(ColorUtils.getColor(R.color.word_color_5))
            delete.setTextColor(ColorUtils.getColor(R.color.word_color_5))
            downloadImg.isSelected = false
            transferImg.isSelected = false
            deleteImg.isSelected = false
        } else {
            download.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            transfer.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            delete.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            downloadImg.isSelected = true
            transferImg.isSelected = true
            deleteImg.isSelected = true
        }
    }

    private fun onRefresh() {
        refreshLayout.setEnableLoadMore(true)
        selectList.clear()
        refreshBottomView()
        dataList.clear()
        adapter?.notifyDataSetChanged()
        page = 1
        getHttpData()
    }

    private fun onLoadMore() {
        getHttpData(true)
    }

    private fun getHttpData(isLoad: Boolean = false) {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("sort", 1)
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.ALBUM_DETAILS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (page == 1) {
                    dismissProgress()
                    refreshLayout.finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    if (dataList.isNullOrEmpty()) {
                        showEmpty()
                    } else {
                        refreshLayout.finishLoadMoreWithNoMoreData()
                    }
                    return
                }
                val list = Gson().fromJson<MutableList<ImageBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<ImageBean>>() {}.type
                )
                dataList.addAll(list)
                if (isAllSelect && isLoad) {
                    selectList.clear()
                    selectList.addAll(dataList)
                }
                adapter?.notifyDataSetChanged()
                if (list.size >= size) {
                    page += 1
                    refreshLayout.finishLoadMore()
                } else {
                    refreshLayout.finishLoadMoreWithNoMoreData()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (page == 1) {
                    refreshLayout.finishRefresh()
                    showFailed {
                        showProgress()
                        getHttpData()
                    }
                } else {
                    refreshLayout.finishLoadMore(false)
                }
            }

        }, params, this)
    }

    fun allSelect(view: View) {
        selectList.clear()
        if (allSelectImg.isSelected) {
            allSelectImg.isSelected = false
            isAllSelect = false
        } else {
            allSelectImg.isSelected = true
            isAllSelect = true
            dataList.let { selectList.addAll(it) }
        }
        adapter?.notifyDataSetChanged()
        refreshBottomView()
    }

    fun downloadBtn(view: View) {
        if (selectList.isNullOrEmpty()) {
            return
        }
        if (selectList.size > 50) {
            val tipsDialog = TipsDialog(this)
            tipsDialog.setDialogTitle(R.string.tips)
            tipsDialog.setMsg(R.string.downloadTips)
            tipsDialog.setLeftListener(R.string.cancel) {
                tipsDialog.dismiss()
            }
            tipsDialog.setRightListener(R.string.download) {
                isAllSelect = false
                allSelectImg.isSelected = true
                adapter?.notifyDataSetChanged()
                refreshBottomView()
                DownloadImageUtil.downloadImage(applicationContext, selectList.subList(0, 50))
                tipsDialog.dismiss()
            }
            tipsDialog.show()
        } else {
            DownloadImageUtil.downloadImage(applicationContext, selectList)
        }
    }

    fun transferBtn(view: View) {
        if (selectList.isNullOrEmpty()) {
            return
        }
        val intent = Intent(this, TransferAlbumActivity::class.java)
        intent.putExtra("ids", getIds() as Serializable)
        intent.putExtra("oldMediaKey", mediaKey)
        startActivityForResult(intent, transferCode)
    }

    fun deleteBtn(view: View) {
        if (selectList.isNullOrEmpty()) {
            return
        }
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.deletePhoto)
        dialog.setMsg(R.string.deletePhotoTips)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.delete) {
            deleteHttp()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteHttp() {
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("ids", getIds())
        HttpRequest.post(
            RequestUrls.DELETE_PICTURES, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    EventBus.getDefault().post(AlbumRefreshEvent())
                    ToastUtils.showLong(R.string.deleteSuccess)
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    private fun getIds(): String {
        var ids = ""
        if (selectList.isNullOrEmpty()) {
            return ids
        }
        for (i in selectList.indices) {
            ids = if (i == 0) {
                selectList[i].id.toString()
            } else {
                ids + "," + selectList[i].id
            }
        }
        return ids
    }

    fun onRightClick(view: View) {
        rightStart()
    }

    private fun rightStart() {
        if (isManager) {
            finish()
            return
        }
        if (!selectList.isNullOrEmpty()) {
            val intent = Intent()
            intent.putExtra("imagePath", selectList[0].imgPath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            ToastUtils.showLong(R.string.selectImageTips)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == transferCode) {
                setResult(Activity.RESULT_OK)
                finish()
            }
            if (requestCode == viewImgCode) {
                selectList.clear()
                val list =
                    data?.getSerializableExtra(ViewAllActivity.IMAGE_LIST) as MutableList<String>
                list.forEach { url ->
                    val beanList = dataList.filter { it.imgPath == url }
                    if (!beanList.isNullOrEmpty()) {
                        selectList.addAll(beanList)
                    }
                }
                if (data.getBooleanExtra("isFinish", false)) {
                    rightStart()
                } else {
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    fun showProgress() {
        if (isSafe()) {
            refreshLayout.visibility = View.GONE
            statusView.showProgress()
        }
    }

    fun dismissProgress() {
        if (isSafe()) {
            refreshLayout.visibility = View.VISIBLE
            statusView.dismissProgress()
        }
    }

    fun showFailed(listener: View.OnClickListener) {
        if (isSafe()) {
            refreshLayout.visibility = View.GONE
            statusView.showFailed(listener)
        }
    }

    fun showEmpty() {
        if (isSafe()) {
            refreshLayout.visibility = View.GONE
            statusView.showEmpty()
        }
    }
}