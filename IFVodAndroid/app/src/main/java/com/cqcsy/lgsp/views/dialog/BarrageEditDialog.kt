package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import kotlinx.android.synthetic.main.barrage_edit_dialog.view.*

/**
 * 弹幕输入框Dialog
 */
class BarrageEditDialog(private var sendBackListener: SendBarrageListener?) : DialogFragment() {

    interface SendBarrageListener {
        fun sendBarrage(inputText: String)
    }

    private var count = 25

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 使用不带Theme的构造器, 获得的dialog边框距离屏幕仍有几毫米的缝隙。
        val barrageDialog = Dialog(requireActivity(), R.style.BarrageEditDialog)
        barrageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 设置Content前设定
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        val contentView =
            View.inflate(activity, R.layout.barrage_edit_dialog, null)
        barrageDialog.setContentView(contentView)
        barrageDialog.setCanceledOnTouchOutside(true) // 外部点击取消
        // 设置宽度为屏宽, 靠近屏幕底部。
        val window = barrageDialog.window
        val lp = window!!.attributes
        lp.gravity = Gravity.BOTTOM // 紧贴底部
        lp.alpha = 1f
        lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
        window.attributes = lp
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        contentView.barrageEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                contentView.barrageSend.isEnabled = s.isNotEmpty()
                contentView.barrageWordNumb.text = (count - s.length).toString()
            }
        })
        contentView.barrageSend.setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(contentView.barrageEdit.text.toString())) {
                ToastUtils.showLong("输入内容为空")
                return@OnClickListener
            } else {
                sendBackListener!!.sendBarrage(contentView.barrageEdit.text.toString())
                dialog?.dismiss()
            }
        })
        contentView.barrageEdit.isFocusable = true
        contentView.barrageEdit.isFocusableInTouchMode = true
        contentView.barrageEdit.requestFocus()
        val handler = Handler()
        barrageDialog.setOnDismissListener { handler.postDelayed({ hideSoftKeyBoard() }, 100) }
        return barrageDialog
    }

    private fun hideSoftKeyBoard() {
        try {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(
                    requireActivity().currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
        } catch (e: NullPointerException) {
        }
    }
}