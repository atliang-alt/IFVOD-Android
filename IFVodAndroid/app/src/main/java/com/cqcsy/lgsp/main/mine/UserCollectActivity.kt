package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.record.AlbumRecordFragment
import com.cqcsy.lgsp.record.DynamicRecordFragment
import com.cqcsy.lgsp.record.RecordListener
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_record.*
import org.json.JSONObject

/**
 * 我的收藏
 */
class UserCollectActivity : NormalActivity(), RecordListener {
    var isEdit = false
    private val episodePosition = 0
    private val shortPosition = 1
    private val dynamicPosition = 2
    private val albumPosition = 3
    var editTab = episodePosition
    private var episodeFragment: EpisodeFragment? = null
    private var shortFragment: ShortVideoFragment? = null
    private var dynamicFragment: DynamicRecordFragment? = null
    private var albumFragment: AlbumRecordFragment? = null

    override fun getContainerView(): Int {
        return R.layout.activity_record
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.my_collection)
        setupTab()
    }

    private fun setupTab() {
        val tabList = StringUtils.getStringArray(R.array.record_tab_text)
        viewPager.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount(): Int {
                return tabList.size
            }

            override fun getItem(position: Int): Fragment {
                var fragment: Fragment? = when (position) {
                    episodePosition -> episodeFragment
                    shortPosition -> shortFragment
                    dynamicPosition -> dynamicFragment
                    albumPosition -> albumFragment
                    else -> null
                }
                if (fragment == null) {
                    fragment = createFragment(position)
                }
                return fragment
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return tabList[position]
            }
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                editTab = position
                if (!isEdit) {
                    val data = when (editTab) {
                        episodePosition -> episodeFragment?.getDataList()
                        shortPosition -> shortFragment?.getDataList()
                        dynamicPosition -> dynamicFragment?.getDataList()
                        albumPosition -> albumFragment?.getDataList()
                        else -> episodeFragment?.getDataList()
                    }
                    if (data?.size == 0) {
                        setRightTextVisible(View.GONE)
                    } else {
                        setRightText(R.string.edit)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
        headerTab.setupWithViewPager(viewPager)
        viewPager.offscreenPageLimit = tabList.size
    }

    private fun createFragment(position: Int): BaseFragment {
        val bundle = Bundle()
        var fragment: BaseFragment? = null
        when (position) {
            episodePosition -> {
                episodeFragment = null
                fragment = EpisodeFragment()
                fragment.setRecordListener(this)
                episodeFragment = fragment
            }
            shortPosition -> {
                shortFragment = null
                fragment = ShortVideoFragment()
                fragment.setRecordListener(this)
                shortFragment = fragment
            }
            dynamicPosition -> {
                dynamicFragment = null
                fragment = DynamicRecordFragment()
                bundle.putBoolean("isRecord", false)
                fragment.arguments = bundle
                fragment.setRecordListener(this)
                dynamicFragment = fragment
            }
            albumPosition -> {
                albumFragment = null
                fragment = AlbumRecordFragment()
                bundle.putBoolean("isRecord", false)
                fragment.arguments = bundle
                fragment.setRecordListener(this)
                albumFragment = fragment
            }
            else -> Fragment()
        }
        return fragment!!
    }

    override fun onRightClick(view: View) {
        if (isEdit) {
            cancelEditState()
        } else {
            setEditState()
        }
    }

    private fun setEditState() {
        if (editContent.visibility == View.VISIBLE) {
            return
        }
        isEdit = true
        setRightText(R.string.cancel)
        viewPager.setIsSlide(true)
        editContent.visibility = View.VISIBLE
        editTab = headerTab.selectedTabPosition
        editText.visibility = View.VISIBLE
        headerTab.visibility = View.GONE
        when (editTab) {
            episodePosition -> editText.text = StringUtils.getString(R.string.episode)
            shortPosition -> editText.text = StringUtils.getString(R.string.short_video)
            dynamicPosition -> editText.text = StringUtils.getString(R.string.dynamic)
            albumPosition -> editText.text = StringUtils.getString(R.string.album)
        }
        refreshFragment()
    }

    private fun cancelEditState(refresh: Boolean = true) {
        if (editContent.visibility == View.GONE) {
            return
        }
        isEdit = false
        viewPager.setIsSlide(false)
        setRightText(R.string.edit)
        editContent.visibility = View.GONE
        editText.visibility = View.GONE
        headerTab.visibility = View.VISIBLE
        if (refresh) {
            refreshFragment()
        }
    }

    private fun refreshFragment() {
        when (editTab) {
            episodePosition -> episodeFragment?.refreshList(isEdit)
            shortPosition -> shortFragment?.refreshList(isEdit)
            dynamicPosition -> dynamicFragment?.refreshList(isEdit)
            albumPosition -> albumFragment?.refreshList(isEdit)
        }
    }

    fun clearAll(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_all)
        tipsDialog.setMsg(R.string.clear_tips)
        tipsDialog.setLeftListener(R.string.think_again) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.clear) {
            tipsDialog.dismiss()
            clearAllRecord()
        }
        tipsDialog.show()
    }

    fun delete(view: View) {
        val data = when (editTab) {
            episodePosition -> episodeFragment?.selectedItem
            shortPosition -> shortFragment?.selectedItem
            dynamicPosition -> dynamicFragment?.selectedItem
            albumPosition -> albumFragment?.selectedItem
            else -> null
        }
        if (data.isNullOrEmpty()) {
            ToastUtils.showLong(R.string.select_data_tip)
            return
        }
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_record)
        tipsDialog.setMsg(R.string.delete_tips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            deleteRecord(data)
        }
        tipsDialog.show()
    }

    private fun clearAllRecord() {
        showProgressDialog()
        val params = HttpParams()
        when (editTab) {
            shortPosition -> {
                // 小视频
                params.put("DeleteType", 1)
                params.put("videoType", 3)
            }
            dynamicPosition -> {
                // 动态
                params.put("DeleteType", 4)
            }
            albumPosition -> {
                // 相册
                params.put("DeleteType", 3)
            }
            else -> {
                params.put("DeleteType", 2)
            }
        }
        HttpRequest.get(RequestUrls.CLEAR_COLLECTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                cancelEditState(false)
                resetList(true, ArrayList<Any>())
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }

    override fun <T> onLoadFinish(index: Int, list: MutableList<T>) {
        if (editTab != index) {
            return
        }
        if (list.size == 0) {
            setRightTextVisible(View.GONE)
        } else {
            setRightText(R.string.edit)
        }
    }

    override fun onDataEmpty(index: Int) {
        if (editTab != index) {
            return
        }
        cancelEditState(true)
        setRightTextVisible(View.GONE)
    }

    override fun <T> removeData(list: MutableList<T>) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.removeDataTitle)
        tipsDialog.setMsg(R.string.removeCollectTips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.remove) {
            tipsDialog.dismiss()
            deleteRecord(list)
        }
        tipsDialog.show()
    }

    override fun <T> checkAvailable(index: Int, bean: T) {
    }

    private fun <T> deleteRecord(selectItems: MutableList<T>) {
        showProgressDialog()
        val params = HttpParams()
        val ids = appendIds(selectItems)
        if (ids.isNotEmpty()) {
            params.put("mediaKey", ids.toString().dropLast(1))
        }
        when (editTab) {
            shortPosition -> {
                // 小视频
                params.put("DeleteType", 1)
                params.put("videoType", 3)
            }
            dynamicPosition -> {
                // 动态
                params.put("DeleteType", 4)
            }
            albumPosition -> {
                // 相册
                params.put("DeleteType", 3)
            }
            else -> {
                params.put("DeleteType", 2)
            }
        }
        HttpRequest.get(RequestUrls.DELETE_COLLECTION, object : HttpCallBack<Any>() {
            override fun onSuccess(response: Any?) {
                dismissProgressDialog()
                resetList(false, selectItems)
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }

    private fun <T> appendIds(selectItems: MutableList<T>): StringBuffer {
        val ids = StringBuffer()
        for (bean in selectItems) {
            when (editTab) {
                episodePosition -> ids.append(bean.toString() + ",")
                shortPosition -> ids.append((bean as VideoBaseBean).mediaKey + ",")
                dynamicPosition -> ids.append((bean as DynamicBean).mediaKey + ",")
                albumPosition -> ids.append((bean as PicturesBean).mediaKey + ",")
            }
        }
        return ids
    }

    private fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        when (editTab) {
            episodePosition -> {
                episodeFragment?.resetList(isClear, list as MutableList<String>)
            }
            shortPosition -> {
                shortFragment?.resetList(isClear, list)
            }
            dynamicPosition -> {
                dynamicFragment?.resetList(isClear, list)
            }
            albumPosition -> {
                albumFragment?.resetList(isClear, list)
            }
        }
        if (isClear) {
            cancelEditState()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isEdit) {
                cancelEditState()
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}