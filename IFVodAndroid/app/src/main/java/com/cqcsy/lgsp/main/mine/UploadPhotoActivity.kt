package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.lgsp.upper.pictures.PicturesBean
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.uploadPicture.PictureUploadManager
import com.cqcsy.library.uploadPicture.PictureUploadTask
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_upload_photo.*

/**
 * 上传相册图片
 */
class UploadPhotoActivity : NormalActivity() {
    private var dataList: MutableList<LocalMediaBean> = ArrayList()
    private var adapter: BaseQuickAdapter<LocalMediaBean, BaseViewHolder>? = null
    private var albumBean: PicturesBean? = null

    override fun getContainerView(): Int {
        return R.layout.activity_upload_photo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.uploadPhoto)
        if (intent.getSerializableExtra("albumBean") != null) {
            albumBean = intent.getSerializableExtra("albumBean") as PicturesBean
        }
        initData()
        initView()
    }

    private fun initData() {
        if (intent.getSerializableExtra("imagePathList") != null) {
            dataList.addAll(intent.getSerializableExtra("imagePathList") as MutableList<LocalMediaBean>)
        }
        dataList.add(0, LocalMediaBean())
    }

    private fun initView() {
        uploadBtn.isEnabled = dataList.size > 1
        albumName.text = albumBean?.title
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(
            XGridBuilder(this).setVLineSpacing(5f).setHLineSpacing(5f).setIncludeEdge(true).build()
        )
        adapter = object : BaseQuickAdapter<LocalMediaBean, BaseViewHolder>(
            R.layout.item_select_upload_photo,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: LocalMediaBean) {
                val position = getItemPosition(item)
                if (position == 0) {
                    ImageUtil.loadLocalId(
                        this@UploadPhotoActivity,
                        R.mipmap.icon_add_photo,
                        holder.getView(R.id.image)
                    )
                    holder.setGone(R.id.deleteImg, true)
                    holder.getView<FrameLayout>(R.id.imageLayout).setOnClickListener {
                        val intent =
                            Intent(this@UploadPhotoActivity, SelectLocalImageActivity::class.java)
                        intent.putExtra(SelectLocalImageActivity.maxCountKey, 1000)
                        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
                        startActivityForResult(intent, 1000)
                    }
                } else {
                    holder.setVisible(R.id.deleteImg, true)
                    ImageUtil.loadImage(
                        this@UploadPhotoActivity,
                        item.path,
                        holder.getView(R.id.image),
                        0,
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    )
                    holder.getView<LinearLayout>(R.id.deleteImg).setOnClickListener {
                        dataList.removeAt(position)
                        adapter?.notifyDataSetChanged()
                        uploadBtn.isEnabled = dataList.size > 1
                    }
                }
            }
        }
        recyclerView.adapter = adapter
    }

    fun uploadClick(view: View) {
        showProgressDialog()
        dataList.removeAt(0)
        if (dataList.isNullOrEmpty()) {
            ToastUtils.showLong(R.string.selectImageTips)
            return
        }
        val list: MutableList<PictureUploadTask> = ArrayList()
        dataList.forEach {
            val taskBean = PictureUploadTask()
            taskBean.taskTag = albumBean?.mediaKey
            taskBean.localPath = it.path
            taskBean.userId = GlobalValue.userInfoBean?.id ?: 0
            taskBean.requestUrl = RequestUrls.INSERT_ALBUM_PHOTO
            taskBean.serverFileName = PictureUploadManager.generateServerFileName(it.path)
            val params = HttpParams()
            params.put("imageKey", "imgpath")
            params.put("title", taskBean.serverFileName)
            params.put("mediaKey", albumBean?.mediaKey)
            taskBean.params = params
            list.add(taskBean)
        }
        PictureUploadManager.uploadImage(list)
        ToastUtils.showLong(R.string.upload_add)
        dismissProgressDialog()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1000) {
                if (data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) != null) {
                    val list = data.getSerializableExtra(SelectLocalImageActivity.imagePathList) as MutableList<LocalMediaBean>
                    if (!list.isNullOrEmpty()) {
                        dataList.addAll(list)
                        adapter?.notifyDataSetChanged()
                    }
                    uploadBtn.isEnabled = dataList.size > 1
                }
            }
        }
    }
}