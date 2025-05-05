package com.cqcsy.lgsp.vip

import android.content.Intent
import android.view.View
import androidx.fragment.app.commit
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.lgsp.vip.util.VipPayBackDialog
import com.cqcsy.lgsp.vip.view.VipInfoDetail
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import kotlinx.android.synthetic.main.activity_open_vip.*

/**
 * 开通或续费VIP
 * 如果已经是VIP，就是续费VIP
 */
class OpenVipActivity : NormalActivity() {

    private var isShowBackDialog: Boolean = true
    private var targetUid: String? = null
    private var selectId: String? = null
    private var categoryId: String? = null

    override fun getContainerView(): Int {
        return R.layout.activity_open_vip
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.openVip)
        targetUid = intent.getStringExtra("targetUid")
        selectId = intent.getStringExtra("selectId")
        categoryId = intent.getStringExtra("categoryId")
        val pathInfo = intent.getStringExtra("pathInfo")
        supportFragmentManager.commit {
            val fragment = OpenVipFragment.newInstance(
                selectId = selectId,
                toUid = targetUid,
                categoryId = categoryId,
                pathInfo = pathInfo
            )
            replace(R.id.fragment_container, fragment)
        }
        setOpenVipListener(object : OpenVipListener {
            override fun onGetVipCategorySize(size: Int) {
                //vipCategory.isVisible = size > 1 && GlobalValue.isVipUser()
            }

            override fun onPayItemClick(vipClassifyBean: VipClassifyBean) {
            }

            override fun onResult(isSuccess: Boolean) {
                if (isSuccess) {
                    setResult(RESULT_OK)
                    isShowBackDialog = false
                    finish()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (GlobalValue.isLogin()) {
            setView()
        } else {
            userInfoContent.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun setView() {
        val userInfoBean = GlobalValue.userInfoBean!!
        userInfoBean.avatar?.let {
            ImageUtil.loadCircleImage(
                this,
                it, userPhoto
            )
        }
        userName.text = userInfoBean.nickName

        vipCategory.text = userInfoBean.vipCategoryName
        vipImage.setImageResource(VipGradeImageUtil.getVipImage(userInfoBean.vipLevel))

        if (GlobalValue.isVipUser()) {
            vipDate.text = StringUtils.getString(
                R.string.renewVipTips, TimeUtils.date2String(
                    TimesUtils.formatDate(userInfoBean.eDate), "yyyy-MM-dd"
                )
            )
            vipDate.setTextColor(ColorUtils.getColor(R.color.word_color_vip))
            setHeaderTitle(R.string.renewVip)
            vipInfoBtn.visibility = View.VISIBLE
        } else {
            vipDate.text = StringUtils.getString(R.string.openVipTips)
            setHeaderTitle(R.string.openVip)
            vipInfoBtn.visibility = View.GONE
        }
        userInfoContent.setOnClickListener(null)
    }

//    fun surePay(view: View) {
//        if (!GlobalValue.isLogin()) {
//            startActivity(Intent(this, LoginActivity::class.java))
//            return
//        }
    //vipClassifyModeView.surePay(this)
//    }

    /**
     * 点击vip详情
     */
    fun vipInfoBtn(view: View) {
        val dialog = VipInfoDetail(this)
        dialog.show()
    }

    override fun onBackPressed() {
        if (isShowBackDialog) {
            dialogShow()
        } else {
            finish()
        }
    }

    private fun dialogShow() {
        val vipPayBackDialog = VipPayBackDialog(this, object : VipPayBackDialog.OnClickListener {
            override fun onBuy() {
            }

            override fun onBack() {
                finish()
            }
        })
        vipPayBackDialog.show()
    }

}