package com.cqcsy.lgsp.main.mine

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import kotlinx.android.synthetic.main.activity_local_video_detail.*
import kotlinx.android.synthetic.main.layout_dynamic_video_bottom_view.*

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/24
 *
 * 动态视频详情页
 */
class DynamicLocalVideoDetailActivity : BaseActivity() {
    companion object {

        @JvmStatic
        fun launch(
            context: Context?,
            dynamicData: DynamicCacheBean,
        ) {
            val intent = Intent(context, DynamicLocalVideoDetailActivity::class.java)
            intent.putExtra("dynamic_data", dynamicData)
            context?.startActivity(intent)
        }
    }

    private var dynamicData: DynamicCacheBean? = null
    private var bottomView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.transparentStatusBar(this)
        setContentView(R.layout.activity_local_video_detail)
        title_container.updateLayoutParams<FrameLayout.LayoutParams> {
            topMargin = BarUtils.getStatusBarHeight()
        }
        dynamicData = intent.getSerializableExtra("dynamic_data") as DynamicCacheBean
        initVideoBottomView()
        initView()
        dynamicData?.let {
            fillData(it)
            var videoCompressPath = it.videoCompressPath
            if (videoCompressPath.isEmpty()) {
                videoCompressPath = it.videoPath
            }
            setVideoPlayer(videoCompressPath)
        }
    }

    private fun initView() {
        bottom_input_container.isVisible = false
        tv_follow.isVisible = false
        iv_back.setOnClickListener { finish() }
        user_action.setOnClickListener { showUserAction() }
    }

    private fun initVideoBottomView() {
        bottomView = layoutInflater.inflate(R.layout.layout_dynamic_video_bottom_view, null, false)
        video_player.addDynamicBottomView(bottomView!!)
    }

    private fun setVideoPlayer(url: String) {
        video_player.isLooping = true
        video_player.setUp(url, false, "")
        video_player.startPlayLogic()
    }

    private fun showExpandAnim() {
        tv_expand.isVisible = false
        tv_fold.isVisible = true
        expand_text.movementMethod = ScrollingMovementMethod.getInstance()
        content_container.transitionToEnd()
    }

    private fun showFoldAnim() {
        tv_expand.isVisible = true
        tv_fold.isVisible = false
        expand_text.movementMethod = null
        content_container.transitionToStart()
    }

    private fun fillData(data: DynamicCacheBean) {
        if (data.description.isEmpty()) {
            fold_text.isVisible = false
            action_container.isVisible = false
        } else {
            fold_text.isVisible = true
            val content = Html.fromHtml(data.description.replace("\n", "<br/>"))
            fold_text.initWidth(ScreenUtils.getScreenWidth() - SizeUtils.dp2px(24f))
            fold_text.text = content
            expand_text.text = content
            if (fold_text.isOverSize(content)) {
                action_container.isVisible = true
                tv_expand.isVisible = true
                tv_fold.isVisible = false
            }
        }
        tv_time.text = TimeUtils.date2String(
            TimesUtils.formatDate(data.createTime),
            "yyyy-MM-dd HH:mm"
        )
        val userInfoBean = GlobalValue.userInfoBean
        if (userInfoBean != null) {
            tv_user_name.text = userInfoBean.nickName ?: ""
            ImageUtil.loadCircleImage(this, userInfoBean.avatar, iv_avatar)
            if (userInfoBean.bigV || userInfoBean.vipLevel > 0) {
                userVipImage.visibility = View.VISIBLE
                userVipImage.setImageResource(
                    if (userInfoBean.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                        userInfoBean.vipLevel
                    )
                )
            } else {
                userVipImage.visibility = View.GONE
            }
        }

        if (data.address.isNotEmpty()) {
            tv_location.visibility = View.VISIBLE
            tv_location.text = data.address
        } else {
            tv_location.visibility = View.GONE
        }
        addTags(data.labels)
        tv_follow.setOnClickListener {
            if (!GlobalValue.isLogin()) {
                startLogin()
            }
        }

        tv_expand.setOnClickListener {
            showExpandAnim()
        }
        tv_fold.setOnClickListener {
            showFoldAnim()
        }
    }

    private fun showUserAction() {
        val menu =
            LayoutInflater.from(this).inflate(R.layout.layout_manage_releasing_dynamic_menu, null)
        val popupWindow = PopupWindow(
            menu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        menu.findViewById<View>(R.id.deleteDynamic).setOnClickListener {
            deleteDialog()
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = menu
        popupWindow.isOutsideTouchable = true
        val lp = window.attributes
        lp.alpha = 0.5f
        window.attributes = lp
        popupWindow.setOnDismissListener {
            lp.alpha = 1f
            window.attributes = lp
        }
        popupWindow.showAsDropDown(user_action, -130, 0)
    }

    private fun deleteDialog() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.deleteDynamic)
        tipsDialog.setMsg(R.string.deleteDynamicTips)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            if (dynamicData != null) {
                DynamicCacheManger.instance.delete(dynamicData!!.id)
            }
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    private fun addTags(label: String?) {
        tag_group.removeAllViews()
        if (!label.isNullOrEmpty()) {
            val tags = label.split(",")
            if (tags.isNotEmpty()) {
                tag_group.visibility = View.VISIBLE
                val color = ColorUtils.getColor(R.color.white_60)
                val padding = SizeUtils.dp2px(5f)
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.rightMargin = SizeUtils.dp2px(10f)
                params.bottomMargin = SizeUtils.dp2px(5f)
                tags.forEach {
                    val tagText = TextView(this)
                    tagText.setBackgroundResource(R.drawable.tag_bg)
                    tagText.text = it
                    tagText.setTextColor(color)
                    tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    tagText.setPadding(padding, 4, padding, 4)

                    tag_group.addView(tagText, params)
                }
            }
        }
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onResume() {
        super.onResume()
        video_player.onVideoResume()
    }

    override fun onPause() {
        super.onPause()
        video_player.onVideoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        video_player.release()
    }
}