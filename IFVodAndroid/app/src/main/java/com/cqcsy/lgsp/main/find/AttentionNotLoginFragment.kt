package com.cqcsy.lgsp.main.find

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.lgsp.login.LoginActivity
import kotlinx.android.synthetic.main.layout_fragment_not_login.*

/**
 * 发现-关注-未登录
 */
class AttentionNotLoginFragment : NormalFragment() {

    override fun getContainerView(): Int {
        return R.layout.layout_fragment_not_login
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attentionLogin.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}