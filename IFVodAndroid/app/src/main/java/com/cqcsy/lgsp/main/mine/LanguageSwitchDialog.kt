package com.cqcsy.lgsp.main.mine

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.cqcsy.lgsp.R
import com.cqcsy.library.utils.AppLanguageUtils
import kotlinx.android.synthetic.main.layout_language_switch_dialog.*
import java.util.*

/**
 * 通用消息提示框
 */
class LanguageSwitchDialog(context: Context) : Dialog(context, R.style.dialog_style) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val attribute = window?.attributes
        val width = context.resources.displayMetrics.widthPixels
        attribute?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attribute?.width = (width * 0.6f).toInt()
        attribute?.gravity = Gravity.CENTER
        window?.attributes = attribute

        setContentView(R.layout.layout_language_switch_dialog)
        leftButton.setOnClickListener {
            dismiss()
        }
        rightButton.setOnClickListener {
            dismiss()
            when (rg_language_options.checkedRadioButtonId) {
                R.id.simple_chinese -> {
                    AppLanguageUtils.applyLanguage(Locale.CHINESE)
                }
                R.id.follow_system -> {
                    AppLanguageUtils.applySystemLanguage()
                }
                else -> {
                    AppLanguageUtils.applyLanguage(Locale.TRADITIONAL_CHINESE)
                }
            }
        }
        when (AppLanguageUtils.getAppliedLanguage()) {
            Locale.CHINESE -> {
                simple_chinese.isChecked = true
            }
            Locale.TRADITIONAL_CHINESE -> {
                traditional_chinese.isChecked = true
            }
            else -> {
                follow_system.isChecked = true
            }
        }
    }

}