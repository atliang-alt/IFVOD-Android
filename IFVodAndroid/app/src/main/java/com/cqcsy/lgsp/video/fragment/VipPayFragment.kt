package com.cqcsy.lgsp.video.fragment

import android.content.Intent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.vip.OpenVipFragment
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.event.LoginEvent
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.layout_vip_pay.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * vip选择套餐支付Fragment
 */
class VipPayFragment : NormalFragment() {

    override fun getContainerView(): Int {
        return R.layout.layout_vip_pay
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_in)
        } else {
            AnimationUtils.loadAnimation(requireActivity(), R.anim.bottom_out)
        }
    }

    override fun initData() {
        super.initData()
        val pathInfo = arguments?.getString("pathInfo") ?: ""
        childFragmentManager.commit {
            val fragment = OpenVipFragment.newInstance(pathInfo = pathInfo)
            replace(R.id.fragment_container, fragment)
            setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        }
        close.setOnClickListener {
            parentFragmentManager.beginTransaction().remove(this).commitNowAllowingStateLoss()
        }
        loginLayout.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        GlobalValue.userInfoBean?.let { setUserInfo(it) }
    }

    private fun setUserInfo(userInfo: UserInfoBean) {
        ImageUtil.loadCircleImage(this, userInfo.avatar, user_image)
        nick_name.text = userInfo.nickName
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogin(event: LoginEvent) {
        GlobalValue.userInfoBean?.apply {
            setUserInfo(this)
        }
    }
}