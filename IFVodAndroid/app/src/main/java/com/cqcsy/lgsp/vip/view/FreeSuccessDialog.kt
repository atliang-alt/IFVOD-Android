package com.cqcsy.lgsp.vip.view

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import com.cqcsy.lgsp.R
import kotlinx.android.synthetic.main.layout_free_success_dialog.*

/**
 ** 2023/7/7
 ** des：免费领取VIP成功提示
 **/

class FreeSuccessDialog(val activity: Context) : Dialog(activity, R.style.dialog_style) {

    companion object {

        fun show(context: Activity) {
            val dialog = FreeSuccessDialog(context)
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_free_success_dialog)
        image_close.setOnClickListener { dismiss() }
        startCountDown()
    }

    override fun dismiss() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
        super.dismiss()
//        if (activity is Activity) {
//            activity.finish()
//        }
    }

    var mCountDownTimer: CountDownTimer? = null

    private fun startCountDown() {
        mCountDownTimer = object : CountDownTimer(4_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                count_down.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                dismiss()
            }

        }
        mCountDownTimer?.start()
    }
}