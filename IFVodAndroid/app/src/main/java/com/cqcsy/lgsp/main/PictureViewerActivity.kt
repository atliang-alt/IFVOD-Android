package com.cqcsy.lgsp.main

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cqcsy.lgsp.R
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.utils.StatusBarUtil
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.decoder.CompatDecoderFactory
import com.davemorrissey.labs.subscaleview.decoder.SkiaImageDecoder
import kotlinx.android.synthetic.main.activity_picture_viewer.*
import kotlinx.android.synthetic.main.layout_picture_view.view.*
import kotlinx.android.synthetic.main.layout_save_pic_dialog.view.*
import kotlinx.android.synthetic.main.layout_video_loading.view.*
import java.io.File

/**
 * 查看大图
 */
open class PictureViewerActivity : BaseActivity() {

    companion object {
        const val SHOW_URLS = "showUrls"    // 需要展示的所有图片地址集合
        const val SHOW_TITLE = "showTitle"  // 显示title，可空
        const val SHOW_BOTTOM = "showBottom"    // 是否显示底部区域
        const val SHOW_INDEX = "showIndex"      // page显示初始位置
        const val SHOW_COUNTS = "showCounts"      // 图片总数量
        const val AUTO_HIDE = "autoHide"        // 是否自动隐藏顶部底部，默认自动隐藏
    }

    var portraitHeight = 0
    var isScreenChange = false
    var showBottom: Boolean = false
    var autoHide: Boolean = true
    var showCount = 0
    var showUrls: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.transparentStatusBar(this)
        //BarUtils.setNavBarVisibility(this, false)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_picture_viewer)
        portraitHeight = SizeUtils.dp2px(56f) + StatusBarUtil.getStatusBarHeight(this)
        initView()

