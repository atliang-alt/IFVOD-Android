package com.cqcsy.lgsp.utils

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.adapter.EmotionGridViewAdapter
import com.cqcsy.lgsp.adapter.VipEmotionGridViewAdapter
import com.cqcsy.lgsp.views.dialog.CommentEditDialog
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.item_select_vip_emotion.view.*

/**
 * 点击表情的全局监听管理类
 */
class EmotionItemClickUtils {
    // 输入框
    private var mEditText: EditText? = null

    // 展示Vip图片的LinearLayout
    private var mVipLayout: LinearLayout? = null

    private var sendCommentListener: CommentEditDialog.SendCommentListener? = null

    // 存储Vip表情地址
    private var mVipList: MutableList<String> = ArrayList()

    private var mListener: OnEmotionChange? = null

    interface OnEmotionChange {
        fun onChange(emotionSize: Int)
    }

    /**
     * 绑定EditText
     */
    fun attachToEditText(editText: EditText) {
        mEditText = editText
    }

    /**
     * 绑定展示Vip图片的LinearLayout
     */
    fun attachToEditText(
        layout: LinearLayout?,
        vipList: MutableList<String>,
        sendCommentListener: CommentEditDialog.SendCommentListener?,
        listener: OnEmotionChange
    ) {
        mVipLayout = layout
        mVipList = vipList
        this.sendCommentListener = sendCommentListener
        this.mListener = listener
    }

    fun getOnItemClickListener(
        context: Context
    ): AdapterView.OnItemClickListener {
        return AdapterView.OnItemClickListener { parent, view, position, id ->
            val itemAdapter: Any = parent.adapter
            if (itemAdapter is EmotionGridViewAdapter) { // 点击的是表情
                if (position == itemAdapter.count - 1) { // 如果点击了最后一个回退按钮,则调用删除键事件
                    mEditText?.dispatchKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL
                        )
                    )
                } else { // 如果点击了表情,则添加到输入框中
                    val emotionName = itemAdapter.getItem(position)
                    // 获取当前光标位置,在指定位置上添加表情图片文本
                    val curPosition = mEditText?.selectionStart ?: 0
                    val sb =
                        StringBuilder(mEditText?.text.toString())
                    sb.insert(curPosition, emotionName)
                    // 特殊文字处理,将表情等转换一下
                    mEditText?.setText(
                        SpanStringUtils.getEmotionContent(
                            context, 14f, sb.toString()
                        )
                    )
                    // 将光标设置到新增完表情的右侧
                    val index = curPosition + emotionName.length
                    if (index <= 255) {
                        mEditText?.setSelection(curPosition + emotionName.length)
                    }
                }
            }
            if (mVipLayout == null && itemAdapter is VipEmotionGridViewAdapter) {
                if (!GlobalValue.isVipUser() && GlobalValue.userInfoBean!!.userExtension?.currentLevel ?: 0 < 3) {
                    sendCommentListener?.sendComment(1, "", ArrayList())
                    return@OnItemClickListener
                }
                sendCommentListener?.sendComment(0, itemAdapter.getItem(position), mVipList)
                return@OnItemClickListener
            }
            if (itemAdapter is VipEmotionGridViewAdapter && mVipLayout?.childCount ?: 0 < 4) {
                if (!GlobalValue.isVipUser() && GlobalValue.userInfoBean!!.userExtension?.currentLevel ?: 0 < 3) {
                    sendCommentListener?.sendComment(1, "", ArrayList())
                    return@OnItemClickListener
                }
                val view = View.inflate(context, R.layout.item_select_vip_emotion, null)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                view.vipEmotionClose.setOnClickListener {
                    mVipLayout?.removeView(view)
                    mVipList.remove(itemAdapter.getItem(position))
                    mListener?.onChange(mVipList.size)
                }
                ImageUtil.loadGif(
                    context,
                    EmotionUtils.getVipImgByName(itemAdapter.getItem(position)),
                    view.vipEmotionImage,
                    ImageView.ScaleType.CENTER_INSIDE,
                    true, defaultImage = 0
                )
                mVipList.add(itemAdapter.getItem(position))
                if (mVipList.size != 4) {
                    params.rightMargin = SizeUtils.dp2px(10f)
                }
                val screenWidth = ScreenUtils.getScreenWidth()
                params.width = (screenWidth - SizeUtils.dp2px(10f) * 3
                        - SizeUtils.dp2px(12f) * 2) / 4
                params.rightMargin = SizeUtils.dp2px(10f)
                view.layoutParams = params
                mVipLayout?.addView(view)
                mListener?.onChange(mVipList.size)
            }
        }
    }

    companion object {
        private var globalOnItemClickManagerUtils: EmotionItemClickUtils? = null

        @get:Synchronized
        val instance: EmotionItemClickUtils
            get() {
                if (globalOnItemClickManagerUtils == null) {
                    globalOnItemClickManagerUtils =
                        EmotionItemClickUtils()
                }
                return globalOnItemClickManagerUtils!!
            }
    }
}