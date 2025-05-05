package com.cqcsy.lgsp.push

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Message
import android.util.SparseArray
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.helper.Logger
import java.util.*

/**
 * 处理tagalias相关的逻辑
 */
class TagAliasOperatorHelper private constructor() {
    private var context: Context? = null

    fun init(context: Context?) {
        if (context != null) {
            this.context = context.applicationContext
        }
    }

    private val setActionCache = SparseArray<Any>()
    operator fun get(sequence: Int): Any {
        return setActionCache[sequence]
    }

    fun remove(sequence: Int): Any {
        return setActionCache[sequence]
    }

    fun put(sequence: Int, tagAliasBean: Any) {
        setActionCache.put(sequence, tagAliasBean)
    }

    private val delaySendHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                DELAY_SEND_ACTION -> if (msg.obj != null && msg.obj is TagAliasBean) {
                    Logger.i(TAG, "on delay time")
                    sequence++
                    val tagAliasBean = msg.obj as TagAliasBean
                    setActionCache.put(sequence, tagAliasBean)
                    if (context != null) {
                        handleAction(
                            context,
                            sequence,
                            tagAliasBean
                        )
                    } else {
                        Logger.e(
                            TAG,
                            "#unexcepted - context was null"
                        )
                    }
                } else {
                    Logger.w(
                        TAG,
                        "#unexcepted - msg obj was incorrect"
                    )
                }
                DELAY_SET_MOBILE_NUMBER_ACTION -> if (msg.obj != null && msg.obj is String) {
                    Logger.i(TAG, "retry set mobile number")
                    sequence++
                    val mobileNumber = msg.obj as String
                    setActionCache.put(sequence, mobileNumber)
                    if (context != null) {
                        handleAction(
                            context,
                            sequence,
                            mobileNumber
                        )
                    } else {
                        Logger.e(
                            TAG,
                            "#unexcepted - context was null"
                        )
                    }
                } else {
                    Logger.w(
                        TAG,
                        "#unexcepted - msg obj was incorrect"
                    )
                }
            }
        }
    }

    fun handleAction(
        context: Context?,
        sequence: Int,
        mobileNumber: String
    ) {
        put(sequence, mobileNumber)
        Logger.d(
            TAG,
            "sequence:$sequence,mobileNumber:$mobileNumber"
        )
        JPushInterface.setMobileNumber(context, sequence, mobileNumber)
    }

    /**
     * 处理设置tag
     */
    fun handleAction(
        context: Context?,
        sequence: Int,
        tagAliasBean: TagAliasBean?
    ) {
        init(context)
        if (tagAliasBean == null) {
            Logger.w(TAG, "tagAliasBean was null")
            return
        }
        put(sequence, tagAliasBean)
        if (tagAliasBean.isAliasAction) {
            when (tagAliasBean.action) {
                ACTION_GET -> JPushInterface.getAlias(
                    context,
                    sequence
                )
                ACTION_DELETE -> JPushInterface.deleteAlias(
                    context,
                    sequence
                )
                ACTION_SET -> JPushInterface.setAlias(
                    context,
                    sequence,
                    tagAliasBean.alias
                )
                else -> {
                    Logger.w(TAG, "unsupport alias action type")
                    return
                }
            }
        } else {
            when (tagAliasBean.action) {
                ACTION_ADD -> JPushInterface.addTags(
                    context,
                    sequence,
                    tagAliasBean.tags
                )
                ACTION_SET -> JPushInterface.setTags(
                    context,
                    sequence,
                    tagAliasBean.tags
                )
                ACTION_DELETE -> JPushInterface.deleteTags(
                    context,
                    sequence,
                    tagAliasBean.tags
                )
                ACTION_CHECK -> {
                    //一次只能check一个tag
                    JPushInterface.checkTagBindState(
                        context,
                        sequence,
                        tagAliasBean.tags!!.toTypedArray()[0]
                    )
                }
                ACTION_GET -> JPushInterface.getAllTags(
                    context,
                    sequence
                )
                ACTION_CLEAN -> JPushInterface.cleanTags(
                    context,
                    sequence
                )
                else -> {
                    Logger.w(TAG, "unsupport tag action type")
                    return
                }
            }
        }
    }

    fun isConnected(context: Context): Boolean {
        val conn =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = conn.activeNetworkInfo
        return info != null && info.isConnected
    }

    private fun RetryActionIfNeeded(errorCode: Int, tagAliasBean: TagAliasBean?): Boolean {
        if (!isConnected(context!!)) {
            Logger.w(TAG, "no network")
            return false
        }
        //返回的错误码为6002 超时,6014 服务器繁忙,都建议延迟重试
        if (errorCode == 6002 || errorCode == 6014) {
            Logger.d(TAG, "need retry")
            if (tagAliasBean != null) {
                val message = Message()
                message.what = DELAY_SEND_ACTION
                message.obj = tagAliasBean
                delaySendHandler.sendMessageDelayed(message, 1000 * 60.toLong())
                val logs =
                    getRetryStr(tagAliasBean.isAliasAction, tagAliasBean.action, errorCode)

                Logger.i(TAG, logs)
                return true
            }
        }
        return false
    }

    private fun RetrySetMObileNumberActionIfNeeded(
        errorCode: Int,
        mobileNumber: String
    ): Boolean {
        if (!isConnected(context!!)) {
            Logger.w(TAG, "no network")
            return false
        }
        //返回的错误码为6002 超时,6024 服务器内部错误,建议稍后重试
        if (errorCode == 6002 || errorCode == 6024) {
            Logger.d(TAG, "need retry")
            val message = Message()
            message.what = DELAY_SET_MOBILE_NUMBER_ACTION
            message.obj = mobileNumber
            delaySendHandler.sendMessageDelayed(message, 1000 * 60.toLong())
            var str = "Failed to set mobile number due to %s. Try again after 60s."
            str = String.format(
                Locale.ENGLISH,
                str,
                if (errorCode == 6002) "timeout" else "server internal error”"
            )
            Logger.i(TAG, str)
            return true
        }
        return false
    }

    private fun getRetryStr(
        isAliasAction: Boolean,
        actionType: Int,
        errorCode: Int
    ): String {
        var str = "Failed to %s %s due to %s. Try again after 60s."
        str = String.format(
            Locale.ENGLISH,
            str,
            getActionStr(actionType),
            if (isAliasAction) "alias" else " tags",
            if (errorCode == 6002) "timeout" else "server too busy"
        )
        return str
    }

    private fun getActionStr(actionType: Int): String {
        when (actionType) {
            ACTION_ADD -> return "add"
            ACTION_SET -> return "set"
            ACTION_DELETE -> return "delete"
            ACTION_GET -> return "get"
            ACTION_CLEAN -> return "clean"
            ACTION_CHECK -> return "check"
        }
        return "unkonw operation"
    }

    fun onTagOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        val sequence = jPushMessage.sequence
        Logger.i(
            TAG,
            "action - onTagOperatorResult, sequence:" + sequence + ",tags:" + jPushMessage.tags
        )
        Logger.i(TAG, "tags size:" + jPushMessage.tags.size)
        init(context)
        //根据sequence从之前操作缓存中获取缓存记录
        val tagAliasBean = setActionCache[sequence] as TagAliasBean
        if (tagAliasBean == null) {
            Logger.i(TAG, "获取缓存记录失败")
            return
        }
        if (jPushMessage.errorCode == 0) {
            Logger.i(
                TAG,
                "action - modify tag Success,sequence:$sequence"
            )
            setActionCache.remove(sequence)
            val logs = getActionStr(tagAliasBean.action) + " tags success"
            Logger.i(TAG, logs)
        } else {
            var logs = "Failed to " + getActionStr(tagAliasBean.action) + " tags"
            if (jPushMessage.errorCode == 6018) { //tag数量超过限制,需要先清除一部分再add
                logs += ", tags is exceed limit need to clean"
            }
            logs += ", errorCode:" + jPushMessage.errorCode
            Logger.e(TAG, logs)
        }
    }

    fun onCheckTagOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        val sequence = jPushMessage.sequence
        Logger.i(
            TAG,
            "action - onCheckTagOperatorResult, sequence:" + sequence + ",checktag:" + jPushMessage.checkTag
        )
        init(context)
        //根据sequence从之前操作缓存中获取缓存记录
        val tagAliasBean = setActionCache[sequence] as TagAliasBean
        if (tagAliasBean == null) {
            Logger.i(TAG, "获取缓存记录失败")
            return
        }
        if (jPushMessage.errorCode == 0) {
            Logger.i(TAG, "tagBean:$tagAliasBean")
            setActionCache.remove(sequence)
            val logs =
                getActionStr(tagAliasBean.action) + " tag " + jPushMessage.checkTag + " bind state success,state:" + jPushMessage.tagCheckStateResult
            Logger.i(TAG, logs)
        } else {
            val logs =
                "Failed to " + getActionStr(tagAliasBean.action) + " tags, errorCode:" + jPushMessage.errorCode
            Logger.e(TAG, logs)
            if (!RetryActionIfNeeded(jPushMessage.errorCode, tagAliasBean)) {
            }
        }
    }

    fun onAliasOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        val sequence = jPushMessage.sequence
        Logger.i(
            TAG,
            "action - onAliasOperatorResult, sequence:" + sequence + ",alias:" + jPushMessage.alias
        )
        init(context)
        //根据sequence从之前操作缓存中获取缓存记录
        val tagAliasBean = setActionCache[sequence] as TagAliasBean
        if (tagAliasBean == null) {
            Logger.i(TAG, "获取缓存记录失败")
            return
        }
        if (jPushMessage.errorCode == 0) {
            Logger.i(
                TAG,
                "action - modify alias Success,sequence:$sequence"
            )
            setActionCache.remove(sequence)
            val logs = getActionStr(tagAliasBean.action) + " alias success"
            Logger.i(TAG, logs)
        } else {
            val logs =
                "Failed to " + getActionStr(tagAliasBean.action) + " alias, errorCode:" + jPushMessage.errorCode
            Logger.e(TAG, logs)
            if (!RetryActionIfNeeded(jPushMessage.errorCode, tagAliasBean)) {
            }
        }
    }

    //设置手机号码回调
    fun onMobileNumberOperatorResult(
        context: Context?,
        jPushMessage: JPushMessage
    ) {
        val sequence = jPushMessage.sequence
        Logger.i(
            TAG,
            "action - onMobileNumberOperatorResult, sequence:" + sequence + ",mobileNumber:" + jPushMessage.mobileNumber
        )
        init(context)
        if (jPushMessage.errorCode == 0) {
            Logger.i(
                TAG,
                "action - set mobile number Success,sequence:$sequence"
            )
            setActionCache.remove(sequence)
        } else {
            val logs =
                "Failed to set mobile number, errorCode:" + jPushMessage.errorCode
            Logger.e(TAG, logs)
            if (!RetrySetMObileNumberActionIfNeeded(
                    jPushMessage.errorCode,
                    jPushMessage.mobileNumber
                )
            ) {
                Logger.i(TAG, logs)
            }
        }
    }

    class TagAliasBean {
        var action = 0
        var tags: Set<String>? = null
        var alias: String? = null
        var isAliasAction = false
        override fun toString(): String {
            return "TagAliasBean{" +
                    "action=" + action +
                    ", tags=" + tags +
                    ", alias='" + alias + '\'' +
                    ", isAliasAction=" + isAliasAction +
                    '}'
        }
    }

    companion object {
        private const val TAG = "JIGUANG-TagAliasHelper"
        var sequence = 1
        /**增加 */
        const val ACTION_ADD = 1
        /**覆盖 */
        const val ACTION_SET = 2
        /**删除部分 */
        const val ACTION_DELETE = 3
        /**删除所有 */
        const val ACTION_CLEAN = 4
        /**查询 */
        const val ACTION_GET = 5
        const val ACTION_CHECK = 6
        const val DELAY_SEND_ACTION = 1
        const val DELAY_SET_MOBILE_NUMBER_ACTION = 2
        private var mInstance: TagAliasOperatorHelper? = null
        val instance: TagAliasOperatorHelper?
            get() {
                if (mInstance == null) {
                    synchronized(TagAliasOperatorHelper::class.java) {
                        if (mInstance == null) {
                            mInstance = TagAliasOperatorHelper()
                        }
                    }
                }
                return mInstance
            }
    }
}