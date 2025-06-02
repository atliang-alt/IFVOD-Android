package com.cqcsy.lgsp.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 作者：wangjianxiong
 * 创建时间：2023/3/14
 *
 *
 */
open class BaseViewPager2Adapter : FragmentStateAdapter {
    private var fragmentList = mutableListOf<Fragment>()
    private var fragmentHashCode = mutableListOf<Long>()

    constructor(fragmentList: MutableList<Fragment>, fragmentActivity: FragmentActivity) : this(
        fragmentList,
        fragmentActivity.supportFragmentManager,
        fragmentActivity.lifecycle
    )

    constructor(fragmentList: MutableList<Fragment>, fragment: Fragment) : this(
        fragmentList,
        fragment.childFragmentManager,
        fragment.lifecycle
    )

    constructor(
        fragmentList: MutableList<Fragment>,
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle
    ) : super(fragmentManager, lifecycle) {
        this.fragmentList = fragmentList
        fragmentList.forEach {
            fragmentHashCode.add(it.hashCode().toLong())
        }
    }

    fun addFragment(fragment: Fragment) {
        fragmentList.add(fragment)
        fragmentHashCode.add(fragment.hashCode().toLong())
        notifyItemInserted(fragmentList.size)
        compatibilityDataSizeChanged(1)
    }

    fun addFragment(fragments: MutableList<Fragment>) {
        fragmentList.addAll(fragments)
        fragments.forEach {
            fragmentHashCode.add(it.hashCode().toLong())
        }
        notifyItemRangeInserted(fragmentList.size - fragments.size, fragments.size)
        compatibilityDataSizeChanged(fragments.size)
    }

    fun removeFragment(position: Int) {
        fragmentList.removeAt(position)
        fragmentHashCode.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun compatibilityDataSizeChanged(size: Int) {
        if (this.fragmentList.size == size) {
            notifyDataSetChanged()
        }
    }

    fun getItem(position: Int): Fragment? {
        if (fragmentList.isEmpty()) {
            return null
        }
        return fragmentList[position]
    }

    override fun getItemId(position: Int): Long {
        return fragmentHashCode[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentHashCode.contains(itemId)
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}