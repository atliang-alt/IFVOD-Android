package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.view.children
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.android.material.appbar.AppBarLayout
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_video_wanted.*
import org.json.JSONObject
import kotlin.math.abs

/**
 * 求片
 */
class VideoWantedActivity : BaseActivity() {
    var selectType: String? = null
    var selectArea: String? = null
    private val taskResultCode: Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_wanted)
        StatusBarUtil.setTransparentForImageView(this, null)
        setScroller()
        setSelect()
    }

    private fun setSelect() {
        videoType.removeAllViews()
        val videoTypes = StringUtils.getStringArray(R.array.video_type)
        val lp = ViewGroup.MarginLayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val padding = SizeUtils.dp2px(10f)
        lp.height = SizeUtils.dp2px(32f)
        lp.rightMargin = padding
        lp.topMargin = padding
        videoTypes.forEach {
            val view = CheckBox(this)
            view.buttonDrawable = null
            view.setBackgroundResource(R.drawable.item_light_check_selector)
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            view.setTextColor(resources.getColorStateList(R.color.button_zan_text_color_selector))
            view.tag = it
            view.setOnClickListener { v ->
                setVideoType(it)
            }
            view.setPadding(padding, 0, padding, 0)
            view.text = it
            videoType.addView(view, lp)
        }
        videoArea.removeAllViews()
        val videoAreas = StringUtils.getStringArray(R.array.video_area)
        videoAreas.forEach {
            val view = CheckBox(this)
            view.buttonDrawable = null
            view.setBackgroundResource(R.drawable.item_light_check_selector)
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            view.setTextColor(resources.getColorStateList(R.color.button_zan_text_color_selector))
            view.tag = it
            view.setOnClickListener { v ->
                setVideoArea(it)
            }
            view.setPadding(padding, 0, padding, 0)
            view.text = it
            videoArea.addView(view, lp)
        }
    }

    private fun setVideoType(tag: String) {
        selectType = tag
        for (item in videoType.children) {
            (item as CheckBox).isChecked = item.tag == tag
        }
    }

    private fun setVideoArea(tag: String) {
        selectArea = tag
        for (item in videoArea.children) {
            (item as CheckBox).isChecked = item.tag == tag
        }
    }

    private fun setScroller() {
        toolbar.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                toolbar.viewTreeObserver.removeOnPreDrawListener(this)
                scrollContent.setPadding(0, 0, 0, toolbar.height)
                return false
            }

        })
        appbarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { layout, i ->
            val total = appbarLayout.totalScrollRange.toFloat()
            when {
                i == 0 -> {   //  完全展开
                    toolbar.alpha = 0f
                }

                abs(i) >= total -> { // 完全收缩
                    toolbar.alpha = 1f
                }

                abs(i) >= total - SizeUtils.dp2px(70f) -> {
                    val alpha: Float = abs(i) / total
                    toolbar.alpha = alpha
                }

                else -> {
                    toolbar.alpha = 0f
                }
            }
        })
    }

    fun onLeftBack(view: View) {
        finish()
    }

    private fun showVipTip() {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.wanted_video)
        dialog.setMsg(R.string.open_vip_not_tip)
        dialog.setLeftListener(R.string.goToUpdate) {
            dialog.dismiss()
            startActivityForResult(Intent(this, TaskCenterActivity::class.java), taskResultCode)
        }
        dialog.setRightListener(R.string.open_vip) {
            dialog.dismiss()
            val intent = Intent(this, OpenVipActivity::class.java)
            intent.putExtra("pathInfo", this.javaClass.simpleName)
            startActivity(intent)
        }
        dialog.show()
    }

    fun onSubmit(view: View) {
        if (!GlobalValue.isEnable(7)) {
            showVipTip()
            return
        }
        val name = videoName.text.toString()
        if (name.isEmpty()) {
            ToastUtils.showShort(R.string.input_video_name)
        } else {
            showProgressDialog()
            val params = HttpParams()
            params.put("Type", 1)
            val jsonObject = JSONObject()
            jsonObject.put("title", name)
            jsonObject.put("actors", videoActor.text.toString())
            jsonObject.put("director", videoDirector.text.toString())
            jsonObject.put("years", videoYear.text.toString())
            jsonObject.put("category", selectType)
            jsonObject.put("region", selectArea)
            params.put("Contxt", jsonObject.toString())
            HttpRequest.post(RequestUrls.VIDEO_WANTED, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgressDialog()
                    ToastUtils.showLong(R.string.suggest_success)
                    finish()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                    ToastUtils.showLong(errorMsg)
                }

            }, params, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == taskResultCode) {
                val index = data?.getIntExtra("index", 0)
                val intent = Intent()
                intent.putExtra("index", index)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}
