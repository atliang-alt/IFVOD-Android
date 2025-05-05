package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.main.PictureViewerActivity
import kotlinx.android.synthetic.main.activity_picture_viewer.*
import kotlinx.android.synthetic.main.layout_select_image_next.view.*
import java.io.Serializable

/**
 * 选择图片大图浏览
 */
class SelectLocalViewActivity : PictureViewerActivity() {
    companion object {
        const val SHOW_SELECT = "showSelect"
        const val SELECT_COUNT = "selectCount"
    }

    var selectData: MutableList<String>? = null
    var selectCount: Int = 1
    private var bottomView: View? = null

    override fun initView() {
        super.initView()
        if (intent.getSerializableExtra(SHOW_SELECT) != null) {
            selectData = intent.getSerializableExtra(SHOW_SELECT) as MutableList<String>
        }
        selectCount = intent.getIntExtra(SELECT_COUNT, 1)
        if (selectCount > 0) {
            val contentParam = RelativeLayout.LayoutParams(
                SizeUtils.dp2px(70f),
                SizeUtils.dp2px(28f)
            )
            val textParam = FrameLayout.LayoutParams(
                SizeUtils.dp2px(56f),
                SizeUtils.dp2px(28f)
            )
            contentParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
            contentParam.addRule(RelativeLayout.CENTER_VERTICAL)
            val rightText = findViewById<TextView>(R.id.rightText)
            val rightContent = findViewById<View>(R.id.rightContent)
            val rightImage = findViewById<ImageView>(R.id.rightImage)
            rightContent.layoutParams = contentParam
            rightText.layoutParams = textParam
            rightText.visibility = View.VISIBLE
            rightText.setBackgroundResource(R.drawable.button_shape_solid_1_corners_4)
            rightText.setTextColor(ColorUtils.getColor(R.color.button_text_color_selector))
            val buttonText = intent.getStringExtra(SelectLocalImageActivity.buttonText)
            if (!buttonText.isNullOrEmpty()) {
                rightText.text = buttonText
            }
            bottomView = View.inflate(this, R.layout.layout_select_image_next, null)
            bottomView!!.selectImage.setOnClickListener {
                val url = showUrls[pictureLarge.currentItem]
                if (!(selectData?.filter { it == url }).isNullOrEmpty()) {
                    selectData?.remove(url)
                    rightImage.isSelected = false
                } else {
                    if (selectCount <= 1) {
                        // 单选
                        selectData?.clear()
                    } else if ((selectData?.size ?: 0) >= selectCount) {
                        // 多选图片最多选择的张数
                        ToastUtils.showLong(StringUtils.getString(R.string.selectCountTip, selectCount))
                        return@setOnClickListener
                    }
                    selectData?.add(url)
                    rightImage.isSelected = true
                }
                isSelectView(pictureLarge.currentItem)
                setSelectText()
            }
            bottomContainer.addView(bottomView)
        }
        setPage()
    }

    private fun setPage() {
        setSelectText()
        isSelectView(pictureLarge.currentItem)
        pictureLarge.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                isSelectView(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    fun onRightClick(view: View) {
        val intent = Intent()
        intent.putExtra(SelectLocalImageActivity.imagePathList, selectData as Serializable)
        intent.putExtra("isFinish", true)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun isSelectView(position: Int) {
        val url = showUrls[position]
        bottomView?.selectImage?.isSelected = !(selectData?.filter { it == url }).isNullOrEmpty()
    }

    override fun onBackPressed() {
        if (selectData.isNullOrEmpty()) {
            finish()
        } else {
            val intent = Intent()
            intent.putExtra(SelectLocalImageActivity.imagePathList, selectData as Serializable)
            intent.putExtra("isFinish", false)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun setSelectText() {
        bottomView?.selectCount?.text =
            StringUtils.getString(R.string.selected, selectData?.size ?: 0)
        findViewById<TextView>(R.id.rightText).isEnabled = selectData?.size ?: 0 > 0
    }
}