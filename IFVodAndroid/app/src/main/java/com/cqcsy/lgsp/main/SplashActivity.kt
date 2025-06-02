package com.cqcsy.lgsp.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.*
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.BaseActivity
import com.cqcsy.lgsp.bean.AdvertBean
import com.cqcsy.lgsp.download.DownloadMgr
import com.cqcsy.lgsp.utils.BackupServer
import com.cqcsy.lgsp.utils.Location
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.library.utils.StatusBarUtil
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.views.MessageToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_splash.*
import org.json.JSONObject
import java.util.*

/**
 * 启动页
 */
class SplashActivity : BaseActivity() {
    var advertBean: AdvertBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val restartAd = intent.getBooleanExtra("restartAd", false)
        // 推送点击或后台启动
        if (!isTaskRoot && !restartAd) {
            finish()
            return
        }
        MessageToast.reset()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        StatusBarUtil.setTranslucent(this)
        BarUtils.setNavBarVisibility(this, false)
        if (NormalUtil.isTv(this)) {
            showTvTip()
            return
        }
        setContentView(R.layout.activity_splash)

        DownloadMgr.initFilePath()
        updateServer()
        mHandler.postDelayed({
            delayToMain()
        }, 15_000)
    }

    private fun updateServer() {

        getSplash()
//        val backupServer = ViewModelProvider(this).get(BackupServer::class.java)
//        backupServer.mServerHost.observe(this) {
//            cacheServer(it)
//        }
//        backupServer.mAllServerFinish.observe(this) {
//            if (it) {
//                SPUtils.getInstance().remove(Constant.KEY_RELEASE_SOCKET_URL)
//                SPUtils.getInstance().remove(Constant.KEY_RELEASE_BASE_URL)
//                SPUtils.getInstance().remove(Constant.KEY_RELEASE_H5_URL)
//                getSplash()
//            }
//        }
//        backupServer.locationResult.observe(this) {
//            backupServer.updateServer(it)
//        }
////        if (!BuildConfig.DEBUG) {
//        backupServer.location()
//        } else {
//            getSplash()
//        }
    }

    private fun cacheServer(json: JSONObject) {
        val api = json.optString("api")
        val socket = json.optString("socket")
        val h5 = json.optString("h5")

        SPUtils.getInstance().put(Constant.KEY_RELEASE_SOCKET_URL, socket)
        SPUtils.getInstance().put(Constant.KEY_RELEASE_BASE_URL, "$api/")
        SPUtils.getInstance().put(Constant.KEY_RELEASE_H5_URL, h5)
        getSplash()
    }

    private fun showTvTip(): Boolean {
        val dialog = TipsDialog(this)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.phone_check_tip)
        dialog.setLeftListener(R.string.ensure) {
            dialog.dismiss()
            finish()
        }
        dialog.show()
        return false
    }

    private fun getSplash() {
        startLocation()
        val advert = SPUtils.getInstance().getString(Constant.KEY_SPLASH_ADVERT)
        if (advert.isNullOrEmpty()) {
            delayToMain()
        } else {
            val json = String(EncodeUtils.base64Decode(advert))
            val listAd = Gson().fromJson<List<AdvertBean>>(
                json,
                object : TypeToken<List<AdvertBean>>() {}.type
            )
            if (listAd != null && listAd.isNotEmpty()) {
                var flag = false
                for (item in listAd) {
                    if (!item.endtime.isNullOrEmpty() && TimesUtils.formatDate(item.endtime!!)?.after(Date()) == true) {
                        flag = true
                        advertBean = item
                        showSplash(item.showURL)
                        item.viewURL?.let { showCallBack(it) }
                        break
                    }
                }
                if (!flag) {
                    delayToMain()
                }
            } else {
                delayToMain()
            }
        }
    }

    private fun delayToMain() {
        mHandler.postDelayed({ startMain() }, 2000)
    }

    /**
     * 广告展示回调
     */
    fun showCallBack(backUrl: String) {
        HttpRequest.get(backUrl, object : HttpCallBack<String>() {
            override fun onSuccess(response: String?) {
            }

            override fun onError(response: String?, errorMsg: String?) {
            }
        })
    }

    private fun showSplash(imageUrl: String) {
        defaultSplash.visibility = View.GONE
        splashContent.visibility = View.VISIBLE
        val width = if (ScreenUtils.getScreenWidth() < 750) {
            ScreenUtils.getScreenWidth()
        } else {
            750
        }
        ImageUtil.loadImage(
            this,
            imageUrl,
            splashImage,
            needAuthor = true,
            imageWidth = width,
            requestListener = object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startMain()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    splashImage.scaleType = ImageView.ScaleType.FIT_XY
                    skipTips.visibility = View.VISIBLE
                    skipTips.text = getString(R.string.skip_ad, currentNumber.toString())
                    if (splashImage.getTag(R.id.view_glide_animate) == null) {
                        splashImage.clearAnimation()
                        val animation = AlphaAnimation(0F, 1F)
                        animation.duration = 300
                        splashImage.startAnimation(animation)
                        splashImage.setTag(R.id.view_glide_animate, true)
                    }
                    mHandler.sendEmptyMessageDelayed(100, 1000)
                    return false
                }

            },
            defaultImage = 0,
            corner = 0
        )
    }

    var currentNumber = 5

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            currentNumber--
            if (currentNumber > 0) {
                skipTips.text = getString(R.string.skip_ad, currentNumber.toString())
                sendEmptyMessageDelayed(100, 1000)
            } else {
                startMain()
            }
        }
    }

    override fun onDestroy() {
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    fun skipSplash(view: View) {
        startMain()
    }

    private fun startLocation() {
        val location = Location.instance(this)
        location.start()
    }

    fun showAdDetail(view: View) {
        if (advertBean == null) {
            return
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("advertBean", advertBean)
        startActivity(intent)
        finish()
    }

    private fun startMain() {
        mHandler.removeCallbacksAndMessages(null)
        if (!intent.getBooleanExtra("restartAd", false)) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}