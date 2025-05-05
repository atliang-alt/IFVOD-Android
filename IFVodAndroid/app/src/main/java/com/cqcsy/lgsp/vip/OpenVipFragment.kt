package com.cqcsy.lgsp.vip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.ActivityStatusBean
import com.cqcsy.lgsp.bean.VipCategoryBean
import com.cqcsy.library.pay.model.VipClassifyBean
import com.cqcsy.library.pay.model.VipPayBean
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.library.pay.card.CreditCardNoticeActivity
import com.cqcsy.library.pay.polymerization.AlipayOrWeChatPayActivity
import com.cqcsy.library.pay.polymerization.TGCPayActivity
import com.cqcsy.lgsp.upper.chat.ChatActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.base.NormalFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import kotlinx.android.synthetic.main.fragment_open_vip.*
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * 创建时间：2022/8/11
 *
 */
class OpenVipFragment : NormalFragment(), View.OnClickListener, OnPayItemClickListener {

    companion object {

        const val REQUEST_KEY = "request_key"
        const val SELECT_OPTION_RESULT = 0
        const val OPEN_RESULT = 1
        const val VIP_CATEGORY_SIZE = 2
        const val REQUEST_CODE = 123
        const val REQUEST_H5_PAY = 125

        @JvmStatic
        fun newInstance(
            hintTitle: Boolean = false,
            toUid: String? = null,
            selectId: String? = null,
            categoryId: String? = null,
            toAccount: String? = null,
            toUserName: String? = null,
            toNickName: String? = null,
            pathInfo: String? = null    // 进入路径信息
        ): OpenVipFragment {
            val args = Bundle()
            args.putBoolean("hint_title", hintTitle)
            if (toUid != null) {
                args.putString("to_uid", toUid)
            }
            args.putString("select_id", selectId)
            args.putString("category_id", categoryId)
            args.putString("to_account", toAccount)
            args.putString("to_user_name", toUserName)
            args.putString("to_nick_name", toNickName)
            args.putString("path_info", pathInfo)
            val fragment = OpenVipFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var classifyBean: VipClassifyBean? = null
    private var activityStatus: ActivityStatusBean? = null
    private var payBean: VipPayBean? = null
    private var toUid: String? = null
    private var toAccount: String? = null
    private var toUserName: String? = null
    private var toNickName: String? = null
    private var selectId: String? = null    // 扫码传递过来的选中id，可以用来判断是否从扫码过来
    private var categoryId: String? = null    // 扫码传递过来的分类id
    private var pathInfo: String? = null    // 扫码传递过来的分类id

    private val mVipPayTypeList: MutableList<VipCategoryBean> = ArrayList()
    private val mFragment = HashMap<Int, WeakReference<Fragment>>()

    override fun getContainerView(): Int {
        return R.layout.fragment_open_vip
    }

    override fun initView() {
        val hintTitle = arguments?.getBoolean("hint_title") ?: false
        toUid = arguments?.getString("to_uid")
        toAccount = arguments?.getString("to_account")
        toUserName = arguments?.getString("to_user_name")
        toNickName = arguments?.getString("to_nick_name")
        selectId = arguments?.getString("select_id")
        categoryId = arguments?.getString("category_id")
        pathInfo = arguments?.getString("path_info")
        println("OpenVipFragment====$pathInfo")
        tvTitle.isVisible = !hintTitle
        vipRemark.isVisible = !hintTitle
        getVipTypeInfo()
        tv_sure_pay.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                startActivity(Intent(context, LoginActivity::class.java))
                return@setOnClickListener
            }
            surePay(toAccount, toUid?.toInt() ?: 0, toUserName, toNickName)
        }
    }

    override fun onVisible() {
        super.onVisible()
        // 登录回来后，如果uid和登录的uid不一致处理
        if (viewPager.adapter != null && viewPager.adapter!!.itemCount > 0) {
            checkLoginUser()
        }
    }

    private fun checkLoginUser() {
        if (!GlobalValue.isLogin() || toUid.isNullOrEmpty() || activity is HelpFriendOpenVipSelect) {
            return
        }
        if (toUid != GlobalValue.userInfoBean?.id.toString()) {
            showOpenOtherTip()
        }
    }

