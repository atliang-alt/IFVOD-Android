package com.cqcsy.lgsp.video.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R


/**
 * 右侧播放菜单基类
 */
abstract class VideoMenuDialog(context: Context) : Dialog(context, R.style.dialog_style) {
    private var column = 1
    var isVertical = false
    private var viewHeight = 0

    fun setMenuColumn(number: Int) {
        this.column = number
    }

    fun getMenuColumn(): Int {
        return column
    }

    fun setHeight(value: Int) {
        this.viewHeight = value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.layout_video_normal_menu)
        setDialog()
        val attribute = window?.attributes
        attribute?.dimAmount = 0f
        if (isVertical) {
            window?.setWindowAnimations(R.style.bottom_dialog_anim)
            attribute?.width = WindowManager.LayoutParams.MATCH_PARENT
            attribute?.height = if (viewHeight != 0) viewHeight else WindowManager.LayoutParams.WRAP_CONTENT
            window?.attributes = attribute
            window?.setGravity(Gravity.BOTTOM)
        } else {
            window?.setWindowAnimations(R.style.right_dialog_anim)
            attribute?.width = SizeUtils.dp2px(if (column == 1) 280f else 360f)
            attribute?.height = WindowManager.LayoutParams.MATCH_PARENT
            window?.attributes = attribute
            window?.setGravity(Gravity.END)
        }
    }

    private fun setDialog() {
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