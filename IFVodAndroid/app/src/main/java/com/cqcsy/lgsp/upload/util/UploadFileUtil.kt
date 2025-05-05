package com.cqcsy.lgsp.upload.util

import java.io.*

/**
 * 上传文件切片处理
 */
object UploadFileUtil {
    private fun getBlock(offset: Long, file: File, blockSize: Int): ByteArray? {
        val result = ByteArray(blockSize)
        var accessFile: RandomAccessFile? = null
        try {
            accessFile = RandomAccessFile(file, "r")
            accessFile.seek(offset)
            return when (val readSize = accessFile.read(result)) {
                -1 -> {
                    null
                }
                blockSize -> {
                    result
                }
                else -> {
                    val tmpByte = ByteArray(readSize)
                    System.arraycopy(result, 0, tmpByte, 0, readSize)
                    tmpByte
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close()
                } catch (e1: IOException) {
                }
            }
        }
        return null
    }

    fun getCutFile(offset: Long, filePath: String, cutFileName: String, blockSize: Int): File? {
        val byteArray = getBlock(offset, File(filePath), blockSize)
        var bos: BufferedOutputStream? = null
        var fos: FileOutputStream? = null
        var file: File? = null
        try {
            if (byteArray == null) {
                return file
            }
            val dir = File(cutFileName).parentFile
            if (dir != null && !dir.exists() && dir.isDirectory) {//判断文件目录是否存在
                dir.mkdirs()
            }
            file = File(cutFileName)
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(byteArray)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                bos?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            try {
                fos?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
        }
        return file
    }
}