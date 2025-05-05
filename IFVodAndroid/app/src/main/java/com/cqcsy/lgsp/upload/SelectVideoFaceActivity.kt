package com.cqcsy.lgsp.upload

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.VideoUtil
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.CachePathUtils
import com.cqcsy.library.utils.ImageUtil
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView
import kotlinx.android.synthetic.main.activity_select_video_face.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 选择视频中获取的图片
 */
class SelectVideoFaceActivity : NormalActivity() {
    private var selectPosition = 0
    private val MAX_PIC = 9
    private var task: AsyncTask<String, Int, MutableList<String>>? = null
    private var imageWidthRatio: Float = 1f
    private var imageHeightRatio: Float = 1f
    private lateinit var cropImageView: GestureCropImageView
    private lateinit var overlayView: OverlayView
    private var savePath: String? = null
    private var adapter: BaseQuickAdapter<String, BaseViewHolder>? = null
    private var selectImagePath: String? = null
    private val imageListener: TransformImageView.TransformImageListener = object :
        TransformImageView.TransformImageListener {
        override fun onRotate(currentAngle: Float) {
        }

        override fun onScale(currentScale: Float) {
        }

        override fun onLoadComplete() {
            uCrop.animate().setDuration(300).interpolator =
                AccelerateInterpolator()
        }

        override fun onLoadFailure(e: Exception) {
            finish()
        }
    }

