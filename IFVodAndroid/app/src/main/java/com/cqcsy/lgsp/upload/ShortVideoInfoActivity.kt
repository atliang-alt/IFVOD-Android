package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.CategoryBean
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.bean.net.CategoryNetBean
import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.manger.UploadCacheManger
import com.cqcsy.lgsp.event.UploadEvent
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.views.FlowLayout
import com.cqcsy.library.base.BaseService
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_short_video_info.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/**
 * 上传小视频信息完善信息页
 */
class ShortVideoInfoActivity : NormalActivity() {
    companion object {
        const val LOCAL_BEAN = "localMediaBean"
        const val SHORT_BEAN = "shortVideoBean"

        // 0:选择视频页进入 1: 上传列表审核不通过进入
        const val FORM_TYPE = "formType"
    }

    private var localMediaBean: LocalMediaBean? = null
    private var shortVideoBean: ShortVideoBean? = null
    private var classifyData: MutableList<CategoryBean> = ArrayList()
    private var classifyTagsList: MutableMap<String, MutableList<CategoryBean>> = HashMap()

    // 保存选中的分类对应标签信息
    private var selectTags: MutableList<CategoryBean> = ArrayList()

    // 封面图路径
    private var faceImagePath: String = ""

    private val selectLocalImageCode = 1001

    private val selectVideoFaceCode = 1002

    private var formType = 0

