package com.cqcsy.lgsp.main.hot

import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.lgsp.bean.net.NavigationBarNetBean
import com.cqcsy.lgsp.event.TabChangeEvent
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_hot.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import kotlin.math.abs

/**
 * 热播
 */
class HotFragment : NormalFragment() {
    private var barList: MutableList<NavigationBarBean> = ArrayList()
    private var localData: MutableList<NavigationBarBean> = ArrayList()
    private val fragmentContainer = HashMap<Int, Fragment>()

    private var categoryId = -1

    override fun getContainerView(): Int {
        return R.layout.layout_hot
    }

    override fun onVisible() {
        super.onVisible()
        val normalColor = ColorUtils.getColor(R.color.grey)
        val selectedColor = ColorUtils.getColor(R.color.word_color_2)
        tabLayout.setTabTextColors(normalColor, selectedColor)
    }

    override fun initData() {
        if (SPUtils.getInstance().getBoolean(Constant.IS_NOTCH_IN_SCREEN)) {
            appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, p1 ->
                if (abs(p1) >= appbarLayout.totalScrollRange) {
                    appbarLayout.setPadding(0, SizeUtils.dp2px(20f), 0, 0)
                } else {
                    appbarLayout.setPadding(0, 0, 0, 0)
                }
            })
        }
        // 获取本地数据
        val local = SPUtils.getInstance().getString(Constant.KEY_HOT_BAR)
        if (!local.isNullOrEmpty()) {
            localData =
                Gson().fromJson(local, object : TypeToken<List<NavigationBarBean>>() {}.type)
            barList.addAll(localData)
//            viewPager.offscreenPageLimit = barList.size
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
                return barList.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return barList[position].name
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//                super.destroyItem(container, position, `object`)
            }
        }
        tabLayout.setupWithViewPager(viewPager)

        if (categoryId >= 0) {
            jumpAppointFragment(categoryId)
        }
    }

    private fun createTabFragment(position: Int): Fragment {
        val fragment = HotTabViewFragment()
        val bundle = Bundle()
        bundle.putString("categoryId", barList[position].categoryId)
        fragment.arguments = bundle
        return fragment
    }

    /**
     * 跳转到指定的fragment
     */
    private fun jumpAppointFragment(type: Int) {
        for (i in barList.indices) {
            if (type.toString() == barList[i].categoryId) {
                viewPager.currentItem = i
                break
            }
        }
    }

    /**
     * 获取热播导航分类信息
     */
    private fun getHttpData() {
        HttpRequest.post(
            RequestUrls.GET_HOT_BAR,
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
                    if (localData.isNullOrEmpty()) {
                        showFailed {
                            showProgress()
                            getHttpData()
                        }
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
        val localVersion = SPUtils.getInstance().getString(Constant.KEY_HOT_VERSION)
        if (localVersion != versionNumb) {
            barList.clear()
            barList.addAll(navigationBarNetBean.list)
            tabLayout.removeAllTabs()
            tabLayout.clearOnTabSelectedListeners()
            viewPager.adapter = null
            setTabPager()
            SPUtils.getInstance().put(Constant.KEY_HOT_BAR, Gson().toJson(barList))
            SPUtils.getInstance().put(Constant.KEY_HOT_VERSION, versionNumb)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCategoryChange(event: TabChangeEvent) {
        if (event.parentPosition == 3) {
            if (barList.isNullOrEmpty()) {
                categoryId = event.childCategoryId
            } else {
                jumpAppointFragment(event.childCategoryId)
            }
        }
    }
}