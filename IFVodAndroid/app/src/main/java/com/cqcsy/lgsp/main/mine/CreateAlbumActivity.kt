package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.view.View
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_create_album.*
import kotlinx.android.synthetic.main.activity_create_album.sureCreate
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * 创建相册
 */
class CreateAlbumActivity : NormalActivity() {
    var isMove: Boolean = false

    override fun getContainerView(): Int {
        return R.layout.activity_create_album
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.newAlbum)
        isMove = intent.getBooleanExtra("isMove", false)
        if (isMove) {
            sureCreate.text = StringUtils.getString(R.string.sureCreateMove)
        }
    }

    fun createAlbum(view: View) {
        val title = editTitle.text.toString().trim()
        if (title.isEmpty()) {
            ToastUtils.showLong(R.string.albumNameTips)
            return
        }
        showProgressDialog()
        val params = HttpParams()
        params.put("title", title)
        params.put("description", editInfo.text.toString().trim())
        HttpRequest.post(RequestUrls.CREATE_PICTURES, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response != null) {
                    val bean = Gson().fromJson<PicturesBean>(response.toString(), object : TypeToken<PicturesBean>() {}.type)
                    if (isMove) {
                        setResult(RESULT_OK, Intent().putExtra("picturesBean", bean))
                    } else {
                        EventBus.getDefault().post(AlbumRefreshEvent())
                        val intent = Intent(this@CreateAlbumActivity, AlbumDetailsActivity::class.java)
                        intent.putExtra(AlbumDetailsActivity.ALBUM_ID, bean.mediaKey)
                        startActivity(intent)
                    }
                    finish()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                dismissProgressDialog()
            }
        }, params, this)
    }
}