        headerLayout.layoutParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, portraitHeight)
        headerLayout.postDelayed({
            hideHeader()
            hideBottom()
        }, 500)
    }

    open fun initView() {
        showUrls = if (intent.getSerializableExtra(SHOW_URLS) == null) {
            ArrayList()
        } else {
            intent.getSerializableExtra(SHOW_URLS) as MutableList<String>
        }
        showBottom = intent.getBooleanExtra(SHOW_BOTTOM, false)
        autoHide = intent.getBooleanExtra(AUTO_HIDE, true)
        showCount = intent.getIntExtra(SHOW_COUNTS, 0)
        if (!showBottom) {
            bottomContainer.visibility = View.GONE
        }
        headerLayout.findViewById<TextView>(R.id.headerTitle).text =
            intent.getStringExtra(SHOW_TITLE)

        setPage()
    }

    private fun setPage() {
        pictureLarge.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (showCount > 1) {
                    pageIndex.text =
                        getString(R.string.page, position + 1, showCount)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        val picAdapter = SamplePagerAdapter(this, showUrls)
        picAdapter.onClickListener = View.OnClickListener {
            if (getLocalVisibleRect(this, headerLayout)) {
                hideHeader()
                hideBottom()
            } else {
                showHeader()
                showBottom()
            }
        }
        pictureLarge.adapter = picAdapter
        pictureLarge.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                pictureLarge.viewTreeObserver.removeOnGlobalLayoutListener(this)
                pictureLarge.setCurrentItem(intent.getIntExtra(SHOW_INDEX, 0), false)
            }

        })
        setPagerIndexStatus()
    }

    fun setPagerIndexStatus() {
        if (showCount > 1) {
            pageIndex.visibility = View.VISIBLE
            pageIndex.text =
                getString(
                    R.string.page,
                    pictureLarge.currentItem + 1,
                    showCount
                )
        } else {
            pageIndex.visibility = View.GONE
        }
    }

    private fun showHeader() {
        if (isScreenChange) {
            isScreenChange = false
            setHeaderLayout()
        }
        val animation = ObjectAnimator.ofPropertyValuesHolder(
            headerLayout, PropertyValuesHolder.ofFloat(
                "translationY",
                -headerLayout.measuredHeight.toFloat(),
                0F
            )
        )
        animation.duration = 500
        animation.start()
    }

    private fun hideHeader() {
        if (!autoHide) {
            return
        }
        val animation = ObjectAnimator.ofPropertyValuesHolder(
            headerLayout, PropertyValuesHolder.ofFloat(
                "translationY",
                0F,
                -headerLayout.measuredHeight.toFloat()
            )
        )
        animation.duration = 500
        animation.start()
    }

    private fun showBottom() {
        if (!showBottom) {
            return
        }
        val animation = ObjectAnimator.ofPropertyValuesHolder(
            bottomContainer, PropertyValuesHolder.ofFloat(
                "translationY",
                bottomContainer.measuredHeight.toFloat(),
                0F
            )
        )
        animation.duration = 500
        animation.start()
    }

    private fun hideBottom() {
        if (!autoHide || !showBottom) {
            return
        }
        val animation = ObjectAnimator.ofPropertyValuesHolder(
            bottomContainer, PropertyValuesHolder.ofFloat(
                "translationY",
                0F,
                bottomContainer.measuredHeight.toFloat()
            )
        )
        animation.duration = 500
        animation.start()
    }

    private fun getLocalVisibleRect(context: Context, view: View): Boolean {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return false
        }
        val p = Point()
        (context as Activity).windowManager.defaultDisplay.getSize(p)
        val screenWidth: Int = p.x
        val screenHeight: Int = p.y
        val rect = Rect(0, 0, screenWidth, screenHeight)
        val location = IntArray(2)
        view.getLocationInWindow(location)
        view.tag = location[1] //存储y方向的位置
        return view.getLocalVisibleRect(rect)
    }

    private fun setHeaderLayout() {
        val layoutParams =
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
            } else {
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    portraitHeight
                )
            }
        headerLayout.layoutParams = layoutParams
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideStatusBar()
        } else {
            showStatusBar()
        }
        hideHeader()
        hideBottom()
        isScreenChange = true
    }

    private fun showStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun hideStatusBar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    /**
     * 长按保存图片弹框
     */
    private fun showSavePic(imgPath: String) {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_save_pic_dialog, null)
        contentView.cancel.setOnClickListener {
            dialog.dismiss()
        }
        contentView.savePic.setOnClickListener {
            ImageUtil.saveImage(this, imgPath)
            ToastUtils.showLong(R.string.saveSuccess)
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        if (isSafe())
            dialog.show()
    }

    inner class SamplePagerAdapter(context: Context, data: MutableList<String>) : PagerAdapter() {
        var mShowData: MutableList<String> = data
        var mContext: Context = context
        var onClickListener: View.OnClickListener? = null

        override fun getCount(): Int {
            return mShowData.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val view = View.inflate(mContext, R.layout.layout_picture_view, null)
            container.addView(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            view.loadingText.setText(R.string.loading)
            view.pictureImage.minScale = 1.0f
            view.pictureImage.maxScale = 4.0f
            view.pictureImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_INSIDE)
            view.pictureGif.setOnClickListener(onClickListener)
            var imageUrl = mShowData[position]
            view.pictureGif.setOnLongClickListener {
                val imgFilePath = view.pictureGif.tag
                if (imgFilePath != null && imgFilePath.toString().isNotEmpty()) {
                    showSavePic(imgFilePath.toString())
                }
                true
            }
            if (imageUrl.startsWith("http") || imageUrl.startsWith("https")) {
                view.pictureImage.setBitmapDecoderFactory(
                    CompatDecoderFactory(
                        SkiaImageDecoder::class.java,
                        Bitmap.Config.ARGB_8888
                    )
                )
                if (!imageUrl.contains(".png", true) && !imageUrl.contains(".gif", true)) {
                    imageUrl += if (imageUrl.contains("?")) ImageUtil.FORMAT_PARAM else "?${ImageUtil.FORMAT_PARAM}"
                }
                imageUrl += "&isapp=1"
                ImageUtil.downloadOnly(this@PictureViewerActivity, imageUrl, object :
                    CustomTarget<File>() {
                    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                        if (imageUrl.contains(".gif", true)) {
                            view.pictureGif.visibility = View.VISIBLE
                            view.pictureImage.visibility = View.GONE
                            ImageUtil.loadGif(
                                this@PictureViewerActivity,
                                resource.absolutePath,
                                view.pictureGif,
                                ImageView.ScaleType.FIT_CENTER,
                                isCache = true
                            )
                            view.pictureGif.tag = resource.absolutePath
                        } else {
                            view.pictureGif.visibility = View.GONE
                            view.pictureImage.visibility = View.VISIBLE
                            val option = BitmapFactory.Options()
                            option.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(resource.absolutePath, option)
                            if (option.outHeight > ScreenUtils.getAppScreenHeight()) {
                                view.pictureImage.setDoubleTapZoomScale(ScreenUtils.getScreenWidth() / option.outWidth.toFloat())
                                view.pictureImage.minScale =
                                    ScreenUtils.getAppScreenHeight() / option.outHeight.toFloat()
                                if (ImageUtil.isLongImage(option.outWidth, option.outHeight)) {
                                    //是长图
                                    view.pictureImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_START)
                                }
                            }
                            view.pictureImage.setImage(ImageSource.uri(resource.absolutePath))
                            view.pictureImage.tag = resource.absolutePath
                        }
                        view.progress.visibility = View.GONE
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        view.pictureImage.setImage(ImageSource.resource(R.mipmap.image_default))
                        view.progress.visibility = View.GONE
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
            } else {
                if (imageUrl.contains(".gif", true)) {
                    view.pictureGif.visibility = View.VISIBLE
                    view.pictureImage.visibility = View.GONE
                    ImageUtil.loadGif(
                        this@PictureViewerActivity,
                        imageUrl,
                        view.pictureGif,
                        ImageView.ScaleType.FIT_CENTER,
                        true
                    )
                    view.progress.visibility = View.GONE
                } else {
                    view.pictureGif.visibility = View.GONE
                    view.pictureImage.visibility = View.VISIBLE
                    view.pictureImage.setBitmapDecoderFactory(
                        CompatDecoderFactory(
                            SkiaImageDecoder::class.java,
                            Bitmap.Config.RGB_565
                        )
                    )
                    view.pictureImage.setOnImageEventListener(object :
                        SubsamplingScaleImageView.OnImageEventListener {
                        override fun onReady() {
                        }

                        override fun onImageLoaded() {
                            view.progress.visibility = View.GONE
                        }

                        override fun onPreviewLoadError(e: Exception?) {
                        }

                        override fun onImageLoadError(e: Exception?) {
                            view.pictureImage.setImage(ImageSource.resource(R.mipmap.image_default))
                            view.progress.visibility = View.GONE
                        }

                        override fun onTileLoadError(e: Exception?) {
                        }

                        override fun onPreviewReleased() {
                        }

                    })
                    view.pictureImage.setImage(ImageSource.uri(imageUrl))
                }
            }
            view.pictureImage.setOnClickListener(onClickListener)
            view.pictureImage.setOnLongClickListener {
                val imgFilePath = view.pictureImage.tag
                if (imgFilePath != null && imgFilePath.toString().isNotEmpty()) {
                    showSavePic(imgFilePath.toString())
                }
                true
            }
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}