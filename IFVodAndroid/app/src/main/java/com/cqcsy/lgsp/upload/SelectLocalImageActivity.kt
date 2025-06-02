package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.medialoader.*
import com.cqcsy.lgsp.upload.bean.MediaFolder
import com.cqcsy.lgsp.upload.clip.PhotoCropActivity
import com.cqcsy.library.base.BaseActivity
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.loader.LocalMediaPageLoader
import kotlinx.android.synthetic.main.activity_selelct_image.*
import kotlinx.android.synthetic.main.layout_select_image_popup.view.*
import java.io.Serializable

/**
 * 手动上传视频封面
 * imageCountKey:传入最多选择的张数, 不传默认1
 * ---剪切比例单选需要传，多选不传---
 * widthProportion: 切图宽度比例
 * heightProportion: 切图高度比例
 */
class SelectLocalImageActivity : BaseActivity() {
    private var imageFolders: MutableList<MediaFolder>? = null
    private val photoClipCode = 1001
    private val selectViewCode = 1002

    // 需要的图片数量
    private var maxCount = 1
    private var maxVideoCount = 1

    // 宽度比例值
    private var widthProportion: Float = 1f

    // 高度比例值
    private var heightProportion: Float = 1f

    // 是否需要剪切图片
    private var isCutPhoto: Boolean = false

    // 是否返回gif图片
    private var isBackGif: Boolean = false

    private var selectFolder = 0
    private var mediaType: MediaType = MediaType.ALL
    private var chooseMode: ChooseMode = ChooseMode.ALL
    private lateinit var albumAdapter: AlbumAdapter

    private val selectList: MutableList<LocalMediaBean>
        get() {
            return albumAdapter.currentChooseList
        }
    private lateinit var mediaLoader: IBridgeMediaLoader

