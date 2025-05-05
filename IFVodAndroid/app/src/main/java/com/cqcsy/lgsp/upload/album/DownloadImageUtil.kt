package com.cqcsy.lgsp.upload.album

import android.content.Context
import android.graphics.drawable.Drawable
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.library.utils.ImageUtil
import java.io.File

/**
 * 下载图片
 */
object DownloadImageUtil {
    fun downloadImage(context: Context, list: MutableList<ImageBean>) {
        startDownload(context, list)
    }

    private fun startDownload(context: Context, list: MutableList<ImageBean>) {
        ToastUtils.showLong(R.string.downloading)
        for (i in list.indices) {
            val it = list[i]
            if (it.imgPath.isNullOrEmpty()) {
                continue
            }
            ImageUtil.downloadOnly(context, it.imgPath!!, object : CustomTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    ImageUtil.saveImage(context, resource.path)
                    if (i == (list.size - 1)) {
                        ToastUtils.showLong(R.string.download_finish)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })
        }
    }

}