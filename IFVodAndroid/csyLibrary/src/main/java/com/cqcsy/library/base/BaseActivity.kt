package com.cqcsy.library.base

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.library.R
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.event.LoginEvent
import com.cqcsy.library.utils.AppLanguageUtils
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.MessageToast
import com.cqcsy.library.views.ProgressDialog
import com.lzy.okgo.OkGo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 所有activity基类
 * 只处理基本的网络，status bar，适配等
 */

abstract class BaseActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    protected var isPaused = false

    companion object {
        @JvmField
        var activityList: MutableList<Activity> = ArrayList()
    }

    fun onBack(view: View) {
        if (view.isVisible) {
            onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppLanguageUtils.applyLanguage(this)
        window.setBackgroundDrawableResource(R.color.background_page_bg)
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ColorUtils.getColor(R.color.background_page_bg)
        activityList.add(this)
        setNormalStatusBar()
        EventBus.getDefault().register(this)
        disableAutoFill()
    }

    /**
     * Android 8以上Edittext自动填充会导致部分手机奔溃或卡死，手动关闭EditView自动填充
     */
    private fun disableAutoFill() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.decorView.importantForAutofill =
                View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
    }

    fun setNormalStatusBar() {
        StatusBarUtil.setColor(this, ColorUtils.getColor(R.color.colorPrimary), 0)
    }

    override fun onResume() {
        super.onResume()
//        MobclickAgent.onResume(this)
        isPaused = false
    }

    override fun onPause() {
        super.onPause()
//        MobclickAgent.onPause(this)
        MessageToast.cancel()
        isPaused = true
    }

    override fun onStop() {
        super.onStop()
//        isBackground()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        OkGo.getInstance().cancelTag(this)
        dismissProgressDialog()
        activityList.remove(this)
        super.onDestroy()
    }

    protected fun setStatusBarColor(@ColorInt colorRes: Int) {
        StatusBarUtil.setColor(this, colorRes)
    }

    protected fun setStatusBarDrawable(@DrawableRes drawable: Int) {
        StatusBarUtil.setDrawable(this, drawable)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onNewMessage(message: ChatMessageBean) {
        if (isPaused || message.isRemind == 0) {
            return
        }
        if (message.fromUid != GlobalValue.userInfoBean?.id) {
            MessageToast.showMessage(this, message)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        if (event.status) {
            onLogin()
        } else {
            onLoginOut()
        }
    }

    /**
     * 登陆成功处理页面调用
     */
    open fun onLogin() {

    }

    /**
     * 退出登陆处理
     */
    open fun onLoginOut() {

    }

    fun showProgressDialog(cancelAble: Boolean = true, tips: Int = 0) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this, cancelAble, tips)
        } else if (tips > 0) {
            progressDialog?.setProgressTip(tips)
        }
        progressDialog!!.show()
    }

    fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    /**
     * 子线程处理结束后，需要在UI线程更新界面的方法，都要先调用这个方法确保页面状态正常
     */
    open fun isSafe(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !isDestroyed
        } else {
            !isFinishing
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(newBase))
    }

//    fun isBackground(): Boolean {
////        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
////            return false
////        }
//        val activityManager: ActivityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//        val appProcesses: List<ActivityManager.RunningAppProcessInfo> =
//            activityManager.runningAppProcesses
//        var isBackground = true
//        for (appProcess in appProcesses) {
//            if (appProcess.processName == packageName) {
//                isBackground =
//                    if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED
//                        || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND
//                    ) {
//                        true
//                    } else !(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
//                            || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE)
//                break
//            }
//        }
//        if (isAppBackground != isBackground) {
//            isAppBackground = isBackground
//            val event = AppStatusChange(isBackground)
//            EventBus.getDefault().post(event)
//        }
//
//        return isBackground
//    }

}