    companion object {
        const val imagePathList = "imagePathList" // 选择的图片集合
        const val maxCountKey = "maxCount" // 最多选择图片数量
        const val widthKey = "widthProportion" // 剪切的宽度比值
        const val heightKey = "heightProportion" // 剪切的高度比值
        const val isCutPhotoKey = "isCutPhoto" // 是否剪切
        const val isBackGifKey = "isBackGif" // 是否返回gif图片
        const val buttonText = "buttonText" // 按钮显示文案

        /**
         * 选择模式
         */
        const val chooseModeKey = "chooseMode"
        const val mediaTypeKey = "mediaType"

        /**
         * 可选最大视频数量
         * 该属性只在chooseModel为only时生效
         */
        const val maxVideoCountKey = "maxVideoCount"

        /**
         * 默认的分页数量
         */
        const val PAGE_SIZE = 40

        //用户保存数据，传递给预览页面；数据量大时通过该方式实现
        var pageIndex = 0
        var currentBucketId = PictureConfig.ALL.toLong()
        var cacheList: MutableList<LocalMediaBean>? = null
        var pictureConfig: SelectorConfig? = null

        fun reset() {
            pageIndex = 0
            currentBucketId = PictureConfig.ALL.toLong()
            cacheList = null
            pictureConfig = null
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data
            val list = data?.getSerializableExtra(AlbumGalleryActivity.SELECTED_LIST) as? MutableList<LocalMediaBean>
            val isFinish = data?.getBooleanExtra(AlbumGalleryActivity.IS_FINISH, false) ?: false
            val mediaType = data?.getSerializableExtra(AlbumGalleryActivity.CURRENT_MEDIA_TYPE) as? MediaType
            if (mediaType != null) {
                albumAdapter.currentMediaType = mediaType
            }
            if (!list.isNullOrEmpty()) {
                if (isFinish) {
                    selectList.clear()
                    selectList.addAll(list)
                    onRightClick()
                    return@registerForActivityResult
                }
                var isRefresh = false
                //对比图片差异，决定是否需要notifyDataSetChanged
                if (selectList.size == list.size) {
                    for ((index, albumEntity) in selectList.withIndex()) {
                        if (albumEntity != list[index]) {
                            isRefresh = true
                            break
                        }
                    }
                } else {
                    isRefresh = true
                }
                if (isRefresh) {
                    //刷新数据
                    selectList.clear()
                    selectList.addAll(list)
                    for (datum in albumAdapter.data) {
                        val indexOf = selectList.indexOf(datum)
                        if (indexOf != -1) {
                            datum.isChecked = true
                            datum.index = indexOf + 1
                        } else {
                            datum.isChecked = false
                            datum.index = -1
                        }
                    }
                    albumAdapter.notifyDataSetChanged()
                    setSelectedText()
                }
            } else {
                selectList.clear()
                for (datum in albumAdapter.data) {
                    datum.isChecked = false
                    datum.index = -1
                }
                albumAdapter.notifyDataSetChanged()
                setSelectedText()
            }
        }

    private var isRefresh = true
    private val resultList = object : OnQueryDataResultListener<LocalMedia>() {
        override fun onComplete(result: ArrayList<LocalMedia>?, isHasMore: Boolean) {
            super.onComplete(result, isHasMore)
            handleResult(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selelct_image)
        headerTitle.setText(R.string.allPhoto)
        reset()
        initData()
        initView()
        initList()
        initMediaLoader()
        requestPermissions()
        setSelectedText()
    }

    private fun initData() {
        maxCount = intent.getIntExtra(maxCountKey, 1)
        widthProportion = intent.getFloatExtra(widthKey, 1f)
        heightProportion = intent.getFloatExtra(heightKey, 1f)
        isCutPhoto = intent.getBooleanExtra(isCutPhotoKey, false)
        isBackGif = intent.getBooleanExtra(isBackGifKey, false)
        chooseMode = (intent.getSerializableExtra(chooseModeKey) as? ChooseMode) ?: ChooseMode.ALL
        mediaType = (intent.getSerializableExtra(mediaTypeKey) as? MediaType) ?: MediaType.ALL
        maxVideoCount = intent.getIntExtra(maxVideoCountKey, 1)
        val buttonText = intent.getStringExtra(buttonText)
        if (!buttonText.isNullOrEmpty()) {
            nextText.text = buttonText
        }
    }

    private fun initView() {
        backImage.setOnClickListener {
            onBackPressed()
        }
        nextText.setOnClickListener {
            onRightClick()
        }
        titleLayout.setOnClickListener {
            if (!imageFolders.isNullOrEmpty()) {
                showTitlePopup()
            }
        }
        emptyView.findViewById<TextView>(R.id.large_tip).text = StringUtils.getString(R.string.noData)
        emptyView.findViewById<TextView>(R.id.little_tip).visibility = View.GONE
    }

    private fun initList() {
        recyclerViewList.layoutManager = GridLayoutManager(this, 3)
        recyclerViewList.addItemDecoration(XGridBuilder(this).setVLineSpacing(5f).setHLineSpacing(5f).setIncludeEdge(true).build())
        recyclerViewList.itemAnimator = null
        albumAdapter = AlbumAdapter(maxCount, maxVideoCount, chooseMode, mediaType)
        albumAdapter.loadMoreModule.isEnableLoadMore = true
        albumAdapter.loadMoreModule.setOnLoadMoreListener {
            isRefresh = false
            mediaLoader.loadPageMediaData(currentBucketId, ++pageIndex, PAGE_SIZE, resultList)
            //mediaLoader.load(++pageIndex * PAGE_SIZE, PAGE_SIZE)
        }
        albumAdapter.loadMoreModule.isEnableLoadMoreIfNotFullPage = false
        albumAdapter.loadMoreModule.isAutoLoadMore = true
        albumAdapter.addChildClickViewIds(R.id.image, R.id.check_container)
        albumAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.image -> {
                    preview(position)
                }

                R.id.check_container -> {
                    choose(position)
                }
            }
        }
        recyclerViewList.adapter = albumAdapter
    }

