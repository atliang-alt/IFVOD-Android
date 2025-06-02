package com.cqcsy.lgsp.upper

import android.os.Bundle
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.main.find.FansAndAttentionFragment
import com.cqcsy.lgsp.base.RequestUrls

/**
 * TA的\我的关注
 */
class UserFocusActivity : NormalActivity() {

    override fun getContainerView(): Int {
        return R.layout.activity_upper_focus
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("isSelf", false)) {
            setHeaderTitle(R.string.my_focus)
        } else {
            setHeaderTitle(R.string.upper_focus)
        }
        initView()
    }

    private fun initView() {
        val fragment = FansAndAttentionFragment()
        val bundle = Bundle()
        bundle.putBoolean("isSelf", intent.getBooleanExtra("isSelf", false))
        bundle.putString("httpUrl", RequestUrls.FIND_ATTENTION_USER)
        fragment.arguments = bundle
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.focusContent, fragment)
        transaction.commitAllowingStateLoss()
    }
}