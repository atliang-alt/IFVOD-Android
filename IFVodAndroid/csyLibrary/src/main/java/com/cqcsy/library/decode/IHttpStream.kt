package com.cqcsy.library.decode

import java.io.IOException
import java.io.OutputStream

/**
 * Created by Sunmeng on 12/30/2016.
 * E-Mail:Sunmeng1995@outlook.com
 * Description:
 */
interface IHttpStream {
    @Throws(IOException::class)
    fun writeStream(
        out: OutputStream?,
        path: String?,
        rangeStart: Int,
        rangeEnd: Int
    ): Boolean

    val isOpen: Boolean
    fun acceptRange(): Boolean
}