package com.cqcsy.lgsp.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.bean.net.NavigationBarNetBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.TabChangeEvent
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.network.HttpRequest
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_home.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 首页
 */
class HomeFragment : BaseFragment(), View.OnClickListener {
    private var classifyList: MutableList<NavigationBarBean> = ArrayList()
    private var localData: MutableList<NavigationBarBean> = ArrayList()
    private val fragmentContainer = HashMap<Int, Fragment>()

    private var categoryId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTopPadding()
        initData()
    }

    private fun initData() {
        // 获取本地数据
        val local = SPUtils.getInstance().getString(Constant.KEY_NAVIGATION_BAR)
        if (!local.isNullOrEmpty()) {
            localData = Gson().fromJson(local, object : TypeToken<List<NavigationBarBean>>() {}.type)
            classifyList.addAll(localData)
            setTabPager()
        }
        getHttpData()
    }

    private fun setTabPager() {
        if (fragmentContainer.isNotEmpty()) {
            fragmentContainer.forEach {
                it.value.onDestroy()
            }
            fragmentContainer.clear()
        }
        viewPager.adapter = object :
            FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                var fragment = fragmentContainer[position]
                if (fragment == null) {
                    fragment = createTabFragment(position)
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
                tab?.customView = null
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView = LayoutInflater.from(context).inflate(R.layout.layout_tab_text, null) as TextView
                textView.text = tab?.text
                tab?.customView = textView

                setChildHidden(false)
                NormalUtil.clearTabLayoutTips(tabLayout)
            }
        })
//        viewPager.offscreenPageLimit = classifyList.size
        tabLayout.setupWithViewPager(viewPager)
        if (categoryId >= 0) {
            jumpAppointFragment(categoryId)
        }
    }

    private fun createTabFragment(position: Int): Fragment {
        val fragment: Fragment
        val classifyNetBean = classifyList[position]
        when (classifyNetBean.type) {
            // 推荐
            0 -> fragment = RecommendFragment()
            // 电视剧、电影、综艺、动漫、体育、纪录片
            1 -> {
                fragment = MovieFragment()
                val bundle = Bundle()
                bundle.putString("categoryId", classifyNetBean.categoryId)
                bundle.putString("categoryName", classifyNetBean.name)
                fragment.arguments = bundle
            }
            // 小视频 新闻
            2 -> {
                fragment = if (classifyNetBean.styleType == 1) {
                    NewsListFragment()
                } else {
                    NewsFragment()
                }
                val bundle = Bundle()
                bundle.putSerializable("navigation", classifyNetBean)
                fragment.arguments = bundle
            }

            else -> {
                fragment = Fragment()
            }
        }
        return fragment
    }

    /**
     * 获取导航分类信息
     */
    private fun getHttpData() {
        HttpRequest.post(
            RequestUrls.HOME_NAVIGATION_BAR,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgress()
                    if (response == null) {
                        if (localData.isNullOrEmpty()) {
                            showEmpty()
                        }
                        return
                    }
                    parseData(response)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgress()
                    if (localData.isNullOrEmpty()) {
                        showFailed()
                    }
                }
            }, tag = this
        )
    }

    /**
     * 解析数据
     */
    private fun parseData(jsonObject: JSONObject) {
        val navigationBarNetBean =
            Gson().fromJson(jsonObject.toString(), NavigationBarNetBean::class.java)
        val versionNumb = navigationBarNetBean.versionNo
        // 获取本地的存储的版本号
        val localVersion = SPUtils.getInstance().getString(Constant.KEY_NAVIGATION_VERSION)
        if (localVersion != versionNumb) {
            classifyList.clear()
            classifyList.addAll(navigationBarNetBean.list)
            tabLayout.removeAllTabs()
            tabLayout.clearOnTabSelectedListeners()
            viewPager.adapter = null
            setTabPager()
            SPUtils.getInstance().put(Constant.KEY_NAVIGATION_BAR, Gson().toJson(classifyList))
            SPUtils.getInstance().put(Constant.KEY_NAVIGATION_VERSION, versionNumb)
        }
    }

    /**
     * 跳转到指定的fragment
     */
    private fun jumpAppointFragment(type: Int) {
        for (i in classifyList.indices) {
            if (type.toString() == classifyList[i].categoryId) {
                viewPager.currentItem = i
                break
            }
        }
    }

    private fun showProgress() {
        if (isSafe()) {
            homeContainer.visibility = View.GONE
            statusView?.showProgress()
        }
    }

    private fun dismissProgress() {
        if (isSafe()) {
            homeContainer.visibility = View.VISIBLE
            statusView?.dismissProgress()
        }
    }

    /**
     * 请求失败页面
     */
    private fun showFailed() {
        if (isSafe()) {
            homeContainer.visibility = View.GONE
            statusView?.showFailed(this)
        }
    }

    /**
     * 获取数据为空
     */
    private fun showEmpty() {
        if (isSafe()) {
            homeContainer.visibility = View.GONE
            statusView?.showEmpty()
        }
    }

    override fun onClick(v: View?) {
        showProgress()
        getHttpData()
    }

    override fun onVisible() {
        super.onVisible()
        val normalColor = ColorUtils.getColor(R.color.grey)
        val selectedColor = ColorUtils.getColor(R.color.word_color_2)
        tabLayout?.setTabTextColors(normalColor, selectedColor)
        if (tabLayout?.getTabAt(tabLayout.selectedTabPosition)?.customView != null) {
            (tabLayout.getTabAt(tabLayout.selectedTabPosition)?.customView as TextView).setTextColor(
                selectedColor
            )
        }
        setChildHidden(false)
    }

    override fun onInvisible() {
        super.onInvisible()
        setChildHidden(true)
    }

    private fun setChildHidden(hidden: Boolean) {
        fragmentContainer.forEach {
            if (hidden) {
                // 不可见都隐藏
                it.value.onHiddenChanged(hidden)
            } else {
                // 可见时判定是否当前选中tab，仅选中tab可见
                if (it.key != tabLayout.selectedTabPosition) {
                    it.value.onHiddenChanged(true)
                } else {
                    it.value.onHiddenChanged(false)
                }
            }
        }
    }

    private fun setTopPadding() {
        statusBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(requireContext())
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCategoryChange(event: TabChangeEvent) {
        if (event.parentPosition == 0) {
            if (classifyList.isNullOrEmpty()) {
                categoryId = event.childCategoryId
            } else {
                jumpAppointFragment(event.childCategoryId)
            }
        }
    }
}