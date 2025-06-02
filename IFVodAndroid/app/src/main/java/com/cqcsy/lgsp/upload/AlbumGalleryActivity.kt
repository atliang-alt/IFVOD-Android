package com.cqcsy.lgsp.upload

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.medialoader.MimeTypeUtil
import com.cqcsy.library.base.BaseActivity
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.loader.LocalMediaPageLoader
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.Serializable

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/13
 *
 *
 */
class AlbumGalleryActivity : BaseActivity() {

    companion object {
        const val SELECTED_LIST = "selected_list"
        const val IS_FINISH = "is_finish"
        private const val MAX_COUNT = "max_count"
        private const val MAX_VIDEO_COUNT = "max_video_count"
        const val CURRENT_MEDIA_TYPE = "current_media_type"
        private const val SHOW_BOTTOM = "show_bottom"
        const val SHOW_URL = "show_url"
        const val INDEX = "index"
        const val SHOW_PAGE_INDEX = "show_page_index"
        const val LOADER_CONFIG = "loader_config"
        const val CURRENT_OFFSET = "current_offset"
        private const val LIMIT = 10

        @JvmStatic
        fun launch(
            context: Context,
            launcher: ActivityResultLauncher<Intent>,
            position: Int,
            mediaList: MutableList<LocalMediaBean>?,
            selectedList: MutableList<LocalMediaBean>,
            maxCount: Int,
            maxVideoCount: Int,
            currentSelectMediaType: MediaType,
            showPageIndex: Boolean = false,
            showBottom: Boolean = true
        ) {
            val intent = Intent(context, AlbumGalleryActivity::class.java)
            intent.putExtra(SHOW_URL, mediaList as? Serializable)
            intent.putExtra(INDEX, position)
            intent.putExtra(MAX_COUNT, maxCount)
            intent.putExtra(SHOW_PAGE_INDEX, showPageIndex)
            intent.putExtra(MAX_VIDEO_COUNT, maxVideoCount)
            intent.putExtra(CURRENT_MEDIA_TYPE, currentSelectMediaType)
            intent.putExtra(SHOW_BOTTOM, showBottom)
            intent.putExtra(SELECTED_LIST, selectedList as Serializable)
            launcher.launch(intent)
        }
    }

