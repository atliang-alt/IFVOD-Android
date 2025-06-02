package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Scroller
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.views.LoadingRecyclerView
import kotlin.math.abs


/**
 * 带侧滑删除功能recyclerView
 */
class SwipeRecyclerView : LoadingRecyclerView {
    //上一次的触摸点
    private var mLastX = 0  //上一次的触摸点
    private var mLastY = 0

    //当前触摸的item的位置
    private var mPosition = 0

    //item对应的布局
    private var mItemLayout: View? = null

    //删除按钮
    private var mDelete: ImageView? = null

    //最大滑动距离(即删除按钮的宽度)
    private var mMaxLength = 0

    //是否在垂直滑动列表
    private var isDragging = false

    //item是在否跟随手指移动
    private var isItemMoving = false

    //item是否开始自动滑动
    private var isStartScroll = false

    //删除按钮状态   0：关闭 1：将要关闭 2：将要打开 3：打开
    private var mDeleteBtnState = 0

    //检测手指在滑动过程中的速度
    private var mVelocityTracker: VelocityTracker? = null
    private var mScroller: Scroller? = null
    private var mListener: OnItemClickListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        isScrollOption = false
        mScroller = Scroller(context, LinearInterpolator())
        mVelocityTracker = VelocityTracker.obtain()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        mVelocityTracker!!.addMovement(e)
        val x = e.x.toInt()
        val y = e.y.toInt()
        when (e.action) {
            MotionEvent.ACTION_DOWN -> when (mDeleteBtnState) {
                0 -> {
                    val view = findChildViewUnder(x.toFloat(), y.toFloat()) ?: return false
                    val viewHolder: BaseViewHolder = getChildViewHolder(view) as BaseViewHolder
                    mItemLayout = viewHolder.itemView
                    mPosition = viewHolder.adapterPosition
                    mDelete = mItemLayout!!.findViewById<View>(R.id.deleteImg) as ImageView
                    mMaxLength = mDelete!!.width
                    mDelete!!.setOnClickListener {
                        mListener!!.onDeleteClick(mPosition)
                    }
                }
                3 -> {
                    mScroller!!.startScroll(mItemLayout!!.scrollX, 0, -mMaxLength, 0, 200)
                    invalidate()
                    mDeleteBtnState = 0
                    return false
                }
                else -> {
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = mLastX - x
                val dy = mLastY - y
                val scrollX = mItemLayout!!.scrollX
                if (abs(dx) > abs(dy)) { //左边界检测
                    isItemMoving = true
                    if (scrollX + dx <= 0) {
                        mItemLayout!!.scrollTo(0, 0)
                        return true
                    } else if (scrollX + dx >= mMaxLength) { //右边界检测
                        mItemLayout!!.scrollTo(mMaxLength, 0)
                        return true
                    }
                    mItemLayout!!.scrollBy(dx, 0) //item跟随手指滑动
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isItemMoving && !isDragging && mListener != null) { //item点击事件
                    mListener!!.onItemClick(mItemLayout, mPosition)
                    mItemLayout!!.scrollTo(0, 0)
                    mDeleteBtnState = 0
                }
                isItemMoving = false
                mVelocityTracker!!.computeCurrentVelocity(1000) //计算手指滑动的速度
                val xVelocity = mVelocityTracker!!.xVelocity //水平方向速度（向左为负）
                val yVelocity = mVelocityTracker!!.yVelocity //垂直方向速度
                var deltaX = 0
                val upScrollX = mItemLayout!!.scrollX
                if (abs(xVelocity) > 100 && abs(xVelocity) > abs(yVelocity)) {
                    if (xVelocity <= -100) { //左滑速度大于100，则删除按钮显示
                        deltaX = mMaxLength - upScrollX
                        mDeleteBtnState = 2
                    } else if (xVelocity > 100) { //右滑速度大于100，则删除按钮隐藏
                        deltaX = -upScrollX
                        mDeleteBtnState = 1
                    }
                } else {
                    if (upScrollX >= mMaxLength / 2) { //item的左滑动距离大于删除按钮宽度的一半，则显示删除按钮
                        deltaX = mMaxLength - upScrollX
                        mDeleteBtnState = 2
                    } else if (upScrollX < mMaxLength / 2) { //否则隐藏
                        deltaX = -upScrollX
                        mDeleteBtnState = 1
                    }
                }
                //item自动滑动到指定位置
                mScroller!!.startScroll(upScrollX, 0, deltaX, 0, 200)
                isStartScroll = true
                invalidate()
                mVelocityTracker!!.clear()
            }
        }
        mLastX = x
        mLastY = y
        return super.onTouchEvent(e)
    }

    override fun computeScroll() {
        if (mScroller!!.computeScrollOffset()) {
            mItemLayout!!.scrollTo(mScroller!!.currX, mScroller!!.currY)
            invalidate()
        } else if (isStartScroll) {
            isStartScroll = false
            if (mDeleteBtnState == 1) {
                mDeleteBtnState = 0
            }
            if (mDeleteBtnState == 2) {
                mDeleteBtnState = 3
            }
        }
    }

    override fun onDetachedFromWindow() {
        mVelocityTracker!!.recycle()
        super.onDetachedFromWindow()
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        isDragging = state == SCROLL_STATE_DRAGGING
    }

    fun resetView() {
        mItemLayout?.scrollTo(0, 0)
        mDeleteBtnState = 0
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mListener = listener
    }

    interface OnItemClickListener {
        /**
         * item点击回调
         *
         * @param view
         * @param position
         */
        fun onItemClick(view: View?, position: Int)

        /**
         * 删除按钮回调
         *
         * @param position
         */
        fun onDeleteClick(position: Int)
    }
}