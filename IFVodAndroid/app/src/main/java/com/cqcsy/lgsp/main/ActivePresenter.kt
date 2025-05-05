package com.cqcsy.lgsp.main

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.lzy.okgo.model.HttpParams
import org.json.JSONObject
import java.util.*

/**
 ** 2022/9/14
 ** des：日活统计
 **/

class ActivePresenter {

    val FIRST_LAUNCHER_TIME = "firstLauncherTime"

    val CURRENT_LOCATION = "currentLocation"

    val LAST_ACTIVE_TIME = "lastActiveTime"

    val ACTIVE_URL = "https://ppt.lgsp.tv/a/o"

    init {
        val time = SPUtils.getInstance().getLong(FIRST_LAUNCHER_TIME, 0)
        if (time == 0L) {
            cacheLauncherTime()
        }
        checkUserActive()
    }

    private fun cacheLauncherTime() {
        val calendar = Calendar.getInstance()
        val zoneOffset = calendar.get(Calendar.ZONE_OFFSET)
        val dstOffset = calendar.get(Calendar.DST_OFFSET)
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset))
        val utcTime = calendar.timeInMillis / 1000
        SPUtils.getInstance().put(FIRST_LAUNCHER_TIME, utcTime)
    }

    /**
     * 用户日活检查
     */
    private fun checkUserActive() {
        val last = SPUtils.getInstance().getString(LAST_ACTIVE_TIME)
        val now = TimeUtils.date2String(Date(), "yyyy-MM-dd")
        if (last == now) return
        getLocation()
    }

    private fun getLocation() {
        HttpRequest.get(RequestUrls.GET_USER_REGION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val code = response?.get("code")
                if (code is String && code.isNotEmpty()) {
                    SPUtils.getInstance().put(CURRENT_LOCATION, code)
                    postActive(code)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        })
    }

    private fun postActive(location: String?) {
        val region = if (location.isNullOrEmpty()) {
            "GL"
        } else {
            location
        }
        val params = HttpParams()
        params.put("region", region)
        params.put("start", SPUtils.getInstance().getLong(FIRST_LAUNCHER_TIME))
        params.put("uuid", DeviceUtils.getUniqueDeviceId())
        params.put("isApp", 1)
        params.put("package", AppUtils.getAppPackageName())
        params.put("appVersion", AppUtils.getAppVersionName())
        params.put("system", "Android")
        params.put("systemVersion", DeviceUtils.getSDKVersionName())
        params.put("deviceInfo", DeviceUtils.getManufacturer() + " " + DeviceUtils.getModel())
        HttpRequest.post(ACTIVE_URL, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                SPUtils.getInstance().put(LAST_ACTIVE_TIME, TimeUtils.date2String(Date(), "yyyy-MM-dd"))
            }

            override fun onError(response: String?, errorMsg: String?) {
                SPUtils.getInstance().put(LAST_ACTIVE_TIME, TimeUtils.date2String(Date(), "yyyy-MM-dd"))
            }
        }, params)
    }
}