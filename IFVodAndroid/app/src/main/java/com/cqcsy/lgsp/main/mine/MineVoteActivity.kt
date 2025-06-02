package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.lgsp.base.RequestUrls
import kotlinx.android.synthetic.main.activity_mine_vote.*

/**
 * 我的 -- 投票管理
 */
class MineVoteActivity: BaseActivity() {
    private val titles = arrayOf("我参与的", "我发起的")
    private val fragmentContainer = HashMap<Int, Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_vote)
        initView()
    }

    private fun initView() {
        viewPager.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                var fragment = fragmentContainer[position]
                if (fragment == null) {
                    fragment = createTabFragment(position)
                    fragmentContainer[position] = fragment
                }
                return fragment
            }

            override fun getCount(): Int {
                return titles.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return titles[position]
            }
        }
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun createTabFragment(position:Int): Fragment {
        val fragment = PartakeVoteFragment()
        val bundle = Bundle()
        if (position == 0) {
            bundle.putString("httpUrl", RequestUrls.MINE_JOIN_VOTE)
        } else {
            bundle.putString("httpUrl", RequestUrls.MINE_RELEASE_VOTE)
        }
        bundle.putInt("formType", position)
        fragment.arguments = bundle
        return fragment
    }

    fun backClick(view: View) {
        onBackPressed()
    }
}