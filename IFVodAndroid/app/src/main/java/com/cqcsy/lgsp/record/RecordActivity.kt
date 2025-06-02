package com.cqcsy.lgsp.record

import android.content.Intent
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
import com.cqcsy.lgsp.bean.MovieModuleBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.VideoBaseBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.database.manger.WatchRecordManger
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_record.*
import org.json.JSONObject

/**
 * 观看记录
 */
class RecordActivity : NormalActivity(), RecordListener {
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
        setHeaderTitle(R.string.historic_records)
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
                fragment.setRecordListener(this)
                dynamicFragment = fragment
            }

            albumPosition -> {
                albumFragment = null
                fragment = AlbumRecordFragment()
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
        viewPager.setIsSlide(true)
        setRightText(R.string.cancel)
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
            if (GlobalValue.isLogin()) {
                clearAllRecord()
            } else {
                when (editTab) {
                    episodePosition -> WatchRecordManger.instance.deleteNotShortVideo()
                    shortPosition -> WatchRecordManger.instance.deleteAll(Constant.VIDEO_SHORT)
                    dynamicPosition -> DynamicRecordManger.instance.deleteType(0)
                    albumPosition -> DynamicRecordManger.instance.deleteType(1)
                }
                resetList(true, ArrayList<String>())
            }
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
            if (GlobalValue.isLogin()) {
                deleteRecord(data)
            } else {
                deleteDataBaseRecord(data)
                resetList(false, data)
            }
        }
        tipsDialog.show()
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
        tipsDialog.setMsg(R.string.removeDataTips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            deleteRecord(list)
        }
        tipsDialog.show()
    }

    private fun <T> removeData(bean: T) {
        val list: MutableList<T> = ArrayList()
        list.add(bean)
        removeData(list)
    }

    override fun <T> checkAvailable(index: Int, bean: T) {
        showProgressDialog()
        val params = HttpParams()
        val videoType = when (index) {
            episodePosition, shortPosition -> {
                params.put("mediaKey", (bean as VideoBaseBean).mediaKey)
                bean.videoType
            }

            dynamicPosition -> {
                params.put("mediaKey", (bean as DynamicBean).mediaKey)
                bean.videoType
            }

            albumPosition -> {
                params.put("mediaKey", (bean as PicturesBean).mediaKey)
                bean.videoType
            }

            else -> 0
        }
        params.put("videoType", videoType)
        HttpRequest.get(RequestUrls.CHECK_AVAILABLE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                val isUnAvailable = response?.optBoolean("isUnAvailable") ?: false
                if (isUnAvailable) {
                    removeData(bean)
                } else {
                    when (index) {
                        episodePosition -> episodeFragment?.startPlay(bean as MovieModuleBean)
                        shortPosition -> shortFragment?.startPlay(bean as ShortVideoBean)
                        dynamicPosition -> dynamicFragment?.startDynamic((bean as DynamicBean))
                        albumPosition -> albumFragment?.startIntent(bean as PicturesBean)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
            }
        }, params, this)
    }

    private fun clearAllRecord() {
        showProgressDialog()
        val params = HttpParams()
        when (editTab) {
            shortPosition -> {
                // 小视频
                params.put("isNormalVideo", 0)
                params.put("videoType", 3)
            }

            dynamicPosition -> {
                // 动态
                params.put("isNormalVideo", 3)
            }

            albumPosition -> {
                // 相册
                params.put("isNormalVideo", 2)
            }

            else -> {
                params.put("isNormalVideo", 1)
            }
        }
        HttpRequest.get(RequestUrls.CLEAR_RECORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                resetList(true, ArrayList<Any>())
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }

    private fun <T> deleteDataBaseRecord(data: MutableList<T>) {
        val list: MutableList<String> = ArrayList()
        when (editTab) {
            episodePosition, shortPosition -> {
                for (bean in data) {
                    list.add((bean as VideoBaseBean).mediaKey)
                }
                WatchRecordManger.instance.delete(list, 0)
            }

            dynamicPosition -> {
                for (bean in data) {
                    (bean as DynamicBean).mediaKey?.let { list.add(it) }
                }
                DynamicRecordManger.instance.delete(list)
            }

            albumPosition -> {
                for (bean in data) {
                    list.add((bean as PicturesBean).mediaKey)
                }
                DynamicRecordManger.instance.delete(list)
            }
        }
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
                params.put("videoType", 3)
            }

            dynamicPosition -> {
                // 动态
                params.put("videoType", 8)
            }

            albumPosition -> {
                // 相册
                params.put("videoType", 7)
            }

            else -> {
                params.put("videoType", 0)
            }
        }
        HttpRequest.get(RequestUrls.DELETE_RECORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
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
                episodePosition, shortPosition -> ids.append((bean as VideoBaseBean).playID.toString() + ",")
                dynamicPosition -> ids.append((bean as DynamicBean).mediaKey + ",")
                albumPosition -> ids.append((bean as PicturesBean).mediaKey + ",")
            }
        }
        return ids
    }

    private fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        when (editTab) {
            episodePosition -> {
                episodeFragment?.resetList(isClear, list)
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

    fun onLoginClick(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
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