    private fun getActivityStatus() {
        if (GlobalValue.isLogin()) {
            HttpRequest.post(RequestUrls.INVITE_ACTIVITY_STATUS, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    activityStatus = GsonUtils.fromJson(response.toString(), object : TypeToken<ActivityStatusBean>() {}.type)
                    for ((index, item) in mVipPayTypeList.withIndex()) {
                        if (item.styleType == VipCategory.INVITE_OFFSALE.styleType) {
                            val tab = vipCategoryTab.getChildAt(index) as ConstraintLayout
                            addCountDownTab(tab, item)
                            val fragment: Fragment? = mFragment.get(index)?.get()
                            if (fragment is InviteForVIPFragment) {
                                fragment.statusBean = activityStatus
                            }
                        }
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                }

            }, tag = this)
        }
    }

    private fun getVipTypeInfo(showLoading: Boolean = true) {
        if (showLoading) showProgress()
        val params = HttpParams()
        params.put("vipCategoryId", GlobalValue.userInfoBean?.vipCategoryId ?: 0)
        params.put("productID", selectId)
        params.put("ToUID", toUid)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.post(RequestUrls.VIP_TYPE_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                response?.let { setViewByResponse(it) }
                checkLoginUser()
                getActivityStatus()
            }

            override fun onError(response: String?, errorMsg: String?) {
                try {
                    dismissProgress()
                    val json = JSONObject(response)
                    val code = json.optInt("ret")
                    if (code == 7003) {  // 不支持代充的判断
                        setViewByResponse(json.optJSONObject("data"))
                        showNotSupport(errorMsg ?: "")
                    } else {
                        showFailed(this@OpenVipFragment)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showFailed(this@OpenVipFragment)
                }
            }
        }, params = params, tag = this)
    }


    private fun setViewByResponse(response: JSONObject) {
        val remark = response.optString("remark")
        vipRemark.text = remark
        val packageArray = response.optJSONArray("categoryList")
        val categoryList: MutableList<VipCategoryBean>? =
            Gson().fromJson(
                packageArray?.toString(),
                object : TypeToken<MutableList<VipCategoryBean>>() {}.type
            )
        mVipPayTypeList.clear()
        if (categoryList.isNullOrEmpty()) {
            return
        }
        mVipPayTypeList.addAll(categoryList)
        setFragmentResult(REQUEST_KEY, Bundle().apply {
            putInt("result_type", VIP_CATEGORY_SIZE)
            putInt("vip_category_size", categoryList.size)
        })
        var selectedPosition = 0
        if (!categoryId.isNullOrEmpty()) {
            for ((i, vipCategory) in categoryList.withIndex()) {
                if (vipCategory.id.toString() == categoryId) {
                    selectedPosition = i
                    break
                }
            }
        }
        setTab(categoryList, selectedPosition)
        setViewPager(categoryList, selectedPosition)

        if (selectId.isNullOrEmpty()) {
            val select = categoryList[selectedPosition]
            if (!select.data.isNullOrEmpty()) {
                onSelectClassify(select.data[0])
            }
        } else {
            val select = categoryList[selectedPosition]
            select.data?.forEach {
                if (it.packageId.toString() == selectId) {
                    onSelectClassify(it)
                }
            }
        }
    }

    private fun setViewPager(categoryList: MutableList<VipCategoryBean>, current: Int) {
        mFragment.forEach {
            it.value.get()?.onDestroy()
        }
        mFragment.clear()
        viewPager.isUserInputEnabled = false
        viewPager.setPageTransformer(null)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setSelectedPosition(position)
            }
        })
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return categoryList.size
            }

            override fun createFragment(position: Int): Fragment {
                val item = categoryList[position]
                var fragment = mFragment[position]?.get()
                if (fragment == null) {
                    fragment = when (item.styleType) {
                        VipCategory.DISCOUNT_VIP.styleType -> {
                            SaleOffVIPFragment.newInstance(item, selectId)
                        }

                        VipCategory.INVITE_OFFSALE.styleType -> {
                            InviteForVIPFragment.newInstance(item, activityStatus, selectId)
                        }

                        else -> {
                            NormalVipFragment.newInstance(item, selectId)
                        }
                    }
                    if (fragment is IGetSelectedInfo) {
                        fragment.listener = this@OpenVipFragment
                    }
                    mFragment[position] = WeakReference(fragment)
                }
                return fragment
            }

        }
        viewPager.offscreenPageLimit = categoryList.size
        setSelectedPosition(current)
    }

    private fun showNotSupport(msg: String) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setMsg(msg)
        tipsDialog.setRightListener(R.string.ensure) {
            tipsDialog.dismiss()
            requireActivity().finish()
        }
        tipsDialog.setCancelable(false)
        tipsDialog.show()
    }

    override fun onClick(v: View?) {
        getVipTypeInfo()
    }

    private fun setTab(categoryList: MutableList<VipCategoryBean>, selectedPosition: Int) {
        vipCategoryTab.removeAllViews()
        if (categoryList.size > 1) {
            vipCategoryTab.isVisible = true
            for ((i, vipCategory) in categoryList.withIndex()) {
                val tab = ConstraintLayout(requireContext())
                val text = TextView(context)
                text.tag = vipCategory
                text.id = R.id.vip_tab_name
                text.textSize = 18f
                text.paint.isFakeBoldText = true
                text.text = vipCategory.name
                val textLp = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )
                textLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                textLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                textLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                textLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

                if (vipCategory.styleType == VipCategory.DISCOUNT_VIP.styleType || vipCategory.styleType == VipCategory.INVITE_OFFSALE.styleType) {
                    val imageView = ImageView(context)
                    imageView.setImageResource(R.mipmap.icon_vip_discount)
                    val imageViewLp = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    )
                    imageView.id = R.id.vip_tab_tag
                    imageViewLp.startToEnd = R.id.vip_tab_name
                    imageViewLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    imageViewLp.topMargin = SizeUtils.dp2px(3f)
                    imageViewLp.leftMargin = SizeUtils.dp2px(5f)
                    tab.addView(imageView, imageViewLp)
                }
                tab.addView(text, textLp)

                if (i == selectedPosition) {
                    text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    tab.setBackgroundResource(if (i % 2 == 0) R.mipmap.image_vip_right_btn else R.mipmap.image_vip_left_btn)
                } else {
                    text.setTextColor(ContextCompat.getColor(requireContext(), R.color.word_color_4))
                    tab.background = null
                }
                tab.setOnClickListener {
                    setSelectedPosition(i)
                }
                val lp = LinearLayout.LayoutParams(0, SizeUtils.dp2px(44f), 1f)
                vipCategoryTab.addView(tab, lp)
            }
        } else {
            vipCategoryTab.isVisible = false
        }
    }

    /**
     * 添加倒计时
     */
    private fun addCountDownTab(tab: ConstraintLayout, vipCategory: VipCategoryBean) {
        if (vipCategory.styleType != VipCategory.INVITE_OFFSALE.styleType || activityStatus == null) return
        if (activityStatus?.type == 0 || activityStatus?.endTime == 0L) {
            removeCountDown(tab)
        } else {
            var time = tab.findViewById<TextView>(R.id.vip_tab_time)
            if (time == null) {
                val tabText = tab.findViewById<TextView>(R.id.vip_tab_name)
                val tabLp = tabText.layoutParams as ConstraintLayout.LayoutParams
                time = TextView(context)
                val timeParam = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                )
                time.id = R.id.vip_tab_time
                time.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                time.setTextColor(ColorUtils.getColor(R.color.word_color_5))
                timeParam.topToBottom = R.id.vip_tab_name
                timeParam.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                timeParam.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                timeParam.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                tab.addView(time, timeParam)

                tabText.textSize = 16f
                tabLp.bottomToTop = R.id.vip_tab_time
                tabText.layoutParams = tabLp
            }
            activityStatus?.endTime?.let {
                countDown(time, it * 1000)
            }
        }
    }

    private fun removeCountDown(tab: ConstraintLayout) {
        stopCountDown()
        val tabText = tab.findViewById<TextView>(R.id.vip_tab_name)
        val tabLp = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
        )
        val timeText = tab.findViewById<TextView>(R.id.vip_tab_time)
        if (timeText != null) {
            tab.removeView(timeText)
        }
        tabText.textSize = 18f

        tabLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        tabLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        tabLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        tabLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        tabText.layoutParams = tabLp
    }

    private fun setSelectedPosition(position: Int) {
        // 1、只有一个类别，底部肯定要显示
        // 2、多个类别，为原价购买类型，必定显示底部
        // 3、多个类型，为分享折扣，且已经分享完成，并且点击N折购买按钮后，显示底部
        val normal = mVipPayTypeList[position].styleType == VipCategory.ORIGINAL_PRICE.styleType
        val discount =
            mVipPayTypeList[position].styleType == VipCategory.DISCOUNT_VIP.styleType && (mFragment[position]?.get() as SaleOffVIPFragment?)?.isShared() == true
        val invite =
            mVipPayTypeList[position].styleType == VipCategory.INVITE_OFFSALE.styleType && activityStatus?.type == 2 && (activityStatus?.price
                ?: 0F) > 0F
        bottom.isVisible = normal || discount || invite

        if (viewPager.currentItem == position) {
            return
        }
        val fragment = mFragment[position]?.get()
        if (fragment is IGetSelectedInfo) {
            fragment.getSelectVip()?.let { onSelectClassify(it) }
        }
        for (i in 0 until vipCategoryTab.childCount) {
            val view = vipCategoryTab.getChildAt(i)
            val text = view.findViewById<TextView>(R.id.vip_tab_name)
            if (i == position) {
                text.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                view.setBackgroundResource(if (i % 2 == 0) R.mipmap.image_vip_right_btn else R.mipmap.image_vip_left_btn)
            } else {
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.word_color_4))
                view.background = null
            }
        }
        viewPager.currentItem = position
    }

    override fun onSelectClassify(vipClassifyBean: VipClassifyBean) {
        classifyBean = vipClassifyBean
        setPrice(vipClassifyBean)
        setFragmentResult(REQUEST_KEY, Bundle().apply {
            putInt("result_type", SELECT_OPTION_RESULT)
            putSerializable("select_vip_classify", vipClassifyBean)
        })
    }

    override fun showBottom(show: Boolean) {
        bottom.isVisible = show
    }

    override fun onActivityStart(statusBean: ActivityStatusBean?) {
        for ((index, item) in mVipPayTypeList.withIndex()) {
            if (item.styleType == VipCategory.INVITE_OFFSALE.styleType) {
                val tab = vipCategoryTab.getChildAt(index) as ConstraintLayout
                addCountDownTab(tab, item)
            }
        }
    }

    override fun refreshStatus() {
        getActivityStatus()
    }

    override fun refreshPackage() {
        getVipTypeInfo(false)
    }

    private fun setPrice(vipClassifyBean: VipClassifyBean) {
        priceSign.text = vipClassifyBean.priceSymbol
        if (!vipClassifyBean.disprice.isNullOrEmpty() && !vipClassifyBean.disrmb.isNullOrEmpty()) {
            price.text = vipClassifyBean.disprice
            rmbPrice.text = StringUtils.getString(R.string.rmbSign, vipClassifyBean.disrmb)
        } else {
            price.text = vipClassifyBean.price
            rmbPrice.text = StringUtils.getString(R.string.rmbSign, vipClassifyBean.rmb)
        }

    }

    /**
     * 代充需要传代充的id和账号：toAccount、toUid
     */
    fun surePay(
        toAccount: String?,
        toUid: Int = 0,
        toUserName: String?,
        toNickName: String?
    ) {
        classifyBean ?: return
        val fragment = mFragment[viewPager.currentItem]?.get()
        if (fragment is IGetSelectedInfo) {
            payBean = fragment.getSelectPayType()
        }
        payBean ?: return

        when (payBean!!.payType) {
            0, 1 -> startPay(
                toAccount,
                toUserName,
                toNickName,
                toUid,
                AlipayOrWeChatPayActivity::class.java
            )

            2 -> startPay(
                toAccount, toUserName, toNickName, toUid, TGCPayActivity::class.java
            )

            3 -> startChat(toAccount, toUserName, toNickName)
            4 -> startPay(toAccount, toUserName, toNickName, toUid, CreditCardNoticeActivity::class.java)

            5 -> {
                createOrder(classifyBean!!, payBean!!)
            }

            else -> startPay(toAccount, toUserName, toNickName, toUid, CreditCardNoticeActivity::class.java)
        }
    }

    private fun startPay(
        toAccount: String?,
        toUserName: String?,
        toNickName: String?,
        toUid: Int = 0,
        cls: Class<*>
    ) {
        val intent = Intent(context, cls)
        intent.putExtra("vipClassifyBean", classifyBean)
        intent.putExtra("vipPayBean", payBean)
        intent.putExtra("pathInfo", pathInfo)
        if (toUid != 0) {
            intent.putExtra("toUid", toUid)
        }
        if (!toAccount.isNullOrEmpty()) {
            intent.putExtra("toAccount", toAccount)
            intent.putExtra("toUserName", toUserName)
            intent.putExtra("toNickName", toNickName)
        }
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun startChat(toAccount: String?, toUserName: String?, toNickName: String?) {
        val userNameRaw = GlobalValue.userInfoBean?.userNameRaw ?: ""
        val nickName = GlobalValue.userInfoBean?.nickName ?: ""
        val giftDays = classifyBean?.giftDays ?: 0
        val giftDay = if (giftDays > 0) {
            String.format("+%d天", giftDays)
        } else {
            ""
        }
        val vipName = classifyBean!!.name + "(" +
                StringUtils.getString(R.string.days, classifyBean!!.validityDays) + giftDay + ")"
        val vipDate = if (GlobalValue.isVipUser()) {
            TimeUtils.date2String(
                TimesUtils.formatDate(GlobalValue.userInfoBean!!.eDate),
                "yyyy-MM-dd"
            )
        } else {
            ""
        }
        val messageModel = if (toAccount.isNullOrEmpty()) {
            StringUtils.getString(
                R.string.artificial_pay_message, vipName, userNameRaw, nickName, vipDate
            )
        } else {
            StringUtils.getString(
                R.string.artificial_pay_message_help, toAccount, vipName, toUserName, toNickName
            )
        }
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra(ChatActivity.SEND_MODEL_MESSAGE, messageModel)
        intent.putExtra(ChatActivity.CHAT_TYPE, ChatActivity.TYPE_SERVER_CHARGE)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE || requestCode == REQUEST_H5_PAY) {
                setFragmentResult(REQUEST_KEY, Bundle().apply {
                    putInt("result_type", OPEN_RESULT)
                    putBoolean("open_vip_result", true)
                })
            }
        }
    }

    private fun showOpenOtherTip() {
        val dialog = TipsDialog(requireContext())
        dialog.setMsg(R.string.help_other_open_vip_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
            requireActivity().finish()
        }
        dialog.setRightListener(R.string.help_other_open_vip) {
            dialog.dismiss()
            val intent = Intent(context, HelpFriendOpenVip::class.java)
            intent.putExtra("targetUid", toUid)
            intent.putExtra("selectId", selectId)
            intent.putExtra("categoryId", categoryId)
            startActivity(intent)
            requireActivity().finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onLogin() {
        getVipTypeInfo()
    }

    override fun onDestroy() {
        stopCountDown()
        super.onDestroy()
    }

    private fun createOrder(vipClassifyBean: VipClassifyBean, vipPayBean: VipPayBean) {
        (activity as BaseActivity).showProgressDialog()
        val params = HttpParams()
        val promotions = vipClassifyBean.promotions
        if (!promotions.isNullOrEmpty()) {
            params.put("Promotions", promotions.joinToString(separator = "|") {
                it.promotionCode
            })
        }
        params.put("ProductID", vipClassifyBean.packageId)
        params.put("SysName", vipPayBean.payType)
        val uid: Any? = if (toUid.isNullOrEmpty()) {
            GlobalValue.userInfoBean?.id ?: 0
        } else {
            toUid
        }
        params.put("ToUID", uid.toString())
        params.put("refer", pathInfo)
        HttpRequest.post(RequestUrls.CREATE_WEB_PAY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                (activity as BaseActivity).dismissProgressDialog()
                if (response != null && !response.isNull("gateway") && !response.isNull("params")) {
                    // 跳转支付H5页面
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.putExtra(WebViewActivity.urlKey, response.optString("gateway"))
                    intent.putExtra(WebViewActivity.postParams, response.optString("params"))
                    intent.putExtra(WebViewActivity.titleKey, vipPayBean.title)
                    startActivityForResult(intent, REQUEST_H5_PAY)
                } else {
                    ToastUtils.showLong(R.string.order_params_error)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                (activity as BaseActivity).dismissProgressDialog()
                if (!errorMsg.isNullOrEmpty()) {
                    ToastUtils.showShort(errorMsg)
                }
            }

        }, params, tag = this)
    }

    var mCountDownTimer: CountDownTimer? = null
    private fun countDown(textView: TextView, time: Long) {
        stopCountDown()
        mCountDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textView.text = CommonUtil.stringForTime(millisUntilFinished)
            }

            override fun onFinish() {
                textView.isVisible = false
                refreshStatus()
            }

        }
        mCountDownTimer?.start()
    }

    private fun stopCountDown() {
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }
}