package com.cqcsy.library.network.callback

import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Progress

abstract class HttpCallBack<T> {
    private var callback: StringCallback? = null

    constructor() {
        callback
    }

    abstract fun onSuccess(response: T?)

    fun onCacheResponse(response: T?) {

    }

    abstract fun onError(response: String?, errorMsg: String?)

    open fun onProgress(progress: Progress) {

    }
}