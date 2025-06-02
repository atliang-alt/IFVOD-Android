package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ServiceItemBean
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.event.AttentionUnreadStatus
import com.cqcsy.lgsp.event.MessageUnreadStatus
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.MainActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.views.dialog.ServiceSelectDialog
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.H5Address
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import kotlinx.android.synthetic.main.layout_mine.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 我的
 */
class MineFragment : BaseFragment() {
    val menuRes = arrayListOf(
        R.mipmap.icon_earn_coin,
        R.mipmap.icon_ad_center,
        R.mipmap.icon_order,
        R.mipmap.icon_autho,
        R.mipmap.icon_help_center,
        R.mipmap.icon_mine_wanted_video,
        R.mipmap.icon_customer_service,
        R.mipmap.icon_large_setting
    )
    private val taskResultCode: Int = 1001
    private val videoWantesCode: Int = 1002
    private var mServiceList: MutableList<ServiceItemBean>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_mine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        if (GlobalValue.userInfoBean != null) {
            setUserInfo(GlobalValue.userInfoBean!!)
        } else {
            setErrorShow()
        }
        setClick()
        val arrays = StringUtils.getStringArray(R.array.person_menu).toMutableList()
        val bigVSwitch = SPUtils.getInstance().getBoolean(Constant.KEY_BIG_V_SWITCH)
        if (!bigVSwitch) {
            arrays.removeAt(3)
            menuRes.removeAt(3)
        }
        personMenu.layoutManager = GridLayoutManager(requireContext(), 4)
        personMenu.addItemDecoration(XGridBuilder(context).setVLineSpacing(20f).setHLineSpacing(20f).build())
        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(
            R.layout.layout_person_menu_item,
            arrays
        ) {
            override fun convert(holder: BaseViewHolder, item: String) {
                holder.setImageResource(R.id.menu_image, menuRes[holder.absoluteAdapterPosition])
                holder.setText(R.id.item_name, item)
                ClickUtils.applyGlobalDebouncing(holder.itemView) {
                    when (menuRes[holder.absoluteAdapterPosition]) {
                        R.mipmap.icon_earn_coin -> {//赚金币
                            if (GlobalValue.isLogin()) {
                                val intent = Intent(context, TaskCenterActivity::class.java)
                                startActivityForResult(intent, taskResultCode)
                            } else {
                                startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }

                        R.mipmap.icon_ad_center -> {//广告中心
                            val intent = Intent(context, WebViewActivity::class.java)
                            intent.putExtra(WebViewActivity.urlKey, H5Address.ADVERT_CENTER)
                            startActivity(intent)
                        }

                        R.mipmap.icon_order -> {//VIP开通记录
                            if (GlobalValue.isLogin()) {
                                val intent = Intent(context, VIPOrderActivity::class.java)
                                startActivity(intent)
                            } else {
                                startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }

                        R.mipmap.icon_autho -> {
                            //大V认证
                            if (GlobalValue.isLogin()) {
                                startBigV()
                            } else {
                                startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }

                        R.mipmap.icon_help_center -> {
                            //帮助中心
                            startHelp()
                        }

                        R.mipmap.icon_mine_wanted_video -> { // 求片
                            if (GlobalValue.isLogin()) {
                                startActivityForResult(
                                    Intent(context, VideoWantedActivity::class.java),
                                    videoWantesCode
                                )
                            } else {
                                startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }

                        R.mipmap.icon_customer_service -> {// 联系客服
                            if (mServiceList.isNullOrEmpty()) {
                                serviceList()
                            } else {
                                showServiceList(mServiceList!!)
                            }
                        }

                        R.mipmap.icon_large_setting -> { //设置
                            startActivity(Intent(requireContext(), SettingActivity::class.java))
                        }
                    }
                }
            }
        }
        personMenu.adapter = adapter
    }

    private fun startBigV() {
        if (GlobalValue.isBigV()) {
            ToastUtils.showShort(R.string.big_v_tip)
            return
        }
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(
            WebViewActivity.urlKey,
            SPUtils.getInstance().getString(Constant.KEY_BIG_V_URL)
        )
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.authentication))
        startActivity(intent)
    }

    private fun startHelp() {
        val intent = Intent(requireContext(), HelpCenterActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.HELP_CENTER)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.help_center))
        startActivity(intent)
    }

    private fun serviceList() {
        HttpRequest.cancelRequest(RequestUrls.SERVICE_LIST)
        HttpRequest.get(RequestUrls.SERVICE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val list = response?.optJSONArray("list")
                if (list == null || list.length() == 0) {
                    ToastUtils.showLong(R.string.service_list_error)
                } else {
                    val result =
                        GsonUtils.fromJson<MutableList<ServiceItemBean>>(list.toString(), object : TypeToken<MutableList<ServiceItemBean>>() {}.type)
                    if (!result.isNullOrEmpty()) {
                        mServiceList = result
                        showServiceList(result)
                    } else {
                        ToastUtils.showLong(R.string.service_list_error)
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(R.string.service_list_error)
            }
        }, tag = RequestUrls.SERVICE_LIST)
    }

    private fun showServiceList(serviceList: MutableList<ServiceItemBean>) {
        val dialog = ServiceSelectDialog(requireContext(), serviceList)

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        refresh(mIsFragmentVisible)
    }

    override fun onPause() {
        super.onPause()
        refresh(false)
    }

    private fun refresh(isVisible: Boolean) {
        if (!isSafe()) {
            return
        }
        if (isVisible) {
            getUserInfo()
            val activityState = SPUtils.getInstance().getBoolean(Constant.ACTIVITY_SWITCH, false)
            if (activityState) {
                inviteImage.visibility = View.VISIBLE
                inviteImage.startAnimation()
            } else if (inviteImage.visibility == View.VISIBLE) {
                inviteImage.visibility = View.GONE
                if (inviteImage.isAnimating) {
                    inviteImage.stopAnimation()
                }
            }
            raffle.isVisible = !GlobalValue.raffleAddress.isNullOrEmpty()
        } else {
            inviteImage.stopAnimation(false)
        }
    }

    override fun onVisible() {
        super.onVisible()
        refresh(mIsFragmentVisible)
    }

    override fun onInvisible() {
        super.onInvisible()
        refresh(mIsFragmentVisible)
    }

    private fun getUserInfo() {
        if (GlobalValue.isLogin()) {
            HttpRequest.get(RequestUrls.USER_INFO, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    val userInfoBean = Gson().fromJson<UserInfoBean>(
                        response.toString(),
                        object : TypeToken<UserInfoBean>() {}.type
                    )
                    setUserInfo(userInfoBean)
                }

                override fun onError(response: String?, errorMsg: String?) {
                }
            }, tag = this)
        } else {
            setErrorShow()
        }
    }

    private fun setErrorShow() {
        nickName.text = getString(R.string.loginOrRegister)
        nickName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        upperDesc.text = getString(R.string.mine_login_tip)
        attentionNumber.text = "-"
        userLevel.text = "-"
        userGold.text = "-"
        fansNumber.text = "-"
        userLogo.setImageResource(R.mipmap.icon_circle_logo)
        userLogo.tag = null
        notVipTip.visibility = View.VISIBLE
        vipInfo.visibility = View.GONE
        userLevelImage.visibility = View.GONE
        userVipImage.visibility = View.GONE
        collectUpdateNumber.visibility = View.GONE
        messageNumber.visibility = View.GONE
    }

    fun setUserInfo(userInfoBean: UserInfoBean) {
        GlobalValue.userInfoBean?.copy(userInfoBean)

        userInfoBean.avatar?.let { ImageUtil.loadCircleImage(this, it, userLogo) }
        if (!userInfoBean.nickName.isNullOrEmpty()) {
            nickName.text = userInfoBean.nickName
        } else {
            nickName.text = ""
        }
        if (userInfoBean.sex == 1) {
            nickName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_sex_man_32, 0)
        } else if (userInfoBean.sex == 0) {
            nickName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_sex_woman_32, 0)
        }
        upperDesc.text = userInfoBean.introduce
        attentionNumber.text = NormalUtil.formatPlayCount(userInfoBean.attentionCount)
        fansNumber.text = NormalUtil.formatPlayCount(userInfoBean.fansCount)
        if (userInfoBean.collectionUpdateCount > 0) {
            collectUpdateNumber.visibility = View.VISIBLE
            collectUpdateNumber.text = if (userInfoBean.collectionUpdateCount > 99) "99+" else userInfoBean.collectionUpdateCount.toString()
        } else {
            collectUpdateNumber.visibility = View.GONE
        }
        if (userInfoBean.totalMsgCount > 0) {
            messageNumber.visibility = View.VISIBLE
            messageNumber.text = if (userInfoBean.totalMsgCount > 99) "99+" else userInfoBean.totalMsgCount.toString()
        } else {
            messageNumber.visibility = View.GONE
        }
        EventBus.getDefault().post(AttentionUnreadStatus(userInfoBean.newWorks))
        EventBus.getDefault().post(MessageUnreadStatus(userInfoBean.totalMsgCount))
        if (userInfoBean.bigV) {
            userVipImage.visibility = View.VISIBLE
            userVipImage.setImageResource(R.mipmap.icon_big_v)
        } else {
            if (userInfoBean.vipLevel <= 0) {
                userVipImage.visibility = View.GONE
            } else {
                userVipImage.visibility = View.VISIBLE
                userVipImage.setImageResource(VipGradeImageUtil.getVipImage(userInfoBean.vipLevel))
            }
        }
        if (userInfoBean.vipLevel <= 0) {
            notVipTip.visibility = View.VISIBLE
            vipInfo.visibility = View.GONE
        } else {
            vipImage.setImageResource(VipGradeImageUtil.getVipImage(userInfoBean.vipLevel))
            vipName.text = userInfoBean.vipTypeName
            vipTime.text = getString(
                R.string.vipDate,
                TimeUtils.date2String(TimesUtils.formatDate(userInfoBean.eDate), "yyyy-MM-dd")
            )
            notVipTip.visibility = View.GONE
            vipInfo.visibility = View.VISIBLE
        }

        if ((userInfoBean.userExtension?.currentLevel ?: 0) > 0) {
            userLevelImage.visibility = View.VISIBLE
            userLevelImage.setImageResource(VipGradeImageUtil.getVipLevel(userInfoBean.userExtension!!.currentLevel))
        } else {
            userLevelImage.visibility = View.GONE
        }
        userLevel.text = getString(R.string.user_level, userInfoBean.userExtension?.currentLevel)
        userGold.text = (userInfoBean.userExtension?.gold ?: "0").toString()
    }

    private fun setClick() {
        nickName.setOnClickListener {
            if (GlobalValue.isLogin()) {
                val intent = Intent(context, EditUserInfoActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        userLogo.setOnClickListener {
            if (GlobalValue.isLogin()) {
                val intent = Intent(context, EditUserInfoActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        messageLayout.setOnClickListener {
            if (GlobalValue.isLogin()) {
                val intent = Intent(context, MessageActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        mineGrade.setOnClickListener {
            if (GlobalValue.isLogin()) {
                val intent = Intent(context, MineGradeActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        mineCoin.setOnClickListener {
            if (GlobalValue.isLogin()) {
                val intent = Intent(context, MineCoinActivity::class.java)
                startActivity(intent)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == taskResultCode || requestCode == videoWantesCode) {
                val index = data?.getIntExtra("index", 0)
                (activity as MainActivity).jumpMainHomeFragment(index ?: 0)
            }
        }
    }

}