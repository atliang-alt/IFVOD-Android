package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import com.blankj.utilcode.util.KeyboardUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.views.BottomBaseDialog
import kotlinx.android.synthetic.main.layout_input_key_word.*

/**
 * 屏蔽关键词输入
 */
class InputKeyWordBoard(context: Context) : BottomBaseDialog(context) {
    val MAX_KEYWORD = 20
    var listener: OnFinishInputListener? = null

    interface OnFinishInputListener {
        fun onFinishInput(input: String)
    }

    fun setOnFinishListener(l: OnFinishInputListener) {
        this.listener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_input_key_word)

        addKeyWord.setOnClickListener {
            listener?.onFinishInput(inputEdit.text.toString())
        }
        inputEdit.addTextChangedListener(object:TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    if (s.length <= MAX_KEYWORD) {
                        inputCounter.text = (MAX_KEYWORD - s.length).toString()
                    } else {
                        s.delete(s.length - 1, s.length)
                    }
                    addKeyWord.isEnabled = true
                } else {
                    inputCounter.text = (MAX_KEYWORD).toString()
                    addKeyWord.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        setOnShowListener {
            Handler().postDelayed({
                inputEdit.requestFocus()
                KeyboardUtils.showSoftInput(inputEdit)
            }, 300)
        }
    }

    override fun dismiss() {
        KeyboardUtils.hideSoftInput(inputEdit)
        super.dismiss()
    }
}