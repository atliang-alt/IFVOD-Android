package com.cqcsy.lgsp.main.mine

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.DynamicTagBean
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.database.bean.DynamicCacheBean
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.upload.SelectVideoFaceActivity
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.utils.VideoUtil
import com.cqcsy.library.views.FlowLayout
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_release_dynamic.*
import kotlinx.android.synthetic.main.activity_release_dynamic_video.*
import kotlinx.android.synthetic.main.activity_release_dynamic_video.editContent
import kotlinx.android.synthetic.main.activity_release_dynamic_video.location
import kotlinx.android.synthetic.main.activity_release_dynamic_video.tagContent
import kotlinx.android.synthetic.main.activity_release_dynamic_video.tv_tag_count
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 作者：wangjianxiong
 * 创建时间：2022/10/24
 *
 * 发布动态小视频
 */
class ReleaseDynamicVideoActivity : NormalActivity() {

    companion object {
        private const val MAX_TAG = 10

        /**
         * 发布新动态
         */
        @JvmStatic
        fun launch(context: Context, videoMediaBean: LocalMediaBean) {
            val intent = Intent(context, ReleaseDynamicVideoActivity::class.java)
            intent.putExtra("video_media", videoMediaBean)
            context.startActivity(intent)
        }

        /**
         * 编辑已发布的动态
         */
        @JvmStatic
        fun launch(fragment: Fragment, dynamicBean: DynamicBean?, requestCode: Int) {
            val intent = Intent(fragment.context, ReleaseDynamicVideoActivity::class.java)
            intent.putExtra("dynamicBean", dynamicBean)
            fragment.startActivityForResult(intent, requestCode)
        }

        /**
         * 编辑发布失败的动态
         */
        @JvmStatic
        fun launch(fragment: Fragment, dynamicBean: DynamicCacheBean, requestCode: Int) {
            val intent = Intent(fragment.context, ReleaseDynamicVideoActivity::class.java)
            intent.putExtra("dynamic_cache_bean", dynamicBean)
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    private val locationCode = 1001

    private var mediaKey: String? = ReleaseDynamicVideoActivity::class.java.simpleName
    private var latitude: Double = 0.00
    private var longitude: Double = 0.00

    // 显示地址 如：中国·北京·北京大学
    private var address: String = ""

    // 详细地址 如：重庆市江北街国金中心T1写字楼
    private var detailedAddress: String = ""
    private val selectTags = mutableListOf<String>()
    private var progressDialog: Dialog? = null
    private var cancelTipDialog: TipsDialog? = null
    private var progressView: View? = null
    private var imageWidthRatio = 16f
    private var imageHeightRatio = 9f

    /**
     * 封面地址
     */
    private var coverImgPath: String? = null

    /**
     * 封面链接
     */
    private var coverImgUrl: String? = null

    /**
     * 视频地址
     */
    private var videoPath: String? = null
    private var videoId: Int? = null

    /**
     * 视频链接
     */
    private var videoUrl: String? = null
    private var dynamicCacheBean: DynamicCacheBean? = null
    override fun getContainerView(): Int {
        return R.layout.activity_release_dynamic_video
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.releaseDynamic)
        setRightText(R.string.publish)
        setRightTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.publish_text_color,
                null
            )
        )
        setRightTextEnabled(false)
        initData()
        initView()
        initTag()
        requestPermissions()
    }

    private fun initData() {
        val dynamicBean = intent.getSerializableExtra("dynamicBean") as? DynamicBean
        val dynamicCacheBean =
            intent.getSerializableExtra("dynamic_cache_bean") as? DynamicCacheBean
        if (dynamicBean != null) {
            mediaKey = dynamicBean.mediaKey
            coverImgUrl = dynamicBean.cover
            latitude = dynamicBean.latitude ?: 0.0
            longitude = dynamicBean.longitude ?: 0.0
            address = dynamicBean.address ?: ""
            videoUrl = dynamicBean.mediaUrl
            videoPath = dynamicBean.videoPath
            val label = dynamicBean.label
            if (!label.isNullOrEmpty()) {
                selectTags.clear()
                selectTags.addAll(label.split(",").toMutableList())
            }
            val trendsDetails = dynamicBean.trendsDetails
            if (!trendsDetails.isNullOrEmpty()) {
                videoId = trendsDetails[0].refID
            }
            detailedAddress = dynamicBean.detailedAddress ?: ""
            if (address.isEmpty()) {
                location.text = StringUtils.getString(R.string.noShowLocation)
            } else {
                location.text = address
            }
            setHeaderTitle(R.string.editDynamic)
            setRightTextEnabled(true)
            editContent.setText(Html.fromHtml(dynamicBean.description?.replace("\n", "<br>")))
            editContent.setSelection(editContent.text.length)
            setCoverRatio(dynamicBean.imageRatioValue)
            ImageUtil.loadImage(this, coverImgUrl, coverImage)
        } else if (dynamicCacheBean != null) {
            this.dynamicCacheBean = dynamicCacheBean
            coverImgPath = dynamicCacheBean.coverPath
            latitude = dynamicCacheBean.latitude
            longitude = dynamicCacheBean.longitude
            address = dynamicCacheBean.address
            videoPath = dynamicCacheBean.videoPath
            detailedAddress = dynamicCacheBean.detailedAddress
            val label = dynamicCacheBean.labels
            if (label.isNotEmpty()) {
                selectTags.clear()
                selectTags.addAll(label.split(",").toMutableList())
            }
            setLocationAddress(address)
            setHeaderTitle(R.string.editDynamic)
            setRightTextEnabled(true)
            editContent.setText(Html.fromHtml(dynamicCacheBean.description.replace("\n", "<br>")))
            editContent.setSelection(editContent.text.length)
            setCoverRatio(dynamicCacheBean.imageRatioValue)
            ImageUtil.loadImage(this, coverImgPath, coverImage)
        } else {
            initVideoConfig()
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun initVideoConfig() {
        val localVideoBean = intent.getSerializableExtra("video_media") as? LocalMediaBean
        videoPath = NormalUtil.getAbsolutePath(localVideoBean?.path)
        videoPath?.let {
            object : AsyncTask<String, Int, IntArray>() {
                override fun doInBackground(vararg params: String): IntArray {
                    val videoBitmapUtil = VideoUtil(params[0])
                    coverImgPath = videoBitmapUtil.getFirstFrame()
                    val videoRatio = videoBitmapUtil.getVideoRatio()
                    videoBitmapUtil.release()
                    return videoRatio
                }

                override fun onPostExecute(videoRatio: IntArray?) {
                    super.onPostExecute(videoRatio)
                    if (videoRatio == null) {
                        return
                    }
                    val ratio = videoRatio[0].toFloat() / videoRatio[1].toFloat()
                    setCoverRatio(ratio)
                    Glide.with(this@ReleaseDynamicVideoActivity).load(coverImgPath).into(coverImage)
                }
            }.execute(it)
        }
    }

    private fun setCoverRatio(ratio: Float) {
        imageHeightRatio = ScreenUtils.getScreenWidth() / ratio
        imageWidthRatio = ScreenUtils.getScreenWidth().toFloat()
    }

    private fun initView() {
        location.setOnClickListener {
            val intent = Intent(this, DynamicLocationActivity::class.java)
            intent.putExtra(DynamicLocationActivity.ADDRESS, address)
            intent.putExtra(DynamicLocationActivity.DETAILED_ADDRESS, detailedAddress)
            intent.putExtra(DynamicLocationActivity.LATITUDE, latitude)
            intent.putExtra(DynamicLocationActivity.LONGITUDE, longitude)
            startActivityForResult(intent, locationCode)
        }
        editContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    setRightTextEnabled(true)
                } else {
                    setRightTextEnabled(false)
                }
            }
        })
        preview_video.setOnClickListener {
            val path = videoPath ?: videoUrl
            if (!path.isNullOrEmpty()) {
                DynamicVideoPreviewActivity.launch(this, path)
            }
        }
        cover_image_container.setOnClickListener {
            if (videoPath != null) {
                SelectVideoFaceActivity.launch(
                    this,
                    videoPath!!,
                    imageWidthRatio,
                    imageHeightRatio,
                    0
                )
            } else {
                val intent = Intent(this, SelectLocalImageActivity::class.java)
                intent.putExtra(SelectLocalImageActivity.isCutPhotoKey, true)
                intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
                intent.putExtra(SelectLocalImageActivity.widthKey, imageWidthRatio)
                intent.putExtra(SelectLocalImageActivity.heightKey, imageHeightRatio)
                startActivityForResult(intent, 1)
            }
        }
    }

    override fun onRightClick(view: View) {
        super.onRightClick(view)
        publish()
    }

    private fun publish() {
        val result = Utils.Consumer<Boolean> {
            val autoNetDownload =
                SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, false)
            if (autoNetDownload || it) {
                startReleaseDynamicService()
            } else {
                tipDialog()
            }
        }
        NetworkUtils.isWifiAvailableAsync(result)
    }

    private fun startReleaseDynamicService() {
        if (mediaKey != ReleaseDynamicVideoActivity::class.java.simpleName) {
            if (!coverImgPath.isNullOrEmpty()) {
                uploadCover()
            } else {
                editDynamic(videoId)
            }
            return
        }
        val coverImgPath = coverImgPath
        if (coverImgPath.isNullOrEmpty() || !FileUtils.isFileExists(coverImgPath)) {
            ToastUtils.showShort(R.string.choose_cover_tip)
            return
        }
        val localVideo = videoPath ?: return
        if (localVideo.isEmpty()) {
            ToastUtils.showShort(R.string.upLoadPath)
            return
        }
        setRightTextEnabled(false)
        if (dynamicCacheBean != null) {
            DynamicCacheManger.instance.delete(dynamicCacheBean!!.id)
        }
        val dynamicCacheBean = DynamicCacheBean().apply {
            dynamicType = DynamicType.VIDEO
            description = editContent.text.toString()
            latitude = this@ReleaseDynamicVideoActivity.latitude
            longitude = this@ReleaseDynamicVideoActivity.longitude
            address = this@ReleaseDynamicVideoActivity.address
            detailedAddress = this@ReleaseDynamicVideoActivity.detailedAddress
            status = DynamicReleaseStatus.RELEASING
            userId = GlobalValue.userInfoBean?.id ?: 0
            coverPath = coverImgPath
            videoPath = localVideo
            ratio = "$imageWidthRatio:$imageHeightRatio"
            videoSize = FileUtils.getLength(localVideo)
            createTime = TimesUtils.getUTCTime()
            labels = selectTags.joinToString(separator = ",") { it }
        }
        //每次发布前清空
        DynamicCacheManger.instance.deleteAll()
        ReleaseDynamicService.start(this, dynamicCacheBean)
        finish()
    }

    private fun initTag() {
        /*val state = SPUtils.getInstance().getBoolean(Constant.KEY_SHOW_AREA_SETTING, false)
        if (state) {
            tagTitleContent.visibility = View.VISIBLE
            tagContent.visibility = View.VISIBLE
            getDynamicTag()
        } else {
            tagTitleContent.visibility = View.GONE
            tagContent.visibility = View.GONE
        }*/
        tv_tag_count.text = getString(R.string.dynamic_tags_tips, MAX_TAG)
        getDynamicTag()
    }

    private fun setLocationAddress(address: String) {
        if (address.isEmpty()) {
            location.text = StringUtils.getString(R.string.noShowLocation)
            location.setTextColor(resources.getColor(R.color.grey_2))
            location.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(
                    resources,
                    R.mipmap.icon_dynamic_unselect_location,
                    null
                ), null, null, null
            )
        } else {
            location.text = address
            location.setTextColor(resources.getColor(R.color.colorAccent))
            location.setCompoundDrawablesWithIntrinsicBounds(
                ResourcesCompat.getDrawable(resources, R.mipmap.icon_dynamic_select_location, null),
                null,
                null,
                null
            )
        }
    }

    private fun getDynamicTag() {
        HttpRequest.get(RequestUrls.DYNAMIC_TAGS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val tagList = response?.optJSONArray("list")
                if (response == null || tagList == null) {
                    return
                }
                val list =
                    GsonUtils.fromJson<MutableList<DynamicTagBean>>(
                        tagList.toString(),
                        object : TypeToken<MutableList<DynamicTagBean>>() {}.type
                    )
                if (!list.isNullOrEmpty()) {
                    addTagView(list)
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showShort(errorMsg)
            }

        })
    }

    /**
     * 动态添加一级标签View
     */
    private fun addTagView(tagList: MutableList<DynamicTagBean>) {
        tagContent.removeAllViews()
        val tvParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        tvParams.topMargin = SizeUtils.dp2px(10f)
        for (bean in tagList) {
            // 添加标题(游戏、华人、新闻等)
            val textView = TextView(this)
            textView.text = bean.parentLabel
            textView.setTextColor(ColorUtils.getColor(R.color.word_color_2))
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            textView.layoutParams = tvParams
            tagContent.addView(textView)
            // 添加标签下所有子标签
            addTagsLayout(bean.subLabels)
        }
    }

    /**
     * 动态添加子标签数据View
     */
    private fun addTagsLayout(list: MutableList<String>?) {
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
        for (item in list) {
            val view = View.inflate(this, R.layout.layout_internet_item, null) as CheckedTextView
            view.text = item
            view.isChecked = selectTags.contains(item)
            view.setOnClickListener {
                if (view.isChecked) {
                    selectTags.remove(item)
                    view.isChecked = false
                } else {
                    if (selectTags.size < MAX_TAG) {
                        selectTags.add(item)
                        view.isChecked = true
                    }
                }
            }
            flowLayout.addView(view, lp)
        }
        tagContent.addView(flowLayout)
    }

    private fun requestPermissions() {
        val permissionUtils = NormalUtil.getLocationPermissionRequest()
        permissionUtils.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                startLocation()
            }

            override fun onDenied() {
                ToastUtils.showLong(R.string.permission_location)
            }
        })
        permissionUtils.request()
    }

    /**
     * 定位获取经纬度
     */
    private fun startLocation() {
        val location = Location.instance(this)
        location.resultListener = object : Location.OnLocationListener {
            override fun onResult(location: BDLocation) {
                latitude = location.latitude
                longitude = location.longitude
            }

            override fun onError(errorMsg: String) {

            }

        }
        location.start()
    }

    private fun tipDialog() {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.netWifiTips)
        dialog.setLeftListener(R.string.cancel) {
            dismissProgressDialog()
            dialog.dismiss()
        }
        dialog.setRightListener(R.string.continueUpload) {
            dialog.dismiss()
            startReleaseDynamicService()
        }
        dialog.show()
    }

    /**
     * 先上传封面图片
     */
    private fun uploadCover() {
        val coverImgPath = coverImgPath
        if (coverImgPath.isNullOrEmpty() || !FileUtils.isFileExists(coverImgPath)) {
            ToastUtils.showShort(R.string.choose_cover_tip)
            return
        }
        setRightTextEnabled(false)
        showProgressDialog()
        val taskBean = PictureUploadTask()
        taskBean.taskTag = mediaKey
        taskBean.localPath = coverImgPath
        taskBean.userId = GlobalValue.userInfoBean?.id ?: 0
        PictureUploadManager.uploadImage(taskBean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadImageEvent(task: PictureUploadTask) {
        if (task.taskTag != mediaKey) {
            return
        }
        when (task.status) {
            PictureUploadStatus.FINISH -> {
                coverImgUrl = task.imageUrl
                if ((task.totalTagSize - task.finishTagSize) == 0) {
                    progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.progress = 100
                    editDynamic(videoId)
                }
            }

            PictureUploadStatus.LOADING -> {
                progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.progress =
                    task.finishTagSize * 100 / task.totalTagSize
            }

            PictureUploadStatus.ERROR -> {
                if ((task.totalTagSize - task.finishTagSize) == 0) {
                    setRightTextEnabled(true)
                    progressDialog?.dismiss()
                    ToastUtils.showLong(R.string.releaseFail)
                }
            }

            else -> {}
        }
    }

    private fun editDynamic(videoId: Int? = null) {
        val param = HttpParams()
        param.put("mediaKey", mediaKey)
        if (videoId != null) {
            param.put("videoId", videoId)
        }
        param.put("description", editContent.text.toString())
        param.put("latitude", latitude)
        param.put("longitude", longitude)
        param.put("trendsDetails", coverImgUrl)
        param.put("address", address)
        param.put("detailedAddress", detailedAddress)
        param.put("label", selectTags.joinToString(separator = ",") { it })
        HttpRequest.post(RequestUrls.RELEASE_DYNAMIC, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                setRightTextEnabled(true)
                progressDialog?.dismiss()
                if (response == null) {
                    ToastUtils.showLong(R.string.releaseFail)
                    return
                }
                val bean = Gson().fromJson<DynamicBean>(
                    response.toString(),
                    object : TypeToken<DynamicBean>() {}.type
                )
                val event = DynamicEvent()
                event.dynamicBean = bean
                if (mediaKey != ReleaseDynamicVideoActivity::class.java.simpleName) {
                    event.action = DynamicEvent.DYNAMIC_UPDATE
                    setResult(RESULT_OK)
                } else {
                    event.action = DynamicEvent.DYNAMIC_ADD
                    DynamicDetailsActivity.launch(this@ReleaseDynamicVideoActivity) {
                        mediaKey = bean.mediaKey ?: ""
                        dynamicVideoList = mutableListOf(bean)
                    }
                }
                EventBus.getDefault().post(event)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                setRightTextEnabled(true)
                progressDialog?.dismiss()
                ToastUtils.showLong(R.string.releaseFail)
            }
        }, param, this)
    }

    private fun showProgressDialog() {
        if (progressDialog?.isShowing == true) {
            return
        }
        progressDialog = Dialog(this, R.style.dialog_style)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        val attr = window?.attributes
        attr?.dimAmount = 0f
        window?.attributes = attr
        progressView = LayoutInflater.from(this).inflate(R.layout.layout_dynamic_progress, null)
        progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.max = 200
        progressDialog?.setContentView(progressView!!)
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
        progressDialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
                showCancelTips()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
    }

    private fun showCancelTips() {
        if (cancelTipDialog?.isShowing == true) {
            return
        }
        cancelTipDialog = TipsDialog(this).apply {
            setDialogTitle(R.string.tips)
            setMsg(R.string.cancel_publish_dynamic_tip)
            setLeftListener(R.string.no) {
                dismiss()
            }
            setRightListener(R.string.yes) {
                setRightTextEnabled(true)
                OkGo.getInstance().cancelTag(this)
                PictureUploadManager.removeTaskByTag(mediaKey)
                progressDialog?.dismiss()
                dismiss()
            }
            show()
        }
    }

    override fun onBackPressed() {
        if (!videoPath.isNullOrEmpty() || editContent.text.toString().isNotEmpty()) {
            val tipsDialog = TipsDialog(this)
            tipsDialog.setDialogTitle(R.string.tips)
            tipsDialog.setMsg(R.string.outReleaseEdit)
            tipsDialog.setLeftListener(R.string.cancel) {
                tipsDialog.dismiss()
            }
            tipsDialog.setRightListener(R.string.confirm) {
                finish()
                tipsDialog.dismiss()
            }
            tipsDialog.show()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                locationCode -> {
                    // 位置选择
                    address = data?.getStringExtra(DynamicLocationActivity.ADDRESS) ?: ""
                    latitude = data?.getDoubleExtra(DynamicLocationActivity.LATITUDE, 0.00) ?: 0.00
                    longitude =
                        data?.getDoubleExtra(DynamicLocationActivity.LONGITUDE, 0.00) ?: 0.00
                    detailedAddress =
                        data?.getStringExtra(DynamicLocationActivity.DETAILED_ADDRESS) ?: ""
                    setLocationAddress(address)
                }

                0 -> {
                    //封面选择
                    coverImgPath = data?.getStringExtra("facePath")?.toString()
                    ImageUtil.loadImage(
                        this,
                        coverImgPath,
                        coverImage,
                        0,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    add_cover_container.isVisible = false
                }

                1 -> {
                    //封面选择
                    coverImgPath = data?.getStringExtra("clipPath")?.toString()
                    ImageUtil.loadImage(
                        this,
                        coverImgPath,
                        coverImage,
                        0,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    add_cover_container.isVisible = false
                }
            }
        }
    }
}