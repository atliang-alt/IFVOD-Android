package com.cqcsy.lgsp.main.find

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.GsonUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.upper.UpperInfoBean
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 发现-关注：直接放在viewpager中，未登录、有关注、未关注在这里面切换
 */

class AttentionBaseFragment : BaseFragment() {

    var attentionList = ArrayList<UpperInfoBean>()

    var navigation: NavigationBarBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigation = arguments?.getSerializable("navigation") as NavigationBarBean?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.layout_base_fragment, container, false)
    }

    override fun onLazyAfterView() {
        super.onLazyAfterView()
        if (GlobalValue.isLogin()) {
            getUserAttentionList()
        } else {
            checkShowFragment()
        }
    }

    private fun checkShowFragment() {
        if (!GlobalValue.isLogin()) {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(
                R.id.childContainer,
                AttentionNotLoginFragment()
            ).commitAllowingStateLoss()
            return
        }
        val bundle = Bundle()
        bundle.putSerializable("navigation", navigation)
        bundle.putBoolean("isFromFind", true)
        val transaction = childFragmentManager.beginTransaction()
        var fragment: Fragment?
        if (attentionList.size > 0) {
            fragment = childFragmentManager.findFragmentByTag(AttentionFragment::class.java.simpleName)
            if (fragment == null) {
                fragment = AttentionFragment()
                bundle.putSerializable("attentionUserList", attentionList)
                fragment.arguments = bundle
                transaction.add(
                    R.id.childContainer,
                    fragment,
                    AttentionFragment::class.java.simpleName
                )
            }
        } else {
            fragment = childFragmentManager.findFragmentByTag(NoAttentionFragment::class.java.simpleName)
            if (fragment == null) {
                fragment = NoAttentionFragment()
                fragment.arguments = bundle
                transaction.add(
                    R.id.childContainer,
                    fragment,
                    NoAttentionFragment::class.java.simpleName
                )
            }
        }
        childFragmentManager.fragments.forEach {
            transaction.hide(it)
        }
        transaction.show(fragment).commitAllowingStateLoss()
    }

    override fun onLogin() {
        getUserAttentionList()
    }

    override fun onLoginOut() {
        attentionList.clear()
        checkShowFragment()
    }

    private fun getUserAttentionList() {
        if (!GlobalValue.isLogin()) return
        HttpRequest.get(RequestUrls.FIND_ATTENTION_USER, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response?.optJSONArray("list") != null) {
                    attentionList = GsonUtils.fromJson(
                        response.optJSONArray("list").toString(),
                        object : TypeToken<List<UpperInfoBean>>() {}.type
                    )
                    checkShowFragment()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                checkShowFragment()
            }
        }, tag = this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoActionResultEvent(event: VideoActionResultEvent) {
        if (event.type != 1) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> {
                val infoBean = UpperInfoBean()
                infoBean.id = event.id.toInt()
                infoBean.avatar = event.userLogo
                infoBean.nickName = event.userName
                attentionList.add(infoBean)
            }
            VideoActionResultEvent.ACTION_REMOVE -> {
                attentionList.removeAll {
                    it.id.toString() == event.id
                }
            }
            VideoActionResultEvent.ACTION_ADD_FINISH -> checkShowFragment()
            VideoActionResultEvent.ACTION_REFRESH -> getUserAttentionList()
        }
    }

}