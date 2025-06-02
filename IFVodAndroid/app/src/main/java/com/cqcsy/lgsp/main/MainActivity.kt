package com.cqcsy.lgsp.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.core.view.isVisible
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.base.WebViewActivity
import com.cqcsy.lgsp.bean.*
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.library.download.server.OkDownload
import com.cqcsy.lgsp.event.*
import com.cqcsy.lgsp.login.AccountHelper
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.find.FindFragment
import com.cqcsy.lgsp.main.home.HomeFragment
import com.cqcsy.lgsp.main.hot.HotActivity
import com.cqcsy.lgsp.main.hot.HotFragment
import com.cqcsy.lgsp.main.mine.*
import com.cqcsy.lgsp.main.vip.ExchangeVipActivity
import com.cqcsy.lgsp.main.vip.VIPFragment
import com.cqcsy.lgsp.main.vip.VIPIntroActivity
import com.cqcsy.lgsp.medialoader.ChooseMode
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.offline.OfflineActivity
import com.cqcsy.lgsp.receiver.ActiveReceiver
import com.cqcsy.lgsp.record.RecordActivity
import com.cqcsy.lgsp.search.CategoryActivity
import com.cqcsy.lgsp.search.SearchActivity
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.upload.UploadCenterActivity
import com.cqcsy.lgsp.upload.UploadService
import com.cqcsy.lgsp.upload.util.UploadMgr
import com.cqcsy.lgsp.upper.UserFansActivity
import com.cqcsy.lgsp.upper.UserFocusActivity
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.library.utils.JumpUtils
import com.cqcsy.lgsp.views.dialog.NoticeDialog
import com.cqcsy.lgsp.views.dialog.NotificationTipDialog
import com.cqcsy.lgsp.views.dialog.PopAdWindow
import com.cqcsy.lgsp.views.dialog.UpdateDialog
import com.cqcsy.lgsp.vip.OpenVipActivity
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.library.base.BaseService
import com.cqcsy.library.bean.ChatMessageBean
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.event.ReloginEvent
import com.cqcsy.library.event.TabClickRefreshEvent
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.TipsDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.Serializable

/**
 * 主框架
 */

