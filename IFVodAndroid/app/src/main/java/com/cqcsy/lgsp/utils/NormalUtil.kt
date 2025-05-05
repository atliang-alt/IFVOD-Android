package com.cqcsy.lgsp.utils

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.view.WindowManager
import com.blankj.utilcode.util.DeviceUtils
import androidx.appcompat.widget.TooltipCompat
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.cqcsy.lgsp.R
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.library.utils.Constant
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt


object NormalUtil {
//    //定义GB的计算常量
//    private const val GB = 1024 * 1024 * 1024
//
//    //定义MB的计算常量
//    private const val MB = 1024 * 1024
//
//    //定义KB的计算常量
//    private const val KB = 1024

    fun formatFileSize(context: Context, bytes: Long): String {
        return Formatter.formatFileSize(context, bytes)
    }

//    fun bytes2kb(bytes: Long): String {
//        //格式化小数
//        val format = DecimalFormat("###.0");
//        return when {
//            bytes / GB >= 1 -> {
//                format.format(bytes / GB) + "GB"
//            }
//            bytes / MB >= 1 -> {
//                format.format(bytes / MB) + "MB"
//            }
//            bytes / KB >= 1 -> {
//                format.format(bytes / KB) + "KB"
//            }
//            else -> {
//                bytes.toString() + "B"
//            }
//        }
//    }

    fun getAvailMemory(context: Context): String { // 获取android当前可用内存大小
        val am: ActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return formatFileSize(context, mi.availMem) // 将获取的内存大小规格化
    }

    fun getTotalMemorySize(context: Context): String {
        val size = getAvailableExternalMemorySize()// + getAvailableInternalMemorySize()
        return formatFileSize(context, size) // 将获取的内存大小规格化
    }

