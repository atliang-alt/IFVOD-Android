package com.cqcsy.lgsp.main.mine

import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cqcsy.lgsp.bean.DynamicBean

/**
 * 作者：wangjianxiong
 * 创建时间：2023/4/20
 *
 *
 */
class DynamicVideoPagerAdapter(
    fragment: Fragment,
    var dataList: MutableList<DynamicBean>,
    private val isFromMineDynamic: Boolean,
    private val showComment: Boolean,
    private val showCommentIndex: Int,
    val commentId: Int,
    val replyId: Int
) : FragmentStateAdapter(fragment) {
    var fragmentList = mutableListOf<DynamicVideoFragment>()
    var idList = mutableListOf<Long>()

    init {
        dataList.forEachIndexed { index, it ->
            val dynamicFragment = DynamicVideoFragment.newInstance(
                it,
                isFromMineDynamic,
                showComment && showCommentIndex == index,
                commentId = commentId,
                replyId = replyId
            )
            fragmentList.add(dynamicFragment)
            idList.add(dynamicFragment.hashCode().toLong())
        }
    }

    fun addData(data: DynamicBean) {
        dataList.add(data)
        val fragment = DynamicVideoFragment.newInstance(data, isFromMineDynamic, false)
        fragmentList.add(fragment)
        idList.add(fragment.hashCode().toLong())
        notifyItemInserted(dataList.size)
        compatibilityDataSizeChanged(1)
    }

    fun addData(@IntRange(from = 0) position: Int, newData: MutableList<DynamicBean>) {
        val fragmentListTemp = mutableListOf<DynamicVideoFragment>()
        val idListTemp = mutableListOf<Long>()
        newData.forEachIndexed { _, it ->
            val fragment = DynamicVideoFragment.newInstance(it, isFromMineDynamic, false)
            fragmentListTemp.add(position, fragment)
            idListTemp.add(position, it.hashCode().toLong())
        }
        fragmentList.addAll(position, fragmentListTemp)
        idList.addAll(position, idListTemp)
        dataList.addAll(position, newData)
        notifyItemRangeInserted(position, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }

    fun addData(newData: MutableList<DynamicBean>) {
        newData.forEachIndexed { _, it ->
            val fragment = DynamicVideoFragment.newInstance(it, isFromMineDynamic, false)
            fragmentList.add(fragment)
            idList.add(it.hashCode().toLong())
        }
        dataList.addAll(newData)
        notifyItemRangeInserted(dataList.size - newData.size, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }

    fun remove(position: Int) {
        dataList.removeAt(position)
        fragmentList.removeAt(position)
        idList.removeAt(position)
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
        return idList[position]
    }

    override fun containsItem(itemId: Long): Boolean {
        return idList.contains(itemId)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}