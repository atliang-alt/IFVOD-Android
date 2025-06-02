package com.cqcsy.lgsp.utils

import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 时间显示工具
 */
object TimesUtils {
    /**
     * 获取年份
     */
    fun getYear(time: String): String {
        if (time.isEmpty()) {
            return ""
        }
        val timeStr = time.replace("T", " ")
        val date = TimeUtils.string2Date(timeStr)
        val simpleDateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
        return simpleDateFormat.format(date).substring(0, 5)
    }

    fun formatDate(time: String): Date? {
        if (time.isEmpty() || time == "0") {
            return Date()
        }
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        var date: Date? = null
        try {
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            var temp = time.replace("T", " ")
            temp = temp.replace("Z", "")
            date = sdf.parse("$temp UTC")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return date
    }

    /**
     * UTC时间 ---> 当地时间
     * @param utcTime   UTC时间
     * @return
     */
    fun utc2Local(utcTime: String, localeFormat: String = "yyyy-MM-dd HH:mm"): String {
        val utcFormater = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z") //UTC时间格式
        utcFormater.timeZone = TimeZone.getTimeZone("UTC")
        try {
            val temp = utcTime.replace("Z", " UTC")
            val gpsUTCDate = utcFormater.parse(temp)
            val localFormater = SimpleDateFormat(localeFormat)//当地时间格式
            localFormater.timeZone = TimeZone.getDefault()
            return localFormater.format(gpsUTCDate?.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun friendDate(date: String): String {
        val dateTime = formatDate(date) ?: return "Unknown"
        val calendar = Calendar.getInstance()
        val dayTime = 24 * 60 * 60 * 1000L
        val days = calendar.timeInMillis - dateTime.time
        return if (days <= dayTime) {
            val hours = (days / 3600000).toInt()
            if (hours <= 0) {
                StringUtils.getString(R.string.just_now)
            } else {
                StringUtils.getString(R.string.hour_ago, hours + 1)
            }
        } else if (days / dayTime in 1 until 4) {
            StringUtils.getString(R.string.day_ago, days / dayTime)
        } else {
            TimeUtils.date2String(dateTime, "yyyy-MM-dd")
        }
    }

    fun formatTime(time: String): Int {
        if (time.isEmpty() || time == "0") {
            return 0
        }
        if (time.indexOf(":") == -1) {
            return 0
        }
        val array = time.split(":")
        return when (array.size) {
            3 -> {
                array[0].toInt() * 60 * 60 + array[1].toInt() * 60 + array[2].toInt()
            }

            2 -> {
                array[0].toInt() * 60 + array[1].toInt()
            }

            1 -> {
                array[0].toInt()
            }

            else -> {
                0
            }
        }
    }

    /**
     * 获取UTC时间
     */
    fun getUTCTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        sdf.timeZone = TimeZone.getTimeZone("gmt")
        return sdf.format(Date())
    }
}