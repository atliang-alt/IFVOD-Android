package com.cqcsy.lgsp.upload

import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.NavigationBarBean
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.views.BottomBaseDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_uploaded_short_video.*
import kotlinx.android.synthetic.main.layout_uploaded_classify.view.*

/**
 * 已上传的小视频列表
 */
class UploadedShortVideoActivity : NormalActivity() {
    private var fragment: UploadedShortVideoFragment? = null

    // 选中的位置值
    private var selectChannel = "0"
    private var selectChannelName = StringUtils.getString(R.string.allChannel)
    private var selectStatus = Constant.ALL_STATUS
    private var selectStatusName = StringUtils.getString(R.string.allStatus)
    private var classifyData: MutableList<NavigationBarBean> = ArrayList()
    private var statusData: MutableList<NavigationBarBean> = ArrayList()

    private val channelType = 0
    private val statusType = 1

    init {
        statusData.clear()
        val navigationBarBean = NavigationBarBean()
        navigationBarBean.name = StringUtils.getString(R.string.allStatus)
        navigationBarBean.categoryId = Constant.ALL_STATUS
        statusData.add(navigationBarBean)

        val releasing = NavigationBarBean()
        releasing.name = StringUtils.getString(R.string.releasing)
        releasing.categoryId = Constant.RELEASING
        statusData.add(releasing)

        val underReview = NavigationBarBean()
        underReview.name = StringUtils.getString(R.string.underReview)
        underReview.categoryId = Constant.UNDER_REVIEW
        statusData.add(underReview)

        val noAdopt = NavigationBarBean()
        noAdopt.name = StringUtils.getString(R.string.noAdopt)
        noAdopt.categoryId = Constant.NO_ADOPT
        statusData.add(noAdopt)


        classifyData.clear()
        val classifyBarBean = NavigationBarBean()
        classifyBarBean.name = StringUtils.getString(R.string.allChannel)
        classifyBarBean.categoryId = "0"
        classifyData.add(classifyBarBean)
        // 获取本地数据
        val local = SPUtils.getInstance().getString(Constant.KEY_NAVIGATION_BAR)
        if (!local.isNullOrEmpty()) {
            val list: MutableList<NavigationBarBean> =
                Gson().fromJson(local, object : TypeToken<List<NavigationBarBean>>() {}.type)
            classifyData.addAll(list.filter { it.type == 2 })
        }
    }

    override fun getContainerView(): Int {
        return R.layout.activity_uploaded_short_video
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.uploadedShortVideo)
        initView()
    }

    private fun initView() {
        fragment = UploadedShortVideoFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment!!)
        transaction.commitAllowingStateLoss()
    }

    fun channelClick(view: View) {
        showAllClassifyDialog(channelType, classifyData)
    }

    fun statusClick(view: View) {
        showAllClassifyDialog(statusType, statusData)
    }

    private fun showAllClassifyDialog(type: Int, list: MutableList<NavigationBarBean>) {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_uploaded_classify, null)
        contentView.cancel.setOnClickListener { dialog.dismiss() }
        contentView.sure.setOnClickListener {
            dialog.dismiss()
            val selectItem = list[contentView.scrollPickerView.currentPosition]
            if (type == channelType) {
                selectChannel = selectItem.categoryId
                selectChannelName = selectItem.name
                channelText.text = selectChannelName
                fragment?.setChannel(selectChannel)
            } else if (type == statusType) {
                selectStatus = selectItem.categoryId
                selectStatusName = selectItem.name
                statusText.text = selectStatusName
                fragment?.setStatus(selectStatus)
            }
        }
        contentView.scrollPickerView.data = list
        contentView.scrollPickerView.setDefaultPosition(
            getSelectItemPosition(
                list,
                if (type == channelType) selectChannel else selectStatus
            )
        )
        contentView.scrollPickerView.setFormatter { (it as NavigationBarBean).name }
        dialog.setContentView(contentView)
        dialog.show()
    }

    private fun getSelectItemPosition(
        list: MutableList<NavigationBarBean>,
        currentType: String
    ): Int {
        var select = 0
        for ((index, bean) in list.withIndex()) {
            if (bean.categoryId == currentType) {
                select = index
                break
            }
        }
        return select
    }
}