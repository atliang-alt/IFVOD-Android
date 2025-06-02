package com.cqcsy.lgsp.upper

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.BlackListEvent
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.lgsp.upper.chat.ChatActivity
import com.cqcsy.lgsp.upper.pictures.UpperPicturesFragment
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_upper.*
import kotlinx.android.synthetic.main.layout_upper_top.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*
import kotlin.math.abs

/**
 * up主页面
 */

class UpperActivity : BaseActivity() {
    companion object {
        const val UPPER_ID = "upperId"
    }

    val tabCount = 5
    var upperId = 0
    var upperInfoBean: UpperInfoBean? = null
    val fragmentCache: WeakHashMap<Int, Fragment> = WeakHashMap()
    private val isMyself: Boolean
        get() {
            return upperId == (GlobalValue.userInfoBean?.id ?: -1)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTransparentForImageView(this, null)
        setContentView(R.layout.activity_upper)
        upperId = intent.getIntExtra(UPPER_ID, 0)
        if (upperId > 0) {
            getUpperInfo(upperId)
        }
        setScrollListener()
        ClickUtils.applySingleDebouncing(blackList) {
            showBlackTip(upperId)
        }
    }

    override fun onResume() {
        super.onResume()
        upperInfoBean?.let {
            setUserStatus(it.focusStatus, it.isBlackList)
        }
    }

    override fun onLogin() {
        if (upperId > 0) {
            getUpperInfo(upperId, false)
        }
    }

    override fun onLoginOut() {
        if (upperId > 0) {
            getUpperInfo(upperId, false)
        }
    }

