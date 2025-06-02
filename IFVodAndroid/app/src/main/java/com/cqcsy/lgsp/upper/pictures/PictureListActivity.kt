package com.cqcsy.lgsp.upper.pictures

import android.os.Bundle
import com.blankj.utilcode.util.GsonUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.database.bean.DynamicRecordBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.utils.LabelUtil
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_picture_list.*
import org.json.JSONObject

/**
 * 相册图片列表
 */
class PictureListActivity : NormalActivity() {

    override fun onViewCreate() {
        val mediaKey = intent.getStringExtra(UpperPicturesFragment.PICTURES_PID)
        val title = intent.getStringExtra(UpperPicturesFragment.PICTURES_TITLE) ?: ""
        setHeaderTitle(title)
        getAlbum(mediaKey)
        addHot(mediaKey)
    }

    override fun getContainerView(): Int {
        return R.layout.activity_picture_list
    }

    override fun onLogin() {
        commentContainer.setUserAvatar()
    }

    private fun getAlbum(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        showProgress()
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.GET_ALBUM, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val picturesBean = GsonUtils.fromJson<PicturesBean>(
                    response.toString(),
                    object : TypeToken<PicturesBean>() {}.type
                )
                dismissProgress()
                if (picturesBean == null) {
                    showFailed {
                        getAlbum(mediaKey)
                    }
                    return
                }
                val bundle = Bundle()
                bundle.putSerializable(UpperPicturesFragment.PICTURES_ITEM, picturesBean)
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, PicturesListFragment::class.java, bundle).commitAllowingStateLoss()
                commentContainer.setPictures(picturesBean)
                if (GlobalValue.isLogin()) {

                } else {
                    addRecord(picturesBean)
                }
                val label = (picturesBean.label?.split(","))?.first() ?: ""
                LabelUtil.addLabels(label, picturesBean.uid)
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    getAlbum(mediaKey)
                }
            }

        }, params)
    }

    private fun addRecord(bean: PicturesBean) {
        val dynamicRecordBean = DynamicRecordBean()
//        dynamicRecordBean.pid = bean.id
        dynamicRecordBean.mediaKey = bean.mediaKey
        dynamicRecordBean.headImg = bean.headImg ?: ""
        dynamicRecordBean.upperName = bean.upperName ?: ""
        dynamicRecordBean.createTime = bean.createTime
        dynamicRecordBean.title = bean.title
        dynamicRecordBean.description = bean.description ?: ""
        dynamicRecordBean.coverPath = bean.coverPath ?: ""
        dynamicRecordBean.photoCount = bean.photoCount
        dynamicRecordBean.comments = bean.comments
        dynamicRecordBean.likeCount = bean.likeCount
        dynamicRecordBean.uid = bean.uid
        dynamicRecordBean.bigV = bean.bigV
        dynamicRecordBean.vipLevel = bean.vipLevel
        dynamicRecordBean.type = 1
        DynamicRecordManger.instance.add(dynamicRecordBean)
    }

    private fun addHot(mediaKey: String?) {
        if (mediaKey.isNullOrEmpty()) return
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        HttpRequest.get(RequestUrls.PICTURES_HOT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {

            }

            override fun onError(response: String?, errorMsg: String?) {
            }

        }, params)
    }
}