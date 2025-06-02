package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.library.utils.Constant
import kotlinx.android.synthetic.main.layout_notification_tip.close
import kotlinx.android.synthetic.main.layout_notification_tip.forbid_tip
import kotlinx.android.synthetic.main.layout_notification_tip.open_notification

/**
 ** 2023/7/6
 ** des：消息通知开关
 **/

class NotificationTipDialog(context: Context) : Dialog(context, R.style.dialog_style), OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_notification_tip)
        close.setOnClickListener(this)
        forbid_tip.setOnClickListener(this)
        open_notification.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        dismiss()
        when (v?.id) {
            R.id.forbid_tip -> SPUtils.getInstance().put(Constant.KEY_OPEN_NOTIFICATION, true)
            R.id.open_notification -> JumpUtils.gotoSet(context)
        }
    }
}