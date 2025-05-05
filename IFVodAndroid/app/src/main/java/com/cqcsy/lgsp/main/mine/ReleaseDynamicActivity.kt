package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.baidu.location.BDLocation
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.DynamicTagBean
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.event.DynamicEvent
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.lgsp.medialoader.ChooseMode
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.upper.pictures.ViewAllActivity
import com.cqcsy.lgsp.utils.CustomItemTouchHelper
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.OnItemPositionListener
import com.cqcsy.library.views.FlowLayout
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadStatus
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.danikula.videocache.file.Md5FileNameGenerator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_release_dynamic.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.Serializable

/**
 * 发图片动态
 */
class ReleaseDynamicActivity : NormalActivity(), OnItemPositionListener {
    // 用于显示的集合
    private val viewImages: MutableList<LocalMediaBean> = ArrayList()

    // 选择的本地图片集合
    private val selImages: MutableList<LocalMediaBean> = ArrayList()
    private var adapter: BaseQuickAdapter<LocalMediaBean, BaseViewHolder>? = null
    private val selectCode = 1000
    private val locationCode = 1001
    private val emptyBean = LocalMediaBean()

    private var itemHelper: ItemTouchHelper? = null
    private var mediaKey: String? = ReleaseDynamicActivity::class.java.simpleName
    private var latitude: Double = 0.00
    private var longitude: Double = 0.00

    // 显示地址 如：中国·北京·北京大学
    private var address: String = ""

    // 详细地址 如：重庆市江北街国金中心T1写字楼
    private var detailedAddress: String = ""

    private var progressDialog: Dialog? = null
    private var cancelTipDialog: TipsDialog? = null
    private var progressView: View? = null
    private val selectTags = mutableListOf<String>()

