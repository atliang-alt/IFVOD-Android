package com.cqcsy.lgsp.main.find

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.bean.net.NavigationBarNetBean
import com.cqcsy.lgsp.event.AttentionUnreadStatus
import com.cqcsy.lgsp.event.ReadNewRecommendEvent
import com.cqcsy.lgsp.main.home.NewsFragment
import com.cqcsy.lgsp.main.home.NewsListFragment
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.StatusBarUtil
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.layout_find.*
import kotlinx.android.synthetic.main.layout_find.statusBar
import kotlinx.android.synthetic.main.layout_find.tabLayout
import kotlinx.android.synthetic.main.layout_tab_red_text.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*

/**
 * 发现
 */
class FindFragment : BaseFragment() {
    private val classifyList: MutableList<NavigationBarBean> = ArrayList()
    private val fragmentContainer = WeakHashMap<Int, Fragment>()
//    private var isPausedByOnPause = false
//    private var isToFull = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_find, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTopPadding()
        setLocalCacheTab()
        getTabs()
    }

//    override fun onVisible() {
//        super.onVisible()
//        setCurrentHidden(false)
//    }

//    override fun onInvisible() {
//        super.onInvisible()
//        setCurrentHidden(true)
//    }

//    private fun setCurrentHidden(hidden: Boolean) {
//        fragmentContainer.forEach {
//            if (hidden) {
//                // 不可见都隐藏
//                it.value.onHiddenChanged(hidden)
//            } else {
//                // 可见时判定是否当前选中tab，仅选中tab可见
//                if (it.key != tabLayout.selectedTabPosition) {
//                    it.value.onHiddenChanged(true)
//                } else {
//                    it.value.onHiddenChanged(false)
//                }
//            }
//        }
//    }

    private fun setLocalCacheTab() {
        val cacheTab = SPUtils.getInstance().getString(Constant.KEY_FIND_NAVIGATION_TAB)
        if (!cacheTab.isNullOrEmpty()) {
            val listTab = GsonUtils.fromJson<List<NavigationBarBean>>(
                cacheTab,
                object : TypeToken<ArrayList<NavigationBarBean>>() {}.type
            )
            classifyList.addAll(listTab)
            setTabPager()
        }
    }

    private fun setTabPager() {
        if (fragmentContainer.isNotEmpty()) {
            fragmentContainer.forEach {
                it.value.onDestroy()
            }
            fragmentContainer.clear()
        }
        smallVideoPager.adapter = object :
            FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                var fragment = fragmentContainer[position]
                if (fragment == null) {
                    fragment = createFragment(classifyList[position])
                    fragmentContainer[position] = fragment
                }
                return fragment
            }

            override fun getCount(): Int {
                return classifyList.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return classifyList[position].name
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//                super.destroyItem(container, position, `object`)
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if (tab?.customView != null) {
                    val textView: TextView = tab.customView!!.findViewById(R.id.tabText)
                    textView.setTextColor(ColorUtils.getColor(R.color.grey))
                    textView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                GSYVideoManager.releaseAllVideos()
                if (tab?.customView != null) {
                    val textView: TextView = tab.customView!!.findViewById(R.id.tabText)
                    textView.setTextColor(ColorUtils.getColor(R.color.word_color_2))
                    textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
                }
//                setCurrentHidden(false)
                NormalUtil.clearTabLayoutTips(tabLayout)
                if (isEnableAttentionRefresh()) {
                    setUnreadStatus()
                }
            }
        })
        tabLayout.setupWithViewPager(smallVideoPager)
        customTabView()
    }

    private fun customTabView() {
        for (i in classifyList.indices) {
            val it = classifyList[i]
            val tab = tabLayout.getTabAt(i)
            val customView = LayoutInflater.from(context).inflate(R.layout.layout_tab_red_text, null)
            customView.setOnClickListener {
                smallVideoPager.currentItem = i
            }
            customView.tabText.text = it.name
            if (i == 0) {
                customView.tabText.setTextColor(ColorUtils.getColor(R.color.word_color_2))
                customView.tabText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            if (i == 1 && GlobalValue.userInfoBean?.newWorks == true) {
                customView.setPadding(SizeUtils.dp2px(5f), 0, 0, 0)
                customView.tabRed.visibility = View.VISIBLE
            } else {
                customView.setPadding(0, 0, 0, 0)
                customView.tabRed.visibility = View.GONE
            }
            tab?.customView = customView
        }
    }

    private fun createFragment(tabItem: NavigationBarBean): Fragment {
        val fragment = when (tabItem.styleType) {
            FindType.TYPE_ATTENTION -> {
                AttentionBaseFragment()
            }

            FindType.TYPE_DYNAMIC_PICTURE,
            FindType.TYPE_RECOMMEND -> {
                AttentionDataListFragment()
            }

            FindType.TYPE_SHORT_VIDEO_LIST -> {
                NewsListFragment()
            }

            else -> {
                NewsFragment()
            }
        }
        val bundle = Bundle()
        bundle.putSerializable("navigation", tabItem)
        bundle.putBoolean("isFromFind", true)
        fragment.arguments = bundle
        return fragment
    }

    private fun getTabs() {
        HttpRequest.get(RequestUrls.VIDEO_TYPE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null) {
                    cacheTabs(response)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, tag = this)
    }

    private fun cacheTabs(jsonObject: JSONObject) {
        val navigationBarNetBean =
            Gson().fromJson(jsonObject.toString(), NavigationBarNetBean::class.java)
        val versionNumb = navigationBarNetBean.versionNo
        // 获取本地的存储的版本号
        val localVersion = SPUtils.getInstance().getString(Constant.KEY_FIND_NAVIGATION_VERSION)
        if (localVersion != versionNumb) {
            classifyList.clear()
            classifyList.addAll(navigationBarNetBean.list)
            tabLayout.removeAllTabs()
            tabLayout.clearOnTabSelectedListeners()
            smallVideoPager.adapter = null
            setTabPager()
            SPUtils.getInstance()
                .put(Constant.KEY_FIND_NAVIGATION_TAB, Gson().toJson(navigationBarNetBean.list))
            SPUtils.getInstance().put(Constant.KEY_FIND_NAVIGATION_VERSION, versionNumb)
        }
    }

    private fun setTopPadding() {
        statusBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(requireContext())
        )
    }

    /**
     * 判断当前选中的tab是不是关注，并且有新推荐
     */
    private fun isEnableAttentionRefresh(): Boolean {
        return tabLayout.selectedTabPosition == 1 && GlobalValue.userInfoBean?.newWorks == true
    }

    private fun setUnreadStatus() {
        val params = HttpParams()
        params.put("MsgType", 3)
        HttpRequest.post(RequestUrls.UPDATE_NEW_WORK_STATUS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                GlobalValue.userInfoBean?.newWorks = false
                EventBus.getDefault().post(AttentionUnreadStatus(false))
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setFindStatusPoint(event: AttentionUnreadStatus) {
        if (tabLayout != null && tabLayout.tabCount >= 2) {
            val customView = tabLayout.getTabAt(1)?.customView
            val redTag = customView?.findViewById<TextView>(R.id.tabRed)
            if (event.status) {
                customView?.setPadding(SizeUtils.dp2px(5f), 0, 0, 0)
                redTag?.visibility = View.VISIBLE
            } else {
                customView?.setPadding(0, 0, 0, 0)
                redTag?.visibility = View.GONE
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun readRecommend(event: ReadNewRecommendEvent) {
        if (isEnableAttentionRefresh()) {   // 切换回发现，且显示的是关注tab，设置已读并刷新数据，刷新数据在AttentionDataListFragment中单独处理
            setUnreadStatus()
        } else {    // 设置未读小红点
            if (tabLayout != null && tabLayout.tabCount >= 2) {
                val customView = tabLayout.getTabAt(1)?.customView
                val redTag = customView?.findViewById<TextView>(R.id.tabRed)
                customView?.setPadding(SizeUtils.dp2px(5f), 0, 0, 0)
                redTag?.visibility = View.VISIBLE
            }
        }
    }

    override fun onLoginOut() {
        if (tabLayout != null && tabLayout.tabCount >= 2) {
            tabLayout.getTabAt(1)?.customView?.findViewById<TextView>(R.id.tabRed)?.visibility = View.GONE
        }
    }

}