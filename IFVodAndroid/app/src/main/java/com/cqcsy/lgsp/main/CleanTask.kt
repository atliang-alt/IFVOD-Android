package com.cqcsy.lgsp.main

import android.content.Context
import android.os.AsyncTask
import com.cqcsy.library.GlideApp
import com.cqcsy.library.utils.GlobalValue
import java.io.File

/**
 * 清理缓存任务
 */
class CleanTask : AsyncTask<Context, Int, Boolean>() {

    companion object {
        var isDeleting = false
    }

    override fun doInBackground(vararg params: Context?): Boolean {
        isDeleting = true
        val context = params[0]
        if (params[0] !is Context) {
            return false
        }
        val fileList: MutableList<File> = ArrayList()
        fileList.addAll(getAllFileInDirs(File(GlobalValue.APP_CACHE_PATH)))
        fileList.addAll(getAllFileInDirs(context!!.cacheDir))
        fileList.addAll(getAllFileInDirs(context.externalCacheDir))
        val fileExits = arrayOf(
            GlobalValue.VIDEO_DOWNLOAD_PATH,
            GlobalValue.IMAGE_CACHE_PATH,
            GlobalValue.IMAGE_SCREEN_SHORT,
            GlobalValue.VIDEO_IMAGE_CLIP,
            GlobalValue.UPLOAD_CUT_FILE,
            GlobalValue.DOWNLOAD_IMAGE
        )
        deleteFiles(fileList, fileExits)
        GlideApp.get(context).clearDiskCache()
        return true
    }

    override fun onPostExecute(result: Boolean) {
        isDeleting = false
    }

    private fun getAllFileInDirs(dir: File?): MutableList<File> {
        val files = ArrayList<File>()
        if (dir != null && dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach {
                if (it.isFile) {
                    if (!it.absolutePath.contains(GlobalValue.VIDEO_DOWNLOAD_PATH)
                        && !it.absolutePath.contains(GlobalValue.DOWNLOAD_IMAGE)
                    ) {
                        files.add(it)
                    }
                } else {
                    files.addAll(getAllFileInDirs(it))
                }
            }
        }
        return files
    }

    private fun deleteFiles(files: MutableList<File>, exceptFile: Array<String>) {
        files.forEach {
            if (it.isFile) {
                it.delete()
            } else {
                if (it.absolutePath in exceptFile) {

                } else {
                    val file = it.listFiles()
                    if (file.isNullOrEmpty()) {
                        it.delete()
                    } else {
                        deleteFiles(file.toMutableList(), exceptFile)
                    }
                }
            }
        }
    }
}