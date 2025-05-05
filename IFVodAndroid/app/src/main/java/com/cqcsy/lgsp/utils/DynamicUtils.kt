package com.cqcsy.lgsp.utils

import android.content.Context
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.library.utils.ImageUtil

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/29
 *
 *
 */
object DynamicUtils {
    private val fiveDp = SizeUtils.dp2px(5f)

    fun addDynamicImages(
        context: Context,
        container: LinearLayout,
        images: MutableList<ImageBean>?,
        photoCount: Int,
        isUnAvailable: Boolean = false
    ) {
        container.removeAllViews()
        if (images == null || images.size == 0) {
            return
        }
        val showData: MutableList<ImageBean> = ArrayList()
        val width: Int
        if (images.size >= 3 || isUnAvailable) {
            width = getCellWidth(3)
            if (!isUnAvailable) {
                showData.addAll(images.subList(0, 3))
            }
        } else {
            width = getCellWidth(2)
            showData.addAll(images)
        }
        val params = LinearLayout.LayoutParams(width, width)
        if (isUnAvailable) {
            val view = View.inflate(context, R.layout.layout_dynamic_item_image, null)
            val imageView = view.findViewById<ImageView>(R.id.image)
            view.layoutParams = params
            imageView.setImageResource(R.mipmap.icon_un_available)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            container.addView(view)
        } else {
            for ((index, bean) in images.withIndex()) {
                val view = View.inflate(context, R.layout.layout_dynamic_item_image, null)
                val imageView = view.findViewById<ImageView>(R.id.image)
                val gifTag = view.findViewById<ImageView>(R.id.gifTag)
                val longImageTag = view.findViewById<ImageView>(R.id.longImageTag)
                if (index < 2) {
                    params.rightMargin = fiveDp
                }
                view.layoutParams = params
                if (bean.imgPath?.contains(".gif", true) == true) {
                    ImageUtil.loadImage(
                        context,
                        bean.imgPath,
                        imageView,
                        imageHeight = width,
                        imageWidth = width,
                        isFormatGif = false,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    gifTag.visibility = View.VISIBLE
                } else {
                    ImageUtil.loadImage(
                        context, bean.imgPath, imageView,
                        scaleType = ImageView.ScaleType.CENTER_CROP,
                        imageHeight = width,
                        imageWidth = width
                    )
                    gifTag.visibility = View.GONE
                }
                longImageTag.isVisible = bean.isLongImage
                val size = view.findViewById<TextView>(R.id.image_size)
                if (photoCount > 3 && index == 2) {
                    size.visibility = View.VISIBLE
                    size.text = String.format("+%d", photoCount - 3)
                } else {
                    size.visibility = View.GONE
                }
                container.addView(view)
            }
        }
        container.postInvalidate()
    }

    fun getCellWidth(dividend: Int = 2): Int {
        val fiveDp = SizeUtils.dp2px(5f)
        return (ScreenUtils.getAppScreenWidth() - SizeUtils.dp2px(24f) - fiveDp) / dividend
    }

    fun getCoverSize(ratio: Float, itemWidth: Int): Size {
        val width: Int
        val height: Int
        if (ratio > 1) {
            width = itemWidth
            height = (width * 9f / 16f).toInt()
        } else if (ratio == 1f) {
            width = itemWidth
            height = itemWidth
        } else {
            width = itemWidth
            height = (width * 4f / 3f).toInt()
        }
        return Size(width, height)
    }
}