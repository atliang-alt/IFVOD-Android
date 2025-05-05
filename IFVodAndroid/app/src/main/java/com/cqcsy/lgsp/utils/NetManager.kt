package com.cqcsy.lgsp.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils


object NetManager {

    /**
     * 判断Wi-Fi是否连接
     */
    fun isWifiConnect(context: Context): Boolean {
        val connectManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return connectManager.isWifiEnabled && connectManager.connectionInfo != null && !connectManager.connectionInfo.ssid.isNullOrEmpty()
    }

    /**
     * 获取已连接Wi-Fi名称
     */
    fun getWifiName(context: Context): String {
        var ssid = "unknown"
        if(!isWifiConnect(context)) {
            return ssid
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
            val connManager =
                context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.activeNetworkInfo
            if (networkInfo?.isConnected == true) {
                if (networkInfo.extraInfo != null) {
                    return networkInfo.extraInfo.replace("\"", "")
                }
            }
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O
            || Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1
        ) {
            val wifiManager = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            val info = wifiManager.connectionInfo
            ssid = info.ssid.replace("\"", "")
            if (!TextUtils.isEmpty(ssid)) {
                return ssid
            }
            //部分手机拿不到WiFi名称
            val networkId = info.networkId
            val configuredNetworks = wifiManager.configuredNetworks
            for (config in configuredNetworks) {
                if (config.networkId == networkId) {
                    ssid = config.SSID
                    break
                }
            }
            //扫描到的网络
//            val scanResults: List<ScanResult> = wifiManager.scanResults
//            for (scanResult in scanResults) {
//                val bssid: String = scanResult.SSID
//            }
            return ssid
        }
        return ssid
    }
}