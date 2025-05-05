package com.cqcsy.lgsp.screenshare

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.cqcsy.lgsp.R
import com.cqcsy.library.views.BottomBaseDialog
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo

class ScreenConnectDialog(context: Context) : BottomBaseDialog(context) {
    var deviceInfo: LelinkServiceInfo? = null
    var cancelListener: View.OnClickListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_screen_connect)
        setCancelable(false)
        val name = findViewById<TextView>(R.id.tv_name)
        name.text = deviceInfo?.name
        findViewById<TextView>(R.id.cancel).setOnClickListener(cancelListener)
    }
}