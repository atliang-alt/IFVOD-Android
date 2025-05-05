package com.cqcsy.library.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import com.cqcsy.library.R
import kotlinx.android.synthetic.main.layout_progress_dialog.*

/**
 * loading dialog
 */
class ProgressDialog(
    context: Context,
    var cancelAble: Boolean,
    @StringRes var tip: Int = R.string.loading
) :
    Dialog(context, R.style.dialog_style) {


    fun setProgressTip(@StringRes tip: Int) {
        progress_tip?.setText(tip)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val attr = window?.attributes
        attr?.dimAmount = 0f
        window?.attributes = attr
        setContentView(R.layout.layout_progress_dialog)
        if (tip > 0)
            progress_tip.setText(tip)
        setCancelable(cancelAble)
        setCanceledOnTouchOutside(false)
    }
}