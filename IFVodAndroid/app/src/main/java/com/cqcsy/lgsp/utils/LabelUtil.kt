package com.cqcsy.lgsp.utils

import com.blankj.utilcode.util.SPUtils
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue

/**
 * 签处理类
 */
object LabelUtil {

    fun cacheDynamicLabel(labelStr: String?, userId: Int) {
        val label = (labelStr?.split(","))?.first() ?: ""
        if (GlobalValue.userInfoBean?.id == userId) {
            return
        }
        addLabels(label, userId)
    }

    fun addLabels(label: String, userId: Int, key: String = Constant.KEY_DYNAMIC_ALBUM_LABELS) {
        if (label.isEmpty()) {
            return
        }
        val cacheKey = if (GlobalValue.isLogin()) {
            key + GlobalValue.userInfoBean?.id
        } else {
            key
        }
        val localOld = SPUtils.getInstance().getString(cacheKey)
        val localList = ArrayList<String>()
        if (!localOld.isNullOrEmpty()) {
            localList.addAll(localOld.split(","))
        }
        var list: MutableList<String> = label.split("·").toMutableList()
        if (list.isNotEmpty()) {
            localList.removeAll(list)
            list.addAll(localList)
            if (list.size > 20) {
                list = list.subList(0, 20)
            }
            val cache = list.joinToString(separator = ",")
            SPUtils.getInstance().put(cacheKey, cache)
        }
        SPUtils.getInstance().put(Constant.KEY_LAST_SHORT_UPPER_ID, userId)
    }

    /**
     * 获取字符串含(,)分割
     * @param videoType 缓存类型
     */
    fun getAllLabels(key: String = Constant.KEY_DYNAMIC_ALBUM_LABELS): String {
        val cacheKey = if (GlobalValue.isLogin()) {
            key + GlobalValue.userInfoBean?.id
        } else {
            key
        }
        return SPUtils.getInstance().getString(cacheKey)
    }

}