package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.lgsp.event.CommentEvent
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.lgsp.upper.pictures.PicturesCommentListActivity
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.upper.pictures.ViewAllActivity
import com.cqcsy.lgsp.video.fragment.VideoCommentFragment
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.uploadPicture.UploadTaskFinishEvent
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_album_details.*
import kotlinx.android.synthetic.main.layout_album_sort_dialog.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.Serializable

/**
 * 相册详情页
 */
class AlbumDetailsActivity : NormalActivity() {
    companion object {
        const val ALBUM_ID = "albumId"
    }

    private var dataList: MutableList<ImageBean> = ArrayList()
    private var page: Int = 1
    private var size: Int = 30

    // 排序， 默认时间倒序
    private var sort: Int = 1

    // 相册信息
    private var albumBean: PicturesBean? = null

    private var isNoLoadMore: Boolean = false

    private var mediaKey: String? = null

    override fun getContainerView(): Int {
        return R.layout.activity_album_details
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRefresh()
        mediaKey = intent.getStringExtra(ALBUM_ID)
        getAlbum()
        addHot(mediaKey)
    }

    private fun getAlbum() {
        showProgressView()
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.GET_ALBUM, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                albumBean = GsonUtils.fromJson<PicturesBean>(
                    response.toString(),
                    object : TypeToken<PicturesBean>() {}.type
                )
                if (albumBean == null) {
                    dismissProgressView()
                    showFailedView {
                        getAlbum()
                    }
                    return
                }
                if (!GlobalValue.isLogin()) {
                    addRecord(albumBean!!)
                }
                initView()
                getHttpData(false)
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressView()
                showFailedView {
                    getAlbum()
                }
            }

        }, params)
    }

    private fun addHot(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.PICTURES_HOT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params)
    }

    private fun addRecord(bean: PicturesBean) {
        val dynamicRecordBean = DynamicRecordBean()
//        dynamicRecordBean.pid = bean.id
        dynamicRecordBean.mediaKey = bean.mediaKey
        dynamicRecordBean.headImg = bean.headImg ?: ""
        dynamicRecordBean.upperName = bean.upperName ?: ""
        dynamicRecordBean.createTime = bean.createTime
        dynamicRecordBean.title = bean.title
        dynamicRecordBean.description = bean.description ?: ""
        dynamicRecordBean.coverPath = bean.coverPath ?: ""
        dynamicRecordBean.photoCount = bean.photoCount
        dynamicRecordBean.comments = bean.comments
        dynamicRecordBean.likeCount = bean.likeCount
        dynamicRecordBean.uid = bean.uid
        dynamicRecordBean.bigV = bean.bigV
        dynamicRecordBean.vipLevel = bean.vipLevel
        dynamicRecordBean.type = 1
        DynamicRecordManger.instance.add(dynamicRecordBean)
    }

    override fun onResume() {
        super.onResume()
        if (albumBean != null) {
            setUpload()
        }
    }

    private fun initView() {
        sort = albumBean?.sort ?: 1
        setHeaderTitle(albumBean?.title ?: "")
        setRightText(R.string.manage)
        browseCount.text = albumBean?.viewCount.toString()
        zanCount.text = albumBean?.likeCount.toString()
        commentCount.text = albumBean?.comments.toString()
        countLayout.setOnClickListener {
            val intent = Intent(this, PicturesCommentListActivity::class.java)
            intent.putExtra(PicturesCommentListActivity.PICTURES_MEDIA_ID, albumBean?.mediaKey)
            intent.putExtra(PicturesCommentListActivity.PICTURES_COMMENT, albumBean?.comments)
            intent.putExtra(PicturesCommentListActivity.PICTURES_TITLE, albumBean?.title)
            intent.putExtra(PicturesCommentListActivity.PICTURES_TYPE, albumBean?.videoType)
            intent.putExtra(VideoCommentFragment.SHOW_INPUT, false)
            startActivity(intent)
        }
        albumDetailsRecycler.layoutManager = GridLayoutManager(this, 3)
        albumDetailsRecycler.addItemDecoration(
            XGridBuilder(this).setVLineSpacing(2.5f).setHLineSpacing(5f).setIncludeEdge(true).build()
        )
        if (!albumBean?.label.isNullOrEmpty()) {
            addLabelView(albumBean?.label!!)
        }
        if (!albumBean?.description.isNullOrEmpty()) {
            albumInfo.visibility = View.VISIBLE
            albumInfo.text = Html.fromHtml(albumBean?.description!!.replace("\n", "<br>"))
        }
        setUpload()
        uploadingLayout.setOnClickListener {
            val intent = Intent(this, UploadPhotoListActivity::class.java)
            intent.putExtra(ALBUM_ID, albumBean?.mediaKey)
            startActivity(intent)
        }
        albumDetailsRecycler.adapter = object : BaseQuickAdapter<ImageBean, BaseViewHolder>(R.layout.item_album_details, dataList) {
            override fun convert(holder: BaseViewHolder, item: ImageBean) {
                val position = getItemPosition(item)
                val imageView = holder.getView<ImageView>(R.id.image)
                val longImageTag = holder.getView<ImageView>(R.id.longImageTag)
                ImageUtil.loadImage(context, item.imgPath, imageView)
                longImageTag.isVisible = item.isLongImage
                holder.getView<ImageView>(R.id.image).setOnClickListener {
                    val intent = Intent(context, ViewAllActivity::class.java)
                    intent.putExtra(UpperPicturesFragment.PICTURES_ITEM, albumBean)
                    intent.putExtra(PictureViewerActivity.SHOW_BOTTOM, false)
                    intent.putExtra(ViewAllActivity.SHOW_DATA, dataList as Serializable)
                    intent.putExtra(PictureViewerActivity.SHOW_INDEX, position)
                    intent.putExtra(PictureViewerActivity.SHOW_COUNTS, albumBean?.photoCount)
                    intent.putExtra(PictureViewerActivity.SHOW_TITLE, albumBean?.title)
                    startActivity(intent)
                }
                holder.getView<ImageView>(R.id.image).setOnLongClickListener {
                    startSelectAlbumImage(position)
                    return@setOnLongClickListener false
                }
            }
        }
    }

    private fun addLabelView(label: String) {
        val labelList = label.split(",")
        if (labelList.isNotEmpty()) {
            labelLayout.visibility = View.VISIBLE
            val color = ColorUtils.getColor(R.color.grey)
            val padding = SizeUtils.dp2px(5f)
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            params.rightMargin = SizeUtils.dp2px(10f)
            params.bottomMargin = SizeUtils.dp2px(10f)
            labelList.forEach {
                val tagText = TextView(this)
                tagText.setBackgroundResource(R.drawable.tag_bg)
                tagText.text = it
                tagText.setTextColor(color)
                tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                tagText.setPadding(padding, 4, padding, 4)
                labelLayout.addView(tagText, params)
            }
        }
    }

    private fun initRefresh() {
        albumDetailsRefresh.setEnableRefresh(true)
        albumDetailsRefresh.setEnableLoadMore(true)
        albumDetailsRefresh.setEnableAutoLoadMore(true)
        albumDetailsRefresh.setEnableOverScrollBounce(false)
        albumDetailsRefresh.setEnableLoadMoreWhenContentNotFull(false)

        albumDetailsRefresh.setDisableContentWhenLoading(false)
        albumDetailsRefresh.setDisableContentWhenRefresh(false)

        albumDetailsRefresh.setOnRefreshListener { onRefresh() }
        albumDetailsRefresh.setOnLoadMoreListener { onLoadMore() }
    }

    private fun onRefresh() {
        reset(false)
    }

    private fun onLoadMore() {
        getHttpData(false)
    }

    private fun getHttpData(isRefresh: Boolean) {
        val params = HttpParams()
        params.put("mediaKey", albumBean?.mediaKey)
        params.put("sort", sort)
        params.put("page", page)
        params.put("size", size)
        HttpRequest.post(RequestUrls.ALBUM_DETAILS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (page == 1) {
                    dataList.clear()
                    albumDetailsRecycler?.adapter?.notifyDataSetChanged()
                    dismissProgressView()
                    albumDetailsRefresh.finishRefresh()
                }
                val jsonArray = response?.optJSONArray("list")
                if ((response == null || jsonArray == null || jsonArray.length() == 0) && dataList.isEmpty()) {
                    showEmptyView()
                    return
                }
                val list = Gson().fromJson<MutableList<ImageBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<ImageBean>>() {}.type
                )
                dataList.addAll(list)
                albumDetailsRecycler?.adapter?.notifyDataSetChanged()
                if (isRefresh) {
                    EventBus.getDefault().post(AlbumRefreshEvent())
                }
                if (list.size >= size) {
                    page += 1
                    albumDetailsRefresh.finishLoadMore()
                    isNoLoadMore = false
                } else {
                    albumDetailsRefresh.finishLoadMoreWithNoMoreData()
                    isNoLoadMore = true
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                if (page == 1) {
                    albumDetailsRefresh.finishRefresh()
                    showFailedView { reset(false) }
                } else {
                    albumDetailsRefresh.finishLoadMore(false)
                }
            }
        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(task: PictureUploadTask) {
        if (!isPaused && task.taskTag == mediaKey) {
            setUpload(task)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveImageEvent(event: UploadTaskFinishEvent) {
        if (event.task != null && event.task!!.taskTag == mediaKey) {
            if (event.isSuccess && event.response != null) {
                val bean = Gson().fromJson(
                    event.response.toString(), ImageBean::class.java
                )
                uploadAddPhoto(bean)
                setFinishView(event.task!!)
            } else {
                setGoneUploadLayout(event.task!!)
            }
        }
    }

    /**
     * 上传成功后保存图片
     */
    private fun setFinishView(task: PictureUploadTask) {
        val uploadSize = task.totalTagSize - task.finishTagSize
        if (uploadSize == 0) {
            setUpload(task)
            return
        }
        uploadCounts?.text = uploadSize.toString()
        val values = (task.finishTagSize * 100) / task.totalTagSize
        uploadProgressBar?.progress = values
    }

    /**
     * 没有上传隐藏布局
     */
    private fun setGoneUploadLayout(task: PictureUploadTask?) {
        if (task == null || (task.totalTagSize - task.finishTagSize) == 0) {
            uploadingLayout?.visibility = View.GONE
            albumDetailsRefresh.setEnableRefresh(true)
        }
    }

    private fun setUpload(taskEvent: PictureUploadTask? = null) {
        val task = PictureUploadManager.getTaskInfoByTag(albumBean?.mediaKey)
        val uploadCount = (task?.totalTagSize?.minus(task.finishTagSize)) ?: 0
        if (uploadCount == 0) {
            setGoneUploadLayout(taskEvent)
            return
        }
        dismissProgressView()
        albumDetailsRefresh.setEnableRefresh(false)
        uploadingLayout?.visibility = View.VISIBLE
        uploadCounts?.text = uploadCount.toString()
        task?.apply {
            val values = finishTagSize * 100 / totalTagSize
            uploadProgressBar?.progress = values
        }
    }

    override fun onRightClick(view: View) {
        val menu = LayoutInflater.from(this).inflate(R.layout.layout_manage_album_menu, null)
        val popupWindow = PopupWindow(
            menu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        if (!dataList.isNullOrEmpty()) {
            menu.findViewById<View>(R.id.batchManagement).visibility = View.VISIBLE
            menu.findViewById<View>(R.id.albumSort).visibility = View.VISIBLE
        }
        menu.findViewById<View>(R.id.editAlbum).setOnClickListener {
            val intent = Intent(this, EditAlbumActivity::class.java)
            intent.putExtra("title", albumBean?.title)
            intent.putExtra("description", albumBean?.description)
            intent.putExtra("mediaKey", albumBean?.mediaKey)
            intent.putExtra("face", albumBean?.coverPath)
            intent.putExtra("pictureCount", albumBean?.photoCount)
            startActivityForResult(intent, 1001)
            popupWindow.dismiss()
        }
        menu.findViewById<View>(R.id.batchManagement).setOnClickListener {
            startSelectAlbumImage(0)
            popupWindow.dismiss()
        }
        menu.findViewById<View>(R.id.albumSort).setOnClickListener {
            showSortDialog()
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = menu
        popupWindow.isOutsideTouchable = true
        val lp = window.attributes
        lp.alpha = 0.5f
        window.attributes = lp
        popupWindow.setOnDismissListener {
            lp.alpha = 1f
            window.attributes = lp
        }
        popupWindow.showAsDropDown(view, -130, 0)
    }

    private fun startSelectAlbumImage(position: Int) {
        val intent = Intent(this, SelectAlbumImageActivity::class.java)
        intent.putExtra("mediaKey", albumBean?.mediaKey)
        intent.putExtra("isManager", true)
        intent.putExtra("isNoLoadMore", isNoLoadMore)
        intent.putExtra("position", position)
        intent.putExtra("page", page)
        intent.putExtra("list", dataList as Serializable)
        startActivityForResult(intent, 1002)
    }

    /**
     * 排序选择dialog
     */
    private fun showSortDialog() {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_album_sort_dialog, null)
        when (sort) {
            0 -> {
                contentView.downImg.visibility = View.GONE
                contentView.upImg.visibility = View.GONE
                contentView.fileImg.visibility = View.VISIBLE
            }

            2 -> {
                contentView.downImg.visibility = View.GONE
                contentView.upImg.visibility = View.VISIBLE
                contentView.fileImg.visibility = View.GONE
            }

            else -> {
                contentView.downImg.visibility = View.VISIBLE
                contentView.upImg.visibility = View.GONE
                contentView.fileImg.visibility = View.GONE
            }
        }
        contentView.downLayout.setOnClickListener {
            if (sort != 1) {
                sort = 1
                reset(true)
                setSortHttp()
            }
            dialog.dismiss()
        }
        contentView.upLayout.setOnClickListener {
            if (sort != 2) {
                sort = 2
                reset(true)
                setSortHttp()
            }
            dialog.dismiss()
        }
        contentView.fileLayout.setOnClickListener {
            if (sort != 0) {
                sort = 0
                reset(true)
                setSortHttp()
            }
            dialog.dismiss()
        }
        contentView.cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        dialog.show()
    }

    private fun reset(isRefresh: Boolean) {
        page = 1
        getHttpData(isRefresh)
    }

    private fun setSortHttp() {
        val params = HttpParams()
        params.put("mediaKey", albumBean?.mediaKey)
        params.put("sort", sort)
        HttpRequest.post(RequestUrls.SET_SORT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }

    /**
     * 添加照片
     */
    fun addPhoto(view: View) {
        val intent = Intent(this, SelectLocalImageActivity::class.java)
        intent.putExtra(SelectLocalImageActivity.maxCountKey, 1000)
        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
        startActivityForResult(intent, 1000)
    }

    /**
     * 上传成功后本地添加图片
     */
    private fun uploadAddPhoto(bean: ImageBean) {
        if (bean.mediaKey == mediaKey && !dataList.contains(bean) && !bean.imgPath.isNullOrEmpty()) {
            albumBean?.photoCount = (albumBean?.photoCount ?: 0) + 1
            if (dataList.isEmpty()) {
                // 如果是空页面需要隐藏显示数据页
                dismissProgressView()
            }
            dataList.add(0, bean)
            albumDetailsRecycler?.adapter?.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommentChange(event: CommentEvent) {
        if (albumBean != null && event.mediaKey == albumBean!!.mediaKey) {
            albumBean!!.comments++
            commentCount.text = albumBean?.comments.toString()
            EventBus.getDefault().post(AlbumRefreshEvent())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                if (data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) != null) {
                    val list = data.getSerializableExtra(SelectLocalImageActivity.imagePathList)
                    val intent = Intent(this, UploadPhotoActivity::class.java)
                    intent.putExtra("imagePathList", list)
                    intent.putExtra("albumBean", albumBean)
                    startActivity(intent)
                }
            }
            if (requestCode == 1001) {
                if (data?.getBooleanExtra("isDelete", false) == true) {
                    finish()
                } else {
                    val title = data?.getStringExtra("title") ?: ""
                    setHeaderTitle(title)
                    if (title.isNotEmpty()) {
                        albumBean?.title = title
                    }
                    val description = data?.getStringExtra("description")
                    if (!description.isNullOrEmpty()) {
                        albumBean?.description = description
                        albumInfo?.visibility = View.VISIBLE
                        albumInfo?.text = Html.fromHtml(description.replace("\n", "<br>"))
                    }
                    val face = data?.getStringExtra("face")
                    albumBean?.coverPath = face
                }
            }
            if (requestCode == 1002) {
                showProgressView()
                reset(false)
            }
        }
    }

    private fun showProgressView() {
        if (isSafe()) {
            statusView.showProgress()
        }
    }

    private fun dismissProgressView() {
        if (isSafe()) {
            statusView.dismissProgress()
        }
    }

    private fun showFailedView(listener: View.OnClickListener) {
        if (isSafe()) {
            statusView.showFailed(listener)
        }
    }

    private fun showEmptyView() {
        if (isSafe()) {
            statusView.showEmpty(
                StringUtils.getString(R.string.noPhotoData),
                StringUtils.getString(R.string.goToUpload)
            )
        }
    }

}