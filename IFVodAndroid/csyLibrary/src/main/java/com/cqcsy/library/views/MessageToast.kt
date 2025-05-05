package com.cqcsy.library.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.library.R
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.utils.ImageUtil
import com.hjq.xtoast.XToast
import kotlinx.android.synthetic.main.layout_new_message.view.*
import java.util.*


object MessageToast {
    var msgQueue: Stack<ChatMessageBean> = Stack()
    var mToast: XToast<*>? = null
    var hasShowed = false   // 本次启动是否已经显示，并且关闭

    //    var hasCheckedFloatWindowPermission: Boolean = false
    const val SHOW_TIME = 15 * 1000

    fun reset() {
        hasShowed = false
        clear()
    }

    private fun clear() {
        msgQueue.clear()
        mToast?.cancel()
        mToast = null
    }

    fun cancel() {
        mToast?.cancel()
    }

    fun showMessage(context: Activity, message: ChatMessageBean) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!hasCheckedFloatWindowPermission && !PermissionUtils.isGrantedDrawOverlays()) {
//                hasCheckedFloatWindowPermission = true
//                showPermission(context, message)
//                return
//            }
//        }
        addMessage(context, message)
    }

    private fun addMessage(context: Activity, message: ChatMessageBean) {
        if (hasShowed && message.type != 1) {
            return
        }
        val searchList = msgQueue.filter { it.fromUid == message.fromUid }
        if (searchList.isEmpty()) {
            msgQueue.push(message)
        } else {
            searchList[0].messageCount++
        }
        show(context)
    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    private fun showPermission(context: Activity, message: ChatMessageBean) {
//        val dialog = TipsDialog(context)
//        dialog.setMsg(R.string.set_permission_message)
//        dialog.setLeftListener(R.string.cancel) { dialog.dismiss() }
//        dialog.setRightListener(R.string.set_permission) {
//            PermissionUtils.requestDrawOverlays(object : PermissionUtils.SimpleCallback {
//                override fun onGranted() {
//                    dialog.dismiss()
//                    addMessage(context, message)
//                }
//
//                override fun onDenied() {
//                    dialog.dismiss()
//                    addMessage(context, message)
//                }
//
//            })
//        }
//        dialog.show()
//    }

    private fun show(context: Activity) {
        if (msgQueue.empty()) {
            mToast = null
        } else {
            if (mToast != null) {
                setView(context, mToast!!.contentView)
                mToast!!.setDuration(SHOW_TIME)
                return
            }
            val view = View.inflate(context, R.layout.layout_new_message, null)
            setView(context, view)

            mToast = XToast<XToast<*>>(context)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasCheckedFloatWindowPermission) {
//                    if (PermissionUtils.isGrantedDrawOverlays()) {
//                        XToast<XToast<*>>(context.application)
//                    } else {
//                        XToast<XToast<*>>(context)
//                    }
//                } else {
//                    XToast<XToast<*>>(context)
//                }
            mToast!!.setContentView(view)
                .setOutsideTouchable(true)
                .setDuration(SHOW_TIME)
                .setGravity(Gravity.RIGHT or Gravity.BOTTOM)
                .setYOffset(SizeUtils.dp2px(65f))
                .setOnToastListener(object : XToast.OnToastListener {
                    override fun onShow(toast: XToast<*>?) {
                    }

                    override fun onDismiss(toast: XToast<*>?) {
                        mToast = null
                    }

                })
                .setOnClickListener(R.id.cancel) { toast, view ->
                    clear()
                    hasShowed = true
                }.setOnClickListener(R.id.message) { toast, view ->
                    val singleUser = checkSingleUser()
                    val targetClass: Class<*>? =
                        Class.forName(if (singleUser) "com.cqcsy.lgsp.upper.chat.ChatActivity" else "com.cqcsy.lgsp.main.mine.PrivateMessageActivity")
                    if (targetClass != null) {
                        val intent = Intent(context, targetClass)
                        if (singleUser) {
                            val message = msgQueue[0]
                            intent.putExtra("nickName", message.nickname)
                            intent.putExtra("userImage", message.avatar)
                            intent.putExtra("userId", message.fromUid.toString())
                        }
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                    reset()
                }
            mToast!!.show()
        }
    }

    private fun checkSingleUser(): Boolean {
        if (msgQueue.size == 1) {
            return true
        } else {
            var lastUserId = 0
            for (message in msgQueue.iterator()) {
                if (lastUserId == 0) {
                    lastUserId = message.fromUid
                } else if (lastUserId != message.fromUid) {
                    return false
                }
            }
        }
        return true
    }

    private fun setView(context: Context, view: View) {
        view.imageContainer.removeAllViews()
        if (msgQueue.size > 1) {
            val imageView = ImageView(context)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER
            imageView.setImageResource(R.mipmap.message_image_bg)
            view.imageContainer.addView(imageView, params)

            addUserImage(context, view.imageContainer, msgQueue.peek())


            val userNumber = TextView(context)
            userNumber.text = "+${msgQueue.size}"
            userNumber.setTextColor(Color.WHITE)
            userNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            view.imageContainer.addView(userNumber, params)
        } else {
            addUserImage(context, view.imageContainer, msgQueue.peek())
        }
        var number = 0
        msgQueue.forEach { number += it.messageCount }
        val layoutParams = view.totalNumber.layoutParams as LinearLayout.LayoutParams
        when (number) {
            in 0..9 -> {
                view.totalNumber.setBackgroundResource(R.drawable.red_circle_bg)
                view.totalNumber.text = number.toString()
                layoutParams.width = SizeUtils.dp2px(14f)
            }

            in 10..99 -> {
                view.totalNumber.setBackgroundResource(R.drawable.red_round_bg)
                view.totalNumber.text = number.toString()
                layoutParams.width = SizeUtils.dp2px(20f)
            }

            else -> {
                view.totalNumber.setBackgroundResource(R.drawable.red_round_bg)
                view.totalNumber.text = "···"
                layoutParams.width = SizeUtils.dp2px(20f)
            }
        }
        view.totalNumber.layoutParams = layoutParams
    }

    private fun addUserImage(context: Context, container: FrameLayout, message: ChatMessageBean) {
        val imageView = ImageView(context)
        val size = SizeUtils.dp2px(28f)
        val params = FrameLayout.LayoutParams(size, size)
        container.addView(imageView, params)
        ImageUtil.loadCircleImage(context, message.avatar, imageView)
    }

}