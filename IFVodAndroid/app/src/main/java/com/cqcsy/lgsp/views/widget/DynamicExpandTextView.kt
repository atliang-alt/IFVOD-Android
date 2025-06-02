package com.cqcsy.lgsp.views.widget

import android.content.Context
import android.os.Build
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.utils.SpanStringUtils

/**
 * 自定义TextView
 * 支持点击全文、收起功能
 */
@Suppress("DEPRECATION")
class DynamicExpandTextView : AppCompatTextView {

    /**
     * TextView可展示宽度，包含paddingLeft和paddingRight
     */
    private var initWidth = 0

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
    }

    fun isOverSize(originalText: CharSequence): Boolean {
        val tempText = charSequenceToSpannable(originalText)
        val layout = createStaticLayout(tempText)
        return layout.lineCount > maxLines

    }

    /**
     * @param spannable
     * @return
     */
    private fun createStaticLayout(spannable: SpannableStringBuilder): Layout {
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

}