package com.cqcsy.lgsp.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.barcode_scan.CameraScanner
import com.cqcsy.barcode_scan.manager.MNScanConfig
import com.cqcsy.barcode_scan.view.ScanResultPointView
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.BuildConfig
import com.cqcsy.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_scan_qr.*

/**
 ** 2022/5/5
 ** des：扫二维码
 **/

class ScanQrActivity : BaseActivity() {
    private var scanner: CameraScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr)
        BarUtils.setStatusBarCustom(status_bar_view)
        checkPermission()
        iv_back.setOnClickListener {
            finish()
        }
    }

    private fun checkPermission() {
        val permissionUtils = PermissionUtils.permission(PermissionConstants.CAMERA)
        permissionUtils.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                initScan()
            }

            override fun onDenied() {
                ToastUtils.showLong(R.string.permission_camera)
            }
        })
        permissionUtils.request()
    }

    private fun initScan() {
        scanner = CameraScanner(this, previewView)
        scanner?.setOnScanResultCallback { bitmap, barcodes ->
            if (barcodes.isNotEmpty()) {
                if (barcodes.size == 1) {
                    result_point_view.isVisible = false
                    val displayValue = barcodes[0].displayValue
                    if (displayValue != null) {
                        if (handleScanResult(displayValue)) {
                            finish()
                            return@setOnScanResultCallback true
                        } else {
                            scanner?.setAnalyze(true)
                            return@setOnScanResultCallback false
                        }
                    }
                } else {
                    tv_scan_tip.text = getString(R.string.scan_multi_code_tip)
                    result_point_view.setData(barcodes, bitmap)
                    title_group.isVisible = false
                    result_point_view.isVisible = true
                    return@setOnScanResultCallback true
                }
            }
            false
        }
        val config = MNScanConfig.Builder().isShowVibrate(true).isShowBeep(true).builder()
        scanner?.setScanConfig(config)
        scanner?.startCamera()
        result_point_view.setStatusBarHeight(BarUtils.getStatusBarHeight())
        result_point_view.setOnResultPointClickListener(object :
            ScanResultPointView.OnResultPointClickListener {
            override fun onPointClick(result: String?) {
                if (result != null) {
                    if (handleScanResult(result)) {
                        finish()
                    } else {
                        cancel()
                        ToastUtils.showLong(getString(R.string.scan_tip))
                    }
                }
            }

            override fun onCancel() {
                cancel()
            }
        })
    }

    private fun cancel() {
        scanner?.setAnalyze(true)
        result_point_view.removeAllPoints()
        result_point_view.isVisible = false
        title_group.isVisible = true
        tv_scan_tip.text = getString(R.string.scan_tip)
    }

    private fun handleScanResult(value: String): Boolean {
        if (value.contains(BuildConfig.HOST)) {
            Log.d(TAG, "scan result：${value}")
            val uri = Uri.parse(value)
            when (uri.getQueryParameter("type")) {
                "1" -> {    // 扫码登录
                    val key = uri.getQueryParameter("key")
                    if (!key.isNullOrEmpty()) {
                        val intent = Intent(this, ScanAuthActivity::class.java)
                        intent.putExtra("authKey", key)
                        startActivity(intent)
                    }
                }

                "2" -> {    // 扫码购买会员
                    val selectId = uri.getQueryParameter("id")
                    val uid = uri.getQueryParameter("uid")
                    val categoryId = uri.getQueryParameter("categoryId")
                    val intent = Intent(this, OpenVipActivity::class.java)
                    if (!uid.isNullOrEmpty()) {
                        intent.putExtra("targetUid", uid)
                    }
                    if (!selectId.isNullOrEmpty()) {
                        intent.putExtra("selectId", selectId)
                    }
                    if (!categoryId.isNullOrEmpty()) {
                        intent.putExtra("categoryId", categoryId)
                    }
                    intent.putExtra("pathInfo", this.javaClass.simpleName)
                    startActivity(intent)
                }

                else -> {
                    val segments = uri.pathSegments
                    if (segments.contains("play") || segments.contains("watch")) {
                        val intent = Intent(this, VideoPlayVerticalActivity::class.java)
                        intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, uri.lastPathSegment)
                        val id = uri.getQueryParameter("id")
                        if (!id.isNullOrEmpty()) {
                            intent.putExtra(VideoBaseActivity.PLAY_CHILD_MEDIA_KEY, id)
                        }
                        startActivity(intent)
                    }/* else if (uri.lastPathSegment == "watch") {
                        val intent = Intent(this, VideoPlayVerticalActivity::class.java)
                        val id = uri.getQueryParameter("id")
                        if (!id.isNullOrEmpty()) {
                            intent.putExtra(VideoBaseActivity.PLAY_VIDEO_MEDIA_KEY, uri.lastPathSegment)
                            startActivity(intent)
                        }
                    }*/ else {
                        WebViewActivity.load(this, value)
                    }
                }
            }
            return true
        }
        return false
    }

    override fun onBackPressed() {
        if (result_point_view.isVisible) {
            cancel()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        scanner?.release()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "ScanQrActivity"
    }
}