    private fun initMediaLoader() {
        val mineType = when (mediaType) {
            MediaType.PHOTO -> SelectMimeType.ofImage()
            MediaType.VIDEO -> SelectMimeType.ofVideo()
            MediaType.ALL -> SelectMimeType.ofAll()
        }
        pictureConfig = SelectorConfig().apply {
            chooseMode = mineType
            filterVideoMaxSecond = 5 * 60 * 1000
            isGif = isBackGif
            sortOrder = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
            isPageStrategy = true
        }
        mediaLoader = LocalMediaPageLoader(this, pictureConfig)
    }

    private fun handleFolderResult(mediaFolder: MutableList<LocalMediaFolder>) {
        val list = mediaFolder.map { media ->
            MediaFolder(media.bucketId, media.folderName, null, media.firstImagePath)
        }.toMutableList()
        imageFolders = list
        if (mediaFolder.isNotEmpty()) {
            headerTitle.text = mediaFolder[0].folderName
        }
    }

    private fun handleResult(data: MutableList<LocalMedia>?) {
        val list = data?.map { localMedia ->
            LocalMediaBean().copy(localMedia)
        }?.toMutableList()
        if (list.isNullOrEmpty()) {
            albumAdapter.loadMoreModule.loadMoreEnd(true)
            showEmpty()
        } else {
            if (cacheList == null) {
                cacheList = mutableListOf()
            }
            showData()
            if (isRefresh) {
                albumAdapter.setList(list)
                cacheList?.clear()
                cacheList?.addAll(list)
            } else {
                albumAdapter.addData(list)
                cacheList?.addAll(list)
            }
            if (list.size < PAGE_SIZE) {
                albumAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
                albumAdapter.loadMoreModule.loadMoreComplete()
            }
        }
    }

