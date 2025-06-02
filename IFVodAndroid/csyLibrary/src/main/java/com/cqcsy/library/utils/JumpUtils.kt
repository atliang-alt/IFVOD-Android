package com.cqcsy.library.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.floor


/**
 * 推送消息处理、广告跳转
 */
object JumpUtils {
    private const val JUMP_APP_PAGE = 1
    private const val JUMP_BROWSER_WEB = 2
    private const val JUMP_OUT_APP = 3
    const val REQUEST_CODE = 1088

    /**
     * 拼接通用参数，跳转其module页面
     */
    fun appendJumpParam(pagePath: String, params: MutableMap<String, Any?>, isNeedLogin: Boolean = false): String {
        val jsonArray = JSONArray()
        val jsonObject = JSONObject()
        jsonObject.put("device", "android")
        jsonObject.put("type", 1)
        jsonObject.put("isNeedLogin", isNeedLogin)
        jsonObject.put("pagePath", pagePath)
        val paramsJson = JSONObject()
        params.forEach {
            paramsJson.put(it.key, it.value)
        }
        jsonObject.put("param", paramsJson)
        jsonArray.put(jsonObject)
        return jsonArray.toString()
    }

    /**
     * 跳转处理 string
     */
    @Synchronized
    fun jumpAnyUtils(context: Context, any: Any) {
        try {
            val jsonArray: JSONArray = when (any) {
                is ArrayList<*> -> {
                    JSONArray(any)
                }

                is String -> {
                    JSONArray(any)
                }

                else -> {
                    JSONArray()
                }
            }
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.optJSONObject(i)
                val device = json.optString("device")
                if (device == "android") {
                    jumpHandleUtils(context, json)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断是否进入跳转处理类 true 进入
     */
    fun isJumpHandle(any: Any?): Boolean {
        if (any == null) {
            return false
        }
        if (any is ArrayList<*> && any.isNotEmpty()) {
            return true
        }
        if (any is String && any.isNotEmpty()) {
            return true
        }
        return false
    }

    /**
     * 跳转处理 jsonObject
     */
    @Synchronized
    private fun jumpHandleUtils(context: Context, jsonObject: JSONObject?) {
        if (jsonObject == null) {
            return
        }
        // 消息类型 0:打开app（默认）1:跳转app内页面 2：跳转外部浏览器 3:打开外部app
        when (jsonObject.optInt("type", 0)) {
            JUMP_APP_PAGE -> {
                val isNeedLogin = jsonObject.optBoolean("isNeedLogin", false)
                val param = jsonObject.optJSONObject("param")
                val pagePath = jsonObject.optString("pagePath", "")
                jumpAppActivity(context, isNeedLogin, pagePath, param)
            }

            JUMP_BROWSER_WEB -> {
                val webUrl = jsonObject.optString("webUrl", "")
                jumpBrowserWeb(context, webUrl)
            }

            JUMP_OUT_APP -> {
                val packageName = jsonObject.optString("packageName", "")
                val launchPage = jsonObject.optString("launchPage", "")
                val downloadUrl = jsonObject.optString("downloadUrl", "")
                jumpOutApp(context, packageName, launchPage, downloadUrl)
            }

            else -> jumpApp(context)
        }
    }

    private fun jumpApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        context.startActivity(intent)
    }

    private fun jumpAppActivity(
        context: Context,
        isNeedLogin: Boolean,
        pagePath: String,
        param: JSONObject?
    ) {
        if (isNeedLogin && !GlobalValue.checkLogin()) {
            return
        }
        if (pagePath.isEmpty()) {
            return
        }
        try {
            val intent = Intent(context, Class.forName(pagePath))
            if (param != null) {
                val keys = param.keys()
                while (keys.hasNext()) {
                    val key = keys.next() as String
                    when (val value = param.opt(key)) {
                        is String -> {
                            intent.putExtra(key, value)
                        }

                        is Int -> {
                            intent.putExtra(key, value)
                        }

                        is Long -> {
                            intent.putExtra(key, value)
                        }

                        is Double -> {
                            if (isInteger(value)) {
                                intent.putExtra(key, value.toInt())
                            } else {
                                intent.putExtra(key, value)
                            }
                        }

                        is Float -> {
                            if (isInteger(value as Double)) {
                                intent.putExtra(key, value.toInt())
                            } else {
                                intent.putExtra(key, value)
                            }
                        }

                        is Boolean -> {
                            intent.putExtra(key, value)
                        }

                        else -> {
                            if (value != null) {
                                intent.putExtra(key, value.toString())
                            }
                        }
                    }
                }
            }
            if (context is Activity) {
                context.startActivityForResult(intent, REQUEST_CODE)
            } else {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 判断double或float小数后面是否全为0
     */
    private fun isInteger(value: Double): Boolean {
        val eps = 1e-10
        return value - floor(value) < eps
    }

    private fun jumpBrowserWeb(context: Context, webUrl: String) {
        if (webUrl.isEmpty()) {
            return
        }
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(webUrl)
        context.startActivity(Intent.createChooser(intent, "请选择浏览器"))
    }

    private fun jumpOutApp(
        context: Context,
        pkgName: String,
        launchPage: String,
        downloadUrl: String
    ) {
        if (pkgName.isEmpty()) {
            return
        }
        if (isExistApp(pkgName, context)) {
            var intent = context.packageManager.getLaunchIntentForPackage(pkgName)
            if (intent == null) {
                if (launchPage.isNotEmpty()) {
                    intent = Intent()
                    val comp = ComponentName(pkgName, launchPage)
                    intent.component = comp
                    context.startActivity(intent)
                }
            } else {
                context.startActivity(intent)
            }
        } else {
            jumpBrowserWeb(context, downloadUrl)
        }
    }

    /**
     * 检查是否安装了某应用
     *
     * @param packageName 包名
     * @return
     */
    private fun isExistApp(packageName: String, context: Context): Boolean {
        val packageManager: PackageManager = context.packageManager
        // 获取所有已安装程序的包信息
        val pagInfo: List<PackageInfo> = packageManager.getInstalledPackages(0)
        for (i in pagInfo.indices) {
            if (pagInfo[i].packageName.equals(packageName, true)) return true
        }
        return false
    }


    /**
     * 可以通过NotificationManagerCompat 中的 areNotificationsEnabled()来判断是否开启通知权限。NotificationManagerCompat 在 android.support.v4.app包中，是API 22.1.0 中加入的。而 areNotificationsEnabled()则是在 API 24.1.0之后加入的。
     * areNotificationsEnabled 只对 API 19 及以上版本有效，低于API 19 会一直返回true
     */
    fun isNotificationEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 19) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            return notificationManagerCompat.areNotificationsEnabled()
        }
        val CHECK_OP_NO_THROW = "checkOpNoThrow"
        val OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION"
        val mAppOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val appInfo = context.applicationInfo
        val pkg = context.applicationContext.packageName
        val uid = appInfo.uid
        var appOpsClass: Class<*>? = null
        /* Context.APP_OPS_MANAGER */try {
            appOpsClass = Class.forName(AppOpsManager::class.java.name)
            val checkOpNoThrowMethod = appOpsClass.getMethod(
                CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
                String::class.java
            )
            val opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION)
            val value = opPostNotificationValue[Int::class.java] as Int
            return checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) as Int == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
    //打开手机设置页面
    /**
     * 假设没有开启通知权限，点击之后就需要跳转到 APP的通知设置界面，对应的Action是：Settings.ACTION_APP_NOTIFICATION_SETTINGS, 这个Action是 API 26 后增加的
     * 如果在部分手机中无法精确的跳转到 APP对应的通知设置界面，那么我们就考虑直接跳转到 APP信息界面，对应的Action是：Settings.ACTION_APPLICATION_DETAILS_SETTINGS */
    fun gotoSet(context: Context) {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= 26) {
            // android 8.0引导
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
        } else if (Build.VERSION.SDK_INT >= 21) {
            // android 5.0-7.0
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        } else {
            // 其他
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = Uri.fromParts("package", context.packageName, null)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}