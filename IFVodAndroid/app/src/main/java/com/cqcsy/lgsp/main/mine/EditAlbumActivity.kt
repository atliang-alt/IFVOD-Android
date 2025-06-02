package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.event.AlbumRefreshEvent
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.library.views.TipsDialog
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_edit_album.*
import kotlinx.android.synthetic.main.layout_edit_album_face_dialog.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.File

/**
 * 编辑相册
 */
class EditAlbumActivity : NormalActivity() {
    private var title = ""
    private var description = ""
    private var face = ""
    private var mediaKey: String? = null
    private var pictureCount = 0

    override fun getContainerView(): Int {
        return R.layout.activity_edit_album
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.editAlbum)
        mediaKey = intent.getStringExtra("mediaKey")
        pictureCount = intent.getIntExtra("pictureCount", 0)
        title = intent.getStringExtra("title") ?: ""
        description = intent.getStringExtra("description") ?: ""
        face = intent.getStringExtra("face") ?: ""
        editTitle.setText(title)
        editTitle.setSelection(editTitle.text.length)
        editInfo.setText(description)
        editInfo.setSelection(editInfo.text.length)
        ImageUtil.loadImage(this, face, faceImg, defaultImage = 0, scaleType = ImageView.ScaleType.CENTER)
        updateFace.setOnClickListener {
            showFaceDialog()
        }
    }

    /**
     * 选择图片dialog
     */
    private fun showFaceDialog() {
        val dialog = object : BottomBaseDialog(this) {}
        val contentView = View.inflate(this, R.layout.layout_edit_album_face_dialog, null)
        if (pictureCount == 0)
            contentView.albumSelect.setTextColor(ColorUtils.getColor(R.color.grey))
        contentView.localUpload.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, SelectLocalImageActivity::class.java)
            intent.putExtra(SelectLocalImageActivity.widthKey, 1f)
            intent.putExtra(SelectLocalImageActivity.heightKey, 1f)
            intent.putExtra(SelectLocalImageActivity.isCutPhotoKey, false)
            intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
            startActivityForResult(intent, 1000)
        }
        contentView.albumSelect.setOnClickListener {
            if (pictureCount == 0) {
                return@setOnClickListener
            }
            dialog.dismiss()
            val intent = Intent(this, SelectAlbumImageActivity::class.java)
            intent.putExtra("mediaKey", mediaKey)
            intent.putExtra("pictureCount", pictureCount)
            intent.putExtra("isManager", false)
            startActivityForResult(intent, 1001)
        }
        contentView.cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        dialog.show()
    }

    fun deleteClick(view: View) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.tips)
        tipsDialog.setMsg(R.string.deleteAlbumTips)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            val params = HttpParams()
            params.put("mediaKey", mediaKey)
            HttpRequest.post(RequestUrls.DELETE_ALBUM, object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    ToastUtils.showLong(R.string.deleteSuccess)
                    EventBus.getDefault().post(AlbumRefreshEvent())
                    setResult(Activity.RESULT_OK, Intent().putExtra("isDelete", true))
                    finish()
                }

                override fun onError(response: String?, errorMsg: String?) {
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    fun saveClick(view: View) {
        editAlbum()
    }

    private fun uploadAlbumPhoto(path: String) {
        showProgressDialog()
        val url = RequestUrls.UPLOAD_ALBUM_PHOTO + "?type=0"
        var imagePath = path
        if (ImageUtils.getImageType(path) == ImageUtils.ImageType.TYPE_WEBP) {
            imagePath = ImageUtil.formatJpePath(path)
        }
        val params = HttpParams()
        params.put("file", File(imagePath))
        HttpRequest.post(url, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                face = response?.optString("filepath").toString()
                ImageUtil.loadImage(
                    this@EditAlbumActivity,
                    face,
                    faceImg,
                    defaultImage = 0,
                    scaleType = ImageView.ScaleType.CENTER
                )
                dismissProgressDialog()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    private fun editAlbum() {
        title = editTitle.text.toString().trim()
        if (title.isEmpty()) {
            ToastUtils.showLong(R.string.title_tip)
            return
        }
        showProgressDialog()
        description = editInfo.text.toString().trim()
        val params = HttpParams()
        params.put("mediaKey", mediaKey)
        params.put("title", title)
        params.put("description", description)
        params.put("coverPath", face)
        HttpRequest.post(RequestUrls.EDIT_ALBUM, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                EventBus.getDefault().post(AlbumRefreshEvent())
                val intent = Intent()
                intent.putExtra("isDelete", false)
                intent.putExtra("title", title)
                intent.putExtra("description", description)
                intent.putExtra("face", face)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                if (data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) != null) {
                    val list = data.getSerializableExtra(SelectLocalImageActivity.imagePathList) as MutableList<LocalMediaBean>
                    PictureUploadManager.getAbsolutePath(list[0].path)?.let {
                        uploadAlbumPhoto(it)
                    }
                }
            }
            if (requestCode == 1001) {
                face = data?.getStringExtra("imagePath") ?: ""
                ImageUtil.loadImage(
                    this@EditAlbumActivity,
                    face,
                    faceImg,
                    defaultImage = 0,
                    scaleType = ImageView.ScaleType.CENTER
                )
            }
        }
    }
}