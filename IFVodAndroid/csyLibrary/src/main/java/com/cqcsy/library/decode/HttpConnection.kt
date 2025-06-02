package com.cqcsy.library.decode

import java.io.*
import java.net.Socket
import java.util.*

/**
 * Created by Sunmeng on 12/30/2016.
 * E-Mail:Sunmeng1995@outlook.com
 * Description:通过Socket建立连接
 */
class HttpConnection internal constructor(var mStream: IHttpStream, var mSocket: Socket) :
    Thread() {
    var mInputStream: InputStream
    var mOutputStream: OutputStream
    override fun run() {
        try {
            process()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun process() {
        val `in` =
            BufferedReader(InputStreamReader(mInputStream))
        var line: String?
        var temp: String
        var range: String
        var fileName: String? = null
        var rangS = -1
        var rangE = -1
        if (mIsDebug) {
            println("[Stream Connection] start process Http request:")
        }
        while (true) {
            line = `in`.readLine()
            if (null == line || line == "\r\n" || line == "") break
            var s = StringTokenizer(line)
            temp = s.nextToken()
            if (temp == "GET") {
                fileName = s.nextToken()
                if (null == fileName) continue
                if (fileName[0] != '/') fileName = "/$fileName"
            } else if (temp == "Range:" && mStream.acceptRange()) {
                try {
                    range = s.nextToken()
                    if (null == range) continue
                    range = range.replace("bytes=", "")
                    s = StringTokenizer(range, "-")
                    //---start
                    if (!s.hasMoreTokens()) continue
                    temp = s.nextToken()
                    rangS = temp.toInt()
                    //---end
                    if (!s.hasMoreTokens()) {
                        rangE = Int.MAX_VALUE
                    } else {
                        temp = s.nextToken()
                        rangE = temp.toInt()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        try {
            mStream.writeStream(mOutputStream, fileName, rangS, rangE)
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }
        mSocket.close()
    }

    companion object {
        const val mIsDebug = true
        @Throws(IOException::class)
        fun sendOkResponse(
            out: OutputStream?,
            length: Long,
            type: String
        ) {
            val header =
                "HTTP/1.1 200 OK\r\nServer: location server\r\nContent-Type: $type\r\nContent-Length: $length\r\nConnection: close\r\n\r\n"
            out?.write(header.toByteArray())
        }

        @Throws(IOException::class)
        fun sendOkResponse(
            out: OutputStream?,
            length: Long,
            type: String,
            rangS: Int,
            rangE: Int,
            rangSize: Int
        ) {
            val header =
                "HTTP/1.1 206 Partial Content\r\nServer: location server\r\nContent-Type: $type\r\nContent-Length: $length\r\nAccept-Ranges: bytes\r\nContent-Range: bytes $rangS-$rangE/$rangSize\r\nConnection: close\r\n\r\n"
            out?.write(header.toByteArray())
        }

        @Throws(IOException::class)
        fun send404Response(out: OutputStream?, path: String) {
            val header =
                "HTTP/1.1 404 File not found\r\nConnection: close\r\nContent-Type: text/html; charset=iso-8859-1\r\n\r\n" +
                        "<html><body><table width='100%' height='100%' border=0><tr>" +
                        "<td align=center valign=middle><b>404 File not found<br>The web page: " + path +
                        " is invalid!<br><br>Please select valid web page by \"Content\" menu.</b></td></tr></table></body></html>\r\n"
            out?.write(header.toByteArray())
        }

        fun getContentType(fileName: String): String {
            val i = fileName.lastIndexOf(".")
            var t = fileName
            if (-1 != i) t = fileName.substring(i + 1)
            val j = t.lastIndexOf("#")
            if (-1 != j) t = t.substring(0, j)
            t = t.toLowerCase()
            if (t == "htm" || t == "html" || t == "txt" || t == "text" || t == "mht" || t == "mhtml" || t == "xht" || t == "xhtml") {
                return "text/html"
            } else if (t == "jpg" || t == "jpeg" || t == "jpe") {
                return "image/jpeg"
            } else if (t == "png") {
                return "image/png"
            } else if (t == "bmp") {
                return "image/bitmap"
            } else if (t == "css") {
                return "text/css"
            } else if (t == "gif") {
                return "image/gif"
            } else if (t == "js") {
                return "application/x-javascript"
            } else if (t == "mp3") {
                return "audio/mp3"
            } else if (t == "mp4") {
                return "video/mpeg4"
            } else if (t == "mp4a") {
                return "video/mp4"
            }
            return "application/octet-stream"
        }
    }

    init {
        mInputStream = mSocket.getInputStream()
        mOutputStream = mSocket.getOutputStream()
    }
}