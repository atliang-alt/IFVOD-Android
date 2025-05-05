package com.cqcsy.lgsp.views.emotion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.lgsp.utils.EmotionUtils
import kotlinx.android.synthetic.main.item_tab_view.view.*
import kotlinx.android.synthetic.main.layout_vip_emotion_fragment.*

/**
 * 私信表情、评论表情 Fragment页
 */
class MessageEmotionFragment : BaseFragment() {
    private var titleList: MutableList<String> = ArrayList()
    private var isEditVip: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_vip_emotion_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isEditVip = arguments?.getBoolean("isEditVip") ?: true
        initView()
    }

    private fun initView() {
        titleList = ArrayList()
        titleList.add(StringUtils.getString(R.string.emoji))
        if (isEditVip) {
            val keyList = EmotionUtils.vipEmotionMap.keys
            titleList.addAll(keyList)
        }
        setTabPager()
    }

    private fun setTabPager() {
//        vipEmotionViewPager.offscreenPageLimit = titleList.size
        vipEmotionViewPager.adapter = object :
            FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                val fragment: Fragment
                if (position == 0) {
                    fragment = EmotionFragment()
                } else {
                    fragment = VipEmotionTabFragment()
                    val bundle = Bundle()
                    bundle.putString("title", titleList[position])
                    fragment.arguments = bundle
                }
                return fragment
            }

            override fun getCount(): Int {
                return titleList.size
            }

            override fun getPageTitle(position: Int): CharSequence {
                return titleList[position]
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//                super.destroyItem(container, position, `object`)
            }
        }
        VipEmotionTabLayout.setupWithViewPager(vipEmotionViewPager)
        for (i in titleList.indices) {
            VipEmotionTabLayout.getTabAt(i)?.customView = getTabView(i != 0, titleList[i])
        }
    }

    private fun getTabView(isVip: Boolean, title: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_view, null)
        if (isVip) {
            view.vipImg.visibility = View.VISIBLE
        } else {
            view.vipImg.visibility = View.GONE
        }
        view.itemTitle.text = title
        return view
    }
}