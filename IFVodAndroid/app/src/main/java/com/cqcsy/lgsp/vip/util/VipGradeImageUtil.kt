package com.cqcsy.lgsp.vip.util

import com.cqcsy.lgsp.R

/**
 * VIP等级图标工具类
 */
object VipGradeImageUtil {
    private val vipImageMax = arrayOf(
        R.mipmap.icon_vip_level_0,
        R.mipmap.icon_vip_level_1,
        R.mipmap.icon_vip_level_1,
        R.mipmap.icon_vip_level_2,
        R.mipmap.icon_vip_level_3
    )

    private val vipImageMin = arrayOf(
        R.mipmap.icon_vip_level_0_min,
        R.mipmap.icon_vip_level_1_min,
        R.mipmap.icon_vip_level_1_min,
        R.mipmap.icon_vip_level_2_min,
        R.mipmap.icon_vip_level_3_min
    )

    private val vipLevel = arrayOf(
        R.mipmap.lv_1,
        R.mipmap.lv_2,
        R.mipmap.lv_3,
        R.mipmap.lv_4,
        R.mipmap.lv_5,
        R.mipmap.lv_6,
        R.mipmap.lv_7,
        R.mipmap.lv_8
    )

    /**
     * 获取VIP等级小图标
     */
    fun getVipMinImage(level: Int): Int {
        if (level >= vipImageMin.size) {
            return vipImageMin[vipImageMin.size - 1]
        }
        return vipImageMin[level]
    }

    /**
     * 获取VIP等级大图标
     */
    fun getVipImage(gid: Int): Int {
        if (gid >= vipImageMax.size) {
            return vipImageMax[vipImageMax.size - 1]
        }
        return vipImageMax[gid]
    }

    /**
     * 获取VIP等级
     */
    fun getVipLevel(level: Int): Int {
        if (level == 0) {
            return vipLevel[0]
        }
        if (level > vipLevel.size) {
            return vipLevel[7]
        }
        return vipLevel[level - 1]
    }

}