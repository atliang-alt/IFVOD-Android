package com.cqcsy.lgsp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.widget.TextView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.utils.EmotionUtils.getImgByName
import com.cqcsy.lgsp.utils.EmotionUtils.getLevelImgByName
import java.util.regex.Pattern

/**
 * 文本中的emoji字符处理为表情图片
 */
object SpanStringUtils {

    fun getEmotionContent(
        context: Context,
        textSizeSp: Float,
        source: String?
    ): SpannableString {
        if (source == null) {
            return SpannableString("")
        }
        val pxSize = SizeUtils.sp2px(textSizeSp)
        val spannableString = SpannableString(source)
        val res = context.resources
        val regexEmotion = "\\(:([\u4e00-\u9fa5\\w])+\\)"
        val patternEmotion = Pattern.compile(regexEmotion)
        val matcherEmotion = patternEmotion.matcher(spannableString)
        while (matcherEmotion.find()) { // 获取匹配到的具体字符
            val key = matcherEmotion.group()
            // 匹配字符串的开始位置
            val start = matcherEmotion.start()
            // 利用表情名字获取到对应的图片
            val imgRes = getImgByName(key)
            if (imgRes != -1) { //-1代表纯文本，没有图片
                // 压缩表情图片
                val size = pxSize * 13 / 10
                val bitmap = BitmapFactory.decodeResource(res, imgRes)
                val scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
                val span = ImageSpan(context, scaleBitmap)
                spannableString.setSpan(
                    span,
                    start,
                    start + key.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannableString
    }

    fun hasEmoji(targetString: String): Boolean {
        val regexEmotion = "\\(:([\u4e00-\u9fa5\\w])+\\)"
        val patternEmotion = Pattern.compile(regexEmotion)
        return patternEmotion.matcher(targetString).find()
    }

    /**
     * 处理转义字符
     */
    fun escapeExprSpecialWord(keyword: String): String {
        var backStr = keyword
        if (backStr.isNotEmpty()) {
            val fbsArr =
                arrayOf("\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|")
            fbsArr.forEach {
                if (backStr.contains(it)) {
                    backStr = backStr.replace(it, "\\" + it)
                }
            }
        }
        return backStr
    }

    /**
     * 直播间聊天文案
     */
    fun getVideoChatText(
        tv: TextView,
        context: Context,
        bean: BarrageBean
    ): SpannableStringBuilder {
        // 获取vip等级
        val nickName = " " + bean.nickName + "："
        val message = bean.contxt
        val gid = bean.gid
        var text = if (gid in 1..3) "(:level_$gid)" else ""
        val sp = SpannableStringBuilder(text)
        sp.append(nickName)
        //昵称文字替换颜色，将对应位置颜色修改
        sp.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.grey_2)),
            text.length, text.length + nickName.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        text += nickName
        sp.append(bean.contxt)
        sp.setSpan(
            ForegroundColorSpan(ColorUtils.getColor(R.color.grey_4)),
            text.length, text.length + message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val res = context.resources
        val regexEmotion = "\\(:([\u4e00-\u9fa5\\w])+\\)"
        val patternEmotion = Pattern.compile(regexEmotion)
        val matcherEmotion = patternEmotion.matcher(sp)
        while (matcherEmotion.find()) { // 获取匹配到的具体字符
            val key = matcherEmotion.group()
            // 匹配字符串的开始位置
            val start = matcherEmotion.start()
            // 利用表情名字获取到对应的图片
            var imgRes = getImgByName(key)
            if (imgRes == -1) {
                imgRes = getLevelImgByName(key)
            }
            if (imgRes != -1) { //-1代表纯文本，没有图片
                // 压缩表情图片
                val size = tv.textSize.toInt() * 13 / 10
                val bitmap = BitmapFactory.decodeResource(res, imgRes)
                val scaleBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
                val span = ImageSpan(context, scaleBitmap)
                sp.setSpan(
                    span,
                    start,
                    start + key.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return sp
    }

    fun isVipText(text: String): Boolean {
        if (text.length == 11 && text.startsWith("(:") && text.endsWith(")")) {
            return true
        }
        return false
    }
}