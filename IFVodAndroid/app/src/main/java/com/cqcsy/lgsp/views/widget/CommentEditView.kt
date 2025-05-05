package com.cqcsy.lgsp.views.widget

import android.content.ClipboardManager
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.utils.SpanStringUtils

/**
 * 兼容粘贴表情图片处理
 */
class CommentEditView : AppCompatEditText {

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipText = clip.primaryClip?.getItemAt(0)?.text.toString()
            val oldText = text.toString()
            var newText = oldText + clipText
            if (newText.length > 255) {
                newText = newText.substring(0, 254)
            }
            setText(SpanStringUtils.getEmotionContent(
                context,
                SizeUtils.px2sp(textSize).toFloat(),
                StringBuilder(newText).toString()
            ))
            setSelection(newText.length)
            return true
        } else {
            return super.onTextContextMenuItem(id)
        }
    }
}