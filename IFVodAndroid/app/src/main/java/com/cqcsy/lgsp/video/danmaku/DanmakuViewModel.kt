package com.cqcsy.lgsp.video.danmaku

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RelativeLayout
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.BarrageBean
import com.cqcsy.lgsp.video.player.LiteVideoPlayer
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItem
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.ecs.component.filter.BlockedTextFilter
import com.kuaishou.akdanmaku.ecs.component.filter.TypeFilter
import com.kuaishou.akdanmaku.ecs.component.filter.UserIdFilter
import com.kuaishou.akdanmaku.render.TypedDanmakuRenderer
import com.kuaishou.akdanmaku.ui.DanmakuListener
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.kuaishou.akdanmaku.ui.DanmakuView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * 弹幕相关控制
 */
class DanmakuViewModel(val danmakuView: DanmakuView, val liteVideoPlayer: LiteVideoPlayer) {
    var mDanmakuPlayer: DanmakuPlayer? = null
    private var mDanmakuItemList: MutableList<DanmakuItem>? = null
    private val mContext = danmakuView.context

    private var danmakuConfig: DanmakuConfig = DanmakuConfig().apply {
        textSizeScale = getDanmakuFont()
        alpha = getDanmakuAlpha()
        screenPart = 0.4f
        dataFilter = arrayListOf(UserIdFilter(), BlockedTextFilter { it == 0L }, TypeFilter())
        rollingDurationMs = getRollingDuration()
        durationMs = getRollingDuration()
    }

    var danmakusList: MutableList<BarrageBean> = ArrayList()
    private var danmakuStartSeekPosition: Long = -1

    init {
        setForbiddenPosition()
    }

    fun clone(config: DanmakuConfig) {
        danmakuConfig = config.copy()
    }

    fun getConfig(): DanmakuConfig {
        return danmakuConfig
    }

    fun isStarted(): Boolean {
        return mDanmakuPlayer?.started == true
    }

