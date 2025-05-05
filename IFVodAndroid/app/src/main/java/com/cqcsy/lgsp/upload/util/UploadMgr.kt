package com.cqcsy.lgsp.upload.util

import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.manger.UploadCacheManger
import com.cqcsy.library.utils.Constant

/**
 * 获取上传数据manager
 */
object UploadMgr {
    /**
     * 获取正在上传列表
     */
    fun getUploadList(): MutableList<UploadCacheBean> {
        val list: MutableList<UploadCacheBean> = ArrayList()
        for (uploadBean in UploadCacheManger.instance.select()) {
            if (uploadBean.status != Constant.UPLOAD_FINISH) {
                list.add(uploadBean)
            }
        }
        return list
    }

    /**
     * 判断是否有暂停或正在上传的数据
     */
    fun isUploading(): Boolean {
        val list: MutableList<UploadCacheBean> = ArrayList()
        for (uploadBean in UploadCacheManger.instance.select()) {
            if (uploadBean.status != Constant.UPLOAD_FINISH && uploadBean.status != Constant.UPLOAD_ERROR) {
                list.add(uploadBean)
            }
        }
        return list.isNotEmpty()
    }
}