package com.cqcsy.lgsp.utils

import android.content.Context
import android.graphics.Color
import android.os.Vibrator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * 拖拽工具类
 */
class CustomItemTouchHelper(var listener: OnItemPositionListener, var context: Context) :
    ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlag: Int
        val swipeFlags: Int
        //如果是表格布局，则可以上下左右的拖动，但是不能滑动
        if (recyclerView.layoutManager is GridLayoutManager) {
            dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            swipeFlags = 0
        }
        //如果是线性布局，那么只能上下拖动，只能左右滑动
        else {
            dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        }
        //通过makeMovementFlags生成最终结果
        return makeMovementFlags(dragFlag, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        //被拖动的item位置
        val fromPosition = viewHolder.layoutPosition
        //他的目标位置
        val targetPosition = target.layoutPosition
        //为了降低耦合，使用接口让Adapter去实现交换功能
        listener.onItemSwap(fromPosition, targetPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //为了降低耦合，使用接口让Adapter去实现交换功能
        listener.onItemMoved(viewHolder.layoutPosition)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        //当开始拖拽的时候
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            viewHolder!!.itemView.setBackgroundColor(Color.LTGRAY)
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(50)
        }
    }

    //当手指松开的时候
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT)
        super.clearView(recyclerView, viewHolder)
    }
}