package com.cqcsy.lgsp.upload

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.refresh.RefreshFragment
import com.cqcsy.lgsp.bean.ShortVideoBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.*
import com.cqcsy.lgsp.video.VideoBaseActivity
import com.cqcsy.lgsp.video.VideoPlayVerticalActivity
import com.cqcsy.library.views.BottomBaseDialog
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import kotlinx.android.synthetic.main.layout_uploaded_more_popup.view.*
import org.json.JSONObject

/**
 * 已上传的小视频列表Fragment
 */
class UploadedShortVideoFragment : RefreshFragment() {
    private var dataList: MutableList<ShortVideoBean> = ArrayList()
    private var channelValue = ""
    private var statusValue = "-1"
    private val shortVideoCode = 1001

    override fun getRefreshChild(): Int {
        return R.layout.layout_recyclerview
    }

    override fun initData() {
        super.initData()
        getUploadVideo()
    }

    override fun initView() {
        super.initView()
        emptyLargeTip.text = getString(R.string.uploadListTips)
        emptyLittleTip.text = getString(R.string.uploadListLitTips)
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val adapter = object : BaseQuickAdapter<ShortVideoBean, BaseViewHolder>(
            R.layout.item_uploaded_short_video,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: ShortVideoBean) {
                item.coverImgUrl?.let {
                    ImageUtil.loadImage(
                        context,
                        it,
                        holder.getView(R.id.shortVideoImage),
                        scaleType = ImageView.ScaleType.CENTER
                    )
                }
                holder.setText(R.id.shortVideoTitle, item.title)
                holder.setText(R.id.time, item.time)
                holder.setText(
                    R.id.releaseDate,
                    TimeUtils.date2String(TimesUtils.formatDate(item.date), "yyyy-MM-dd")
                )
                holder.setText(R.id.playCount, NormalUtil.formatPlayCount(item.playCount))
                holder.setText(R.id.likeCount, item.likeCount.toString())
                holder.getView<ImageView>(R.id.moreImage).setOnClickListener {
                    showMoreDialog(getItemPosition(item))
                }
                if (item.isHot) {
                    holder.setVisible(R.id.isHot, true)
                } else {
                    holder.setVisible(R.id.isHot, false)
                }
                when (item.statusId) {
                    Constant.RELEASING -> {
                        holder.setText(R.id.status, R.string.releasing)
                        holder.setTextColor(R.id.status, ColorUtils.getColor(R.color.word_color_5))
                    }
                    Constant.UNDER_REVIEW -> {
                        holder.setText(R.id.status, R.string.underReview)
                        holder.setTextColor(R.id.status, ColorUtils.getColor(R.color.blue))
                    }
                    Constant.NO_ADOPT -> {
                        holder.setText(R.id.status, R.string.noAdopt)
                        holder.setTextColor(R.id.status, ColorUtils.getColor(R.color.red))
                    }
                    else -> {
                        holder.setText(R.id.status, R.string.underReview)
                        holder.setTextColor(R.id.status, ColorUtils.getColor(R.color.blue))
                    }
                }
            }
        }
        adapter.setOnItemClickListener { _, _, position ->
            if (dataList[position].statusId == Constant.RELEASING) {
                val intent = Intent(context, VideoPlayVerticalActivity::class.java)
                intent.putExtra(VideoBaseActivity.PLAY_VIDEO_BEAN, dataList[position])
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter
    }

    override fun onRefresh() {
        super.onRefresh()
        page = 1
        dataList.clear()
        getUploadVideo()
    }

    override fun onLoadMore() {
        super.onLoadMore()
        getUploadVideo()
    }

    fun setChannel(channel: String) {
        if (channelValue != channel) {
            channelValue = channel
            page = 1
            dataList.clear()
            getUploadVideo()
        }
    }

    fun setStatus(status: String) {
        if (statusValue != status) {
            statusValue = status
            page = 1
            dataList.clear()
            getUploadVideo()
        }
    }

    /**
     * 获取上传的小视频
     */
    private fun getUploadVideo() {
        if (dataList.isEmpty()) {
            showProgress()
        }
        val params = HttpParams()
        params.put("category", channelValue)
        params.put("status", statusValue)
        params.put("Size", size)
        params.put("Page", page)
        HttpRequest.post(
            RequestUrls.GET_UPLOAD_VIDEO + "?videoType=3",
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    dismissProgress()
                    if (page == 1) {
                        finishRefresh()
                    }
                    val jsonArray = response?.optJSONArray("list")
                    if (jsonArray == null || jsonArray.length() == 0) {
                        if (dataList.isEmpty()) {
                            showEmpty()
                        } else {
                            finishLoadMoreWithNoMoreData()
                        }
                        return
                    }
                    val list = Gson().fromJson<List<ShortVideoBean>>(
                        jsonArray.toString(), object : TypeToken<List<ShortVideoBean>>() {}.type
                    )
                    dataList.addAll(list)
                    recyclerView.adapter?.notifyDataSetChanged()
                    if (list.isNullOrEmpty()) {
                        finishLoadMoreWithNoMoreData()
                    } else {
                        page += 1
                        finishLoadMore()
                    }
                }

                override fun onError(response: String?, errorMsg: String?) {
                    if (page == 1) {
                        finishRefresh()
                        showFailed {
                            dataList.clear()
                            getUploadVideo()
                        }
                    } else {
                        errorLoadMore()
                    }
                }

            },
            params,
            this
        )
    }

    private fun showMoreDialog(position: Int) {
        val dialog = object : BottomBaseDialog(requireContext()) {}
        val contentView = View.inflate(context, R.layout.layout_uploaded_more_popup, null)
        if (GlobalValue.userInfoBean!!.token.gid > 100) {
            contentView.hotLayout.visibility = View.VISIBLE
        } else {
            contentView.hotLayout.visibility = View.GONE
        }
        val shortVideoBean = dataList[position]
        if (shortVideoBean.statusId == Constant.RELEASING) {
            contentView.reasonLayout.visibility = View.GONE
        }
        when (shortVideoBean.statusId) {
            Constant.RELEASING -> {
                contentView.reasonLayout.visibility = View.GONE
                contentView.editLayout.visibility = View.VISIBLE
                contentView.deleteLayout.visibility = View.VISIBLE
            }
            Constant.UNDER_REVIEW -> {
                contentView.reasonLayout.visibility = View.GONE
                contentView.editLayout.visibility = View.VISIBLE
                contentView.deleteLayout.visibility = View.VISIBLE
            }
            Constant.NO_ADOPT -> {
                contentView.reasonLayout.visibility = View.VISIBLE
                contentView.editLayout.visibility = View.GONE
                contentView.deleteLayout.visibility = View.VISIBLE
            }
            else -> {
                contentView.reasonLayout.visibility = View.GONE
                contentView.editLayout.visibility = View.VISIBLE
                contentView.deleteLayout.visibility = View.VISIBLE
            }
        }
        contentView.hotLayout.setOnClickListener {
            // 设置为热播
            setHotVideo(position)
            dialog.dismiss()
        }
        contentView.reasonLayout.setOnClickListener {
            // 查看原因
            reasonDialog(shortVideoBean)
            dialog.dismiss()
        }
        contentView.editLayout.setOnClickListener {
            // 编辑
            val intent = Intent(context, ShortVideoInfoActivity::class.java)
            intent.putExtra(ShortVideoInfoActivity.FORM_TYPE, 1)
            intent.putExtra(ShortVideoInfoActivity.SHORT_BEAN, shortVideoBean)
            startActivityForResult(intent, shortVideoCode)
            dialog.dismiss()
        }
        contentView.deleteLayout.setOnClickListener {
            // 删除
            deleteDialog(shortVideoBean)
            dialog.dismiss()
        }
        contentView.cancel.setOnClickListener {
            // 取消
            dialog.dismiss()
        }
        dialog.setContentView(contentView)
        dialog.show()
    }

    /**
     * 设置/取消热播视频
     */
    private fun setHotVideo(position: Int) {
        val currentHot = !dataList[position].isHot
        val params = HttpParams()
        params.put("mediaKey", dataList[position].mediaKey)
        params.put("isHot", currentHot)
        HttpRequest.post(RequestUrls.SET_VIDEO_HOT, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                dataList[position].isHot = currentHot
                recyclerView.adapter?.notifyItemChanged(position)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, tag = this)
    }

    /**
     * 删除dialog
     */
    private fun deleteDialog(shortVideoBean: ShortVideoBean) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.deleteVideo)
        tipsDialog.setMsg(R.string.deleteTips)
        tipsDialog.setLeftListener(R.string.think_again) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.delete) {
            deleteHttp(shortVideoBean)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    /**
     * 删除对应的上传信息
     */
    private fun deleteHttp(shortVideoBean: ShortVideoBean) {
        val params = HttpParams()
        params.put("mediaKey", shortVideoBean.mediaKey)
        HttpRequest.post(RequestUrls.DELETE_UPLOAD_INFO, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dataList.remove(shortVideoBean)
                recyclerView.adapter?.notifyDataSetChanged()
                if (dataList.isEmpty()) {
                    showEmpty()
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }

        }, params, this)
    }

    /**
     * 查看不通过原因dialog
     */
    private fun reasonDialog(shortVideoBean: ShortVideoBean) {
        val tipsDialog = TipsDialog(requireContext())
        tipsDialog.setDialogTitle(R.string.noAdoptReason)
        tipsDialog.setMsg(shortVideoBean.returnMsg)
        tipsDialog.setLeftListener(R.string.close) {
            tipsDialog.dismiss()
        }
        tipsDialog.setRightListener(R.string.resetEdit) {
            val intent = Intent(context, ShortVideoInfoActivity::class.java)
            intent.putExtra(ShortVideoInfoActivity.FORM_TYPE, 1)
            intent.putExtra(ShortVideoInfoActivity.SHORT_BEAN, shortVideoBean)
            startActivityForResult(intent, shortVideoCode)
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == shortVideoCode) {
                page = 1
                dataList.clear()
                getUploadVideo()
            }
        }
    }
}