package com.cqcsy.lgsp.login

import com.blankj.utilcode.util.ActivityUtils
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.main.mine.ReleaseDynamicService
import com.cqcsy.library.utils.GlobalValue

/**
 * 作者：wangjianxiong
 * 创建时间：2023/1/5
 *
 *
 */
object AccountHelper {

    fun logout() {
        ReleaseDynamicService.stop(ActivityUtils.getTopActivity())
        DynamicCacheManger.instance.deleteAll()
        GlobalValue.loginOut()
    }
}