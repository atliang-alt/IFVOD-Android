package com.cqcsy.library.decode

import java.io.IOException
import java.io.InterruptedIOException
import java.net.ServerSocket

/**
 * Created by Sunmeng on 12/30/2016.
 * E-Mail:Sunmeng1995@outlook.com
 * Description:服务端建立连接
 */
class HttpServiceThread(
    var mStream: IHttpStream?,
    group: ThreadGroup?,
    var mPort: Int
) : Thread(group, "Listener:$mPort") {
    var mSocket: ServerSocket?
    @Volatile
    var mStop = false

    val isBound: Boolean
        get() = mSocket != null && mSocket!!.isBound

    val port: Int
        get() = if (null != mSocket) mSocket!!.localPort else 0

    fun close() {
        mStop = true
        interrupt()
        try {
            mSocket!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Synchronized
    fun setStream(stream: IHttpStream?) {
        mStream = stream
    }

    override fun run() {
        while (!mStop) {
            try {
                val client = mSocket!!.accept()
                synchronized(mStream!!) {
                    if (null != mStream && mStream!!.isOpen) {
                        val c = HttpConnection(mStream!!, client)
                        c.start()
                    }
                }
            } catch (ignored: InterruptedIOException) {
            } catch (io: IOException) {
                io.printStackTrace()
            }
        }
    }

    init {
        mSocket = ServerSocket(mPort)
        mSocket!!.soTimeout = 0
        if (!mSocket!!.reuseAddress) mSocket!!.reuseAddress = true
    }
}