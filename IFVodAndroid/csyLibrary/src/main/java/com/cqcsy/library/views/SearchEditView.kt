package com.cqcsy.library.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.cqcsy.library.R


class SearchEditView : AppCompatEditText, TextWatcher {
    private var mPaint: Paint? = null
    private var delBtn: Drawable? = null
    private var mListener: CustomDeletedCallback?= null

    /**
     * 手指抬起时的X坐标
     */
    private var xUp = 0

    constructor(context: Context) : this(context, null) {
        initView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0) {
        initView()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView()
    }

    private fun initView() {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        mPaint = Paint()
        mPaint?.strokeWidth = 3.0f
        delBtn = ResourcesCompat.getDrawable(resources, R.mipmap.icon_eliminate, null)
        addTextChangedListener(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = this.scrollX
        val w = this.measuredWidth
        canvas.drawLine(
            0f, (this.height - 1).toFloat(), (w + x).toFloat(),
            (this.height - 1).toFloat(), mPaint!!
        )
    }

    // 处理删除事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (delBtn != null && event.action == MotionEvent.ACTION_UP) {
            // 获取点击时手指抬起的X坐标
            xUp = event.x.toInt()
            // 当点击的坐标到当前输入框右侧的距离小于等于getCompoundPaddingRight()的距离时，则认为是点击了删除图标
            if (width - xUp <= compoundPaddingRight) {
                if (text.toString().isNotEmpty()) {
                    setText("")
                    //删除回调
                    mListener?.onDeleted()
                }
            }
        } else if (text?.isNotEmpty() == true) {
            setCompoundDrawablesWithIntrinsicBounds(null, null, delBtn, null)
        }
        return super.onTouchEvent(event)
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            // 如果为空，则不显示删除图标
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        } else {
            // 如果非空，则要显示删除图标
            setCompoundDrawablesWithIntrinsicBounds(null, null, delBtn, null)
        }
    }

    //自定义右侧清空按钮回调事件
    interface CustomDeletedCallback {
        fun onDeleted()
    }

    //自定义删除回调
    fun setCustomDeletedCallback(customDeletedCallback: CustomDeletedCallback){
        mListener = customDeletedCallback
    }
}