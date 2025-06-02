package com.cqcsy.lgsp.upload

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.database.bean.UploadCacheBean
import com.cqcsy.lgsp.database.manger.UploadCacheManger
import com.cqcsy.lgsp.event.UploadListenerEvent
import com.cqcsy.lgsp.upload.util.UploadMgr
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.base.BaseService
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.OkGo
import kotlinx.android.synthetic.main.activity_uploading.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 上传列表
 */
class UploadingListActivity : NormalActivity() {
    private var uploadTag = ""
    var isEdit = false
    val checkUpload: Int = 1001
    var statusChange = false
    var selectedItem: MutableList<UploadCacheBean> = ArrayList()
    private lateinit var uploadListAdapter: BaseQuickAdapter<UploadCacheBean, BaseViewHolder>
    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            val list = getUploadingTask()
            for (uploadBean in list) {
                if (uploadBean.status == Constant.UPLOADING) {
                    statusChange = false
                    uploadTag = uploadBean.path
                }
            }
            if (list.size > 0 && uploadTag.isEmpty()) {
                sendEmptyMessageDelayed(checkUpload, 500)
            }
        }
    }

    override fun getContainerView(): Int {
        return R.layout.activity_uploading
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.uploadList)
        setRightText(R.string.edit)
        initList()
        uploadTag = if (!intent.getStringExtra("uploadTag").isNullOrEmpty()) {
            intent.getStringExtra("uploadTag") ?: ""
        } else {
            getUploading()
        }
        reloadUploading()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(uploadEvent: UploadListenerEvent) {
        when (uploadEvent.event) {
            UploadListenerEvent.onStart -> {
                for ((i, data) in uploadListAdapter.data.withIndex()) {
                    if (data.lid == uploadEvent.uploadCacheBean.lid && uploadEvent.uploadCacheBean.path == data.path) {
                        data.status = Constant.UPLOAD_WAIT
                        uploadListAdapter.notifyItemChanged(i)
                    }
                }
                reloadUploading()
            }
            UploadListenerEvent.onFinish -> {
                for ((i, data) in uploadListAdapter.data.withIndex()) {
                    if (data.lid == uploadEvent.uploadCacheBean.lid && uploadEvent.uploadCacheBean.path == data.path) {
                        data.status = Constant.UPLOAD_FINISH
                        uploadListAdapter.notifyItemChanged(i)
                    }
                }
                LogUtils.i("uploadServer onUploadEvent onFinish")
                reloadUploading()
            }
            UploadListenerEvent.onProgress -> {
                for ((i, data) in uploadListAdapter.data.withIndex()) {
                    if (data.lid == uploadEvent.uploadCacheBean.lid && uploadEvent.uploadCacheBean.path == data.path) {
                        data.status = Constant.UPLOADING
                        data.speed = uploadEvent.uploadCacheBean.speed
                        data.progress = uploadEvent.uploadCacheBean.progress
                        uploadListAdapter.notifyItemChanged(i)
                    }
                }
                if (!statusChange) {
                    statusChange = true
                    LogUtils.i("uploadServer onUploadEvent onProgress")
                    reloadUploading()
                }
            }
            UploadListenerEvent.onError -> {
                for ((i, data) in uploadListAdapter.data.withIndex()) {
                    if (data.lid == uploadEvent.uploadCacheBean.lid && uploadEvent.uploadCacheBean.path == data.path) {
                        data.status = Constant.UPLOAD_ERROR
                        uploadListAdapter.notifyItemChanged(i)
                    }
                }
                setUploadingState()
            }
            UploadListenerEvent.onPause -> {
                for ((i, data) in uploadListAdapter.data.withIndex()) {
                    if (data.lid == uploadEvent.uploadCacheBean.lid && uploadEvent.uploadCacheBean.path == data.path) {
                        data.status = Constant.UPLOAD_PAUSE
                        uploadListAdapter.notifyItemChanged(i)
                    }
                }
                setUploadingState()
            }
            UploadListenerEvent.onTipsDialog -> {
                tipDialog()
            }
        }
    }

    private fun tipDialog() {
        val dialog = TipsDialog(this)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.netWifiTips)
        dialog.setLeftListener(R.string.cancel, View.OnClickListener {
            dialog.dismiss()
        })
        dialog.setRightListener(R.string.continueUpload, View.OnClickListener {
            dialog.dismiss()
            startUploadServer()
        })
        dialog.show()
    }

    private fun getUploading(): String {
        for (uploadBean in UploadMgr.getUploadList()) {
            if (uploadBean.status == Constant.UPLOADING) {
                return uploadBean.path
            }
        }
        return ""
    }

    private fun reloadUploading() {
        val taskList = getUploadingTask()
        if (taskList.isEmpty()) {
            showEmpty()
            editContent.visibility = View.GONE
            topContent.visibility = View.GONE
            emptyLargeTip.text = getString(R.string.noUploadingVideo)
            emptyLittleTip.text = getString(R.string.noUploadingVideoTips)
            setRightTextVisible(View.GONE)
        } else {
            uploadListAdapter.setList(taskList)
            setUploadingState()
        }
    }

    private fun initList() {
        uploadRecycle.layoutManager = LinearLayoutManager(this)
        (uploadRecycle.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        uploadRecycle.addItemDecoration(
            XLinearBuilder(this).setSpacing(10f).build()
        )
        uploadListAdapter =
            object : BaseQuickAdapter<UploadCacheBean, BaseViewHolder>(
                R.layout.layout_uploading_item,
            ) {
                override fun convert(holder: BaseViewHolder, item: UploadCacheBean) {
                    val checkBox = holder.getView<CheckBox>(R.id.itemCheck)
                    if (isEdit) {
                        checkBox.visibility = View.VISIBLE
                    } else {
                        checkBox.visibility = View.GONE
                    }
                    checkBox.isChecked = selectedItem.contains(item)
                    checkBox.setOnClickListener {
                        if (selectedItem.contains(item)) {
                            selectedItem.remove(item)
                        } else {
                            selectedItem.add(item)
                        }
                    }
                    val image = holder.getView<ImageView>(R.id.itemImage)
                    ImageUtil.loadImage(
                        context,
                        if (item.imagePath.isNotEmpty()) item.imagePath else item.path,
                        image,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    holder.setText(R.id.itemTitle, item.title)
                    val uploadSpeed = holder.getView<TextView>(R.id.uploadSpeed)
                    val uploadProgress = holder.getView<ProgressBar>(R.id.uploadProgress)
                    holder.getView<TextView>(R.id.uploadInfo).text = Html.fromHtml(
                        getString(
                            R.string.wait_download_info,
                            NormalUtil.formatFileSize(this@UploadingListActivity, item.progress),
                            NormalUtil.formatFileSize(this@UploadingListActivity, item.videoSize)
                        )
                    )
                    when (item.status) {
                        // 暂停状态
                        Constant.UPLOAD_PAUSE -> {
                            uploadSpeed.setTextColor(ColorUtils.getColor(R.color.red))
                            uploadSpeed.text = getString(R.string.pauseing)
                            uploadProgress.progress =
                                ((item.progress * 100) / item.videoSize).toInt()
                        }
                        //下载状态
                        Constant.UPLOADING -> {
                            uploadSpeed.setTextColor(ColorUtils.getColor(R.color.blue))
                            uploadSpeed.text = NormalUtil.formatFileSize(context, item.speed) + "/s"
                            uploadProgress.progress =
                                ((item.progress * 100) / item.videoSize).toInt()
                        }
                        //上传失败
                        Constant.UPLOAD_ERROR -> {
                            uploadSpeed.setTextColor(ColorUtils.getColor(R.color.red))
                            uploadSpeed.text = getString(R.string.uploadError)
                        }
                        // 等待状态
                        else -> {
                            uploadSpeed.setTextColor(ColorUtils.getColor(R.color.grey))
                            uploadSpeed.text = getString(R.string.waitUpload)
                        }
                    }
                }

            }
        uploadListAdapter.setOnItemClickListener { _, _, position ->
            if (!isEdit) {
                val uploadCacheBean = uploadListAdapter.getItem(position)
                if (uploadCacheBean.status == Constant.UPLOADING) {
                    uploadCacheBean.status = Constant.UPLOAD_PAUSE
                    OkGo.getInstance().cancelTag(uploadCacheBean.path)
                    UploadCacheManger.instance.update(uploadCacheBean)
                    uploadTag = ""
                    if (hasWaitingTask()) {
                        mHandler.sendEmptyMessageDelayed(checkUpload, 500)
                    }
                    statusChange = false
                    reloadUploading()
                } else {
                    uploadTag = uploadCacheBean.path
                    startUploadServer()
                    statusChange = false
                    reloadUploading()
                }
            }
        }
        uploadRecycle.adapter = uploadListAdapter
    }

    private fun setUploadingState() {
        if (isEdit) {
            return
        }
        if (!checkUploadingState()) {  // 处理开始
            uploadTag = ""
            uploadAction.setText(R.string.startUpload)
            uploadImage.setImageResource(R.mipmap.icon_play_32)
        } else {
            uploadAction.setText(R.string.stop_all)
            uploadImage.setImageResource(R.mipmap.icon_pause_32)
            if (uploadTag.isEmpty()) {
                mHandler.sendEmptyMessageDelayed(checkUpload, 500)
            }
        }
    }

    private fun hasWaitingTask(): Boolean {
        val list = getUploadingTask()
        for (progress in list) {
            if (progress.status == Constant.UPLOAD_WAIT) {
                return true
            }
        }
        return false
    }

    /**
     * 检测是否有上传任务
     */
    private fun checkUploadingState(): Boolean {
        for (uploadBean in UploadCacheManger.instance.select()) {
            if (uploadBean.status == Constant.UPLOADING) {
                return true
            }
        }
        return false
    }

    /**
     * 获取上传任务集合
     */
    private fun getUploadingTask(): MutableList<UploadCacheBean> {
        val list = ArrayList<UploadCacheBean>()
        for (uploadBean in UploadCacheManger.instance.select()) {
            if (uploadBean.status != Constant.UPLOAD_FINISH) {
                list.add(uploadBean)
            }
        }
        return list
    }

    /**
     * 清空全部
     */
    fun clearAll(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.clear_all)
        tipsDialog.setMsg(R.string.clear_tips)
        tipsDialog.setLeftListener(R.string.think_again, View.OnClickListener {
            tipsDialog.dismiss()
        })
        tipsDialog.setRightListener(R.string.clear, View.OnClickListener {
            tipsDialog.dismiss()
            if (uploadTag.isNotEmpty()) {
                OkGo.getInstance().cancelTag(uploadTag)
                uploadTag = ""
            }
            OkGo.getInstance().cancelTag(UploadService)
            UploadCacheManger.instance.delete()
            onRightClick(rightContent)
            reloadUploading()
        })
        tipsDialog.show()
    }

    /**
     * 删除
     */
    fun delete(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.delete_record)
        tipsDialog.setMsg(R.string.delete_tips)
        tipsDialog.setLeftListener(R.string.save) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            tipsDialog.dismiss()
            for (uploadBean in selectedItem) {
                if (uploadTag == uploadBean.path) {
                    uploadTag = ""
                    if (hasWaitingTask()) {
                        statusChange = false
                        mHandler.sendEmptyMessageDelayed(checkUpload, 500)
                    }
                }
                UploadCacheManger.instance.delete(uploadBean.path)
                if (uploadBean.status == Constant.UPLOADING) {
                    OkGo.getInstance().cancelTag(uploadBean.path)
                }
            }
            if (getUploadingTask().size == 0) {
                onRightClick(rightContent)
            }
            reloadUploading()
        }
        tipsDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(checkUpload)
    }

    /**
     * 点击编辑
     */
    override fun onRightClick(view: View) {
        if (isEdit) {
            setRightText(R.string.edit)
            editContent.visibility = View.GONE
            leftImageView.visibility = View.VISIBLE
        } else {
            setRightText(R.string.cancel)
            editContent.visibility = View.VISIBLE
            leftImageView.visibility = View.INVISIBLE
        }
        isEdit = !isEdit
        selectedItem.clear()
        reloadUploading()
    }

    /**
     * 全部暂停
     */
    fun allPause(view: View) {
        if (isEdit) {
            return
        }
        if (checkUploadingState()) {
            OkGo.getInstance().cancelTag(UploadService.TAG)
            if (uploadTag.isNotEmpty()) {
                OkGo.getInstance().cancelTag(uploadTag)
                for (data in uploadListAdapter.data) {
                    if (data.path == uploadTag) {
                        data.status = Constant.UPLOAD_PAUSE
                        UploadCacheManger.instance.update(data)
                    }
                }
            }
            reloadUploading()
        } else {
            startUploadServer()
        }
    }

    private fun startUploadServer() {
        val intent = Intent(this, UploadService::class.java)
        intent.putExtra(UploadService.START_UPLOAD, uploadTag)
        BaseService.startService(this, intent)
    }
}