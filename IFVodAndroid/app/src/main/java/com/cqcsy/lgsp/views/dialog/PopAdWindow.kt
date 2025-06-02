package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.layout_pop_ad_window.*

/**
 * 首页弹窗广告
 */
class PopAdWindow(context: Context) : Dialog(context, R.style.dialog_style) {
    var advertBean: AdvertBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_pop_ad_window)
        setCanceledOnTouchOutside(false)
        adClose.setOnClickListener { dismiss() }
        if (advertBean != null) {
            ImageUtil.loadImage(
                context,
                advertBean!!.showURL,
                adImage,
                scaleType = ImageView.ScaleType.CENTER,
                needAuthor = true,
                corner = 8
            )
            adImage.setOnClickListener {
                if (JumpUtils.isJumpHandle(advertBean?.appParam)) {
                    JumpUtils.jumpAnyUtils(context, advertBean?.appParam!!)
                    return@setOnClickListener
                }
                if (!advertBean?.linkURL.isNullOrEmpty()) {
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.putExtra(WebViewActivity.urlKey, advertBean?.linkURL)
                    context.startActivity(intent)
                    dismiss()
                }
            }
        }
    }

    override fun show() {
        if (advertBean == null) {
            return
        }
        super.show()
    }
}