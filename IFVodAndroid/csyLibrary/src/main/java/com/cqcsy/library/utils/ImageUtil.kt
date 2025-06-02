package com.cqcsy.library.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cqcsy.library.R
import com.cqcsy.library.base.BaseFragment
import com.plattysoft.leonids.ParticleSystem
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * 图片加载
 */
object ImageUtil {
    const val cropParam = "&scale=both&mode=crop"
    const val widthParam = "&width=%1\$d"
    const val heightParam = "&height=%1\$d"
    const val FORMAT_PARAM = "format=jpg"

    /**
     * 加载圆形图片
     */
    fun loadCircleImage(container: Any, url: String?, imageView: ImageView) {
        loadImage(
            container,
            url,
            imageView,
            corner = 0,
            defaultImage = R.mipmap.icon_circle_logo,
            transform = CircleCrop(),
            resize = false
        )
    }

    /**
     * 加载普通图片，无任何处理
     * corner:pt\dp
     */
    fun loadImage(
        container: Any,
        url: String?,
        imageView: ImageView,
        corner: Int = 2,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY,
        defaultImage: Int = R.mipmap.image_default,
        needAuthor: Boolean = false,
        resize: Boolean = true,
        transform: BitmapTransformation? = null,
        requestListener: RequestListener<Drawable>? = null,
        isFormatGif: Boolean = true,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
    ) {
//        if (imageView.tag == url) {
//            return
//        }
        imageView.tag = url
        baseLoadImage(
            container,
            url,
            imageView,
            corner,
            scaleType,
            defaultImage,
            transform,
            needAuthor,
            resize,
            requestListener,
            isFormatGif, imageWidth, imageHeight
        )
    }

    private fun getManager(container: Any): RequestManager? {
        return when (container) {
            is Context -> {
                if (container.isRestricted) {
                    return null
                }
                Glide.with(container)
            }

            is Fragment -> {
                if (container is BaseFragment && !container.isSafe()) {
                    return null
                }
                Glide.with(container)
            }

            is Activity -> {
                if (container.isDestroyed || container.isFinishing) {
                    return null
                }
                Glide.with(container)
            }

            is View -> {
                Glide.with(container)
            }

            else -> return null
        }
    }

    /**
     * t图片加载基础类
     */
    private fun baseLoadImage(
        container: Any,
        url: String?,
        imageView: ImageView,
        corner: Int = 2,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY,
        defaultImage: Int = R.mipmap.image_default,
        transform: BitmapTransformation? = null,
        needAuthor: Boolean = false,
        resize: Boolean = true,
        requestListener: RequestListener<Drawable>? = null,
        isFormatGif: Boolean = true,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
    ) {
        val requestManager = getManager(container) ?: return
        requestManager.clear(imageView)
        imageView.setImageDrawable(null)
        imageView.scaleType = ImageView.ScaleType.CENTER
        var options = RequestOptions().skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate()
        if (defaultImage > 0) {
            options = options.error(defaultImage)
                .fallback(defaultImage)
                .placeholder(defaultImage)
        }
        if (transform != null) {
            options = options.transform(transform)
            if (transform is CircleCrop) {
                imageView.scaleType = scaleType
            }
        } else if (corner > 0) {
            options = options.transform(CornerBitmapTransform(SizeUtils.dp2px(corner.toFloat())))
        }
        var width = imageWidth ?: imageView.measuredWidth
        var height = imageHeight ?: imageView.measuredHeight

        if ((width <= 0 || height <= 0) && imageView.layoutParams != null) {
            width = imageView.layoutParams.width
            height = imageView.layoutParams.height
        }
        if (resize && (width <= 0 || height <= 0)) {
            imageView.post {
                width = imageView.measuredWidth
                height = imageView.measuredHeight
                download(
                    requestManager,
                    options,
                    url,
                    imageView,
                    scaleType,
                    width,
                    height,
                    needAuthor,
                    resize,
                    requestListener,
                    isFormatGif
                )
            }
        } else {
            download(
                requestManager,
                options,
                url,
                imageView,
                scaleType,
                width,
                height,
                needAuthor,
                resize,
                requestListener,
                isFormatGif
            )
        }
    }

