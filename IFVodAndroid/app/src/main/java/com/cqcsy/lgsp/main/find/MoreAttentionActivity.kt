package com.cqcsy.lgsp.main.find

import android.os.Bundle
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.utils.NormalUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_more_attention.*

/**
 * 更多关注
 */
class MoreAttentionActivity : NormalActivity() {

    override fun getContainerView(): Int {
        return R.layout.activity_more_attention
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.moreAttention)
        initView()
    }

    private fun initView() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.recommended))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.mine))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tabLayout.tabCount != 2) {
                    return
                }
                val transaction = supportFragmentManager.beginTransaction()
                var fragment = supportFragmentManager.findFragmentByTag(tab?.position.toString())
                if (fragment == null) {
                    when (tab?.position) {
                        0 -> {
                            val bundle = Bundle()
                            bundle.putString("httpUrl", RequestUrls.FIND_RECOMMEND_USER)
                            fragment = FansAndAttentionFragment()
                            fragment.arguments = bundle
                        }
                        1 -> {
                            val bundle = Bundle()
                            bundle.putString("httpUrl", RequestUrls.FIND_ATTENTION_USER)
                            fragment = FansAndAttentionFragment()
                            fragment.arguments = bundle
                        }
                    }
                    if (fragment != null) {
                        transaction.add(R.id.moreContainer, fragment, tab?.position.toString())
                    }
                }
                for (temp in supportFragmentManager.fragments) {
                    if (temp != fragment) {
                        transaction.hide(temp)
                    }
                }
                if (fragment != null) {
                    transaction.show(fragment).commitNow()
                }
                NormalUtil.clearTabLayoutTips(tabLayout)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        val transaction = supportFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("httpUrl", RequestUrls.FIND_RECOMMEND_USER)
        val fragment = FansAndAttentionFragment()
        fragment.arguments = bundle
        transaction.add(R.id.moreContainer, fragment, "0").show(fragment).commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }
}