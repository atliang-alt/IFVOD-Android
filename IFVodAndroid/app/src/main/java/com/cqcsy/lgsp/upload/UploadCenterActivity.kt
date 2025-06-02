package com.cqcsy.lgsp.upload

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.ColorUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.event.UploadListenerEvent
import com.cqcsy.library.network.H5Address
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upload.util.UploadMgr
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import kotlinx.android.synthetic.main.activity_upload_center.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * 上传中心页面
 */
class UploadCenterActivity : NormalActivity() {
    private var uploadTag = ""

    override fun getContainerView(): Int {
        return R.layout.activity_upload_center
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.upLoadCenterTitle)
        setRightImage(R.mipmap.icon_explain)
        getUploadInfo()
    }

    override fun onResume() {
        super.onResume()
        setUploadInfo()
    }

    private fun getUploadInfo() {
        HttpRequest.post(RequestUrls.GET_UPLOAD_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                releaseCounts.text = response.optString("videoCount")
                playCounts.text = response.optString("playCount")
                fabulousCounts.text = response.optString("likeCount")
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, tag = this)
    }

    private fun setUploadInfo() {
        val uploadList = UploadMgr.getUploadList()
        uploadCounts.text = uploadList.size.toString()
        if (uploadList.isEmpty()) {
            uploadLayout.visibility = View.INVISIBLE
            resetDownloading()
        } else {
            for (uploadBean in uploadList) {
                if (uploadBean.status != Constant.UPLOAD_FINISH || uploadTag == uploadBean.path) {
                    uploadTag = uploadBean.path
                    uploadSecond.text = NormalUtil.formatFileSize(this, uploadBean.speed) + "/s"
                    uploadProgressBar.progress =
                        ((uploadBean.progress * 100) / uploadBean.videoSize).toInt()
                    when (uploadBean.status) {
                        // 暂停状态
                        Constant.UPLOAD_PAUSE -> {
                            uploadSecond.setTextColor(ColorUtils.getColor(R.color.red))
                            uploadSecond.text = getString(R.string.pauseing)
                        }
                        //上传中状态
                        Constant.UPLOADING -> {
                            uploadSecond.setTextColor(ColorUtils.getColor(R.color.blue))
                        }
                        //上传失败
                        Constant.UPLOAD_ERROR -> {
                            uploadSecond.setTextColor(ColorUtils.getColor(R.color.red))
                            uploadSecond.text = getString(R.string.uploadError)
                        }
                        // 等待状态
                        else -> {
                            uploadSecond.setTextColor(ColorUtils.getColor(R.color.grey))
                            uploadSecond.text = getString(R.string.waitUpload)
                        }
                    }
                    uploadTitle.text = uploadBean.title
                    if (uploadBean.imageBase.isNotEmpty()) {
                        uploadImage.setImageBitmap(ImageUtil.base64ToImage(uploadBean.imageBase))
                    } else {
                        ImageUtil.loadImage(
                            this,
                            uploadBean.path,
                            uploadImage,
                            0,
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        )
                    }
                    uploadInfoContent.visibility = View.VISIBLE
                    uploadLayout.visibility = View.VISIBLE
                    break
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUploadEvent(uploadEvent: UploadListenerEvent) {
        when (uploadEvent.event) {
            UploadListenerEvent.onFinish -> {
                uploadSecond.text = getString(R.string.uploadFinish)
                resetDownloading()
                setUploadInfo()
            }
            UploadListenerEvent.onProgress -> {
                uploadSecond.setTextColor(ColorUtils.getColor(R.color.blue))
                uploadSecond.text = NormalUtil.formatFileSize(this, uploadEvent.uploadCacheBean.speed) + "/s"
                uploadProgressBar.progress =
                    ((uploadEvent.uploadCacheBean.progress * 100) / uploadEvent.uploadCacheBean.videoSize).toInt()
            }
            UploadListenerEvent.onPause -> {
                uploadSecond.setTextColor(ColorUtils.getColor(R.color.red))
                uploadSecond.text = getString(R.string.pauseing)
            }
            UploadListenerEvent.onError -> {
                uploadSecond.setTextColor(ColorUtils.getColor(R.color.red))
                uploadSecond.text = getString(R.string.uploadError)
            }
        }
    }

    private fun resetDownloading() {
        uploadTag = ""
        uploadTitle.text = ""
        uploadSecond.text = ""
        uploadProgressBar.progress = 0
    }

    override fun onRightClick(view: View) {
        dataTips()
    }

    /**
     * 数据显示提示框
     */
    private fun dataTips() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.dataExplain)
        tipsDialog.setMsg(R.string.dataExplainTips)
        tipsDialog.setRightListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    fun uploadShortVideo(view: View) {
        startActivity(Intent(this, SelectVideoActivity::class.java))
    }

    /**
     * 已上传的视频列表
     */
    fun mineVideo(view: View) {
        startActivity(Intent(this, UploadedShortVideoActivity::class.java))
    }

    /**
     * 进入下载列表页
     */
    fun uploadLayout(view: View) {
        val intent = Intent(this, UploadingListActivity::class.java)
        intent.putExtra("uploadTag", uploadTag)
        startActivity(intent)
    }

    fun showUploadAgreement(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.UPLOAD_VIDEO_AGREEMENT)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.uploadAgreement))
        startActivity(intent)
    }

    fun showForbiddenAgreement(view: View) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.urlKey, H5Address.VIDEO_FORBIDDEN_AGREEMENT)
        intent.putExtra(WebViewActivity.titleKey, getString(R.string.uploadProhibit))
        startActivity(intent)
    }
}