package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.os.Build
import android.text.*
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.utils.SpanStringUtils
import com.cqcsy.lgsp.views.ClickableMovementMethod

/**
 * 自定义TextView
 * 支持点击全文、收起功能
 */
@Suppress("DEPRECATION")
class ExpandTextView : AppCompatTextView {
    private val ellipsisString = String(charArrayOf('\u2026'))
    private var isClosed = false
    private var mMaxLines = 3

    /**
     * TextView可展示宽度，包含paddingLeft和paddingRight
     */
    private var initWidth = 0
    private var mOpenSpannableStr: SpannableStringBuilder? = null
    private var mCloseSpannableStr: SpannableStringBuilder? = null
    private var mExpandable = false
    private var mOpenSuffixSpan: SpannableString? = null
    private var mCloseSuffixSpan: SpannableString? = null
    private var listener: OnExpandClickListener? = null
    private var expandText: String = "全文"
    private var foldText: String = "收起"
    private var mOriginalText: CharSequence? = null
    var mLinkHitFlag = false

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ExpandTextView)
        val expandText = a.getString(R.styleable.ExpandTextView_expand_text)
        val foldText = a.getString(R.styleable.ExpandTextView_fold_text)
        if (expandText != null) {
            this.expandText = expandText
        }
        if (foldText != null) {
            this.foldText = foldText
        }
        a.recycle()
        movementMethod = ClickableMovementMethod.getInstance()
        includeFontPadding = false
        updateOpenSuffixSpan()
        updateCloseSuffixSpan()
    }

    override fun hasOverlappingRendering(): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mLinkHitFlag = false
        super.onTouchEvent(event)
        return mLinkHitFlag
    }

    fun setExpandText(expandText: String) {
        this.expandText = expandText
    }

    fun setFoldText(foldText: String) {
        this.foldText = foldText
    }

    fun setMaxLine(maxLines: Int) {
        mMaxLines = maxLines
    }

    fun setOriginalText(originalText: CharSequence, expand: Boolean) {
        mOriginalText = originalText
        mExpandable = false
        val tempText = charSequenceToSpannable(mOriginalText!!)
        mOpenSpannableStr = charSequenceToSpannable(mOriginalText!!)
        val layout = createStaticLayout(tempText)
        mExpandable = layout.lineCount > mMaxLines
        if (mExpandable) {
            //拼接展开内容
            if (mCloseSuffixSpan != null) {
                mOpenSpannableStr?.append(mCloseSuffixSpan)
            }
            //计算原文截取位置
            val endPos = layout.getLineEnd(mMaxLines - 1)
            mCloseSpannableStr = if (mOriginalText!!.length <= endPos) {
                charSequenceToSpannable(mOriginalText!!)
            } else {
                charSequenceToSpannable(mOriginalText!!.subSequence(0, endPos - expandText.length))
            }
            var tempText2: SpannableStringBuilder = charSequenceToSpannable(mCloseSpannableStr!!).append(ellipsisString)
            if (mOpenSuffixSpan != null) {
                tempText2.append(mOpenSuffixSpan)
            }
            //循环判断，收起内容添加展开后缀后的内容
            var tempLayout = createStaticLayout(tempText2)
            while (tempLayout.lineCount > mMaxLines) {
                val lastSpace: Int = mCloseSpannableStr!!.length - 1
                if (lastSpace == -1) {
                    break
                }
                mCloseSpannableStr = if (mOriginalText!!.length <= lastSpace) {
                    charSequenceToSpannable(mOriginalText!!)
                } else {
                    charSequenceToSpannable(mOriginalText!!.subSequence(0, lastSpace - expandText.length))
                }
                tempText2 = charSequenceToSpannable(mCloseSpannableStr!!).append(ellipsisString)
                if (mOpenSuffixSpan != null) {
                    tempText2.append(mOpenSuffixSpan)
                }
                tempLayout = createStaticLayout(tempText2)
            }
            var lastSpace: Int = mCloseSpannableStr!!.length - mOpenSuffixSpan!!.length
            if (lastSpace >= 0 && mOriginalText!!.length > lastSpace) {
                val redundantChar = mOriginalText!!.subSequence(lastSpace, lastSpace + mOpenSuffixSpan!!.length - expandText.length)
                val offset = hasEnCharCount(redundantChar) - hasEnCharCount(mOpenSuffixSpan!!) + 1
                lastSpace = if (offset <= 0) lastSpace else lastSpace - offset
                mCloseSpannableStr = charSequenceToSpannable(mOriginalText!!.subSequence(0, lastSpace - expandText.length))
            }
            mCloseSpannableStr!!.append(ellipsisString)
            if (mOpenSuffixSpan != null) {
                mCloseSpannableStr!!.append(mOpenSuffixSpan)
            }
        } else {
            mCloseSpannableStr = mOpenSpannableStr
        }
        isClosed = expand
        text = if (expand) {
            mOpenSpannableStr
        } else {
            mCloseSpannableStr
        }
    }

    private fun hasEnCharCount(str: CharSequence): Int {
        var count = 0
        if (!TextUtils.isEmpty(str)) {
            for (element in str) {
                if (element in ' '..'~') {
                    count++
                }
            }
        }
        return count
    }

    private fun switchOpenClose() {
        if (mExpandable) {
            isClosed = !isClosed
            if (isClosed) {
                close()
            } else {
                open()
            }
        }
    }

    /**
     * 展开
     */
    fun open() {
        text = mOpenSpannableStr ?: ""
    }

    /**
     * 收起
     */
    fun close() {
        text = mCloseSpannableStr
    }

    /**
     * @param spannable
     * @return
     */
    private fun createStaticLayout(spannable: SpannableStringBuilder): Layout {
        if (initWidth == 0) {
            initWidth = measuredWidth
        }
        val contentWidth = initWidth - paddingLeft - paddingRight
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder = StaticLayout.Builder.obtain(
                spannable,
                0,
                spannable.length,
                paint,
                contentWidth
            )
            builder.setAlignment(Layout.Alignment.ALIGN_NORMAL)
            builder.setIncludePad(includeFontPadding)
            builder.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            builder.build()
        } else {
            StaticLayout(
                spannable, paint, contentWidth, Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier, lineSpacingExtra, includeFontPadding
            )
        }
    }

    /**
     * @param charSequence
     * @return
     */
    private fun charSequenceToSpannable(charSequence: CharSequence): SpannableStringBuilder {
        val spannable = SpannableStringBuilder()
        spannable.append(
            SpanStringUtils.getEmotionContent(
                context,
                SizeUtils.px2sp(textSize).toFloat(),
                charSequence.toString()
            )
        )
        return spannable
    }

    /**
     * 初始化TextView的可展示宽度
     *
     * @param width
     */
    fun initWidth(width: Int) {
        initWidth = width
    }

    /**
     * 更新展开后缀Spannable
     */
    private fun updateOpenSuffixSpan() {
        val openSuffix = " $expandText"
        if (TextUtils.isEmpty(openSuffix)) {
            mOpenSuffixSpan = null
            return
        }
        mOpenSuffixSpan = SpannableString(openSuffix)
        mOpenSuffixSpan?.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                switchOpenClose()
                listener?.onExpandStatusChange(true)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ColorUtils.getColor(R.color.blue)
                ds.isUnderlineText = false
            }
        }, 0, openSuffix.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    }

    /**
     * 更新收起后缀Spannable
     */
    private fun updateCloseSuffixSpan() {
        val closeSuffix = " $foldText"
        if (TextUtils.isEmpty(closeSuffix)) {
            mCloseSuffixSpan = null
            return
        }
        mCloseSuffixSpan = SpannableString(closeSuffix)
        mCloseSuffixSpan?.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                switchOpenClose()
                listener?.onExpandStatusChange(false)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ColorUtils.getColor(R.color.blue)
                ds.isUnderlineText = false
            }
        }, 1, closeSuffix.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun setOnExpandClickListener(l: OnExpandClickListener) {
        listener = l
    }

    interface OnExpandClickListener {

        fun onExpandStatusChange(expand: Boolean)
    }
}