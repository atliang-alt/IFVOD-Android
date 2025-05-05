package com.cqcsy.lgsp.push

import android.content.Context
import cn.jpush.android.api.JPushInterface
import com.cqcsy.library.utils.GlobalValue

/**
 * 用户绑定推送
 */
object PushPresenter {

    fun bindTag(context: Context) {
        if(!GlobalValue.isLogin()) {
            return
        }
        val registerId = JPushInterface.getRegistrationID(context.applicationContext)
        if(registerId.isNullOrEmpty()) {
            return
        }
        JPushInterface.setAlias(context, GlobalValue.userInfoBean!!.id, "push_${GlobalValue.userInfoBean!!.id}")
        if(GlobalValue.isVipUser()) {
            JPushInterface.setTags(context, GlobalValue.userInfoBean!!.id, setOf("push_vip"))
        } else {
            JPushInterface.setTags(context, GlobalValue.userInfoBean!!.id, setOf("push_novip"))
        }
//        val params = HttpParams()
//        params.put("Tag", registerId)
//        HttpRequest.post(RequestUrls.PUSH_TAG_BIND, object:HttpCallBack<JSONObject>() {
//            override fun onSuccess(response: JSONObject?) {
//                Log.d("PushPresenter", " bind success $response")
//            }
//
//            override fun onError(response: String?, errorMsg: String?) {
//                Log.d("PushPresenter", " bind failed $response")
//            }
//
//        }, params)
    }
}