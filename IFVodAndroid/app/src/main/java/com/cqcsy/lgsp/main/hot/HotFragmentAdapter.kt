package com.cqcsy.lgsp.main.hot

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.cqcsy.lgsp.bean.NavigationBarBean

/**
 * 作者：wangjianxiong
 * 创建时间：2022/12/20
 *
 *
 */
class HotFragmentAdapter(
    fragmentManager: FragmentManager,
    private val tabBars: MutableList<NavigationBarBean>
) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return createTabFragment(position)
    }

    override fun getCount(): Int {
        return tabBars.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tabBars[position].name
    }

    private fun createTabFragment(position: Int): Fragment {
        val fragment = HotTabViewFragment()
        val bundle = Bundle()
        bundle.putString("categoryId", tabBars[position].categoryId)
        fragment.arguments = bundle
        return fragment
    }

    /* override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                 super.destroyItem(container, position, `object`)
     }*/
}