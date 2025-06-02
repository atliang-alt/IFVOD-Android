package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.uploadPicture.UploadTaskFinishEvent
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XLinearBuilder
import kotlinx.android.synthetic.main.activity_upload_photo_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 上传图片进度显示页
 */
class UploadPhotoListActivity : NormalActivity() {
    private var currentProgressBar: ProgressBar? = null
    private var mediaKey: String? = null
    private var isPause: Boolean = false

    override fun getContainerView(): Int {
        return R.layout.activity_upload_photo_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.uploadPhotoList)
        mediaKey = intent.getStringExtra(AlbumDetailsActivity.ALBUM_ID)
        initView()
        setData()
    }

    override fun onResume() {
        super.onResume()
        isPause = false
    }

    override fun onPause() {
        super.onPause()
        isPause = true
    }

    private fun setData() {
        val uploadList = PictureUploadManager.getTaskListByTag(mediaKey)
        if (uploadList.size == 0) {
            setEmpty()
        } else {
            setRecord(uploadList)
        }
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            XLinearBuilder(this).setSpacing(10f).build()
        )
    }

    private fun setRecord(data: MutableList<PictureUploadTask>) {
        val adapter: BaseQuickAdapter<PictureUploadTask, BaseViewHolder> =
            object : BaseQuickAdapter<PictureUploadTask, BaseViewHolder>(R.layout.layout_upload_album_item, data) {
                override fun convert(holder: BaseViewHolder, item: PictureUploadTask) {
                    ImageUtil.loadImage(
                        this@UploadPhotoListActivity,
                        item.localPath,
                        holder.getView(R.id.image_film),
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    if (item.status == PictureUploadStatus.LOADING) {
                        currentProgressBar = holder.getView(R.id.uploadProgress)
                    }
                    holder.getView<ProgressBar>(R.id.uploadProgress).progress =
                        (item.progress * 100).toInt()
                }
            }
        recyclerView.adapter = adapter
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(task: PictureUploadTask) {
        if (!isPause && task.taskTag == mediaKey) {
            when (task.status) {
                PictureUploadStatus.LOADING -> {
                    currentProgressBar?.progress = (task.progress * 100).toInt()
                }

                PictureUploadStatus.ERROR -> {
                    setData()
                }

                else -> {}
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveImageEvent(event: UploadTaskFinishEvent) {
        if (event.task?.taskTag == mediaKey) {
            setData()
        }
    }

    private fun setEmpty() {
        currentProgressBar = null
        uploadingLayout.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        lookPhoto.setOnClickListener {
            finish()
        }
    }
}