class MainActivity : BaseActivity(), NetworkUtils.OnNetworkStatusChangedListener,
    View.OnTouchListener {
    var lastBackTime: Long = 0
    var isFirst = true
    private var mActiveReceiver: ActiveReceiver? = null
    private val songViewModel: SongViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkUtils.registerNetworkStatusChangedListener(this)
        setBottomCheck()
        refreshToken()

        checkVersion()
        getShareUrl()
        getActivitySwitch()
        checkIntent(intent)
        checkDownloadAndUpload()
        getSplashAdvert()
        getRechargeActivity()

        mActiveReceiver = ActiveReceiver()
        registerReceiver(mActiveReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        JPushInterface.requestRequiredPermission(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        SPUtils.getInstance().put(Constant.IS_NOTCH_IN_SCREEN, StatusBarUtil.hasNotchInScreen(this))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            checkIntent(intent)
        }
    }

    private fun clearActivity() {
        activityList.forEach {
            if (it !is MainActivity) {
                it.finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTranslucentImage()
        if (!isFirst && !button_mine.isChecked) {
            getUserInfo()
        }
        isFirst = false

        // 日活检查 不能放create，后台再次打开不能检查
        ActivePresenter()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppStatusChange(event: AppStatusChange) {
        if (event.isBackground) {
            SocketClient.disconnect()
            SPUtils.getInstance().put("startBackgroundTime", System.currentTimeMillis())
        } else {
            val lastTime = SPUtils.getInstance().getLong("startBackgroundTime")
            if (System.currentTimeMillis() - lastTime >= Constant.MAX_BACKGROUND_TIME_ADVERT) {
                val intent = Intent(this, SplashActivity::class.java)
                intent.putExtra("restartAd", true)
                startActivity(intent)
            }
            if (GlobalValue.isLogin()) {
                SocketClient.userLogin()
            }
        }
        // 日活检查 APP前后台切换时也需要判定
        ActivePresenter()
    }

    override fun onDestroy() {
        Location.instance(this).destory()
        unregisterReceiver(mActiveReceiver)
        NetworkUtils.unregisterNetworkStatusChangedListener(this)
        stopService()
        stopService(Intent(this, PlayerService::class.java))
        super.onDestroy()
    }

    private fun getSplashAdvert() {
        val params = HttpParams()
        params.put("type", 1)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.post(RequestUrls.GET_ADS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                SPUtils.getInstance().put(
                    Constant.KEY_SPLASH_ADVERT,
                    EncodeUtils.base64Encode2String(jsonArray.toString().toByteArray())
                )
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, params, this)
    }

    private fun stopService() {
        SocketClient.disconnect()
        stopService(Intent(this, UploadService::class.java))
        stopService(Intent(this, ReleaseDynamicService::class.java))
        OkDownload.getInstance().pauseAll()
    }

    private fun checkIntent(intent: Intent) {
        if (intent.getSerializableExtra("advertBean") != null) {
            val advertBean = intent.getSerializableExtra("advertBean") as AdvertBean
            if (JumpUtils.isJumpHandle(advertBean.appParam)) {
                JumpUtils.jumpAnyUtils(this, advertBean.appParam!!)
                return
            }
            if (!advertBean.linkURL.isNullOrEmpty()) {
                val webIntent = Intent(this, WebViewActivity::class.java)
                webIntent.putExtra(WebViewActivity.urlKey, advertBean.linkURL)
                startActivity(webIntent)
                return
            }
        } else if (intent.getIntExtra("position", -1) in 0..4) {
            val position = intent.getIntExtra("position", -1)
            buttonGroup.getChildAt(position).performClick()
            clearActivity()
            val selectTab = intent.getIntExtra("selectTab", -1)
            if (selectTab >= 0) {
                EventBus.getDefault().post(TabChangeEvent(position, selectTab))
            }
        }
    }

    private fun setBottomCheck() {
        button_home.setOnTouchListener(this)
        button_find.setOnTouchListener(this)
        button_hot.setOnTouchListener(this)
        button_mine.setOnTouchListener(this)
        button_vip.setOnClickListener {
            if (button_vip.isSelected) {
                return@setOnClickListener
            }
            buttonGroup.clearCheck()
            siv_vip_activity.isVisible = false
            vip_activity_desc.isVisible = false
            iv_vip.isVisible = true
            vip_desc.isVisible = true
            button_vip.isSelected = true
            switch(R.id.button_vip)
            SPUtils.getInstance().put(Constant.KEY_VIP_ACTIVITY_TIME, System.currentTimeMillis())
        }
        buttonGroup.setOnCheckedChangeListener { _, checkedId ->
            button_vip.isSelected = false
            switch(checkedId)
        }
        buttonGroup.getChildAt(0).performClick()
    }

    private fun switch(id: Int) {
        GSYVideoManager.instance().stop()
        GSYVideoManager.releaseAllVideos()
        val transaction = supportFragmentManager.beginTransaction()
        var fragment = supportFragmentManager.findFragmentByTag(id.toString())
        if (fragment == null) {
            when (id) {
                R.id.button_home -> fragment = HomeFragment()
                R.id.button_find -> fragment = FindFragment()
                R.id.button_vip -> fragment = VIPFragment()
                R.id.button_hot -> fragment = HotFragment()
                R.id.button_mine -> fragment = MineFragment()
            }
            if (fragment != null) {
                transaction.add(R.id.fragmentContainer, fragment, id.toString())
            }
        }
        for (temp in supportFragmentManager.fragments) {
            if (temp != fragment) {
                transaction.hide(temp)
            }
        }
        if (fragment != null) {
            transaction.show(fragment).commitAllowingStateLoss()
        }
        if (BrightnessUtils.getWindowBrightness(window) != BrightnessUtils.getBrightness()) {
            BrightnessUtils.setWindowBrightness(window, BrightnessUtils.getBrightness())
        }
        if (id == R.id.button_find && GlobalValue.userInfoBean?.newWorks == true) { // 切换到发现需要且有新推荐需要发送事件刷新数据
            EventBus.getDefault().post(ReadNewRecommendEvent())
        }
    }

    /**
     * 回到首页HomeFragment
     */
    fun jumpMainHomeFragment(index: Int = 0) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = supportFragmentManager.findFragmentByTag(index.toString())
        for (temp in supportFragmentManager.fragments) {
            if (temp != fragment) {
                transaction.hide(temp)
            }
        }
        if (fragment != null) {
            transaction.show(fragment).commitAllowingStateLoss()
        }
        buttonGroup.getChildAt(index).performClick()
    }

    /**
     * 获取分享下载地址
     */
    private fun getShareUrl() {
        HttpRequest.post(RequestUrls.APP_DOWNLOAD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                GlobalValue.downloadH5Address = response.optString("url")
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, tag = this)
    }

    /**
     * 获取活动开关状态
     */
    private fun getActivitySwitch() {
        HttpRequest.post(RequestUrls.ACTIVITY_SWITCH, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val jsonArray = response.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list = Gson().fromJson<MutableList<ActivitySwitchBean>>(
                    jsonArray.toString(),
                    object : TypeToken<MutableList<ActivitySwitchBean>>() {}.type
                )
                list.forEach {
                    when (it.key) {
                        "bigv" -> {
                            SPUtils.getInstance().put(Constant.KEY_BIG_V_SWITCH, it.status)
                            if (it.content != null) {
                                SPUtils.getInstance().put(Constant.KEY_BIG_V_URL, it.content.toString())
                            }
                        }

                        "inviteCode" -> {
                            SPUtils.getInstance().put(Constant.ACTIVITY_SWITCH, it.status)
                            if (it.content != null) {   // 如果返回content=null，则表示不限制区域
                                SPUtils.getInstance().put(Constant.ACTIVITY_AREA, it.content.toString())
                            }
                        }
                    }
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        }, tag = this)
    }

    private fun checkVersion() {
        HttpRequest.get(RequestUrls.CHECK_VERSION, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response != null && response.length() > 0) {
                    showUpdate(response)
                } else {
                    getDialogNotice()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                getDialogNotice()
            }
        }, tag = this)
    }

    private fun showUpdate(json: JSONObject) {
        val dialog = UpdateDialog(this)
        dialog.setUpdateInfo(json)
        dialog.show()
    }

    private fun checkDownloadAndUpload() {
        if (!GlobalValue.isLogin()) {
            return
        }
        val result = Utils.Consumer<Boolean> {
            val autoNetDownload =
                SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_DOWNLOAD_MOBILE_NET, false)
            if (autoNetDownload || it) {
                DownloadMgr.restoreAll()
            }
            val autoNetUpload =
                SPUtils.getInstance().getBoolean(Constant.KEY_AUTO_UPLOAD_MOBILE_NET, false)
            if (autoNetUpload || it) {
                if (UploadMgr.isUploading()) {
                    BaseService.startService(this, UploadService::class.java)
                }
                val list =
                    DynamicCacheManger.instance.selectByStatus(DynamicReleaseStatus.RELEASING)
                if (list.isNotEmpty()) {
                    ReleaseDynamicService.start(this, list[0])
                }
            }

        }
        NetworkUtils.isWifiAvailableAsync(result)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (PlayerManager.instance.isPlaying) {
                //两种方式都可以实现退出后再次打开，避免多次启动app，前者返回桌面上次停留的页面，后者会返回桌面的默认页面
                moveTaskToBack(true)
                return true
            }
            if (GSYVideoManager.backFromWindowFull(this)) {
                return true
            }
            val time = System.currentTimeMillis()
            if (time - lastBackTime > 2000) {
                lastBackTime = time
                ToastUtils.showShort(R.string.key_back_tip)
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 刷新token
     */
    private fun refreshToken() {
        if (!GlobalValue.isLogin()) {
            return
        }
        if (!NetworkUtils.isConnected() && !NetworkUtils.isWifiConnected()) {
            return
        }
        HttpRequest.post(RequestUrls.REFRESH_TOKEN, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val bean = Gson().fromJson(response.toString(), UserInfoBean::class.java)
                SPUtils.getInstance().put(
                    Constant.KEY_USER_INFO,
                    EncodeUtils.base64Encode2String(response.toString().toByteArray())
                )
                GlobalValue.userInfoBean = bean
                SocketClient.userLogin()
                getUserInfo()
            }

            override fun onError(response: String?, errorMsg: String?) {
                AccountHelper.logout()
            }
        }, tag = this)
    }

    private fun getUserInfo() {
        if (GlobalValue.isLogin()) {
            HttpRequest.get(RequestUrls.USER_INFO, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    if (response == null) {
                        button_mine.setShowSmallDot(false)
                        button_find.setShowSmallDot(false)
                        return
                    }
                    val userInfoBean =
                        Gson().fromJson(response.toString(), UserInfoBean::class.java)
                    GlobalValue.userInfoBean?.copy(userInfoBean)
                    setMessagePoint(MessageUnreadStatus(userInfoBean.totalMsgCount))
                    setFindStatusPoint(AttentionUnreadStatus(userInfoBean.newWorks))
                    EventBus.getDefault().post(AttentionUnreadStatus(userInfoBean.newWorks))
                }

                override fun onError(response: String?, errorMsg: String?) {
                    button_mine.setShowSmallDot(false)
                    button_find.setShowSmallDot(false)
                }
            }, tag = this)
        } else {
            button_mine.setShowSmallDot(false)
            button_find.setShowSmallDot(false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setMessagePoint(status: MessageUnreadStatus) {
        button_mine.setShowSmallDot(status.messageCount > 0)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setFindStatusPoint(event: AttentionUnreadStatus) {
        button_find.setShowSmallDot(event.status)
    }

    fun showMenu(view: View) {
        val menu = LayoutInflater.from(this).inflate(R.layout.layout_home_popup_menu, null)
        val popupWindow = PopupWindow(
            menu,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        menu.findViewById<View>(R.id.upload_item).setOnClickListener {
            popupWindow.dismiss()
            uploadCenter(null)
        }
        menu.findViewById<View>(R.id.history_item).setOnClickListener {
            popupWindow.dismiss()
            startRecord(null)
        }
        menu.findViewById<View>(R.id.offline_item).setOnClickListener {
            popupWindow.dismiss()
            offlineVideo(null)
        }
        menu.findViewById<View>(R.id.dynamic_item).setOnClickListener {
            popupWindow.dismiss()
            releaseDynamic()
        }
        menu.findViewById<View>(R.id.picture_item).setOnClickListener {
            popupWindow.dismiss()
            mineAlbum(null)
        }
        menu.findViewById<View>(R.id.scan_item).setOnClickListener {
            popupWindow.dismiss()
            startScan(it)
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.contentView = menu
        popupWindow.isOutsideTouchable = true
        val lp = window.attributes
        lp.alpha = 0.5f
        window.attributes = lp
        popupWindow.setOnDismissListener {
            lp.alpha = 1f
            window.attributes = lp
        }
        val w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        menu.measure(w, h)
        popupWindow.showAsDropDown(
            view,
            menu.measuredWidth / -1 + view.measuredWidth / 2,
            0
        )
    }

    fun startScan(view: View?) {
        startActivity(Intent(this, ScanQrActivity::class.java))
    }

    fun uploadCenter(view: View?) {
        judgeLogin(UploadCenterActivity::class.java)
    }

    /**
     * 我的相册
     */
    fun mineAlbum(view: View?) {
        judgeLogin(MineAlbumActivity::class.java)
    }

    /**
     * 我的动态
     */
    fun mineDynamic(view: View?) {
        judgeLogin(MineDynamicActivity::class.java)
    }

    /**
     * 发布动态
     */
    fun releaseDynamic() {
        if (GlobalValue.isLogin()) {
            if (isReleasing()) {
                releasingDialog(this)
                return
            }
            val intent = Intent(this, SelectLocalImageActivity::class.java)
            intent.putExtra(SelectLocalImageActivity.maxCountKey, 18)
            intent.putExtra(SelectLocalImageActivity.isBackGifKey, true)
            intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.ALL)
            intent.putExtra(SelectLocalImageActivity.chooseModeKey, ChooseMode.ONLY)
            startActivityForResult(intent, 1000)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun isReleasing(): Boolean {
        val selectData = DynamicCacheManger.instance.selectByStatus(DynamicReleaseStatus.RELEASING)
        return selectData.isNotEmpty()
    }

    private fun releasingDialog(context: Context) {
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.releasing_dynamic_tip)
        dialog.setRightListener(R.string.sure) {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            val list = data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) as? MutableList<LocalMediaBean>
            if (!list.isNullOrEmpty()) {
                if (list.size == 1 && list[0].isVideo) {
                    ReleaseDynamicVideoActivity.launch(this, list[0])
                } else {
                    val intent = Intent(this, ReleaseDynamicActivity::class.java)
                    intent.putExtra("selectImg", list as Serializable)
                    startActivity(intent)
                }
            }
        }
    }

    fun offlineVideo(view: View?) {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if (!GlobalValue.isVipUser()) {
            val intent = Intent(this, OpenVipActivity::class.java)
            intent.putExtra("pathInfo", this.javaClass.simpleName)
            startActivity(intent)
        } else {
            judgeLogin(OfflineActivity::class.java)
        }
    }

    fun selectCategory(view: View) {
        startActivity(Intent(this, CategoryActivity::class.java))
    }

    fun startRecord(view: View?) {
        startActivity(Intent(this, RecordActivity::class.java))
    }

    fun toHotList(view: View) {
        startActivity(Intent(this, HotActivity::class.java))
    }

    fun startSearch(view: View) {
        startActivity(Intent(this, SearchActivity::class.java))
    }

    fun startReceiveVip(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(
                WebViewActivity.urlKey,
                SPUtils.getInstance().getString(Constant.KEY_BIG_V_URL)
            )
            intent.putExtra(WebViewActivity.titleKey, getString(R.string.authentication))
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun exchangeVip(view: View) {
        judgeLogin(ExchangeVipActivity::class.java)
    }

    fun startBuySelf(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, OpenVipActivity::class.java)
            intent.putExtra("pathInfo", this.javaClass.simpleName)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun fans(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, UserFansActivity::class.java)
            intent.putExtra("isSelf", true)
            intent.putExtra("userId", GlobalValue.userInfoBean?.id)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun focus(view: View) {
        if (GlobalValue.isLogin()) {
            val intent = Intent(this, UserFocusActivity::class.java)
            intent.putExtra("isSelf", true)
            intent.putExtra("userId", GlobalValue.userInfoBean?.id)
            startActivity(intent)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    fun startUserCollect(view: View) {
        judgeLogin(UserCollectActivity::class.java)
    }

    fun showVipTab(view: View) {
        startActivity(Intent(this, VIPIntroActivity::class.java))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReloginEvent(event: ReloginEvent) {
        AccountHelper.logout()
        if (event.needLogin) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
//        else {
//            ToastUtils.showLong(R.string.login_time_out)
//        }
    }

    override fun onLogin() {
        super.onLogin()
        songViewModel.syncLocalMusic()
    }

    override fun onLoginOut() {
        stopService()
        button_mine.setShowSmallDot(false)
        button_find.setShowSmallDot(false)
        PlayListMgr.clearCollect()
    }

    /**
     * 点击立即签到
     */
    fun signInClick(view: View) {
        judgeLogin(SignGetGiftActivity::class.java)
    }

    /**
     * 邀请有礼
     */
    fun inviteClick(view: View) {
        judgeLogin(PoliteInvitationActivity::class.java)
    }

    /**
     * 抽奖活动
     */
    fun raffleClick(view: View) {
        if (!GlobalValue.isLogin()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else if (!GlobalValue.raffleAddress.isNullOrEmpty()) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(WebViewActivity.urlKey, GlobalValue.raffleAddress)
            startActivity(intent)
        }
    }

    private fun judgeLogin(cls: Class<*>) {
        if (GlobalValue.isLogin()) {
            startActivity(Intent(this, cls))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setTranslucentImage() {
        StatusBarUtil.setTranslucentForImageViewInFragment(this, 0, null)
    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        GlobalValue.computeScaleSize()
        if (networkType != NetworkUtils.NetworkType.NETWORK_WIFI) {
            OkDownload.getInstance().pauseAll()
        } else {
            OkDownload.getInstance().startAll()
        }
    }

    override fun onDisconnected() {
        OkDownload.getInstance().pauseAll()
    }

    private fun getDialogNotice() {
        HttpRequest.post(RequestUrls.GET_NOTICE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val bean = Gson().fromJson(response.toString(), NoticeBean::class.java)
                // 获取本地存储的公告id
                val idStr = SPUtils.getInstance().getString(Constant.KEY_NOTICE_IDS)
                val idList: MutableList<String> = if (idStr.isEmpty()) {
                    ArrayList()
                } else {
                    Gson().fromJson(idStr, object : TypeToken<MutableList<String>>() {}.type)
                }
                val value =
                    (GlobalValue.userInfoBean?.id ?: 0).toString() + "-" + bean.id.toString()
                if (bean != null && !bean.activityContent.isNullOrEmpty() && !idList.contains(value)) {
                    val dialog = NoticeDialog(this@MainActivity)
                    dialog.setData(bean.title ?: "", bean.activityContent!!)
                    dialog.show()
                    idList.add(value)
                    SPUtils.getInstance().put(Constant.KEY_NOTICE_IDS, Gson().toJson(idList))
                } else {
                    getDialogAd()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                getDialogAd()
            }
        }, tag = this)
    }

    private fun getDialogAd() {
        val params = HttpParams()
        params.put("type", 5)
        params.put("region", NormalUtil.getAreaCode())
        HttpRequest.post(RequestUrls.GET_ADS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    checkNotification()
                    return
                }
                val listAd = Gson().fromJson<List<AdvertBean>>(
                    jsonArray.toString(),
                    object : TypeToken<List<AdvertBean>>() {}.type
                )
                if (listAd.isNotEmpty() && isSafe()) {
                    val dialog = PopAdWindow(this@MainActivity)
                    dialog.advertBean = listAd[0]
                    dialog.show()
                } else {
                    checkNotification()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                checkNotification()
            }
        }, params, this)
    }

    private fun getRechargeActivity() {
        HttpRequest.post(RequestUrls.RECHARGE_ACTIVITY, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val status = response.optBoolean("status")
                val type = response.optInt("type")  // 0 折扣活动 1 砍价活动
                if (type == 0) {
                    val mills = SPUtils.getInstance().getLong(Constant.KEY_VIP_ACTIVITY_TIME)
                    if (status && !TimeUtils.isToday(mills) && button_vip?.isSelected == false) {
                        val title = response.optString("title")
                        vip_activity_desc.text = title
                        siv_vip_activity.isVisible = true
                        vip_activity_desc.isVisible = true
                        iv_vip.isVisible = false
                        vip_desc.isVisible = false
                    } else {
                        setNormalVipTab()
                    }
                } else if (status && type == 1) {
                    GlobalValue.raffleAddress = response.optString("url")
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, tag = this)
    }

    private fun setNormalVipTab() {
        siv_vip_activity.isVisible = false
        vip_activity_desc.isVisible = false
        iv_vip.isVisible = true
        vip_desc.isVisible = true
    }

    /**
     * 检查消息通知开关
     */
    private fun checkNotification() {
        val forbidden = SPUtils.getInstance().getBoolean(Constant.KEY_OPEN_NOTIFICATION, false)
        val lastTime = SPUtils.getInstance().getLong(Constant.KEY_NOTIFICATION_TIME, 0)
        if (!TimeUtils.isToday(lastTime) && !forbidden && !JumpUtils.isNotificationEnabled(this)) {
            val dialog = NotificationTipDialog(this)
            dialog.show()
            SPUtils.getInstance().put(Constant.KEY_NOTIFICATION_TIME, System.currentTimeMillis())
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v is RadioButton && v.isChecked && event?.action == MotionEvent.ACTION_UP) {
            EventBus.getDefault().post(TabClickRefreshEvent(v.id))
        } else if (v is RadioButton && !v.isChecked && event?.action == MotionEvent.ACTION_UP) {
            v.performClick()
        }
        return false
    }

    override fun onNewMessage(message: ChatMessageBean) {
        super.onNewMessage(message)
        button_mine.setShowSmallDot(true)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun cleanCache(event: CleanCacheEvent) {
        if (!CleanTask.isDeleting)
            CleanTask().execute(applicationContext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserStatusChange(status: UserStatusChangeBean) {
        if (status.action == 0) {
            refreshToken()
        }
    }
}