    private fun requestPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            PermissionUtils.permission(PermissionConstants.STORAGE)
        }
        permission.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                mediaLoader.loadAllAlbum { handleFolderResult(it) }
                mediaLoader.loadPageMediaData(currentBucketId, 0, PAGE_SIZE, resultList)
            }

            override fun onDenied() {
                ToastUtils.showLong(R.string.permission_album)
                finish()
            }
        })
        permission.request()
    }

    private fun setSelectedText() {
        selectCount.text = StringUtils.getString(R.string.selected, selectList.size)
        nextText.isEnabled = selectList.size > 0
    }

    private fun preview(position: Int) {
        val item = albumAdapter.getItem(position)
        if (albumAdapter.currentMediaType == MediaType.PHOTO && item.isVideo) {
            return
        }
        if (item.isVideo) {
            val list = mutableListOf(item)
            AlbumGalleryActivity.launch(this, launcher, 0, list, list, 1, maxVideoCount, MediaType.VIDEO, false)
            return
        }
        var index = 0
        for (bean in albumAdapter.data) {
            if (!bean.isVideo) {
                if (item == bean) {
                    break
                }
                index++
            }
        }
        val data: MutableList<LocalMediaBean> = ArrayList()
        data.addAll(albumAdapter.data)
        data.removeAll { MimeTypeUtil.isVideo(it.mimeType) }
        AlbumGalleryActivity.launch(this, launcher, index, data, selectList, maxCount, maxVideoCount, MediaType.PHOTO)
    }

    private fun choose(position: Int) {
        val item = albumAdapter.getItem(position)
        if (chooseMode == ChooseMode.ONLY) {
            if (albumAdapter.currentMediaType == MediaType.PHOTO && MimeTypeUtil.isVideo(item.mimeType)) {
                //只能选图片
                return
            } else if (albumAdapter.currentMediaType == MediaType.VIDEO
                && (MimeTypeUtil.isImage(item.mimeType) || selectList.size <= maxVideoCount)
                && !item.isChecked
            ) {
                //只能选视频
                return
            }
            albumAdapter.currentMediaType = if (item.isChecked && selectList.size == 1) {
                //说明是最后一个，恢复初始状态
                mediaType
            } else {
                when {
                    MimeTypeUtil.isImage(item.mimeType) -> {
                        MediaType.PHOTO
                    }

                    MimeTypeUtil.isVideo(item.mimeType) -> {
                        MediaType.VIDEO
                    }

                    else -> {
                        MediaType.ALL
                    }
                }
            }
        }
        if (selectList.size >= maxCount && !item.isChecked) {
            ToastUtils.showLong(StringUtils.getString(R.string.selectCountTip, maxCount))
            return
        }
        val selectedSize = selectList.size
        if (item.isChecked) {
            item.isChecked = false
            selectList.remove(item)
        } else {
            item.isChecked = true
            selectList.add(item)
        }
        for (data in albumAdapter.data) {
            val indexOf = selectList.indexOf(data)
            if (indexOf != -1) {
                data.isChecked = true
                data.index = indexOf + 1
            } else {
                data.isChecked = false
                data.index = -1
            }
        }
        // 从0->1和1->0都需要全部刷新
        if ((selectedSize == 1 && selectList.size == 0) || (selectedSize == 0 && selectList.size == 1)) {
            albumAdapter.notifyItemRangeChanged(0, albumAdapter.data.size)
        } else {
            albumAdapter.notifyItemChanged(position)
        }
        setSelectedText()
    }

    private fun showTitlePopup() {
        titleImage.setImageResource(R.mipmap.icon_select_title_up)
        val menu = LayoutInflater.from(this).inflate(R.layout.layout_select_image_popup, null)
        val popupWindow = PopupWindow(menu, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        menu.recyclerView.layoutManager = LinearLayoutManager(this)
        menu.recyclerView.adapter = object : BaseQuickAdapter<MediaFolder, BaseViewHolder>(R.layout.item_select_title_popup, imageFolders) {
            override fun convert(holder: BaseViewHolder, item: MediaFolder) {
                val position = getItemPosition(item)
                holder.setText(R.id.title, item.name)
                if (selectFolder == position) {
                    holder.setVisible(R.id.titleImage, true)
                } else {
                    holder.setVisible(R.id.titleImage, false)
                }
                holder.getView<LinearLayout>(R.id.itemLayout).setOnClickListener {
                    selectFolder = position
                    selectList.clear()
                    isRefresh = true
                    pageIndex = 0
                    currentBucketId = item.bucketId
                    mediaLoader.loadPageMediaData(currentBucketId, 0, PAGE_SIZE, resultList)
                    headerTitle.text = item.name
                    setSelectedText()
                    popupWindow.dismiss()
                }
            }
        }
        popupWindow.setOnDismissListener {
            titleImage.setImageResource(R.mipmap.icon_select_title_down)
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = menu
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(titleLayout, 0, 0, Gravity.CENTER_HORIZONTAL)
    }

    private fun showEmpty() {
        recyclerViewList.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun showData() {
        recyclerViewList.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun onRightClick() {
        if (isCutPhoto) {
            val intent = Intent(this, PhotoCropActivity::class.java)
            intent.putExtra("imagePath", selectList[0].path)
            intent.putExtra("widthProportion", widthProportion)
            intent.putExtra("heightProportion", heightProportion)
            startActivityForResult(intent, photoClipCode)
            return
        }
        val intent = Intent()
        intent.putExtra(imagePathList, selectList as Serializable)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == photoClipCode) {
                val clipPath = data?.getStringExtra("clipPath")
                val intent = Intent()
                intent.putExtra("clipPath", clipPath)
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else if (requestCode == selectViewCode) {
                selectList.clear()
                val list = data?.getSerializableExtra(imagePathList) as MutableList<String>
                list.forEach { url ->
                    for (item in albumAdapter.data) {
                        if (item.path == url) {
                            item.isChecked = true
                            selectList.add(item)
                        }
                    }
                }
                if (data.getBooleanExtra("isFinish", false)) {
                    onRightClick()
                } else {
                    albumAdapter.notifyDataSetChanged()
                    setSelectedText()
                }
            }
        }
    }
}