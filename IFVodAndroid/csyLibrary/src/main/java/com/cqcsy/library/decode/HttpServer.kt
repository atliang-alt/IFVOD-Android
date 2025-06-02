package com.cqcsy.library.decode

/**
 * Created by Sunmeng on 12/30/2016.
 * E-Mail:Sunmeng1995@outlook.com
 * Description: 模拟服务端
 */
class HttpServer private constructor() {
    private var mPort = 0
    private var mServiceThread: HttpServiceThread? = null
    private val mThreadGroup: ThreadGroup
    fun start(stream: IHttpStream?, port: Int): Boolean {
        var port = port
        if (0 == port) port = SERVER_PORT
        mPort = port
        try {
            if (null != mServiceThread) {
                if (mServiceThread!!.isBound) {
                    mPort = mServiceThread!!.port
                    mServiceThread!!.setStream(stream)
                    return true
                }
                mServiceThread!!.close()
            }
            mServiceThread = HttpServiceThread(stream, mThreadGroup, port)
            mPort = mServiceThread!!.port
            mServiceThread!!.start()
            return true
        } catch (e: Exception) {
            mServiceThread = null
            e.printStackTrace()
        }
        return false
    }

    fun stop() {
        if (null != mServiceThread) {
            mServiceThread!!.close()
            mServiceThread = null
        }
    }

    val httpAddr: String
        get() = "http://127.0.0.1:$mPort"

    @Throws(Throwable::class)
    protected fun finalize() {
        stop()
    }

    companion object {
        private const val TAG = "HttpServer"
        private const val SERVER_PORT = 0
        private var mHttpServer: HttpServer? = null
        val instance: HttpServer
            get() {
                if (null == mHttpServer) {
                    mHttpServer = HttpServer()
                }
                return mHttpServer!!
            }
    }

    init {
        mThreadGroup = ThreadGroup(HttpServer::class.java.name)
    }
}