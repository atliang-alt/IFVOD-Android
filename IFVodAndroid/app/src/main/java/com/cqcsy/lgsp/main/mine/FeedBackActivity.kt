package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.bean.AreaBean
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.library.pay.area.AreaSelectActivity
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_feed_back.*
import org.json.JSONObject
import java.io.File


/**
 * 在线反馈
 */
class FeedBackActivity : NormalActivity() {
    var selectInternet = ""
    var imageList: MutableList<String> = ArrayList()

    override fun getContainerView(): Int {
        return R.layout.activity_feed_back
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.feed_back)
        setInternetEnvironment()
        setSelectImage()
        questionDesc.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.isNotEmpty()) {
                    setButtonState()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        startLocation()
    }

    private fun startLocation() {
        HttpRequest.get(RequestUrls.GET_USER_REGION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                country.text = response?.optString("country")
                country.setCompoundDrawablesWithIntrinsicBounds(
                    R.mipmap.icon_location,
                    0,
                    R.mipmap.icon_grey_arrow,
                    0
                )
                setButtonState()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(R.string.location_permission_deny)
            }
        }, tag = this)
    }

    fun startLocation(view: View) {
        startLocation()
    }

    private fun setInternetEnvironment() {
        internetEnvironment.removeAllViews()
        val array = StringUtils.getStringArray(R.array.internet_environment)
        val lp = ViewGroup.MarginLayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val padding = SizeUtils.dp2px(10f)
        lp.height = SizeUtils.dp2px(32f)
        lp.rightMargin = padding
        lp.topMargin = padding
        array.forEach {
            val view: CheckedTextView = View.inflate(this, R.layout.layout_internet_item, null) as CheckedTextView
            view.tag = it
            view.setOnClickListener { v ->
                setCheckItem(it)
                setButtonState()
            }
            view.setPadding(padding, 0, padding, 0)
            view.text = it
            internetEnvironment.addView(view, lp)
        }
    }

    private fun setCheckItem(tag: String) {
        selectInternet = tag
        for (item in internetEnvironment.children) {
            (item as CheckedTextView).isChecked = item.tag == tag
        }
    }

    private fun setSelectImage() {
        imageList.add(R.mipmap.image_upload_add.toString())

        imageSelected.layoutManager = GridLayoutManager(this, 3)
        imageSelected.addItemDecoration(
            XGridBuilder(this).setHLineSpacing(10f).setVLineSpacing(10f).build()
        )

        val adapter = object : BaseQuickAdapter<String, BaseViewHolder>(R.layout.layout_item_image_with_delete, imageList) {
            override fun convert(holder: BaseViewHolder, item: String) {
                val delete = holder.getView<ImageView>(R.id.item_delete)
                holder.itemView.tag = item
                if (item == R.mipmap.image_upload_add.toString()) {
                    holder.setImageResource(R.id.item_image, R.mipmap.image_upload_add)
                    delete.visibility = View.GONE
                } else {
                    ImageUtil.loadImage(
                        this@FeedBackActivity,
                        item,
                        holder.getView(R.id.item_image)
                    )
                    delete.visibility = View.VISIBLE
                    delete.setOnClickListener {
                        imageList.remove(item)
                        notifyItemRemoved(holder.absoluteAdapterPosition)
                        if (imageList.size < 3 && !imageList.contains(R.mipmap.image_upload_add.toString())) {
                            imageList.add(R.mipmap.image_upload_add.toString())
                            notifyItemInserted(imageList.size - 1)
                        }
                    }
                }
            }

        }
        adapter.setOnItemClickListener { _, _, position ->
            if (adapter.getItem(position) == R.mipmap.image_upload_add.toString()) {
                val intent = Intent(this, SelectLocalImageActivity::class.java)
                intent.putExtra(
                    SelectLocalImageActivity.maxCountKey,
                    if (imageList.size == 0) 3 else 3 - (imageList.size - 1)
                )
                intent.putExtra(SelectLocalImageActivity.isCutPhotoKey, false)
                intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
                startActivityForResult(intent, 1000)
            }
        }
        imageSelected.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    if (data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) != null) {
                        imageList.remove(R.mipmap.image_upload_add.toString())
                        val result = data.getSerializableExtra(SelectLocalImageActivity.imagePathList) as MutableList<LocalMediaBean>
                        result.forEach {
                            PictureUploadManager.getAbsolutePath(it.path)?.let { it1 -> imageList.add(it1) }
                        }
                        if (imageList.size < 3 && !imageList.contains(R.mipmap.image_upload_add.toString())) {
                            imageList.add(R.mipmap.image_upload_add.toString())
                        }
                        imageSelected.adapter?.notifyDataSetChanged()
                    }
                }

                1001 -> {
                    val area = data?.getSerializableExtra(AreaSelectActivity.selectedArea) as AreaBean
                    country.text = area.chinese
                    country.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_grey_arrow, 0)
                    setButtonState()
                }
            }

        }
    }

    fun selectArea(view: View) {
        startActivityForResult(Intent(this, AreaSelectActivity::class.java), 1001)
    }

    fun setButtonState() {
        buttonSubmit.isEnabled = country.text.isNotEmpty() && selectInternet.isNotEmpty() && questionDesc.text.isNotEmpty()
    }

    fun submitSuggestion(view: View) {
        when {
            country.text.isEmpty() -> {
                ToastUtils.showShort(R.string.choose_area_tip)
            }

            selectInternet.isEmpty() -> {
                ToastUtils.showShort(R.string.choose_net_env)
            }

            questionDesc.text.isEmpty() -> {
                ToastUtils.showShort(R.string.input_question_tip)
            }

            else -> {
                uploadImage()
            }
        }
    }

    private fun submit(imageUrls: String) {
        if (imageUrls.isEmpty()) {
            showProgressDialog()
        }
        val params = HttpParams()
        params.put("Area", country.text.toString())
        params.put("Environment", selectInternet)
        params.put("Describe", questionDesc.text.toString())
        params.put("Email", emailEdit.text.toString())
        params.put("Image", imageUrls)
        HttpRequest.post(RequestUrls.ONLINE_SUGGESTION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                ToastUtils.showLong(R.string.suggest_success)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
            }
        }, params, this)
    }

    private fun uploadImage() {
        if (imageList.isEmpty() || (imageList.size == 1 && imageList[0] == R.mipmap.image_upload_add.toString())) {
            submit("")
        } else {
            showProgressDialog()
            val params = HttpParams()
            imageList.forEach {
                if (FileUtils.isFileExists(it)) {
                    var imagePath = it
                    if (ImageUtils.getImageType(it) == ImageUtils.ImageType.TYPE_WEBP) {
                        imagePath = ImageUtil.formatJpePath(it)
                    }
                    params.put(imagePath, File(imagePath))
                }
            }
            HttpRequest.post(RequestUrls.UPLOAD_FILE_NOT_LOGIN, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    response?.optString("filepath")?.let { submit(it) }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                }
            }, params, this)
        }
    }
}