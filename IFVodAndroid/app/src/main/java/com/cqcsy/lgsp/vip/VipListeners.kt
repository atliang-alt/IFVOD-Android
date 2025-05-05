package com.cqcsy.lgsp.vip

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.cqcsy.library.pay.model.VipClassifyBean

/**
 * 作者：wangjianxiong
 * 创建时间：2022/8/12
 *
 *
 */

interface OpenVipListener {
    fun onGetVipCategorySize(size: Int)
    fun onPayItemClick(vipClassifyBean: VipClassifyBean)
    fun onResult(isSuccess: Boolean)
}

fun Fragment.setOpenVipListener(listener: OpenVipListener) {
    childFragmentManager.setOpenVipListener(this, listener)
}

fun FragmentActivity.setOpenVipListener(listener: OpenVipListener) {
    supportFragmentManager.setOpenVipListener(this, listener)
}

fun FragmentManager.setOpenVipListener(
    lifecycleOwner: LifecycleOwner,
    listener: OpenVipListener
) {
    setFragmentResultListener(
        OpenVipFragment.REQUEST_KEY,
        lifecycleOwner
    ) { _, bundle ->
        when (bundle.getInt("result_type")) {
            OpenVipFragment.OPEN_RESULT -> {
                val openVipResult = bundle.getBoolean("open_vip_result")
                listener.onResult(openVipResult)
            }
            OpenVipFragment.VIP_CATEGORY_SIZE -> {
                val size = bundle.getInt("vip_category_size")
                listener.onGetVipCategorySize(size)
            }
            OpenVipFragment.SELECT_OPTION_RESULT -> {
                val vipClassifyBean = bundle.getSerializable("select_vip_classify") as VipClassifyBean
                listener.onPayItemClick(vipClassifyBean)
            }
        }
    }
}