    fun finish(v: View) {
        onBackPressed()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    fun sendMessage(view: View) {
        if (upperInfoBean == null) {
            return
        }
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(ChatActivity.USER_ID, upperInfoBean?.id.toString())
            intent.putExtra(ChatActivity.NICK_NAME, upperInfoBean?.nickName)
            intent.putExtra(ChatActivity.USER_IMAGE, upperInfoBean?.avatar)
            intent.putExtra(ChatActivity.CHAT_TYPE, upperInfoBean?.role)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setScrollListener() {
        val topHeight = SizeUtils.dp2px(70f)
        appbarLayout.addOnOffsetChangedListener { _, i ->
            val total = appbarLayout.totalScrollRange.toFloat()
            when {
                i == 0 -> {   //  完全展开
                    titleName.alpha = 0f
                    tabLayoutTop.alpha = 0f
                    topContainer.visibility = View.GONE
                    setPagerTopMargin(0)
                }

                abs(i) >= total -> { // 完全收缩
                    titleName.alpha = 1f
                    tabLayoutTop.alpha = 1f
                    topContainer.alpha = 1f
                    topContainer.visibility = View.VISIBLE
                    setPagerTopMargin(topHeight)
                }

                abs(i) >= total - topHeight -> {
                    titleName.alpha = 1f
                    tabLayoutTop.alpha = 1f
                    topContainer.alpha = 1f
                    topContainer.visibility = View.VISIBLE
                }

                else -> {
                    titleName.alpha = 0f
                    tabLayoutTop.alpha = 0f
                    topContainer.alpha = 0f
                    topContainer.visibility = View.GONE
                    setPagerTopMargin((topHeight * abs(i) / total).toInt())
                }
            }
        }
    }

    private fun setPagerTopMargin(topMargin: Int) {
        val layoutParams = upList.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.topMargin = topMargin
        upList.layoutParams = layoutParams
    }

    // 0普通；1运营
    private fun setupTab() {
        upList.adapter = object : FragmentStatePagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getItem(position: Int): Fragment {
                var fragment: Fragment? = fragmentCache[position]
                if (fragment == null) {
                    fragment = when (position) {
                        0 -> AllResourceFragment()
                        1 -> UpperEpisodeFragment()
                        2 -> UpperShortFragment()
                        3 -> UpperDynamicFragment()
                        else -> UpperPicturesFragment()
                    }
                    val bundle = Bundle()
                    bundle.putInt("userId", upperId)
                    fragment.arguments = bundle
                    fragmentCache[position] = fragment
                }
                return fragment
            }

            override fun getCount(): Int {
                return tabCount
            }

            override fun getPageTitle(position: Int): CharSequence {
                return when (position) {
                    0 -> StringUtils.getString(R.string.movieAll)
                    1 -> createTabText(R.string.episode, upperInfoBean?.videoCount)
                    2 -> createTabText(R.string.short_video, upperInfoBean?.smallVideoCount)
                    3 -> createTabText(R.string.dynamic, upperInfoBean?.trendsCount)
                    else -> createTabText(R.string.pictures, upperInfoBean?.photoCount)
                }
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//                super.destroyItem(container, position, `object`)
            }
        }
        tabLayout.setupWithViewPager(upList)
        tabLayoutTop.setupWithViewPager(upList)
    }

    private fun createTabText(res: Int, count: Int?): SpannableStringBuilder {
        val spannableString = SpannableStringBuilder(StringUtils.getString(res))
        if (count != null && count > 0) {
            spannableString.append(" $count")
            val textSpan = AbsoluteSizeSpan(SizeUtils.dp2px(12f))
            spannableString.setSpan(
                textSpan,
                spannableString.length - count.toString().length,
                spannableString.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
        }
        return spannableString
    }

    private fun setUpperInfo(upperInfoBean: UpperInfoBean) {
        accountForbidden.isVisible = upperInfoBean.frozenStatus
        if (!upperInfoBean.avatar.isNullOrEmpty()) {
            ImageUtil.loadCircleImage(this, upperInfoBean.avatar, userLogo)
        }
        if (upperInfoBean.bigV) {
            userVip.visibility = View.VISIBLE
            userVip.setImageResource(R.mipmap.icon_big_v)
        } else {
            if (upperInfoBean.vipLevel > 0) {
                userVip.visibility = View.VISIBLE
                userVip.setImageResource(VipGradeImageUtil.getVipImage(upperInfoBean.vipLevel))
            }
        }
        if (!upperInfoBean.from.isNullOrEmpty()) {
            userLocal.visibility = View.VISIBLE
            userLocal.text = upperInfoBean.from
        }
        // 0女1男，-1没有选择
        if (upperInfoBean.sex == 1) {
            upperName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_sex_man_32,
                0
            )
        } else if (upperInfoBean.sex == 0) {
            upperName.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.mipmap.icon_sex_woman_32,
                0
            )
        }
        upperName.text = upperInfoBean.nickName
        upperDesc.text = upperInfoBean.introduce
        upperAttentionNumber.text = NormalUtil.formatPlayCount(upperInfoBean.worksCount)
        setUpperFansCount(upperInfoBean.fansCount)
        upperFabulousNumber.text = NormalUtil.formatPlayCount(upperInfoBean.likeCount)
        ClickUtils.applySingleDebouncing(ll_like_container) {
            LikeDetailDialog(this, upperInfoBean.likeCount).show()
        }
        setUserStatus(upperInfoBean.focusStatus, upperInfoBean.isBlackList)
        titleName.text = upperInfoBean.nickName
        if (upperInfoBean.level > 0) {
            userLevel.visibility = View.VISIBLE
            userLevel.setImageResource(VipGradeImageUtil.getVipLevel(upperInfoBean.level))
        }
        this.upperInfoBean = upperInfoBean
    }

    private fun setUpperFansCount(fansCount: Int) {
        upperFansNumber.text = NormalUtil.formatPlayCount(fansCount)
    }

    private fun setUserStatus(isFocus: Boolean, isInBlackList: Boolean) {
        if (isMyself) {
            upperAttention.isVisible = false
            blackList.isVisible = false
            upperChat.isVisible = false
        } else if (isInBlackList) {
            upperAttention.isVisible = false
            blackList.isVisible = true
            upperChat.isVisible = true
        } else {
            blackList.isVisible = false
            upperAttention.isVisible = true
            upperChat.isVisible = true
            upperAttention.isSelected = isFocus
            upperAttention.text = if (isFocus) {
                resources.getString(R.string.followed)
            } else {
                resources.getString(R.string.attention)
            }
        }
    }

