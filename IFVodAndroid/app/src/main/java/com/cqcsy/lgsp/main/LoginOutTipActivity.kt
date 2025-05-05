package com.cqcsy.lgsp.main

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.cqcsy.lgsp.R

class LoginOutTipActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_tip_dialog)
        findViewById<TextView>(R.id.dialogTitle).setText(R.string.tips)
        findViewById<TextView>(R.id.dialogMsg).setText(R.string.login_out_by_other)
        findViewById<TextView>(R.id.leftButton).visibility = View.GONE
        findViewById<View>(R.id.verticalLine).visibility = View.GONE
        findViewById<TextView>(R.id.rightButton).setOnClickListener { finish() }
    }
}