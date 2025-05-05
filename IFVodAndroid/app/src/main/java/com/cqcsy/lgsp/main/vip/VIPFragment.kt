package com.cqcsy.lgsp.main.vip

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.constant.TimeConstants
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.VipIntroBean
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.HelpFriendOpenVip
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.lgsp.vip.view.VipItemDetail
import com.cqcsy.library.base.BaseFragment
import com.cqcsy.library.network.H5Address
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.utils.StatusBarUtil
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_vip.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * VIP
 */
class VIPFragment : BaseFragment() {
    private val itemIconArray = arrayOf(
        R.mipmap.icon_vip_1080,
        R.mipmap.icon_vip_ad,
        R.mipmap.icon_vip_video,
        R.mipmap.icon_vip_cloud_speed,
        R.mipmap.icon_vip_speed,
        R.mipmap.icon_vip_download,
        R.mipmap.icon_vip_tan,
        R.mipmap.icon_vip_expression,
        R.mipmap.icon_vip_vote
    )

    private val detailIconArray = arrayOf(
        R.mipmap.icon_vip_1080_detail,
        R.mipmap.icon_vip_ad_detail,
        R.mipmap.icon_vip_video_detail,
        R.mipmap.icon_vip_cloud_speed_detail,
        R.mipmap.icon_vip_speed_detail,
        R.mipmap.icon_vip_download_detail,
        R.mipmap.icon_vip_tan_detail,
        R.mipmap.icon_vip_expression_detail,
        R.mipmap.icon_vip_vote_detail
    )

