package com.cqcsy.library.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.cqcsy.library.R

/**
 * 底部弹窗基类
 */
abstract class BottomBaseDialog(context: Context) : Dialog(context, R.style.dialog_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setWindowAnimations(R.style.bottom_dialog_anim)
        window?.decorView?.setPadding(0, 0, 0, 0)
        val attribute = window?.attributes
        attribute?.width = WindowManager.LayoutParams.MATCH_PARENT
        attribute?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = attribute
        window?.setGravity(Gravity.BOTTOM)
    }

    fun setDialog() {
        window?.decorView?.setPadding(0, 0, 0, 0)
        window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window?.decorView?.setOnSystemUiVisibilityChangeListener {
            var uiOptions: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or  //布局位于状态栏下方
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or  //全屏
                    View.SYSTEM_UI_FLAG_FULLSCREEN or  //隐藏导航栏
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            uiOptions = uiOptions or 0x00001000
            window?.decorView?.systemUiVisibility = uiOptions
        }
    }
}