    private fun download(
        requestManager: RequestManager,
        options: RequestOptions,
        url: String?,
        imageView: ImageView,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY,
        width: Int,
        height: Int,
        needAuthor: Boolean = false,
        resize: Boolean = true,
        requestListener: RequestListener<Drawable>? = null,
        isFormatGif: Boolean = true
    ) {
        val tempUrl =
            if (!url.isNullOrEmpty() && (url.startsWith("http") || url.startsWith("https"))) {
                var temp = if (url.contains("?")) {
                    "$url&"
                } else {
                    "$url?"
                }
                if (url.contains(".gif", true)) {
                    if (isFormatGif) {
                        temp += FORMAT_PARAM
                    }
                } else if (!url.contains(".png", true)) {
                    temp += FORMAT_PARAM
                }
                if (needAuthor) {
                    temp += "&anchor=topcenter"
                }
                if (resize) {
                    temp += cropParam
                    if (width > 0) {
                        temp += String.format(widthParam, width)
                    }
                    if (height > 0) {
                        temp += String.format(heightParam, height)
                    }
                }
                "$temp&isapp=1"
            } else {
                url
            }
//        println("load image url ====$tempUrl")
        requestManager.load(tempUrl).apply(options)
            .addListener(requestListener ?: object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    for (value in options.transformations.values) {
                        if (value is CircleCrop) {
                            imageView.scaleType = scaleType
                            return false
                        }
                    }
                    if (imageView.tag == url) {
                        imageView.scaleType = ImageView.ScaleType.CENTER
                        return false
                    }
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (imageView.tag == url) {
                        imageView.scaleType = scaleType
                        imageView.setImageDrawable(resource)
                    } else {
                        imageView.scaleType = ImageView.ScaleType.CENTER
                    }
//                    println("load image url ====$tempUrl====success===${imageView.tag}")
                    return true
                }

            }).into(imageView)
    }

    /**
     * 加载GIF表情图
     */
    fun loadGif(
        container: Any,
        url: String?,
        imageView: ImageView,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_CENTER,
        isCache: Boolean = false,
        requestListener: RequestListener<GifDrawable>? = null,
        defaultImage: Int = R.mipmap.image_default
    ) {
        val requestManager = getManager(container) ?: return
        imageView.tag = url
        imageView.scaleType = ImageView.ScaleType.CENTER
        var builder = requestManager.asGif().load(url)
        if (defaultImage > 0) {
            builder = builder.error(defaultImage)
                .fallback(defaultImage)
                .placeholder(defaultImage)
        }
        builder.skipMemoryCache(isCache)
            .diskCacheStrategy(if (isCache) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)
            .addListener(requestListener ?: object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (imageView.tag == url) {
                        imageView.scaleType = ImageView.ScaleType.CENTER
                        return false
                    }
                    return true
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (imageView.tag == url) {
                        imageView.scaleType = scaleType
                        return false
                    } else {
                        imageView.scaleType = ImageView.ScaleType.CENTER
                    }
                    return true
                }
            })
            .into(imageView)
    }

    /**
     * 加载本地id资源图片
     */
    fun loadLocalId(
        container: Any,
        urlId: Int,
        imageView: ImageView,
        scaleType: ImageView.ScaleType = ImageView.ScaleType.FIT_XY
    ) {
        val requestManager = getManager(container) ?: return
        imageView.scaleType = scaleType
        requestManager.load(urlId)
            .error(R.mipmap.image_default)
            .fallback(R.mipmap.image_default)
            .placeholder(R.mipmap.image_default)
            .diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView)
    }

    /**
     * 下载网络图片
     */
    fun downloadOnly(container: Any, url: String, target: Target<File>) {
        val requestManager = getManager(container) ?: return
        requestManager.download(url).into(target)
    }

    /**
     * 图片保存到本地
     */
    fun saveImage(context: Context, imagePath: String) {
//        val file = File(imagePath)
//        val targetFile = File(GlobalValue.DOWNLOAD_IMAGE + System.currentTimeMillis() + getImageType(imagePath))
//        FileUtils.copy(file, targetFile)
        ImageUtils.save2Album(BitmapFactory.decodeFile(imagePath), Bitmap.CompressFormat.JPEG)
//        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://${targetFile.absolutePath}")))
    }

    fun formatUrl(
        url: String?,
        width: Int? = null,
        height: Int? = null,
        isFormatGif: Boolean = false,
        needAuthor: Boolean = false,
        resize: Boolean = true
    ): String? {
        return if (!url.isNullOrEmpty() && (url.startsWith("http") || url.startsWith("https"))) {
            var temp = if (url.contains("?")) {
                "$url&"
            } else {
                "$url?"
            }
            if (url.contains(".gif", true)) {
                if (isFormatGif) {
                    temp += FORMAT_PARAM
                }
            } else if (!url.contains(".png", true)) {
                temp += FORMAT_PARAM
            }
            if (needAuthor) {
                temp += "&anchor=topcenter"
            }
            if (resize) {
                if (width != null && height != null) {
                    temp += String.format(
                        cropParam,
                        (width / GlobalValue.scaleImageSize).toInt(),
                        (height / GlobalValue.scaleImageSize).toInt()
                    )
                }
            }
            "$temp&isapp=1"
        } else {
            url
        }
    }

    fun getImageType(filePath: String): String {
        return when (ImageUtils.getImageType(filePath)) {
            ImageUtils.ImageType.TYPE_PNG -> ".png"
            ImageUtils.ImageType.TYPE_WEBP -> ".webp"
            ImageUtils.ImageType.TYPE_BMP -> ".bmp"
            ImageUtils.ImageType.TYPE_GIF -> ".gif"
            else -> ".jpeg"
        }
    }

    fun showCircleAnim(imageView: ImageView?) {
        if (imageView == null) {
            return
        }
        imageView.animate().rotation(3600000f).duration = 100000
    }

    fun closeCircleAnim(imageView: ImageView?) {
        if (imageView == null) {
            return
        }
        imageView.animate().rotation(0f).duration = 0
    }

    /**
     * 点击收藏、点赞动效
     */
    fun clickAnim(parentView: Any?, view: View) {
        if (parentView == null) {
            return
        }
        val particleSystem: ParticleSystem? = if (parentView is ViewGroup) {
            ParticleSystem(parentView, 50, ResourceUtils.getDrawable(R.drawable.shape_click_anim), 300)
        } else if (parentView is Activity) {
            ParticleSystem(parentView, 50, R.drawable.shape_click_anim, 300)
        } else {
            null
        }
        particleSystem?.setSpeedModuleAndAngleRange(0.1f, 0.25f, 225, 315)
            ?.setScaleRange(0.3f, 1f)
            ?.oneShot(view, 50)
    }

    /**
     *  Base64字符串转图片
     */
    fun base64ToImage(base64String: String): Bitmap? {
        if (base64String.isEmpty()) {
            return null
        }
        val imageBytes: ByteArray = Base64.decode(base64String, 0)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    /**
     * 图片转base64
     */
    fun imageToBase64(imagePath: String): String {
        val bos = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeFile(imagePath)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        return Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * 图片格式处理
     */
    fun formatJpePath(filePath: String): String {
        return formatImage(filePath, Bitmap.CompressFormat.JPEG)
    }

    /**
     * 图片格式处理
     */
    fun formatImage(filePath: String, format: Bitmap.CompressFormat): String {
        val bitmap = ImageUtils.getBitmap(filePath)
        val byteImage = ImageUtils.bitmap2Bytes(bitmap, format, 100)
        val imagePath = getFilePath()
        ImageUtils.save(
            ImageUtils.bytes2Bitmap(byteImage),
            imagePath,
            format,
            true
        )
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
        return imagePath
    }

    /**
     * 图片压缩
     */
    fun compressImage(path: String): String {
        var imagePath = path
        if (ImageUtils.getImageType(path) == ImageUtils.ImageType.TYPE_WEBP) {
            imagePath = formatJpePath(path)
        }
        val imageSize = ImageUtils.getSize(imagePath)
        if (imageSize[0] > 1080) {
            val bit = imageSize[0].toFloat() / imageSize[1].toFloat()
            val bitmap = ImageUtils.getBitmap(imagePath)
            imagePath = getFilePath()
            val targetBitmap =
                ImageUtils.compressByScale(bitmap, 1080, (1080.0 / bit).toInt(), true)
            ImageUtils.save(
                ImageUtils.bytes2Bitmap(ImageUtils.compressByQuality(targetBitmap, 90)),
                imagePath,
                Bitmap.CompressFormat.JPEG,
                true
            )
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            if (targetBitmap != null && !targetBitmap.isRecycled) {
                targetBitmap.recycle()
            }
        }
        return imagePath
    }

    fun isLongImage(imageWidth: Int, imageHeight: Int): Boolean {
        val appScreenWidth = ScreenUtils.getScreenWidth()
        val appScreenHeight = ScreenUtils.getScreenHeight()
        if (imageHeight > appScreenHeight) {
            return true
        }
        val displayHeight = imageHeight.toFloat() * appScreenWidth / imageWidth.toFloat()
        val diff = displayHeight - appScreenHeight
        val min = ScreenUtils.getAppScreenHeight() * 0.1
        return diff > min
    }

    /**
     * 保存压缩后图片路径
     */
    private fun getFilePath(): String {
        //var path: String = GlobalValue.VIDEO_IMAGE_CLIP
        var path: String = CachePathUtils.getImageCachePath()
        val timeStamp = System.currentTimeMillis()
        path = "$path${File.separator}$timeStamp.jpg"
        return path
    }
}