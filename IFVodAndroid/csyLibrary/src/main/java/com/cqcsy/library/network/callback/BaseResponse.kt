package com.cqcsy.library.network.callback

import java.io.Serializable

class BaseResponse<T> : Serializable {
    public var ret = 0
    public var data: T? = null
    public var msg: String? = null
}