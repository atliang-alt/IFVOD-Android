package com.cqcsy.lgsp.screenshare

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.BuildConfig
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NetManager
import com.cqcsy.lgsp.utils.NormalUtil
import com.hpplay.sdk.source.api.IConnectListener
import com.hpplay.sdk.source.api.LelinkSourceSDK
import com.hpplay.sdk.source.browse.api.IBrowseListener
import com.hpplay.sdk.source.browse.api.LelinkServiceInfo
import kotlinx.android.synthetic.main.activity_screen_share.*


class ScreenShareActivity : NormalActivity() {
    companion object {
        const val SEARCH_TIME: Long = 10 * 1000
    }

    var deviceList: MutableList<LelinkServiceInfo> = ArrayList()
    var dialog: ScreenConnectDialog? = null
    var timer: CountDownTimer? = null
    var lelinkServiceInfo: LelinkServiceInfo? = null
    var mHandler = Handler()

    override fun getContainerView(): Int {
        return R.layout.activity_screen_share
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.screen_share)
        requestPermissions()
        initDLNA()
    }

    private fun initDLNA() {
        ImageUtil.showCircleAnim(progressImage)
        LelinkSourceSDK.getInstance().bindSdk(this, BuildConfig.LEBO_ID, BuildConfig.LEBO_KEY) {
            if (it) {
                println("初始化成功")
                mHandler.post {
                    initList()
                    initLeLink()
                }
            } else {
                println("初始化失败")
            }
        }
    }

    private fun initLeLink() {
        LelinkSourceSDK.getInstance().setConnectListener(object : IConnectListener {
            override fun onConnect(serviceInfo: LelinkServiceInfo?, extra: Int) {
                lelinkServiceInfo = serviceInfo
                notifyDataChange()
                val intent = Intent()
                intent.putExtra("serviceInfo", serviceInfo)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            override fun onDisconnect(serviceInfo: LelinkServiceInfo?, what: Int, extra: Int) {
                lelinkServiceInfo = null
                notifyDataChange()
                ToastUtils.showShort(R.string.collect_tv_failed)
            }

        })
        search()
    }

    private fun search() {
        LelinkSourceSDK.getInstance().setBrowseResultListener { resultCode, list ->
            if (resultCode == IBrowseListener.BROWSE_SUCCESS) {
                deviceList.clear()
                if (!list.isNullOrEmpty()) {
                    deviceList.addAll(list)
                    timer?.cancel()
                    LelinkSourceSDK.getInstance().stopBrowse()
                    notifyDataChange()
                } else {
                    println("搜索无设备")
                }
            } else {
                println("搜索设备失败 $resultCode")
            }
            mHandler.post {
                shareMachineList.adapter?.notifyDataSetChanged()
            }
        }
        LelinkSourceSDK.getInstance().startBrowse()
        mHandler.post {
            startCountDown()
        }
    }

    private fun startCountDown() {
        timer = object : CountDownTimer(SEARCH_TIME, 1000) {
            override fun onFinish() {
                if (deviceList.size == 0) {
                    setFailed()
                }
                LelinkSourceSDK.getInstance().stopBrowse()
                ImageUtil.closeCircleAnim(progressImage)
            }

            override fun onTick(millisUntilFinished: Long) {
                if (deviceList.size > 0) {
                    timer?.cancel()
                }
            }

        }.start()
        ImageUtil.showCircleAnim(progressImage)
    }

    private fun requestPermissions() {
        val permissionUtils = NormalUtil.getLocationPermissionRequest()
        permissionUtils.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                wifiName.text = getString(
                    R.string.current_wifi,
                    NetManager.getWifiName(this@ScreenShareActivity)
                )
            }

            override fun onDenied() {
                wifiName.text =
                    getString(R.string.current_wifi, getString(R.string.permission_wifi))
            }
        })
        permissionUtils.request()
    }

    private fun initList() {
        shareMachineList.layoutManager = LinearLayoutManager(this)
        val adapter = object :
            BaseQuickAdapter<LelinkServiceInfo, BaseViewHolder>(
                R.layout.layout_tv_list_item,
                deviceList
            ) {
            override fun convert(holder: BaseViewHolder, item: LelinkServiceInfo) {
                val name = holder.getView<TextView>(R.id.tv_name)
                val state = holder.getView<TextView>(R.id.tv_state)
                name.text = item.name
                if (lelinkServiceInfo != null && lelinkServiceInfo!!.uid == item.uid) {
                    name.setTextColor(
                        ColorUtils.getColor(R.color.blue)
                    )
                    name.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_tv_current, 0, 0, 0)
                    state.visibility = View.VISIBLE
                } else {
                    name.setTextColor(
                        ColorUtils.getColor(R.color.word_color_2)
                    )
                    name.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_tv_normal, 0, 0, 0)
                    state.visibility = View.GONE
                }
            }

        }
        adapter.setOnItemClickListener { adapter, view, position ->
            val deviceInfo = adapter.getItem(position) as LelinkServiceInfo
            if (lelinkServiceInfo != null && lelinkServiceInfo!!.uid == deviceInfo.uid) {
                return@setOnItemClickListener
            }
            LelinkSourceSDK.getInstance().connect(deviceInfo)
            showConnectDialog(deviceInfo)
        }
        shareMachineList.adapter = adapter
    }

    private fun notifyDataChange() {
        mHandler.post {
            ImageUtil.closeCircleAnim(progressImage)
            dialog?.dismiss()
            if (deviceList.isEmpty()) {
                shareMachineList.visibility = View.GONE
                searchTipContent.visibility = View.GONE
                noMachineTip.visibility = View.VISIBLE
            } else {
                shareMachineList.visibility = View.VISIBLE
                searchTipContent.visibility = View.GONE
                noMachineTip.visibility = View.GONE
            }
        }
    }

    fun startSearchTv(view: View) {
        shareMachineList.visibility = View.VISIBLE
        searchTipContent.visibility = View.VISIBLE
        noMachineTip.visibility = View.GONE
        deviceList.clear()
        shareMachineList.adapter?.notifyDataSetChanged()
        shareMachineList.visibility = View.GONE
        search()
    }

    private fun showConnectDialog(deviceInfo: LelinkServiceInfo) {
        dialog = ScreenConnectDialog(this)
        dialog?.deviceInfo = deviceInfo
        dialog?.cancelListener = View.OnClickListener {
            LelinkSourceSDK.getInstance().disConnect(deviceInfo)
            dialog?.dismiss()
        }
        dialog?.show()
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    fun setFailed() {
        shareMachineList.visibility = View.GONE
        searchTipContent.visibility = View.GONE
        noMachineTip.visibility = View.VISIBLE
    }
}