    companion object {
        @JvmStatic
        fun launch(activity: Activity, videoPath: String, requestCode: Int) {
            val intent = Intent(activity, SelectVideoFaceActivity::class.java)
            intent.putExtra("video_path", videoPath)
            activity.startActivityForResult(intent, requestCode)
        }

        @JvmStatic
        fun launch(
            activity: Activity,
            videoPath: String,
            width: Float,
            height: Float,
            requestCode: Int
        ) {
            val intent = Intent(activity, SelectVideoFaceActivity::class.java)
            intent.putExtra("video_path", videoPath)
            intent.putExtra("width", width)
            intent.putExtra("height", height)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun getContainerView(): Int {
        return R.layout.activity_select_video_face
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.selectVideoFace)
        initCropImage()
        initOverlay()
        initView()
        val videoPath = intent.getStringExtra("video_path")
        imageWidthRatio = intent.getFloatExtra("width", 1f)
        imageHeightRatio = intent.getFloatExtra("height", 1f)
        if (!videoPath.isNullOrEmpty()) {
            setRightText(R.string.ensure)
            NormalUtil.getAbsolutePath(videoPath)?.let { getVideoFaceImage(it) }
        }
    }

    private fun initOverlay() {
        overlayView = uCrop.overlayView.apply {
            setPadding(0, 0, 0, 0)
            setShowCropGrid(false)
            setDimmedColor(ColorUtils.getColor(R.color.transparent_50))
            setCropFrameColor(ColorUtils.getColor(R.color.white))
            setShowCropFrame(true)
            setCropFrameStrokeWidth(SizeUtils.dp2px(1f))
        }
    }

    private fun initCropImage() {
        cropImageView = uCrop.cropImageView.apply {
            setPadding(0, 0, 0, 0)
            setTransformImageListener(imageListener)
            isRotateEnabled = false
            isScaleEnabled = true
            setImageToWrapCropBounds()
            targetAspectRatio = imageWidthRatio / imageHeightRatio
        }
    }

    private fun initView() {
        recyclerView.addItemDecoration(
            object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.left = SizeUtils.dp2px(5f)
                }
            }
        )
        (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        adapter = object :
            BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_select_video_face) {
            override fun convert(holder: BaseViewHolder, item: String) {
                val position = getItemPosition(item)
                val imageView = holder.getView<ImageView>(R.id.faceImage)
                if (selectPosition == position) {
                    holder.setGone(R.id.faceSelectBg, false)
                } else {
                    holder.setGone(R.id.faceSelectBg, true)
                }
                ImageUtil.loadImage(
                    this@SelectVideoFaceActivity,
                    item,
                    imageView,
                    0,
                    ImageView.ScaleType.CENTER_CROP
                )
                imageView.setOnClickListener {
                    if (selectPosition != -1) {
                        notifyItemChanged(selectPosition)
                    }
                    selectImagePath = null
                    not_crop_view.isVisible = false
                    uCrop.isVisible = true
                    selectPosition = position
                    notifyItemChanged(selectPosition)
                    setCurrentCover(item)
                }
            }
        }
        recyclerView.adapter = adapter
        selectImage.setOnClickListener {
            val intent = Intent(this, SelectLocalImageActivity::class.java)
            intent.putExtra(SelectLocalImageActivity.maxCountKey, 1)
            intent.putExtra(SelectLocalImageActivity.isBackGifKey, false)
            intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
            startActivityForResult(intent, 1000)
        }
    }

    private fun setCurrentCover(path: String) {
        uCrop.resetCropImageView()
        initCropImage()
        try {
            val saveFilePath = getSaveFilePath()
            savePath = saveFilePath
            val inputUri = Uri.fromFile(File(path))
            val outputUri = Uri.fromFile(File(saveFilePath))
            cropImageView.setImageUri(inputUri, outputUri)
        } catch (e: Exception) {
            finish()
        }
    }

    private fun setNotCropCover(path: String) {
        /*if (resource.height > resource.width * 3) {
            not_crop_view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
            not_crop_view.setImage(ImageSource.cachedBitmap(resource))
        } else {
            not_crop_view.setImage(ImageSource.cachedBitmap(resource))
        }*/
        //not_crop_view.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
        not_crop_view.setImageURI(Uri.fromFile(File(path)))
    }

    private fun getSaveFilePath(): String {
        var path: String = CachePathUtils.getImageCachePath()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        path = "$path${File.separator}$timeStamp.jpg"
        return path
    }

    /**
     * 获取视频九张封面图
     */
    @SuppressLint("StaticFieldLeak")
    private fun getVideoFaceImage(videoPath: String) {
        showProgress()
        val videoBitmapUtil = VideoUtil(videoPath)
        task = object : AsyncTask<String, Int, MutableList<String>>() {
            override fun doInBackground(vararg params: String): MutableList<String> {
                val pics = videoBitmapUtil.extractPics(MAX_PIC)
                videoBitmapUtil.release()
                return pics
            }

            override fun onPostExecute(picList: MutableList<String>?) {
                super.onPostExecute(picList)
                if (!picList.isNullOrEmpty()) {
                    setCurrentCover(picList[0])
                }
                adapter?.setList(picList)
                dismissProgress()
            }
        }
        task?.execute()
    }

    override fun onRightClick(view: View) {
        if (!selectImagePath.isNullOrEmpty()) {
            val intent = Intent()
            intent.putExtra("facePath", selectImagePath)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            cropImageView.cropAndSaveImage(
                Bitmap.CompressFormat.JPEG,
                80,
                object : BitmapCropCallback {
                    override fun onBitmapCropped(
                        resultUri: Uri,
                        offsetX: Int,
                        offsetY: Int,
                        imageWidth: Int,
                        imageHeight: Int
                    ) {
                        val intent = Intent()
                        intent.putExtra("facePath", savePath)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }

                    override fun onCropFailure(t: Throwable) {
                        finish()
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val list = data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) as? MutableList<LocalMediaBean>
            if (!list.isNullOrEmpty()) {
                val position = selectPosition
                selectPosition = -1
                recyclerView.adapter?.notifyItemChanged(position)
                not_crop_view.isVisible = true
                uCrop.isVisible = false
                val path = NormalUtil.getAbsolutePath(list[0].path)
                if (path != null) {
                    selectImagePath = path
                    setNotCropCover(path)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //清除缓存
        val dirPath = CachePathUtils.getVideoFrameImageCacheDir()
        FileUtils.deleteFilesInDir(dirPath)
        task?.cancel(true)
        task = null
    }
}