    private var maxCount: Int = 1
    private var maxVideoCount: Int = 1
    private var showBottom: Boolean = true
    private var selectedList: MutableList<LocalMediaBean>? = null
    private var currentSelectMediaType: MediaType? = null
    private var isMaskGone: Boolean = false
    var currentPosition: Int = 0
        private set
    private lateinit var galleryAdapter: AlbumGalleryPagerAdapter
    private var urls: MutableList<LocalMediaBean>? = null
    private var showPageIndex: Boolean = false
    private var mediaLoader: IBridgeMediaLoader? = null
    private var index = 0
    private var hasMore = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.transparentStatusBar(this)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_gallery)
        status_bar_view.updateLayoutParams<LinearLayout.LayoutParams> {
            height = BarUtils.getStatusBarHeight()
        }
        initExtraParam()
        initView()
        initPager()
        initMediaLoader()
    }

    private fun initExtraParam() {
        val list = intent.getSerializableExtra(SHOW_URL) as? MutableList<LocalMediaBean>
        urls = list ?: SelectLocalImageActivity.cacheList
        currentPosition = intent.getIntExtra(INDEX, 0)
        showPageIndex = intent.getBooleanExtra(SHOW_PAGE_INDEX, true)
        maxCount = intent.getIntExtra(MAX_COUNT, 1)
        showBottom = intent.getBooleanExtra(SHOW_BOTTOM, true)
        maxVideoCount = intent.getIntExtra(MAX_VIDEO_COUNT, 1)
        selectedList = intent.getSerializableExtra(SELECTED_LIST) as? MutableList<LocalMediaBean>
        currentSelectMediaType = intent.getSerializableExtra(CURRENT_MEDIA_TYPE) as MediaType
    }

    private fun initMediaLoader() {
        if (currentSelectMediaType == MediaType.VIDEO) {
            hasMore = false
            return
        }
        index = SelectLocalImageActivity.pageIndex
        val config = SelectLocalImageActivity.pictureConfig
        config?.chooseMode = when (currentSelectMediaType) {
            MediaType.PHOTO -> SelectMimeType.ofImage()
            MediaType.VIDEO -> SelectMimeType.ofVideo()
            else -> SelectMimeType.ofAll()
        }
        mediaLoader = LocalMediaPageLoader(this, config)
        loadData()
    }

    private fun loadData() {
        mediaLoader?.loadPageMediaData(
            SelectLocalImageActivity.currentBucketId,
            index++,
            SelectLocalImageActivity.PAGE_SIZE,
            object : OnQueryDataResultListener<LocalMedia>() {
                override fun onComplete(result: ArrayList<LocalMedia>?, isHasMore: Boolean) {
                    super.onComplete(result, isHasMore)
                    if (!result.isNullOrEmpty()) {
                        hasMore = false
                        return
                    }
                    val list = result?.map { localMedia ->
                        LocalMediaBean().copy(localMedia)
                    }?.toMutableList()
                    if (!list.isNullOrEmpty()) {
                        if (list.size < LIMIT) {
                            hasMore = false
                        }
                        list.removeAll { MimeTypeUtil.isVideo(it.mimeType) }
                        galleryAdapter.addData(list)
                    } else {
                        hasMore = false
                    }
                }
            })
    }

    private fun initView() {
        leftImage.setOnClickListener {
            onBack()
        }
        select.setOnClickListener {
            select()
        }
        next.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                putExtra(IS_FINISH, true)
                putExtra(SELECTED_LIST, (selectedList ?: mutableListOf()) as Serializable)
                if (!selectedList.isNullOrEmpty()) {
                    putExtra(CURRENT_MEDIA_TYPE, currentSelectMediaType)
                }
            })
            finish()
        }
        setSelectedText()
        bottom_container.isVisible = showBottom
    }

    private fun initPager() {
        pageIndex.isVisible = showPageIndex
        val size = urls?.size ?: 0
        setPageIndex(currentPosition + 1, size)
        galleryPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                onPageChanged(position)
            }
        })
        galleryAdapter = AlbumGalleryPagerAdapter(urls)
        galleryPager.adapter = galleryAdapter
        galleryPager.offscreenPageLimit = if (size > 10) {
            10
        } else {
            size
        }
        galleryPager.setCurrentItem(currentPosition, false)
    }

    private fun onPageChanged(position: Int) {
        currentPosition = position
        val itemViewType = galleryAdapter.getItemViewType(position)
        val item = galleryAdapter.getItem(position)
        if (currentSelectMediaType != null) {
            if (itemViewType == 1) {
                selectCount.isVisible = false
                select.isVisible = false
            } else {
                selectCount.isVisible = true
                select.isVisible = true
            }
        }
        select.isChecked = selectedList?.contains(item) == true
        if (!hasMore) {
            return
        }
        if (position == galleryAdapter.data.size - 1) {
            if (!hasMore) {
                return
            }
            //当前划到倒数第二个
            loadData()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(IS_FINISH, false)
            if (currentSelectMediaType != MediaType.VIDEO) {
                putExtra(SELECTED_LIST, (selectedList ?: mutableListOf()) as Serializable)
                if (!selectedList.isNullOrEmpty()) {
                    putExtra(CURRENT_MEDIA_TYPE, currentSelectMediaType)
                }
            }
        })
        super.onBackPressed()
    }

    private fun onBack() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(IS_FINISH, false)
            if (currentSelectMediaType != MediaType.VIDEO) {
                putExtra(SELECTED_LIST, (selectedList ?: mutableListOf()) as Serializable)
                if (!selectedList.isNullOrEmpty()) {
                    putExtra(CURRENT_MEDIA_TYPE, currentSelectMediaType)
                }
            }
        })
        finish()
    }

    private fun select() {
        val entity = galleryAdapter.getItem(currentPosition)
        val isChecked = selectedList?.contains(entity) == true
        if (isChecked) {
            select.isChecked = false
            entity.isChecked = false
            selectedList?.remove(entity)
        } else {
            if ((selectedList?.size ?: 0) >= maxCount) {
                ToastUtils.showLong(
                    StringUtils.getString(R.string.selectCountTip, maxCount)
                )
                return
            }
            currentSelectMediaType = if (entity.isVideo) {
                MediaType.VIDEO
            } else {
                MediaType.PHOTO
            }
            select.isChecked = true
            entity.isChecked = true
            selectedList?.add(entity)
        }
        setSelectedText()
    }

    private fun setPageIndex(index: Int, size: Int) {
        pageIndex.text = String.format("%d/%d", index, size)
    }

    private fun setSelectedText() {
        selectCount.text =
            getString(R.string.selected, selectedList?.size ?: 0)
        next.isEnabled = (selectedList?.size ?: 0) > 0
    }

    private fun showGoneAnim() {
        val statusBarHeight = BarUtils.getStatusBarHeight()
        isMaskGone = if (isMaskGone) {
            headerLayout.animate()
                .translationY(-(headerLayout.height + BarUtils.getStatusBarHeight()).toFloat())
                .translationY(0f).setDuration(200).start()
            bottom_container.animate().translationY(bottom_container.height.toFloat())
                .translationY(0f).setDuration(200).start()
            false
        } else {
            headerLayout.animate()
                .translationY(-(headerLayout.height + statusBarHeight).toFloat())
                .setDuration(200).start()
            bottom_container.animate().translationY(bottom_container.height.toFloat())
                .setDuration(200).start()
            true
        }
    }

    override fun onDestroy() {
        GSYVideoManager.releaseAllVideos()
        super.onDestroy()
    }
}