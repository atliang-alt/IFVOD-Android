package com.cqcsy.lgsp.upper.pictures

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_picture_viewer.*
import kotlinx.android.synthetic.main.layout_select_image_next.view.*
import org.json.JSONObject
import java.io.Serializable

/**
 * 查看相册大图
 */
class ViewAllActivity : PictureViewerActivity() {
    companion object {
        const val PID = "pid"
        const val SHOW_DATA = "showData"
        const val SHOW_SELECT = "showSelect"
        const val IMAGE_LIST = "imageList" // 选择的图片集合
    }

    private val size = 20
    var mediaKey: String? = null
    var isLoading = false
    var showData: MutableList<ImageBean>? = null
    var selectData: MutableList<String>? = null
    private var bottomView: View? = null

    override fun initView() {
        super.initView()
        if (intent.getSerializableExtra(SHOW_DATA) != null) {
            showData = intent.getSerializableExtra(SHOW_DATA) as MutableList<ImageBean>
        }
        mediaKey = intent.getStringExtra(PID)
        if (intent.getSerializableExtra(SHOW_SELECT) != null) {
            selectData = intent.getSerializableExtra(SHOW_SELECT) as MutableList<String>
        }
        if (showBottom) {
            if (intent.getSerializableExtra(UpperPicturesFragment.PICTURES_ITEM) != null) {
                val picturesBean =
                    intent.getSerializableExtra(UpperPicturesFragment.PICTURES_ITEM) as PicturesBean
                val commentContainer = CommentView(this)
                commentContainer.setPictures(picturesBean)
                bottomContainer.addView(
                    commentContainer,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
            if (selectData != null) {
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
                rightContent.layoutParams = contentParam
                rightText.layoutParams = textParam
                rightText.visibility = View.VISIBLE
                rightText.setBackgroundResource(R.drawable.button_shape_solid_1_corners_4)
                rightText.setTextColor(ColorUtils.getColor(R.color.button_text_color_selector))
                rightText.text = StringUtils.getString(R.string.finish)
                bottomView = View.inflate(this, R.layout.layout_select_image_next, null)
                bottomView!!.selectImage.setOnClickListener {
                    val url = showUrls[pictureLarge.currentItem]
                    if (!(selectData?.filter { it == url }).isNullOrEmpty()) {
                        selectData?.remove(url)
                    } else {
                        selectData?.clear() // 单选清空
                        selectData?.add(url)
                    }
                    isSelectView(pictureLarge.currentItem)
                    setSelectText()
                }
                bottomContainer.addView(bottomView)
            }
        }
        setPage()
    }

    private fun setPage() {
        showData?.let { setShowUrlsWithList(it) }
        if (selectData != null) {
            setSelectText()
            isSelectView(pictureLarge.currentItem)
        }
        pictureLarge.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (selectData != null) {
                    isSelectView(position)
                }
                if (hasMore(position)) {
                    loadMore()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun isSelectView(position: Int) {
        val url = showUrls[position]
        bottomView?.selectImage?.isSelected = !(selectData?.filter { it == url }).isNullOrEmpty()
    }

    private fun setSelectText() {
        bottomView?.selectCount?.text =
            StringUtils.getString(R.string.selected, selectData?.size ?: 0)
        findViewById<TextView>(R.id.rightText).isEnabled = selectData?.size ?: 0 > 0
    }

    private fun setShowUrlsWithList(data: MutableList<ImageBean>) {
        data.forEach {
            it.imgPath?.let { it1 -> showUrls.add(it1) }
        }
        setPagerIndexStatus()
        pictureLarge.adapter?.notifyDataSetChanged()
    }

    private fun hasMore(position: Int): Boolean {
        if (showData == null) {
            return false
        }
        if (showData!!.size < showCount && showData!!.size - position <= 3) {
            return true
        }
        return false
    }

    private fun loadMore() {
        if (isLoading) {
            return
        }
        isLoading = true
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("page", showData!!.size / size + 1)
        params.put("size", size)
        HttpRequest.get(RequestUrls.ALBUM_DETAILS, params = params, callBack = object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list: MutableList<ImageBean> = GsonUtils.fromJson(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<ImageBean>>() {}.type
                )
                setShowUrlsWithList(list)
                showData?.addAll(list)
                isLoading = false
            }

            override fun onError(response: String?, errorMsg: String?) {
                isLoading = false
            }

        })
    }

    fun onRightClick(view: View) {
        if (selectData.isNullOrEmpty()) {
            finish()
        } else {
            startResult(true)
        }
    }

    override fun onBackPressed() {
        if (selectData.isNullOrEmpty()) {
            finish()
        } else {
            startResult(false)
        }
    }

    private fun startResult(isFinish: Boolean) {
        val intent = Intent()
        intent.putExtra(IMAGE_LIST, selectData as Serializable)
        intent.putExtra("isFinish", isFinish)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}