package com.cqcsy.lgsp.utils

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * GPS转换为GCJ
 */
object CoordinateUtil {
    private val a = 6378245.0
    private val ee = 0.00669342162296594323

    /**
     * 手机GPS坐标转火星坐标
     */
    fun transformFromWGSToGCJ(lat: Double, lng: Double): DoubleArray {
        //如果在国外，则默认不进行转换
        if (outOfChina(lat, lng)) {
            return DoubleArray(2) {
                if (it == 0) lat else lng
            }
        }
        var dLat = transformLat(
            lng - 105.0,
            lat - 35.0
        )
        var dLon = transformLon(
            lng - 105.0,
            lat - 35.0
        )
        val radLat = lat / 180.0 * Math.PI
        var magic = sin(radLat)
        magic = 1 - ee * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = dLat * 180.0 / (a * (1 - ee) / (magic * sqrtMagic) * Math.PI)
        dLon = dLon * 180.0 / (a / sqrtMagic * cos(radLat) * Math.PI)
        return DoubleArray(2) {
            if (it == 0) lat + dLat else lng + dLon
        }
    }

    fun transformLat(x: Double, y: Double): Double {
        var ret =
            -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(if (x > 0) x else -x)
        ret += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(
            2.0 * x
                    * Math.PI
        )) * 2.0 / 3.0
        ret += (20.0 * sin(y * Math.PI) + 40.0 * sin(
            (y / 3.0
                    * Math.PI)
        )) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * Math.PI) + 320 * sin(
            y
                    * Math.PI / 30.0
        )) * 2.0 / 3.0
        return ret
    }

    fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + (2.0 * y) + (0.1 * x * x) + (0.1 * x * y) + ((0.1
                * sqrt(if (x > 0) x else -x)))
        ret += (20.0 * sin(6.0 * x * Math.PI) + 20.0 * sin(
            (2.0 * x * Math.PI)
        )) * 2.0 / 3.0
        ret += (20.0 * sin(x * Math.PI) + 40.0 * sin(
            (x / 3.0 * Math.PI)
        )) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * Math.PI) + 300.0 * sin(
            x / 30.0 * Math.PI
        )) * 2.0 / 3.0
        return ret
    }

    fun outOfChina(lat: Double, lon: Double): Boolean {
        if (lon < 72.004 || lon > 137.8347) return true
        return lat < 0.8293 || lat > 55.8271
    }
}