    fun danmakuOnPause() {
        try {
            mDanmakuPlayer?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDanmaKuData(data: MutableList<BarrageBean>) {
        releaseDanmaku()
        danmakusList.clear()
        mDanmakuPlayer =
            DanmakuPlayer(
                TypedDanmakuRenderer(
                    SpannedRenderer(danmakuView.context),
                    Pair(DanmakuStyle.DANMAKU_STYLE_COMMON, CommonRenderer(danmakuView.context)),
                    Pair(DanmakuStyle.DANMAKU_STYLE_SPAN, SpannedRenderer(danmakuView.context)),
                    Pair(DanmakuStyle.DANMAKU_STYLE_AD, DanmakuAdRenderer(danmakuView.context)),
                )
            ).also {
                it.bindView(danmakuView)
            }
        mDanmakuPlayer?.listener = object : DanmakuListener {
            override fun onDanmakuShown(item: DanmakuItem) {
                val itemData = item.data
                if (itemData is AdvertModel) {
                    itemData.callback?.let {
                        HttpRequest.get<Nothing>(it, null)
                    }
                }
            }
        }
        if (data.isEmpty()) {
            return
        }
        danmakusList.addAll(data)
        CoroutineScope(Dispatchers.IO).launch {
            val barrageList: MutableList<DanmakuItemData> = ArrayList()
            for ((index, model) in data.withIndex()) {
                val danmakuItemData = getDanmaku(index, model)
                barrageList.add(danmakuItemData)
            }
            mDanmakuItemList = mDanmakuPlayer?.updateData(barrageList)?.toMutableList()
            Handler(Looper.getMainLooper()).post {
                startDanmaku()
            }
        }
    }

    fun updateDanmaKuData(data: MutableList<BarrageBean>) {
        if (data.isEmpty()) {
            return
        }
        danmakusList.addAll(data)
        val barrageList: MutableList<DanmakuItem> = ArrayList()
        for ((index, model) in data.withIndex()) {
            val danmakuItemData = getDanmaku(index, model)
            val danmakuItem = DanmakuItem(danmakuItemData, mDanmakuPlayer)
            barrageList.add(danmakuItem)
            mDanmakuItemList?.add(danmakuItem)
        }
        mDanmakuPlayer?.updateItems(barrageList)
    }

    fun toggleDanmakuShow() {
        val show = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        SPUtils.getInstance().put(Constant.KEY_SWITCH_DANAMA, !show)
        resolveDanmakuShow()
    }

    /**
     * 弹幕的显示与关闭
     */
    fun resolveDanmakuShow() {
        val show = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        if (show) {
            show()
        } else {
            hide()
        }
    }

    private fun show() {
        danmakuView.visibility = View.VISIBLE
        danmakuView.postInvalidate()
    }

    private fun hide() {
        danmakuView.visibility = View.INVISIBLE
    }

    /**
     * 弹幕偏移
     */
    fun resolveDanmakuSeek(time: Long) {
        if (liteVideoPlayer.hasPlayed() && liteVideoPlayer.isInPlayingState && mDanmakuPlayer?.isReleased == false) {
            if (danmakuStartSeekPosition != -1L) {
                mDanmakuPlayer?.seekTo(danmakuStartSeekPosition)
            } else {
                mDanmakuPlayer?.seekTo(time)
            }
            danmakuStartSeekPosition = -1L
            danmakuConfig.updateFirstShown()
//            startDanmaku()
        } else {
            danmakuStartSeekPosition = time
        }
    }

    fun isEnableShowBarrage(): Boolean {
        val switchStatus = SPUtils.getInstance().getBoolean(Constant.KEY_SWITCH_DANAMA, true)
        return switchStatus
    }

    fun startDanmaku() {
        mDanmakuPlayer?.start(danmakuConfig)
        if (mDanmakuPlayer?.getSpeed() != liteVideoPlayer.speed) {
            setPlaySpeed(liteVideoPlayer.speed)
        }
        if (danmakuStartSeekPosition != -1L) {
            resolveDanmakuSeek(danmakuStartSeekPosition)
        }
        if (!liteVideoPlayer.isPlaying) {
            danmakuOnPause()
        }
        if (isEnableShowBarrage()) show() else hide()
    }

    fun hold(item: DanmakuItem?) {
        danmakuView.danmakuPlayer?.hold(item)
    }

    /**
     * 释放弹幕控件
     */
    fun releaseDanmaku() {
        mDanmakuPlayer?.reset()
        mDanmakuPlayer?.release()
    }

    private fun getForbidden(value: String): MutableList<Int>? {
        if (value.isEmpty()) {
            return null
        }
        val splitArray = value.split(",")
        val list = ArrayList<Int>()
        for (temp in splitArray) {
            if (temp.isNotEmpty()) {
                list.add(temp.toInt())
            }
        }
        return list
    }

    /**
     * 模拟添加弹幕数据
     */
    @Synchronized
    private fun getDanmaku(index: Int, model: BarrageBean): DanmakuItemData {
        val defaultSize = SizeUtils.sp2px(18.0f)
        val imageSize = SizeUtils.sp2px(24.0f)
        if (model.isAdvert) {
            val advertModel = AdvertModel(
                index.toLong(),
                defaultSize,
                model
            )
            loadImage(imageSize, model.avatar) { bitmap ->
                if (bitmap == null) {
                    val option = BitmapFactory.Options()
                    option.outWidth = imageSize
                    option.outHeight = imageSize
                    val logo =
                        BitmapFactory.decodeResource(
                            mContext.resources,
                            R.mipmap.icon_circle_logo,
                            option
                        )
                    advertModel.userImage = logo
                    updateItem(advertModel)
                } else {
                    advertModel.userImage = bitmap
                    danmakuConfig.apply {
                        updateCache()
                        updateRender()
                    }
                }
            }
            return advertModel
        } else if (model.prefix != 3) {// prefix 0 所有打开 1 昵称和头像 2 国家 3 关闭所有
            val spannedModel = SpannedModel(
                index.toLong(),
                defaultSize, barrageBean = model
            )
            if (model.prefix == 0 || model.prefix == 1) {
                loadImage(imageSize, model.avatar) { bitmap ->
                    if (bitmap == null) {
                        val option = BitmapFactory.Options()
                        option.outWidth = imageSize
                        option.outHeight = imageSize
                        val logo =
                            BitmapFactory.decodeResource(
                                mContext.resources,
                                R.mipmap.icon_circle_logo,
                                option
                            )
                        spannedModel.userImage = logo
                        updateItem(spannedModel)
                    } else {
                        spannedModel.userImage = bitmap
                        danmakuConfig.apply {
                            updateCache()
                            updateRender()
                        }
                    }
                }
            }
            return spannedModel
        } else {
            return BaseDanmakuItemData(
                index.toLong(),
                defaultSize,
                model
            )
        }
    }

    fun addDanmaku(barrageBean: BarrageBean) {
        val danmakuItemData = getDanmaku(Random(100000).nextInt(), barrageBean)
        // 收到的实时弹幕，如果因为网络延迟，不超过3秒，就直接显示当前时间
        val current = mDanmakuPlayer?.getCurrentTimeMs() ?: 0L
        if (barrageBean.isLive) {
            danmakuItemData.position = current
        } else if (abs(barrageBean.second * 1000 - current) < 3000) {
            danmakuItemData.position = current
        }
        startDanmaku()
        val item = mDanmakuPlayer?.send(danmakuItemData)
        if (item != null) {
            mDanmakuItemList?.add(item)
        }
        if (!barrageBean.isLive && !barrageBean.isAdvert) {
            danmakusList.add(barrageBean)
        }
    }

    fun addDanmaku(barrageBeans: MutableList<BarrageBean>) {
        for (barrageBean in barrageBeans) {
            val danmakuItemData = getDanmaku(Random(100000).nextInt(), barrageBean)
            // 收到的实时弹幕，如果因为网络延迟，不超过3秒，就直接显示当前时间
            if (barrageBean.isLive) {
                danmakuItemData.position = mDanmakuPlayer?.getCurrentTimeMs() ?: 0L
            }
            startDanmaku()
            val item = mDanmakuPlayer?.send(danmakuItemData)
            if (item != null) {
                mDanmakuItemList?.add(item)
            }
            if (!barrageBean.isLive && !barrageBean.isAdvert) {
                danmakusList.add(barrageBean)
            }
        }
    }

    fun loadImage(imageSize: Int, userImage: String?, callback: (bitmap: Bitmap?) -> Unit) {
        if (userImage.isNullOrEmpty()) {
            callback.invoke(null)
            return
        }
        var imageUrl = if (userImage.contains("?")) {
            "$userImage&"
        } else {
            "$userImage?"
        }
        if (imageUrl.contains(".gif", true) || imageUrl.contains(".png", true)) {
            imageUrl += ImageUtil.FORMAT_PARAM
        }
        imageUrl += "&isapp=1"
        imageUrl += String.format(ImageUtil.cropParam, imageSize, imageSize)
        val option = RequestOptions().transform(CircleCrop())
        Glide.with(mContext).load(imageUrl).apply(option)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        resource.setBounds(0, 0, imageSize, imageSize)
                        callback.invoke((resource as BitmapDrawable).bitmap)
                    }
                    return true
                }

            }).submit(imageSize, imageSize)
    }

    fun getDanmakuItemList(): MutableList<DanmakuItem>? {
        return mDanmakuItemList
    }

    fun updateItem(item: DanmakuItemData) {
        if (mDanmakuItemList == null) {
            return
        }
        for (data in mDanmakuItemList!!) {
            if (data.data.danmakuId == item.danmakuId) {
                mDanmakuPlayer?.updateItem(data)
                break
            }
        }
    }

    /**
     * 设置屏蔽用户弹幕
     */
    fun setForbiddenUserDanmaku(uid: Long) {
        danmakuConfig = danmakuConfig.copy()
        (danmakuConfig.dataFilter[0] as? UserIdFilter)?.addFilterItem(uid)
        danmakuConfig.updateFilter()
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun removeForbiddenUserDanmaku(uid: Long) {
        danmakuConfig = danmakuConfig.copy()
        (danmakuConfig.dataFilter[0] as? UserIdFilter)?.removeFilterItem(uid)
        danmakuConfig.updateFilter()
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun setForbiddenUserDanmaku(data: MutableList<Long>?) {
        danmakuConfig = danmakuConfig.copy()
        val filter = danmakuConfig.dataFilter[0] as? UserIdFilter
        if (data.isNullOrEmpty()) {
            filter?.clear()
        } else {
            filter?.addFilterItems(data)
        }
        danmakuConfig.updateFilter()
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun setForbiddenWord(data: MutableList<CharSequence>?) {
        danmakuConfig = danmakuConfig.copy()
        val filter = danmakuConfig.dataFilter[1] as? BlockedTextFilter
        filter?.clear()
        if (data != null) {
            filter?.addFilterItems(data)
        }
        danmakuConfig.updateFilter()
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun getDanmakuAlpha(): Float {
        return SPUtils.getInstance()
            .getFloat(Constant.KEY_WATCH_DANAMA_ALPHA + GlobalValue.userInfoBean?.id, 1f)
    }

    fun setDanmakuAlpha() {
        danmakuConfig = danmakuConfig.copy(alpha = getDanmakuAlpha())
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun setPlaySpeed(speed: Float) {
        mDanmakuPlayer?.updatePlaySpeed(speed)
    }

    fun setDanmakuSpeed() {
        val duration = getRollingDuration()
        danmakuConfig = danmakuConfig.copy(rollingDurationMs = duration, durationMs = duration)
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    private fun getRollingDuration(): Long {
        val speed = SPUtils.getInstance()
            .getInt(Constant.KEY_WATCH_DANAMA_SPEED + GlobalValue.userInfoBean?.id, 2)
        val isFullscreen = liteVideoPlayer.isIfCurrentIsFullscreen && !liteVideoPlayer.getIsVerticalVideo()
        val duration = if (!isFullscreen) {
            when (speed) {
                1 -> 7000L    // 慢
                2 -> 5500L  // 适中
                else -> 3000L  // 快
            }
        } else {
            when (speed) {
                1 -> 7000L    // 慢
                2 -> 5000L  // 适中
                else -> 4000L  // 快
            }
        }
        return duration
    }

    fun setDanmakuTextSize() {
        danmakuConfig = danmakuConfig.copy(textSizeScale = getDanmakuFont())
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    private fun getDanmakuFont(): Float {
        val font = SPUtils.getInstance()
            .getInt(Constant.KEY_WATCH_DANAMA_FONT + GlobalValue.userInfoBean?.id, 2)
        return when (font) {
            1 -> 2 / 3f
            3 -> 2.0f
            else -> 1.0f
        }
    }

    fun setForbiddenPosition() {
        val forbidden = SPUtils.getInstance().getString(
            Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id,
            ""
        )
        val forbiddenList = getForbidden(forbidden)
        danmakuConfig = danmakuConfig.copy()
        val filter = danmakuConfig.dataFilter[2] as? TypeFilter
        filter?.clear()
        if (forbiddenList != null && forbiddenList.size > 0) {
            if (forbiddenList.contains(DanmakuItemData.DANMAKU_MODE_ROLLING)) {
                filter?.addFilterItem(DanmakuItemData.DANMAKU_MODE_ROLLING)
            }
            if (forbiddenList.contains(DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM)) {
                filter?.addFilterItem(DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM)
            }
            if (forbiddenList.contains(DanmakuItemData.DANMAKU_MODE_CENTER_TOP)) {
                filter?.addFilterItem(DanmakuItemData.DANMAKU_MODE_CENTER_TOP)
            }
        }
        danmakuConfig.updateFilter()
        mDanmakuPlayer?.updateConfig(danmakuConfig)
    }

    fun setVerticalMargin(margin: Int) {
        val params = danmakuView.layoutParams as RelativeLayout.LayoutParams
        params.setMargins(0, margin, 0, margin)
        danmakuView.layoutParams = params
    }
}