    private val itemDetailImageArray = arrayOf(
        R.mipmap.image_detail_1080,
        R.mipmap.image_detail_ad,
        R.mipmap.image_detail_get_video,
        R.mipmap.image_detail_cloud_speed,
        R.mipmap.image_detail_play_speed,
        R.mipmap.image_detail_offline,
        R.mipmap.image_detail_tan,
        R.mipmap.image_detail_expression,
        R.mipmap.image_detail_vote
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_vip, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTopPadding()
        if (SPUtils.getInstance().getBoolean(Constant.KEY_BIG_V_SWITCH)) {
            receiveVip.visibility = View.VISIBLE
            receiveVipLine.visibility = View.VISIBLE
        } else {
            receiveVip.visibility = View.GONE
            receiveVipLine.visibility = View.GONE
        }
        initView()
        setIntro()
        userInfoContent.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        vipQuestion.setOnClickListener {
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, H5Address.HELP_CENTER)
            intent.putExtra(WebViewActivity.titleKey, getString(R.string.vip_question))
            startActivity(intent)
        }
        vipRule.setOnClickListener {
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, H5Address.VIP_AGREEMENT)
            intent.putExtra(WebViewActivity.titleKey, getString(R.string.vip_rule))
            startActivity(intent)
        }
        ClickUtils.applySingleDebouncing(rechargeOther) {
            if (!GlobalValue.isLogin()) {
                startActivity(Intent(context, LoginActivity::class.java))
                return@applySingleDebouncing
            }
            HttpRequest.cancelRequest(RequestUrls.IS_MALAYSIA)
            val params = HttpParams()
            params.put("region", NormalUtil.getAreaCode())
            HttpRequest.post(RequestUrls.IS_MALAYSIA, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (GlobalValue.isLogin()) {
                        startActivity(Intent(context, HelpFriendOpenVip::class.java))
                    } else {
                        startActivity(Intent(context, LoginActivity::class.java))
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (!errorMsg.isNullOrEmpty()) {
                        ToastUtils.showShort(errorMsg)
                    }
                }
            }, params = params, tag = RequestUrls.IS_MALAYSIA)
        }
    }

    override fun onResume() {
        super.onResume()
        if (mIsFragmentVisible) {
            initView()
        }
    }

    override fun onVisible() {
        super.onVisible()
        initView()
    }

    private fun setTopPadding() {
        if (activity is VIPIntroActivity) {
            return
        }
        statusBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            StatusBarUtil.getStatusBarHeight(requireContext())
        )
    }

    private fun initView() {
        if (GlobalValue.isLogin()) {
            GlobalValue.userInfoBean!!.avatar?.let {
                ImageUtil.loadCircleImage(
                    this,
                    it, userImage
                )
            }
            userName.text = GlobalValue.userInfoBean!!.nickName
            if (GlobalValue.isVipUser()) {
                infoContent.setBackgroundResource(R.mipmap.image_vip_info_bg)
                // vip续费
                btnRight.text = StringUtils.getString(R.string.renew)
                userVipTip.visibility = View.VISIBLE
                userTip.visibility = View.GONE
                vipInfoLayout.visibility = View.VISIBLE
                val data = TimeUtils.string2Date(GlobalValue.userInfoBean!!.eDate, "yyyy-MM-dd")
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val day = TimeUtils.getTimeSpan(TimeUtils.date2Millis(data), calendar.timeInMillis, TimeConstants.DAY) + 1
                vipDays.text = Html.fromHtml(StringUtils.getString(R.string.vipDay, if (day < 0) 0 else day))
                vipDate.text = StringUtils.getString(
                    R.string.vipDate, TimeUtils.date2String(
                        TimesUtils.formatDate(GlobalValue.userInfoBean!!.eDate), "yyyy-MM-dd"
                    )
                )
                if (GlobalValue.userInfoBean!!.bigV || GlobalValue.userInfoBean!!.vipLevel in 1..4) {
                    userName.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, if (GlobalValue.userInfoBean!!.bigV) R.mipmap.icon_big_v_small else VipGradeImageUtil.getVipMinImage(
                            GlobalValue.userInfoBean!!.vipLevel
                        ), 0
                    )
                } else {
                    userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
                getAdsCounts()
            } else {
                infoContent.setBackgroundResource(R.mipmap.image_not_vip_info_bg)
                // 非vip 开通
                btnRight.text = StringUtils.getString(R.string.buy_vip)
                userVipTip.visibility = View.GONE
                vipInfoLayout.visibility = View.GONE
                userTip.visibility = View.VISIBLE
                userTip.text = StringUtils.getString(R.string.openVipTips)
            }
        } else {
            infoContent.setBackgroundResource(R.mipmap.image_not_vip_info_bg)
            // 未登录
            btnRight.text = StringUtils.getString(R.string.buy_vip)
            userName.text = StringUtils.getString(R.string.noLogin)
            userImage.setImageResource(R.mipmap.icon_circle_logo)
            userName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            vipInfoLayout.visibility = View.GONE
            userVipTip.visibility = View.GONE
            userTip.visibility = View.VISIBLE
            userTip.text = StringUtils.getString(R.string.openVipTips)
        }
    }

    private fun setIntro() {
        val itemList = ArrayList<VipIntroBean>()
        val arrayName = resources.getStringArray(R.array.vip_intro_names)
        val arraySub = resources.getStringArray(R.array.vip_intro_sub_des)
        val arrayDetail = resources.getStringArray(R.array.vip_intro_detail)
        for (i in itemIconArray.indices) {
            val bean = VipIntroBean()
            bean.itemName = arrayName[i]
            bean.itemSubDes = arraySub[i]
            bean.itemImage = itemIconArray[i]
            bean.itemDetail = arrayDetail[i]
            bean.detailIcon = detailIconArray[i]
            bean.detailImage = itemDetailImageArray[i]
            itemList.add(bean)
        }
        vipIntro.layoutManager = GridLayoutManager(context, 3)
        vipIntro.addItemDecoration(
            XGridBuilder(context).setHLineSpacing(40f).setIncludeEdge(true).build()
        )
        val adapter = object : BaseQuickAdapter<VipIntroBean, BaseViewHolder>(
            R.layout.layout_vip_intro_item,
            itemList
        ) {
            override fun convert(holder: BaseViewHolder, item: VipIntroBean) {
                holder.setText(R.id.item_name, item.itemName)
                holder.setImageResource(R.id.item_image, item.itemImage)
            }
        }
        adapter.setOnItemClickListener { adapter, view, position ->
            showDetail(
                adapter.getItem(
                    position
                ) as VipIntroBean
            )
        }
        vipIntro.adapter = adapter
    }

    private fun showDetail(vipIntroBean: VipIntroBean) {
        val dialog = VipItemDetail(requireContext())
        dialog.vipIntroBean = vipIntroBean
        dialog.show()
    }

    /**
     * 获取过滤的广告数量
     */
    private fun getAdsCounts() {
        val params = HttpParams()
        params.put("s", -1)
        GlobalValue.userInfoBean?.id?.let { params.put("uid", it) }
        params.put("gid", GlobalValue.userInfoBean?.token!!.gid)
        HttpRequest.get(RequestUrls.AD_CONTROL, object : HttpCallBack<String>() {
            override fun onSuccess(response: String?) {
                val str = StringUtils.getString(R.string.filterAd, response ?: "0")
                userVipTip.text = Html.fromHtml(str)
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

}