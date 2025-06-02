package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.ImageUtil
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import kotlinx.android.synthetic.main.layout_recyclerview.*

/**
 * 选择上传的视频
 */
class SelectVideoActivity : NormalActivity() {
    private val videoPreviewCode = 1001
    private lateinit var videoListAdapter: BaseQuickAdapter<LocalMediaBean, BaseViewHolder>

    override fun getContainerView(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.selectVideo)
        initView()
        requestPermissions()
    }

    private fun requestPermissions() {
        val permissionUtils = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            PermissionUtils.permission(
                PermissionConstants.STORAGE
            )
        }
        permissionUtils.callback(object : PermissionUtils.SimpleCallback {
            override fun onGranted() {
                PictureSelector.create(this@SelectVideoActivity)
                    .dataSource(SelectMimeType.ofVideo())
                    .setFilterVideoMaxSecond(5 * 60)
                    .isPageStrategy(false)
                    .setQuerySortOrder(MediaStore.MediaColumns.DATE_MODIFIED + " DESC")
                    .obtainMediaData {
                        val list = it?.map { localMedia ->
                            LocalMediaBean().copy(localMedia)
                        }?.toMutableList()
                        if (list.isNullOrEmpty()) {
                            showEmpty()
                        } else {
                            videoListAdapter.setList(list)
                        }
                    }
            }

            override fun onDenied() {
                ToastUtils.showLong(R.string.permission_album)
                finish()
            }
        })
        permissionUtils.request()
    }

    private fun initView() {
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.addItemDecoration(
            XGridBuilder(this).setVLineSpacing(5f).setHLineSpacing(5f).setIncludeEdge(true).build()
        )
        videoListAdapter = object : BaseQuickAdapter<LocalMediaBean, BaseViewHolder>(
            R.layout.item_select_short_video,
        ) {
            override fun convert(holder: BaseViewHolder, item: LocalMediaBean) {
                holder.setText(R.id.duration, CommonUtil.stringForTime(item.duration))
                ImageUtil.loadImage(
                    this@SelectVideoActivity,
                    item.path,
                    holder.getView(R.id.image),
                    0,
                    scaleType = ImageView.ScaleType.CENTER_CROP
                )
                holder.setVisible(R.id.checkbox, false)
                holder.getView<FrameLayout>(R.id.imageLayout).setOnClickListener {
                    startVideoPreview(item)
                }
            }
        }
        recyclerView.adapter = videoListAdapter
    }

    private fun startVideoPreview(localMediaBean: LocalMediaBean) {
        val intent = Intent(this, VideoPreviewActivity::class.java)
        intent.putExtra("LocalMediaBean", localMediaBean)
        startActivityForResult(intent, videoPreviewCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == videoPreviewCode) {
                val intent = Intent(this, UploadingListActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}