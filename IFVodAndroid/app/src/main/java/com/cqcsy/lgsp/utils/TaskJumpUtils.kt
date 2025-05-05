package com.cqcsy.lgsp.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.cqcsy.lgsp.main.mine.AccountAndSecurityActivity
import com.cqcsy.lgsp.main.mine.EditUserInfoActivity
import com.cqcsy.lgsp.main.mine.SignGetGiftActivity
import com.cqcsy.lgsp.upload.UploadCenterActivity

/**
 * 任务点击跳转判断类
 */
object TaskJumpUtils {

    // 签到
    private const val userAttend = "UserAttend"

    // 绑定邮箱和手机
    private const val bindEmailTel = "BindEmailTel"

    // 上传头像
    private const val uploadedHeadImg = "UploadedHeadImg"

    // 上传视频
    private const val uploadVideo = "UploadVideo"

    // 充值
    private const val pay = "Pay"

    /**
     * 跳转去完成任务
     */
    fun jumpFinishTask(activity: Activity, type: String) {
        when (type) {
            bindEmailTel -> {
                activity.startActivity(Intent(activity, AccountAndSecurityActivity::class.java))
            }
            uploadedHeadImg -> {
                activity.startActivity(Intent(activity, EditUserInfoActivity::class.java))
            }
            uploadVideo -> {
                activity.startActivity(Intent(activity, UploadCenterActivity::class.java))
            }
            userAttend -> {
                activity.startActivity(Intent(activity, SignGetGiftActivity::class.java))
            }
            pay -> {
                activity.setResult(RESULT_OK, Intent().putExtra("index", 2))
                activity.finish()
            }
            else -> {
                activity.setResult(RESULT_OK, Intent().putExtra("index", 0))
                activity.finish()
            }
        }
    }
}