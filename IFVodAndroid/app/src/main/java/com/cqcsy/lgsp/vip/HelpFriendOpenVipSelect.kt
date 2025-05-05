package com.cqcsy.lgsp.vip

import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.cqcsy.lgsp.R
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.base.NormalActivity

/**
 * 帮好友代充vip选择套餐支付页
 */
class HelpFriendOpenVipSelect : NormalActivity() {
    var toUid: Int = 0
    var toAccount: String = ""
    var toUserName: String = ""
    var toNickName: String = ""
    var selectId: String = ""
    var categoryId: String = ""

    override fun getContainerView(): Int {
        return R.layout.activity_help_friends_open_vip_select
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.select_vip_classify)
        toUid = intent.getIntExtra("toUid", 0)
        toAccount = intent.getStringExtra("toAccount") ?: ""
        toUserName = intent.getStringExtra("toUserName") ?: ""
        toNickName = intent.getStringExtra("toNickName") ?: ""
        selectId = intent?.getStringExtra("selectId") ?: ""
        categoryId = intent?.getStringExtra("categoryId") ?: ""
        supportFragmentManager.commit {
            val fragment = OpenVipFragment.newInstance(
                hintTitle = true,
                toUid = toUid.toString(),
                selectId = selectId,
                categoryId = categoryId,
                pathInfo = this@HelpFriendOpenVipSelect.javaClass.simpleName
            )
            replace(R.id.fragment_container, fragment)
            setMaxLifecycle(fragment, Lifecycle.State.RESUMED)
        }
        setOpenVipListener(object : OpenVipListener {
            override fun onGetVipCategorySize(size: Int) {

            }

            override fun onPayItemClick(vipClassifyBean: VipClassifyBean) {
            }

            override fun onResult(isSuccess: Boolean) {
                setResult(RESULT_OK)
                finish()
            }

        })
    }

}