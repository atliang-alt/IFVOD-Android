package com.cqcsy.lgsp.utils

interface OnItemPositionListener {
    //交换
    fun onItemSwap(from: Int, target: Int)

    //滑动
    fun onItemMoved(position: Int)
}