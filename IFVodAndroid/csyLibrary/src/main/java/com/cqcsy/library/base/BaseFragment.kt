package com.cqcsy.library.base

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.cqcsy.library.event.LoginEvent
import com.gyf.immersionbar.components.ImmersionFragment
import com.lzy.okgo.OkGo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 所有fragment基类
 * 取消请求，屏幕适配
 */
abstract class BaseFragment : ImmersionFragment() {

    var mIsFragmentVisible = false
        get() = field

    override fun initImmersionBar() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        OkGo.getInstance().cancelTag(this)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        if (event.status) {
            onLogin()
        } else {
            onLoginOut()
        }
    }

    /**
     * 登陆成功处理页面调用
     */
    open fun onLogin() {

    }

    /**
     * 退出登陆处理
     */
    open fun onLoginOut() {

    }

    fun isSafe(): Boolean {
        return !(this.isRemoving || this.activity == null || this.isDetached || !this.isAdded || this.view == null)
    }

    override fun onVisible() {
        super.onVisible()
        mIsFragmentVisible = true
    }

    override fun onInvisible() {
        super.onInvisible()
        mIsFragmentVisible = false
    }

    /**
     * 下发生命周期事件
     */
    fun dispatch(event: Lifecycle.Event) {
        if (lifecycle is LifecycleRegistry) {
            (lifecycle as LifecycleRegistry).handleLifecycleEvent(event)
        }
    }
}