    private fun getUpperInfo(userId: Int, isInitFragment: Boolean = true) {
        if (isInitFragment) {
            showProgressDialog()
        }
        val params = HttpParams()
        params.put("userId", userId)
        HttpRequest.get(RequestUrls.UPPER_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null) {
                    val upperInfo = Gson().fromJson<UpperInfoBean>(
                        response.toString(),
                        object : TypeToken<UpperInfoBean>() {}.type
                    )
                    setUpperInfo(upperInfo)
                    if (isInitFragment) {
                        setupTab()
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
            }
        }, params, this)
    }

    /**
     * 关注、取消关注接口
     */
    fun attention(view: View) {
        if (!GlobalValue.checkLogin()) return
        if (upperId == 0) {
            return
        }
        val params = HttpParams()
        params.put("userId", upperId)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")
                val event = VideoActionResultEvent()
                event.id = upperId.toString()
                event.type = 1
                event.userLogo = upperInfoBean?.avatar ?: ""
                event.userName = upperInfoBean?.nickName ?: ""
                var fansCount = upperInfoBean?.fansCount ?: 0
                if (selected) {
                    upperInfoBean?.fansCount = ++fansCount
                    setUpperFansCount(fansCount)
                    ToastUtils.showLong(StringUtils.getString(R.string.followSuccess))
                    event.action = VideoActionResultEvent.ACTION_ADD
                } else {
                    if (fansCount > 0) {
                        upperInfoBean?.fansCount = --fansCount
                        setUpperFansCount(fansCount)
                    }
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                }
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }
        }, params, this)
    }

    private fun showBlackTip(userId: Int) {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.blacklist_remove)
        dialog.setMsg(R.string.in_black_list_tip)
        dialog.setLeftListener(R.string.cancel) {
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.ensure) {
            dialog.dismiss()
            removeBlackList(userId)
        }
        dialog.show()
    }

    /**
     * 取消拉黑
     */
    private fun removeBlackList(uid: Int) {
        val params = HttpParams()
        params.put("uid", uid)
        params.put("status", true)
        HttpRequest.post(RequestUrls.CHAT_FORBIDDEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val status = response.optBoolean("status")
                EventBus.getDefault().post(BlackListEvent(uid, status))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        }, params, this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onZanChange(event: VideoActionResultEvent) {
        if (event.type != 2 || event.isCommentLike) {
            return
        }
        val fragment = fragmentCache[2]
        if (fragment != null) {
            (fragment as UpperPicturesFragment).refreshItem(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBlackListChange(event: BlackListEvent) {
        if (upperId == event.uid) {
            upperInfoBean?.isBlackList = event.status
            setUserStatus(false, event.status)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusChange(event: VideoActionResultEvent) {
        when (event.type) {
            1 -> {
                //关注
                if (upperInfoBean?.id.toString() == event.id) {
                    val focusStatus = event.action == VideoActionResultEvent.ACTION_ADD
                    upperInfoBean?.focusStatus = event.action == VideoActionResultEvent.ACTION_ADD
                    setUserStatus(focusStatus, false)
                }
            }

            2 -> {
                if (event.isCommentLike) {
                    return
                }
                val fragment = fragmentCache[2]
                if (fragment != null) {
                    (fragment as UpperPicturesFragment).refreshItem(event)
                }
            }

            else -> {}
        }

    }

    fun showLargeImage(view: View) {
        if (upperInfoBean != null && upperInfoBean!!.avatar.isNotEmpty()) {
            val intent = Intent(this, PictureViewerActivity::class.java)
            intent.putExtra(PictureViewerActivity.SHOW_TITLE, upperInfoBean!!.nickName)
            intent.putExtra(PictureViewerActivity.SHOW_URLS, arrayListOf(upperInfoBean!!.avatar))
            startActivity(intent)
        }
    }
}