package com.cqcsy.lgsp.views.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.StringUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.event.DownloadAccessFailed
import com.cqcsy.library.download.server.DownloadListener
import com.cqcsy.library.download.server.OkDownload
import com.lzy.okgo.model.Progress
import kotlinx.android.synthetic.main.layout_dialog_update.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File

/**
 * 更新提示
 */
class UpdateDialog(context: Context) : Dialog(context, R.style.dialog_style) {
    var updateJson: JSONObject? = null

    fun setUpdateInfo(update: JSONObject) {
        this.updateJson = update
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_dialog_update)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        updateVersion.text =
            StringUtils.getString(R.string.update_version, updateJson?.optString("version", ""))
        updateInfo.text = updateJson?.optString("describe")
        updateInfo.movementMethod = ScrollingMovementMethod.getInstance()
        leftButton.setOnClickListener { dismiss() }
        if (updateJson?.optInt("updateType") == 1) {
            leftButton.visibility = View.GONE
            verticalLine.visibility = View.GONE
        }
        rightButton.setOnClickListener { startDownload(updateJson?.optString("updateUrl")) }
    }

    private fun startDownload(downloadUrl: String?) {
        if (downloadUrl.isNullOrEmpty()) {
            return
        }
        DownloadMgr.startDownload(downloadUrl = downloadUrl)
        setListener(downloadUrl)
    }

    private fun setListener(downloadTag: String) {
        if (downloadTag.isEmpty()) {
            return
        }
        downloadContent.visibility = View.VISIBLE
        updateWifiTips.visibility = View.GONE
        updateInfo.visibility = View.GONE
        horizontalLine.visibility = View.GONE
        bottomLayout.visibility = View.GONE

        mHandler.sendEmptyMessage(100)
    }

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {

        override fun handleMessage(msg: Message) {
            val downloadTask = OkDownload.getInstance().getTask(updateJson?.optString("updateUrl"))
            if (downloadTask == null) {
                sendEmptyMessageDelayed(100, 1000)
                return
            }
            downloadTask.register(object : DownloadListener(updateJson?.optString("updateUrl")) {
                override fun onFinish(t: File, progress: Progress) {
                    dismiss()
                    if (t.name.endsWith(".apk")) {
                        AppUtils.installApp(t)
                    }
                }

                override fun onRemove(progress: Progress) {

                }

                override fun onProgress(progress: Progress) {
                    val pro: Int = (progress.fraction * 100).toInt()
                    downloading.progress = pro
                    progressText.text = "$pro%"
                }

                override fun onError(progress: Progress) {
                    setCanceledOnTouchOutside(true)
                    setCancelable(true)
                    progressText.text = StringUtils.getString(R.string.download_error)
                }

                override fun onStart(progress: Progress) {

                }
            })
        }
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)

        val attribute = window?.attributes
        val width = ScreenUtils.getAppScreenWidth()
        attribute?.height = WindowManager.LayoutParams.WRAP_CONTENT
        attribute?.width = (width * 0.72f).toInt()
        attribute?.gravity = Gravity.CENTER
        window?.attributes = attribute
    }

    override fun dismiss() {
        EventBus.getDefault().unregister(this)
        super.dismiss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDownloadFailed(event: DownloadAccessFailed) {
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        progressText.text = StringUtils.getString(R.string.download_error)
    }
}