    /**
     * 判断外部存储是否可用
     */
    fun externalMemoryAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 获取手机内部剩余储存
     */
    fun getAvailableInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val statFs = StatFs(path.path)
        val blockSize = statFs.blockSizeLong
        val availableBlocks = statFs.availableBlocksLong
        return blockSize * availableBlocks
    }

    /**
     * 获取外部存储剩余
     */

    fun getAvailableExternalMemorySize(): Long {
        return if (externalMemoryAvailable()) {
            val path = Environment.getExternalStorageDirectory()
            val statFs = StatFs(path.path)
            val blockSize = statFs.blockSizeLong
            val availableBlocks = statFs.availableBlocksLong
            blockSize * availableBlocks
        } else {
            0
        }
    }

    /**
     * 格式化手机号显示
     */
    fun formatPhoneNumber(phone: String): String {
        val phoneLength = phone.length
        if (phone.isEmpty() || phoneLength <= 4) {
            return phone
        }
        val sb = StringBuilder()
        for (i in phone.indices) {
            val end = phoneLength - 2
            val char = phone[i]
            if (i in 2 until end) {
                sb.append("*")
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    /**
     * 格式化邮箱显示
     */
    fun formatEmail(email: String): String {
        if (email.isEmpty()) {
            return email
        }
        val index = email.indexOf("@")
        if (index == -1) {
            return email
        }
        val startStr = email.substring(0, index)
        if (startStr.length <= 2) {
            return email
        }
        val sb = StringBuilder()
        for (i in email.indices) {
            val char = email[i]
            if (i in 2 until index) {
                sb.append("*")
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

    fun urlEncode(url: String?): String {
        if (url.isNullOrEmpty()) {
            return ""
        }
        return Uri.encode(url, "-![.:/,%?&=]")
    }

    /**
     * 截取mediaId
     */
    fun formatMediaId(mediaId: String): String {
        if (mediaId.isEmpty()) {
            return ""
        }
        val index = mediaId.indexOf("_")
        if (index != -1) {
            return mediaId.subSequence(0, index).toString()
        }
        return ""
    }

    /**
     * 格式化主演人名
     */
    fun formatActorName(names: String): String {
        var newNames = names
        if (names.isEmpty()) {
            return newNames
        }
        if (names.contains(",")) {
            newNames = names.replace(",", "  ")
        }
        return newNames
    }

    fun copyText(text: CharSequence?) {
        val cm: ClipboardManager =
            Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(Utils.getApp().packageName, text))
    }

    fun getAreaCode(): String {
        val showArea = SPUtils.getInstance().getBoolean(Constant.KEY_SHOW_AREA_SETTING, false)
        if (!showArea) {
            return ""
        }
        var areaCode = SPUtils.getInstance().getString(Constant.AREA_CODE, "")
        if (areaCode.indexOf(",") != -1) {
            areaCode = areaCode.substring(0, areaCode.indexOf(","))
        }
        return areaCode
    }

    fun getAreaName(): String {
        var areaCode = SPUtils.getInstance().getString(Constant.AREA_CODE, "")
        if (areaCode.indexOf(",") != -1) {
            areaCode = areaCode.substring(areaCode.indexOf(",") + 1)
        }
        return areaCode
    }

    /**
     * 拼接VIP表情字符串
     */
    fun getVipString(vipList: MutableList<String>): String {
        var vipString = ""
        for (i in vipList.indices) {
            vipString += vipList[i]
        }
        return vipString
    }

    fun isPhone(context: Context): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val dm = DisplayMetrics()
        display.getMetrics(dm)
        val x = (dm.widthPixels / dm.xdpi).toDouble().pow(2.0)
        val y = (dm.heightPixels / dm.ydpi).toDouble().pow(2.0)
        // 屏幕尺寸
        val screenInches = sqrt(x + y)
        // 小于7尺寸则为手机
        return screenInches < 8.5
    }

    fun isTv(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.type.television")
    }

    /**
     * 获取本地保存的国家区域码
     */
    fun getLocalAreaList(): MutableList<AreaBean>? {
        val areaStr = SPUtils.getInstance().getString(Constant.KEY_COUNTRY_AREA_INFO)
        val jsonObject = if (!areaStr.isNullOrEmpty()) {
            JSONObject(areaStr)
        } else {
            JSONObject()
        }
        val saveTime = jsonObject.optLong(Constant.KEY_COUNTRY_AREA_INFO_TIME)
        val country = jsonObject.optJSONArray("country")
        if (System.currentTimeMillis() - saveTime > Constant.COUNTRY_TIME || country == null || country.length() == 0) {
            return null
        }
        return Gson().fromJson<ArrayList<AreaBean>>(
            country.toString(),
            object : TypeToken<ArrayList<AreaBean>>() {}.type
        )
    }

    /**
     * 格式化播放量、粉丝量、评论数量、点赞数量
     */
    fun formatPlayCount(playCount: Int): String {
        val str: String
        if (playCount < 10000) {
            return playCount.toString()
        }
        val number = playCount / 10000f
        val format = DecimalFormat("0.0")
        // 去掉四舍五入
        format.roundingMode = RoundingMode.DOWN
        str = format.format(number) + StringUtils.getString(R.string.ten_thousand)
        return str
    }

    /**
     * 判定颜色值十六进制字符串是否正确
     */
    fun isColor(color: String?): Boolean {
        if (color.isNullOrEmpty()) {
            return false
        }
        //^#([0-9a-fA-f]{6}|[0-9a-fA-f]{8})$
        val result = color.matches(Regex("^#([0-9a-fA-f]{6}|[0-9a-fA-f]{8})$"))
        return result
    }

    /**
     * 获取文件绝对路径
     */
    fun getAbsolutePath(path: String?): String? {
        var result = path
        try {
            val uri = Uri.parse(path)
            if (DeviceUtils.getSDKVersionCode() >= Build.VERSION_CODES.Q && uri.scheme == "content") {
                val proj = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = Utils.getApp().contentResolver.query(uri, proj, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    result = cursor.getString(index)
                }
                cursor?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 去除TabLayout长按提示
     */
    fun clearTabLayoutTips(tabLayout: TabLayout) {
        for (position in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(position)
            tab?.customView?.setOnLongClickListener {
                true
            }
            tab?.view?.setOnLongClickListener {
                true
            }
            tab?.apply {
                TooltipCompat.setTooltipText(view, null)
                customView?.let { TooltipCompat.setTooltipText(it, null) }
            }
        }
    }

    fun getLocationPermissionRequest(): PermissionUtils {
        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            PermissionUtils.permission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            PermissionUtils.permission(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }
}