    override fun getContainerView(): Int {
        return R.layout.activity_short_video_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.uploadShortVideo)
        initData()
        initView()
    }

    private fun initData() {
        formType = intent.getIntExtra(FORM_TYPE, 0)
    }

    private fun initView() {
        if (formType == 1) {
            // 重新编辑
            shortVideoBean = intent.getSerializableExtra(SHORT_BEAN) as ShortVideoBean
            faceImagePath = shortVideoBean!!.coverImgUrl!!
            selectFaceImage.visibility = View.GONE
            editTitle.setText(shortVideoBean?.title)
            if (shortVideoBean?.title!!.length <= 20) {
                editTitle.setSelection(shortVideoBean?.title!!.length)
            } else {
                editTitle.setSelection(20)
            }
            editContext.setText(shortVideoBean?.introduce)
            editContext.setSelection(shortVideoBean?.introduce!!.length)
            uploadBtn.isEnabled = true
            ImageUtil.loadImage(
                this,
                faceImagePath,
                faceImage,
                0,
                scaleType = ImageView.ScaleType.CENTER
            )
        } else {
            selectFaceImage.visibility = View.VISIBLE
            localMediaBean = intent.getSerializableExtra(LOCAL_BEAN) as LocalMediaBean
            ImageUtil.loadImage(
                this,
                localMediaBean!!.path,
                faceImage,
                0,
                scaleType = ImageView.ScaleType.CENTER
            )
        }
        editTitle.addTextChangedListener { text ->
            uploadBtn.isEnabled = text!!.isNotEmpty() && selectTags.isNotEmpty()
        }
        getClassifyData()
    }

    /**
     * 动态添加一级标签View
     */
    private fun addClassifyView() {
        selectTags.clear()
        classifyLayout.removeAllViews()
        val tvParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvParams.topMargin = SizeUtils.dp2px(10f)
        for (bean in classifyData) {
            // 添加标题(游戏、华人、新闻等)
            val textView = TextView(this)
            textView.text = bean.classifyName
            textView.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            textView.layoutParams = tvParams
            classifyLayout.addView(textView)
            // 添加标签下所有子标签
            addTagsLayout(classifyTagsList[bean.classifyId])
        }
    }

    /**
     * 动态添加子标签数据View
     */
    private fun addTagsLayout(list: MutableList<CategoryBean>?) {
        if (list.isNullOrEmpty()) {
            return
        }
        val lp = ViewGroup.MarginLayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val padding = SizeUtils.dp2px(10f)
        lp.height = SizeUtils.dp2px(32f)
        lp.rightMargin = padding
        lp.topMargin = padding
        val flowLayout = FlowLayout(this)
        for (bean in list) {
            val view = View.inflate(this, R.layout.layout_internet_item, null) as CheckedTextView
            view.text = bean.classifyName
            if (shortVideoBean != null) {
                val labelsList = shortVideoBean!!.contentType?.split("·")
                if (!labelsList.isNullOrEmpty()) {
                    for (labelIt in labelsList) {
                        if (bean.classifyName == labelIt || bean.subID == labelIt) {
                            selectTags.add(bean)
                            view.isChecked = true
                            break
                        }
                    }
                }
            }
            view.setOnClickListener {
                if (view.isChecked) {
                    selectTags.remove(bean)
                    view.isChecked = false
                } else {
                    if (selectTags.size < 3) {
                        selectTags.add(bean)
                        view.isChecked = true
                    }
                }
                uploadBtn.isEnabled =
                    editTitle.text.isNotEmpty() && selectTags.isNotEmpty()
            }
            flowLayout.addView(view, lp)
        }
        classifyLayout.addView(flowLayout)
        uploadBtn.isEnabled =
            editTitle.text.isNotEmpty() && selectTags.isNotEmpty()
    }

    private fun getClassifyData() {
        HttpRequest.post(RequestUrls.CHANNEL_NAVIGATION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                for (i in 0 until jsonArray.length()) {
                    val categoryNetBean =
                        Gson().fromJson(jsonArray[i].toString(), CategoryNetBean::class.java)
                    val leftCategoryBean = CategoryBean()
                    leftCategoryBean.classifyId = categoryNetBean.categoryId
                    leftCategoryBean.classifyName = categoryNetBean.name
                    if (categoryNetBean.list.isNullOrEmpty()) {
                        continue
                    }
                    val rightArray =
                        JsonParser.parseString(Gson().toJson(categoryNetBean.list)).asJsonArray
                    val rightList: MutableList<CategoryBean> =
                        Gson().fromJson(
                            rightArray.toString(),
                            object : TypeToken<MutableList<CategoryBean>>() {}.type
                        )
                    if (categoryNetBean.type == 2) {
                        classifyData.add(leftCategoryBean)
                        classifyTagsList[categoryNetBean.categoryId] = rightList
                    }
                }
                addClassifyView()
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, tag = this)
    }

    fun manualUpload(view: View) {
        val intent = Intent(this, SelectLocalImageActivity::class.java)
        intent.putExtra(SelectLocalImageActivity.widthKey, 16f)
        intent.putExtra(SelectLocalImageActivity.heightKey, 9f)
        intent.putExtra(SelectLocalImageActivity.isCutPhotoKey, true)
        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
        startActivityForResult(intent, selectLocalImageCode)
    }

    fun selectFace(view: View) {
        localMediaBean?.let {
            SelectVideoFaceActivity.launch(this, it.path, 16f, 9f, selectVideoFaceCode)
        }
    }

    /**
     * 确定上传
     */
    fun uploadBtn(view: View) {
        showProgressDialog()
        if (formType == 1) {
            val params = HttpParams()
            params.put("LID", NormalUtil.formatMediaId(shortVideoBean!!.mediaId))
            params.put("CID", "0,3")
            params.put("Labels", getTagIds(selectTags))
            params.put("Title", editTitle.text.toString())
            params.put("Contxt", editContext.text.toString())
            params.put("UID", GlobalValue.userInfoBean!!.token.uid)
            params.put("Corver", imageBase64(faceImage))
            HttpRequest.post(RequestUrls.RESUBMIT_UPLOAD_INFO, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgressDialog()
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                    ToastUtils.showLong(errorMsg)
                }

            }, params, this)
        } else {
            val upload = UploadCacheBean()
            upload.title = editTitle.text.toString()
            upload.context = editContext.text.toString()
            upload.path = NormalUtil.getAbsolutePath(localMediaBean!!.path) ?: ""
            upload.videoSize = localMediaBean!!.size
            upload.imageBase = imageBase64(faceImage)
            upload.imagePath = faceImagePath
            upload.cid = "0,3"
            upload.labels = getTagIds(selectTags)
            upload.status = Constant.UPLOAD_WAIT
            val result = Utils.Consumer<Boolean> {
                val autoNetDownload =
                    SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, false)
                if (autoNetDownload || it) {
                    startUpload(upload)
                } else {
                    tipDialog(upload)
                }
            }
            NetworkUtils.isWifiAvailableAsync(result)
        }
    }

    private fun tipDialog(uploadCacheBean: UploadCacheBean) {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.netWifiTips)
        dialog.setLeftListener(R.string.cancel) {
            dismissProgressDialog()
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.continueUpload) {
            dialog.dismiss()
            startUpload(uploadCacheBean)
        }
        dialog.show()
    }

    /**
     * 上传视频
     */
    private fun startUpload(uploadInfoBean: UploadCacheBean) {
        if (uploadInfoBean.path.isEmpty()) {
            dismissProgressDialog()
            ToastUtils.showShort(R.string.upLoadPath)
            return
        }
        val uploadCacheBean = UploadCacheManger.instance.select(uploadInfoBean.path)
        if (uploadCacheBean != null) {
            dismissProgressDialog()
            ToastUtils.showShort(R.string.upLoadExit)
            return
        }
        val intent = Intent(this, UploadService::class.java)
        intent.putExtra(UploadService.UPLOAD_INFO, uploadInfoBean)
        BaseService.startService(this, intent)
    }

    private fun imageBase64(imageView: ImageView): String {
        val bos = ByteArrayOutputStream()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
    }

    /**
     * 获取分类对应的标签ID
     */
    private fun getTagIds(list: MutableList<CategoryBean>): String {
        var tagIds = ""
        for (it in list) {
            tagIds = if (tagIds.isEmpty()) {
                if (!it.subID.isNullOrEmpty()) {
                    it.subID.toString()
                } else {
                    it.classifyName
                }
            } else {
                if (!it.subID.isNullOrEmpty()) {
                    tagIds + "," + it.subID
                } else {
                    tagIds + "," + it.classifyName
                }
            }
        }
        return tagIds
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(uploadEvent: UploadEvent) {
        dismissProgressDialog()
        if (uploadEvent.event) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == selectLocalImageCode) {
                faceImagePath = data?.getStringExtra("clipPath").toString()
            }
            if (requestCode == selectVideoFaceCode) {
                faceImagePath = data?.getStringExtra("facePath").toString()
            }
            ImageUtil.loadImage(
                this,
                faceImagePath,
                faceImage,
                0,
                scaleType = ImageView.ScaleType.CENTER_CROP
            )
        }
    }

    override fun onDestroy() {
        FileUtils.delete(GlobalValue.VIDEO_IMAGE_CLIP)
        super.onDestroy()
    }
}