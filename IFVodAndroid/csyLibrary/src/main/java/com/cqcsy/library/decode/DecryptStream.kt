package com.cqcsy.library.decode

import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

class DecryptStream : IHttpStream {
    override val isOpen: Boolean
        get() = true

    @Throws(IOException::class)
    override fun writeStream(
        out: OutputStream?,
        path: String?,
        rangS: Int,
        rangE: Int
    ): Boolean { //this.outStream = out;
        var rangE = rangE
        var readLen: Int
        var leftLen: Int
        val uri = Uri.parse(path)
        val pathString = uri.getQueryParameter("path")
        if (pathString == null || pathString == "") {
            if (path != null) {
                HttpConnection.send404Response(out, path)
            }
        } else {
            val type = HttpConnection.getContentType(pathString)
            val decryption = VideoDecryption(FileInputStream(pathString), File(pathString).length())
            decryption.setDecodeKey(File(pathString).name)
            if (rangS > 512 * 1024) {
                val buffer = ByteArray(512 * 1024)
                decryption.inputStream?.skip(rangS.toLong())
                rangE =
                    if (rangE > decryption.totalLength) decryption.totalLength.toInt() else rangE
                HttpConnection.sendOkResponse(
                    out,
                    (rangE - rangS).toLong(),
                    type,
                    rangS,
                    rangE,
                    decryption.totalLength.toInt()
                )
                leftLen = rangE - rangS
                while (leftLen > 0) {
                    readLen = decryption.inputStream!!.read(buffer)
                    out?.write(buffer, 0, readLen)
                    leftLen -= readLen
                }
            } else {
                HttpConnection.sendOkResponse(out, decryption.totalLength, type)
                decryption.outStream = out
                decryption.start()
            }
            out?.flush()
            out?.close()
        }
        return false
    }

    override fun acceptRange(): Boolean {
        return true
    }
}