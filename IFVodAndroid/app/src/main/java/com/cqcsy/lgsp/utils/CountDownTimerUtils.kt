package com.cqcsy.lgsp.utils

import android.content.Context
import android.os.CountDownTimer
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R

/**
 * 倒计时工具类
 */
class CountDownTimerUtils : CountDownTimer {
    private var mTextView: TextView
    private var mContext: Context
    private var mListener: CallBackListener? = null

    constructor(context: Context, textView: TextView, millisInFuture: Long, countDownInterval: Long) : super(
        millisInFuture,
        countDownInterval
    ) {
        mTextView = textView
        mContext = context
    }

    fun setListener(listener: CallBackListener) {
        mListener = listener
    }

    override fun onTick(millisUntilFinished: Long) {
        mTextView.isClickable = false
        mTextView.text = mContext.resources.getString(R.string.second, (millisUntilFinished / 1000).toInt())
        mTextView.setTextColor(ColorUtils.getColor(R.color.word_color_5))
        if ((millisUntilFinished / 1000).toInt() == 30) {
            mListener?.callBack()
        }
    }

    override fun onFinish() {
        mTextView.text = mContext.resources.getString(R.string.resetGetCode)
        mTextView.setTextColor(ColorUtils.getColor(R.color.blue))
        mTextView.isClickable = true
    }

    interface CallBackListener {
        fun callBack()
    }
}