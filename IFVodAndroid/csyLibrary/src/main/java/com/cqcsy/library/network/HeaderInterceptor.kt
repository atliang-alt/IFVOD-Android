package com.cqcsy.library.network

import com.cqcsy.library.utils.AppLanguageUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.util.*

/**
 * 创建时间：2022/12/14
 *
 */
class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        val lang = when (AppLanguageUtils.getAppliedLanguage()) {
            Locale.CHINESE -> {
                "0"
            }
            Locale.TRADITIONAL_CHINESE -> {
                "1"
            }
            else -> {
                when (AppLanguageUtils.getSystemLanguage().country) {
                    "CN" -> {
                        "0"
                    }
                    "TW", "HK" -> {
                        "1"
                    }
                    else -> {
                        "0"
                    }
                }
            }
        }
        builder.addHeader(
            "Lang",
            lang
        )
        return chain.proceed(builder.build())
    }
}