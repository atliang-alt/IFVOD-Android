package com.cqcsy.lgsp.upper.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.lgsp.vip.OpenVipFragment
import com.cqcsy.lgsp.vip.OpenVipListener
import com.cqcsy.lgsp.vip.setOpenVipListener
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.open_vip_dialog.view.*

class OpenVipDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.dialog_style)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val window = dialog?.window
        val contentView = inflater.inflate(
            R.layout.open_vip_dialog,
            window?.findViewById(android.R.id.content) as ViewGroup,
            false
        )
        // 设置宽度为屏宽, 靠近屏幕底部。
        val lp = window.attributes
        window.setWindowAnimations(R.style.bottom_dialog_anim)
        lp.gravity = Gravity.BOTTOM // 紧贴底部
        lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
        lp.height = ScreenUtils.getAppScreenHeight() - SizeUtils.dp2px(200f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes = lp
        dialog?.setCanceledOnTouchOutside(true) // 外部点击取消
        contentView.vipCloseImage.setOnClickListener {
            dialog?.dismiss()
        }
        GlobalValue.userInfoBean!!.avatar?.let {
            ImageUtil.loadCircleImage(
                this,
                it, contentView.vipUserPhoto
            )
        }
        contentView.vipUserName.text = GlobalValue.userInfoBean!!.nickName
        addEmotionView()
        return contentView
    }

    private fun addEmotionView() {
        val fragment = OpenVipFragment()
        fragment.arguments = arguments
        childFragmentManager.commit(allowStateLoss = true) {
            replace(R.id.openVip, fragment)
        }
        setOpenVipListener(object : OpenVipListener {
            override fun onGetVipCategorySize(size: Int) {

            }

            override fun onPayItemClick(vipClassifyBean: VipClassifyBean) {
            }

            override fun onResult(isSuccess: Boolean) {

            }
        })
    }
}