    override fun getContainerView(): Int {
        return R.layout.activity_release_dynamic
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.releaseDynamic)
        setRightTextColor(
            ResourcesCompat.getColorStateList(
                resources,
                R.color.publish_text_color,
                null
            )
        )
        setRightTextEnabled(false)
        setRightText(R.string.publish)
        initTag()
        initView()
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
                println("location error:   $errorMsg")
            }

        }
        location.start()
    }

    private fun initView() {
        val beanSerializable = intent.getSerializableExtra("dynamicBean")
        val selectImgSerializable = intent.getSerializableExtra("selectImg")
        if (selectImgSerializable != null) {
            val list = selectImgSerializable as MutableList<LocalMediaBean>
            viewImages.addAll(list)
            selImages.addAll(list)
        }
        if (beanSerializable is DynamicBean) {
            mediaKey = beanSerializable.mediaKey
            latitude = beanSerializable.latitude ?: 0.0
            longitude = beanSerializable.longitude ?: 0.0
            address = beanSerializable.address ?: ""
            detailedAddress = beanSerializable.detailedAddress ?: ""
            setLocationAddress(address)
            setHeaderTitle(R.string.editDynamic)
            setRightTextEnabled(true)
            beanSerializable.trendsDetails?.forEach {
                if (!it.imgPath.isNullOrEmpty()) {
                    val localMediaBean = LocalMediaBean()
                    localMediaBean.path = it.imgPath!!
                    viewImages.add(localMediaBean)
                }
            }
            editContent.setText(Html.fromHtml(beanSerializable.description?.replace("\n", "<br>")))
            editContent.setSelection(editContent.text.length)
            val label = beanSerializable.label
            if (!label.isNullOrEmpty()) {
                selectTags.clear()
                selectTags.addAll(label.split(",").toMutableList())
            }
        }
        if (viewImages.size < MAX_COUNT) {
            viewImages.add(emptyBean)
        }
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
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(
            XGridBuilder(this).setVLineSpacing(5f).setHLineSpacing(5f).build()
        )
        setAdapter()
    }

    override fun onRightClick(view: View) {
        super.onRightClick(view)
        publish()
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

    private fun setAdapter() {
        adapter = object : BaseQuickAdapter<LocalMediaBean, BaseViewHolder>(R.layout.item_select_upload_photo, viewImages) {
            override fun convert(holder: BaseViewHolder, item: LocalMediaBean) {
                if (item.path.isEmpty()) {
                    holder.setGone(R.id.deleteImg, true)
                    ImageUtil.loadLocalId(this@ReleaseDynamicActivity, R.mipmap.icon_add_photo, holder.getView(R.id.image))
                } else {
                    holder.setVisible(R.id.deleteImg, true)
                    if (item.path.contains(".gif", true)) {
                        ImageUtil.loadGif(this@ReleaseDynamicActivity, item.path, holder.getView(R.id.image), ImageView.ScaleType.CENTER_CROP)
                    } else {
                        ImageUtil.loadImage(
                            this@ReleaseDynamicActivity,
                            item.path,
                            holder.getView(R.id.image),
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        )
                    }
                }
                holder.getView<ImageView>(R.id.image).setOnClickListener {
                    if (item.path.isNotEmpty()) {
                        val list: MutableList<String> = ArrayList()
                        viewImages.forEach {
                            if (it.path.isNotEmpty()) {
                                list.add(it.path)
                            }
                        }
                        val intent = Intent(this@ReleaseDynamicActivity, ViewAllActivity::class.java)
                        intent.putExtra(PictureViewerActivity.SHOW_URLS, list as Serializable)
                        intent.putExtra(PictureViewerActivity.SHOW_INDEX, getItemPosition(item))
                        intent.putExtra(PictureViewerActivity.SHOW_COUNTS, list.size)
                        startActivity(intent)
                        return@setOnClickListener
                    }
                    val intent = Intent(this@ReleaseDynamicActivity, SelectLocalImageActivity::class.java)
                    intent.putExtra(SelectLocalImageActivity.maxCountKey, MAX_COUNT - viewImages.size + 1)
                    intent.putExtra(SelectLocalImageActivity.chooseModeKey, ChooseMode.ONLY)
                    intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
                    intent.putExtra(SelectLocalImageActivity.isBackGifKey, true)
                    startActivityForResult(intent, selectCode)
                }
                holder.getView<LinearLayout>(R.id.deleteImg).setOnClickListener {
//                    if (viewImages.size == 2) {
//                        showTips()
//                        return@setOnClickListener
//                    }
                    selImages.remove(item)
                    viewImages.removeAt(holder.absoluteAdapterPosition)
                    if (!viewImages.contains(emptyBean)) {
                        viewImages.add(emptyBean)
                    }
                    notifyDataSetChanged()
                }
            }
        }
        recyclerView.adapter = adapter
        itemHelper = ItemTouchHelper(CustomItemTouchHelper(this, this))
        itemHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onBackPressed() {
        if (selImages.isNotEmpty() || editContent.text.toString().isNotEmpty()) {
            val tipsDialog = TipsDialog(this)
            tipsDialog.setDialogTitle(R.string.tips)
            tipsDialog.setMsg(R.string.outReleaseEdit)
            tipsDialog.setLeftListener(R.string.cancel) {
                tipsDialog.dismiss()
            }
            tipsDialog.setRightListener(R.string.confirm) {
                tipsDialog.dismiss()
                finish()
            }
            tipsDialog.show()
        } else {
            finish()
        }
    }

    private fun publish() {
        val images = viewImages.filter { it != emptyBean }
        if (images.isEmpty() && selImages.isEmpty()) {
            ToastUtils.showShort(R.string.least_one_picture)
            return
        }
        val input = editContent.text.trim().toString()
        if (input.isNullOrEmpty()) {
            ToastUtils.showShort(R.string.input_dynamic_content)
            editContent.requestFocus()
            return
        }
        setRightTextEnabled(false)
        showProgressDialog()
        if (selImages.isEmpty()) {
            progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.progress = 90
            release()
            return
        }
        val list: MutableList<PictureUploadTask> = ArrayList()
        selImages.forEach {
            if (it.path.isNotEmpty()) {
                val taskBean = PictureUploadTask()
                taskBean.taskTag = mediaKey
                taskBean.localPath = it.path
                taskBean.userId = GlobalValue.userInfoBean?.id ?: 0
                list.add(taskBean)
            }
        }
        PictureUploadManager.uploadImage(list)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(task: PictureUploadTask) {
        if (task.taskTag != mediaKey) {
            return
        }
        when (task.status) {
            PictureUploadStatus.FINISH -> {
                for (item in viewImages) {
                    if (NormalUtil.getAbsolutePath(item.path) == task.localPath) {
                        selImages.remove(item)
                        item.path = task.imageUrl ?: ""
                        break
                    }
                }
                if ((task.totalTagSize - task.finishTagSize) == 0) {
                    progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.progress = 100
                    release()
                }
            }

            PictureUploadStatus.LOADING -> {
                progressView?.findViewById<ProgressBar>(R.id.releaseProgress)?.progress =
                    task.finishTagSize * 100 / task.totalTagSize
            }

            PictureUploadStatus.ERROR -> {
                setRightTextEnabled(true)
                progressDialog?.dismiss()
                ToastUtils.showLong(R.string.upload_picture_fail)
            }

            else -> {}
        }
    }

    private fun release() {
        val param = HttpParams()
        val imagesUrl = appendImage()
        val description = editContent.text.toString()
        if (mediaKey == ReleaseDynamicActivity::class.java.simpleName) {
            param.put("key", Md5FileNameGenerator().generate(imagesUrl))
        } else {
            param.put("mediaKey", mediaKey)
        }
        param.put("description", description)
        param.put("trendsDetails", imagesUrl)
        param.put("latitude", latitude)
        param.put("longitude", longitude)
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
                if (mediaKey != ReleaseDynamicActivity::class.java.simpleName) {
                    event.action = DynamicEvent.DYNAMIC_UPDATE
                    setResult(RESULT_OK)
                } else {
                    event.action = DynamicEvent.DYNAMIC_ADD
                    DynamicDetailsActivity.launch(this@ReleaseDynamicActivity) {
                        mediaKey = bean.mediaKey ?: ""
                        isFromMineDynamic = true
                    }
                }
                EventBus.getDefault().post(event)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                setRightTextEnabled(true)
                progressDialog?.dismiss()
                ToastUtils.showLong(errorMsg ?: StringUtils.getString(R.string.releaseFail))
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

    private fun appendImage(): String {
        var pathStr = ""
        if (viewImages.isEmpty()) {
            return pathStr
        }
        for ((i, image) in viewImages.withIndex()) {
            if (image.path.isNotEmpty()) {
                pathStr = if (i == 0) {
                    image.path
                } else {
                    pathStr + "," + image.path
                }
            }
        }
        return pathStr
    }

    private fun showTips() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(R.string.deleteSelectImgTips)
        tipsDialog.setLeftListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
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

    override fun onItemSwap(from: Int, target: Int) {
        //交换数据
        val localMediaBean = viewImages[from]
        if (localMediaBean == emptyBean || viewImages[target] == emptyBean) {
            return
        }
        viewImages.removeAt(from)
        viewImages.add(target, localMediaBean)
        adapter?.notifyItemMoved(from, target)
    }

    override fun onItemMoved(position: Int) {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == selectCode) {
                val list = data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) as? MutableList<LocalMediaBean>
                if (!list.isNullOrEmpty()) {
                    selImages.addAll(list)
                    viewImages.remove(emptyBean)
                    viewImages.addAll(list)
                    if (viewImages.size < MAX_COUNT) {
                        viewImages.add(emptyBean)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
            if (requestCode == locationCode) {
                // 位置选择
                address = data?.getStringExtra(DynamicLocationActivity.ADDRESS) ?: ""
                latitude = data?.getDoubleExtra(DynamicLocationActivity.LATITUDE, 0.00) ?: 0.00
                longitude = data?.getDoubleExtra(DynamicLocationActivity.LONGITUDE, 0.00) ?: 0.00
                detailedAddress =
                    data?.getStringExtra(DynamicLocationActivity.DETAILED_ADDRESS) ?: ""
                setLocationAddress(address)
            }
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

    companion object {
        private const val MAX_COUNT = 18
        private const val MAX_TAG = 10
    }
}