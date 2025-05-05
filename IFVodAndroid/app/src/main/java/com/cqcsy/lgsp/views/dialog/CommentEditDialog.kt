package com.cqcsy.lgsp.views.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.DialogFragment
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.utils.EmotionItemClickUtils
import com.cqcsy.lgsp.views.emotion.MessageEmotionFragment
import kotlinx.android.synthetic.main.comment_edit_dialog.view.*
import java.lang.Exception

/**
 * 评论输入框Dialog
 * Emoji表情、Vip表情
 * isEditVip 是否可以输入VIP表情 默认true可以输入
 */
class CommentEditDialog(
    private var replyName: String?,
    private var sendCommentListener: SendCommentListener?,
    private var isEditVip: Boolean = true,
    var isPicture: Boolean = false
) : DialogFragment(), EmotionItemClickUtils.OnEmotionChange {

    /**
     * type: 0 发送，1 跳转Vip支付页
     */
    interface SendCommentListener {
        fun sendComment(type: Int, inputText: String, vipList: MutableList<String>)
    }

    private var contentView: View? = null

    private var emotionFragment: MessageEmotionFragment? = null

    // 存储Vip表情地址
    private var vipList: MutableList<String> = ArrayList()

    var isClickEmotion: Boolean = false
    var isClickVipEmotion: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.BarrageEditDialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        activity?.window?.let { window ->
            KeyboardUtils.registerSoftInputChangedListener(window) { height ->
                if (height <= 0) {
                    if (!isClickEmotion && !isClickVipEmotion) {
                        dismissAllowingStateLoss()
                    }
                } else {
                    isClickEmotion = false
                    isClickVipEmotion = false
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        contentView = View.inflate(activity, R.layout.comment_edit_dialog, null)
        // 设置宽度为屏宽, 靠近屏幕底部。
        val window = dialog?.window
        val lp = window!!.attributes
        lp.gravity = Gravity.BOTTOM // 紧贴底部
        lp.width = WindowManager.LayoutParams.MATCH_PARENT // 宽度持平
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.attributes = lp
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setCanceledOnTouchOutside(true) // 外部点击取消

        contentView?.commentEdit?.isFocusable = true
        contentView?.commentEdit?.isFocusableInTouchMode = true
        contentView?.commentEdit?.requestFocus()
        if (!replyName.isNullOrEmpty()) {
            contentView?.commentEdit?.hint =
                resources.getString(R.string.reply) + " " + replyName + ":"
        } else if (isPicture) {
            contentView?.commentEdit?.setHint(R.string.picture_dynamic_warning)
        }
        // 点击表情的全局监听管理类 绑定EditText
        EmotionItemClickUtils.instance.attachToEditText(contentView!!.commentEdit)
        // 点击VIP表情的全局监听管理类 绑定LinearLayout
        EmotionItemClickUtils.instance.attachToEditText(
            contentView!!.vipEmotionLayout,
            vipList,
            sendCommentListener, this
        )
        setClick()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ScreenUtils.getScreenWidth(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setClick() {
        contentView?.commentEdit?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                val tag = try {
                    contentView?.commentSend?.tag.toString().toInt()
                } catch (e: Exception) {
                    e.printStackTrace()
                    0
                }
                contentView?.commentSend?.isEnabled = s.isNotEmpty() || tag > 0
            }
        })
        contentView?.commentEdit?.setOnClickListener {
            hideEmotionFragment()
            contentView?.emojiImage?.isSelected = false
        }
        contentView?.commentSend?.setOnClickListener(View.OnClickListener {
            val input = contentView?.commentEdit?.text.toString().trim()
            if (TextUtils.isEmpty(input) && vipList.isNullOrEmpty()) {
                ToastUtils.showLong(R.string.replyTips)
                return@OnClickListener
            } else {
                sendCommentListener!!.sendComment(0, input, vipList)
                dialog?.dismiss()
            }
        })
        contentView?.emojiImage?.setOnClickListener {
            isClickEmotion = true
            if (contentView?.emojiImage?.isSelected == true) {
                contentView?.emojiImage?.isSelected = false
                Handler().postDelayed({ showSoftKeyBoard() }, 100)
                hideEmotionFragment()
            } else {
                contentView?.emojiImage?.isSelected = true
                addEmotionView()
                Handler().postDelayed({ hideSoftKeyBoard() }, 100)
            }
        }
    }

    /**
     * 添加表情包页面
     */
    private fun addEmotionView() {
        val transaction = childFragmentManager.beginTransaction()
        if (emotionFragment == null) {
            emotionFragment = MessageEmotionFragment()
            val bundle = Bundle()
            bundle.putBoolean("isEditVip", isEditVip)
            emotionFragment!!.arguments = bundle
            transaction.add(R.id.emojiContentLayout, emotionFragment!!)
        } else {
            transaction.show(emotionFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }

    /**
     * 隐藏普通表情包页面
     */
    private fun hideEmotionFragment() {
        if (emotionFragment != null) {
            childFragmentManager.beginTransaction().hide(emotionFragment!!)
                .commitAllowingStateLoss()
        }
    }

    private fun hideSoftKeyBoard() {
        dialog?.window?.let {
            KeyboardUtils.hideSoftInput(it)
        }
        /*  try {
              (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                  .hideSoftInputFromWindow(
                      contentView?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
                  )
          } catch (e: NullPointerException) {
          }*/
    }

    private fun showSoftKeyBoard() {
        isClickVipEmotion = false
        isClickEmotion = false
        contentView?.commentEdit?.let {
            KeyboardUtils.showSoftInput(it)
        }
        /*try {
            isClickVipEmotion = false
            isClickEmotion = false
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                contentView?.commentEdit, 0
            )
        } catch (e: NullPointerException) {
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (emotionFragment != null) {
            childFragmentManager.beginTransaction().remove(emotionFragment!!)
                .commitAllowingStateLoss()
        }
        activity?.window?.let {
            KeyboardUtils.unregisterSoftInputChangedListener(it)
        }
    }

    override fun onChange(emotionSize: Int) {
        contentView?.commentSend?.tag = emotionSize
        contentView?.commentSend?.isEnabled = emotionSize > 0 || !contentView?.commentEdit?.text.isNullOrEmpty()
    }
}