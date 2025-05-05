package com.cqcsy.lgsp.upload.clip

import android.content.Intent
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.utils.CachePathUtils
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.view.GestureCropImageView
import com.yalantis.ucrop.view.OverlayView
import com.yalantis.ucrop.view.TransformImageView.TransformImageListener
import kotlinx.android.synthetic.main.activity_image_crop.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 图片剪切
 */
class PhotoCropActivity : BaseActivity() {
    private var mCompressFormat = CompressFormat.JPEG
    private var mCompressQuality = 50
    private var imagePath = ""
    private var savePath: String? = ""

    // 宽度比例值
    private var widthProportion: Float = 1f

    // 高度比例值
    private var heightProportion: Float = 1f
    private var mGestureCropImageView: GestureCropImageView? = null
    private var mOverlayView: OverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        imagePath = NormalUtil.getAbsolutePath(intent.getStringExtra("imagePath")) ?: ""
        widthProportion = intent.getFloatExtra(SelectLocalImageActivity.widthKey, 1f)
        heightProportion = intent.getFloatExtra(SelectLocalImageActivity.heightKey, 1f)
        initiateViews()
        setImageData()
    }

    override fun onStop() {
        super.onStop()
        mGestureCropImageView?.cancelAllAnimations()
    }

    private fun initiateViews() {
        mGestureCropImageView = uCrop.cropImageView
        mOverlayView = uCrop.overlayView
        mOverlayView?.setPadding(0, 0, 0, 0)
        mGestureCropImageView?.setPadding(0, 0, 0, 0)
        mGestureCropImageView?.setTransformImageListener(mImageListener)
    }

    /**
     * 设置图片
     */
    private fun setImageData() {
        savePath = getFilePath()
        val bitmap = ImageUtils.getBitmap(imagePath)
        if (bitmap.width > 4096 || bitmap.height > 4096) {
            imagePath = getFilePath()
            ImageUtils.save(ImageUtils.scale(bitmap, 720, 1080), imagePath, CompressFormat.JPEG)
        }
        val inputUri = Uri.fromFile(File(imagePath))
        val outputUri = Uri.fromFile(File(savePath))
        processOptions()
        if (inputUri != null && outputUri != null) {
            try {
                mGestureCropImageView?.setImageUri(inputUri, outputUri)
            } catch (e: Exception) {
                finish()
            }
        } else {
            finish()
        }
    }

    /**
     * 保存剪切后图片路径
     */
    private fun getFilePath(): String {
        var path: String = CachePathUtils.getImageCachePath()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Date())
        path = "$path${File.separator}$timeStamp.jpg"
        return path
    }

    private fun processOptions() {
        // 设置不可旋转
        mGestureCropImageView?.isRotateEnabled = false
        // Overlay view options
        // 设置剪切框外蒙层
        mOverlayView?.setDimmedColor(ColorUtils.getColor(R.color.transparent_50))
        // 设置剪切框线条颜色
        mOverlayView?.setCropFrameColor(ColorUtils.getColor(R.color.transparent_50))
        mOverlayView?.setCropFrameStrokeWidth(SizeUtils.dp2px(1f))
        mOverlayView?.setShowCropGrid(false)
        mGestureCropImageView?.targetAspectRatio =
            widthProportion / heightProportion
        mGestureCropImageView?.setImageToWrapCropBounds()
    }

    private val mImageListener: TransformImageListener = object : TransformImageListener {
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

    private fun cropAndSaveImage() {
        mGestureCropImageView?.cropAndSaveImage(
            mCompressFormat,
            mCompressQuality,
            object : BitmapCropCallback {
                override fun onBitmapCropped(
                    resultUri: Uri,
                    offsetX: Int,
                    offsetY: Int,
                    imageWidth: Int,
                    imageHeight: Int
                ) {
                    setResultUri(resultUri)
                }

                override fun onCropFailure(t: Throwable) {
                    finish()
                }
            })
    }

    private fun setResultUri(uri: Uri?) {
        setResult(RESULT_OK, Intent().putExtra("clipPath", uri?.path))
        finish()
    }

    fun cancelClick(view: View) {
        onBackPressed()
    }

    fun sureClick(view: View) {
        cropAndSaveImage()
    }
}