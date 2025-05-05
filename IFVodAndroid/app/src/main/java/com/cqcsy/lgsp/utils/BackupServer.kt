package com.cqcsy.lgsp.utils

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.StringCallback
import okhttp3.Call
import org.json.JSONObject

/**
 ** 2023/3/8
 ** des：备用服务器更新
 **/

class BackupServer : ViewModel() {
    val backupServerList = arrayListOf("https://datawilldo.github.io/rawApp%s.json")
    private var mIndex = 0
    val mServerHost = MutableLiveData<JSONObject>()
    var mAllServerFinish = MutableLiveData(false)
    var locationResult = MutableLiveData<String?>()

    fun updateServer(country: String?) {
        var target = backupServerList[mIndex]
        target = String.format(target, country ?: "")
        OkHttpUtils.get().url(target).build().execute(object : StringCallback() {

            override fun onError(call: Call?, e: Exception?, id: Int) {
                if (country.isNullOrEmpty()) {
                    getServerFromNext(null)
                } else {
                    getServerFromNext(country)
                }
            }

            override fun onResponse(response: String?, id: Int) {
                if (response.isNullOrEmpty()) {
                    getServerFromNext(null)
                } else {
                    try {
                        val json = JSONObject(response)
                        if (json.length() == 0) {
                            getServerFromNext(null)
                        } else {
                            mServerHost.value = json
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        getServerFromNext(null)
                    }
                }
            }

        })
    }

    private fun getServerFromNext(country: String?) {
        if (country.isNullOrEmpty()) {
            mIndex++
        }
        if (mIndex < backupServerList.size) {
            updateServer(null)
        } else {
            mAllServerFinish.value = true
        }
    }

    fun location() {
        OkHttpUtils.get().url("https://api.country.is/").build()
            .execute(object : StringCallback() {

                override fun onError(call: Call?, e: Exception?, id: Int) {
                    locationResult.value = null
                }

                override fun onResponse(response: String?, id: Int) {
                    if (response != null) {
                        try {
                            val json = JSONObject(response)
                            locationResult.value = json.optString("country")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            locationResult.value = null
                        }
                    } else {
                        locationResult.value = null
                    }
                }

            })
    }
}