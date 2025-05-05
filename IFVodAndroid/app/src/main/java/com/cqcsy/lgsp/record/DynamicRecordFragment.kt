package com.cqcsy.lgsp.record

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.DynamicBean
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.database.manger.DynamicRecordManger
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.main.mine.DynamicDetailsActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.utils.DynamicUtils
import com.cqcsy.lgsp.utils.NormalUtil
import com.cqcsy.lgsp.utils.TimesUtils
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_login_tip.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray

/**
 * 动态记录
 */
class DynamicRecordFragment : RefreshDataFragment<DynamicBean>() {
    var listener: RecordListener? = null
    var isRecord: Boolean = true
    val selectedItem: MutableList<DynamicBean> = ArrayList()
    var isEdit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isRecord = arguments?.getBoolean("isRecord") ?: true
        if (isRecord) {
            emptyLargeTip.setText(R.string.commentEmpty)
        } else {
            emptyLargeTip.setText(R.string.no_collect)
            emptyLittleTip.setText(R.string.no_collect_tip)
        }
        super.onViewCreated(view, savedInstanceState)
        if (!GlobalValue.isLogin()) {
            getLocalData()
        }
    }

    override fun isHttpTag(): Boolean {
        return GlobalValue.isLogin()
    }

    private fun getLocalData() {
        val list = DynamicRecordManger.instance.selectAll()
        if (list.isNotEmpty()) {
            val localDynamic: MutableList<DynamicBean> = ArrayList()
            for (it in list) {
                if (it.type == 1) {
                    continue
                }
                val dynamicBean = DynamicBean()
                dynamicBean.mediaKey = it.mediaKey
                dynamicBean.headImg = it.headImg
                dynamicBean.upperName = it.upperName
                dynamicBean.createTime = it.createTime
                dynamicBean.description = it.description
                dynamicBean.trendsDetails = Gson().fromJson(
                    it.trendsDetails, object : TypeToken<List<ImageBean>>() {}.type
                )
                dynamicBean.comments = it.comments
                dynamicBean.likeCount = it.likeCount
                dynamicBean.uid = it.uid
                dynamicBean.bigV = it.bigV
                dynamicBean.vipLevel = it.vipLevel
                if (it.type == 0) {
                    dynamicBean.photoType = 1
                } else {
                    dynamicBean.photoType = 2
                }
                dynamicBean.coverPath = it.coverPath
                localDynamic.add(dynamicBean)
            }
            mAdapter?.addData(localDynamic)
        } else {
            showEmpty()
        }
        listener?.onLoadFinish(2, getDataList())
    }

    override fun onLogin() {
        if (loginTipContent != null) {
            loginTipContent.visibility = View.GONE
        }
        enableRefresh()
        onRefresh()
    }

    fun setRecordListener(listener: RecordListener) {
        this.listener = listener
    }

    fun refreshList(isEdit: Boolean) {
        if (isEdit || !GlobalValue.isLogin()) {
            disableRefresh()
        } else {
            enableRefresh()
        }
        this.isEdit = isEdit
        selectedItem.clear()
        refreshView()
    }

    fun <T> resetList(isClear: Boolean, list: MutableList<T>?) {
        if (isClear) {
            clearAll()
        } else {
            removeData(list)
        }
        selectedItem.clear()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCollectChange(event: VideoActionResultEvent) {
        if (event.type != 4 || isRecord || event.actionType != VideoActionResultEvent.TYPE_DYNAMIC) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> onRefresh()
            VideoActionResultEvent.ACTION_REMOVE -> {
                val removeList = ArrayList<DynamicBean>()
                getDataList().forEach {
                    if (event.id == it.mediaKey) {
                        removeList.add(it)
                    }
                }
                removeData(removeList)
            }
        }
    }

    override fun addPinHeaderLayout(): View? {
        if (!GlobalValue.isLogin()) {
            return View.inflate(requireContext(), R.layout.layout_login_tip, null)
        }
        return null
    }

    override fun getUrl(): String {
        return if (isRecord) {
            RequestUrls.GET_RECORD
        } else {
            RequestUrls.MINE_DYNAMIC_COLLECT
        }
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        if (isRecord) {
            params.put("isNormalVideo", "3")
        }
        return params
    }

    override fun getItemLayout(): Int {
        return R.layout.item_search_dynamic
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<DynamicBean> {
        return Gson().fromJson(
            jsonArray.toString(),
            object : TypeToken<List<DynamicBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: DynamicBean, position: Int) {
        val checkBox = holder.getView<CheckBox>(R.id.itemCheck)
        if (isEdit) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.GONE
        }
        checkBox.isChecked = selectedItem.contains(item)
        if (item.description.isNullOrEmpty()) {
            holder.setGone(R.id.item_des, true)
        } else {
            holder.setText(R.id.item_des, Html.fromHtml(item.description!!.replace("\n", "<br>")))
            holder.setGone(R.id.item_des, false)
        }
        if (!item.createTime.isNullOrEmpty()) {
            holder.setText(
                R.id.release_time, StringUtils.getString(
                    R.string.release_time, TimeUtils.date2String(
                        TimesUtils.formatDate(item.createTime ?: ""), "yyyy-MM-dd"
                    )
                )
            )
        } else {
            holder.setText(R.id.release_time, "")
        }
        if (item.address.isNullOrEmpty()) {
            holder.setGone(R.id.dynamic_location, true)
        } else {
            holder.setText(R.id.dynamic_location, item.address)
            holder.setGone(R.id.dynamic_location, false)
        }
        holder.setText(R.id.nickName, item.upperName)
        ImageUtil.loadCircleImage(requireActivity(), item.headImg, holder.getView(R.id.userPhoto))
        setVipLevel(holder.getView(R.id.userVip), item)
        holder.setText(R.id.comment_count, NormalUtil.formatPlayCount(item.comments))
        holder.setText(R.id.zan_count, NormalUtil.formatPlayCount(item.likeCount))
            .setText(R.id.view_count, NormalUtil.formatPlayCount(item.viewCount))
        val imageContainer = holder.getView<LinearLayout>(R.id.imageContainer)
        val videoContainer = holder.getView<FrameLayout>(R.id.videoContainer)
        if (item.photoType == 1) {
            imageContainer.isVisible = true
            videoContainer.isVisible = false
            DynamicUtils.addDynamicImages(
                requireActivity(),
                imageContainer,
                item.trendsDetails,
                item.photoCount,
                item.isUnAvailable
            )
        } else {
            imageContainer.isVisible = false
            videoContainer.isVisible = true
            val videoCover = holder.getView<ImageView>(R.id.iv_video_cover)
            if (item.isUnAvailable) {
                videoContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                    this.width = LinearLayout.LayoutParams.WRAP_CONTENT
                    this.height = LinearLayout.LayoutParams.WRAP_CONTENT
                }
                videoCover.setImageResource(R.mipmap.icon_un_available)
            } else {
                videoCover.setImageDrawable(null)
                val size = DynamicUtils.getCoverSize(item.imageRatioValue, DynamicUtils.getCellWidth())
                videoContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                    this.width = size.width
                    this.height = size.height
                }
                ImageUtil.loadImage(
                    this,
                    item.cover,
                    videoCover,
                    imageWidth = size.width,
                    imageHeight = size.height,
                    corner = 2
                )
            }
        }
        checkBox.setOnClickListener {
            if (selectedItem.contains(item)) {
                selectedItem.remove(item)
            } else {
                selectedItem.add(item)
            }
        }
        holder.getView<ImageView>(R.id.userPhoto).setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, item.uid)
            startActivity(intent)
        }
    }

    override fun onItemClick(position: Int, dataBean: DynamicBean, holder: BaseViewHolder) {
        val checkBox = holder.getView<CheckBox>(R.id.itemCheck)
        if (isEdit) {
            if (selectedItem.contains(dataBean)) {
                selectedItem.remove(dataBean)
            } else {
                selectedItem.add(dataBean)
            }
            checkBox.isChecked = selectedItem.contains(dataBean)
            return
        }
        if (!GlobalValue.isLogin()) {
            listener?.checkAvailable(2, dataBean)
        } else {
            if (dataBean.isUnAvailable) {
                selectedItem.add(dataBean)
                listener?.removeData(selectedItem)
            } else {
                startDynamic(dataBean)
            }
        }
    }

    fun startDynamic(data: DynamicBean) {
        /*        if (data.photoType == 1) {
                    DynamicDetailFragment.launch(requireContext(), data.id)
                } else {
                    DynamicVideoDetailActivity.launch(context, 0, mutableListOf(data))
                    //DynamicVideoDetailActivity.launch(requireContext(), data.id)
                }*/
        DynamicDetailsActivity.launch(requireContext()) {
            mediaKey = data.mediaKey ?: ""
            dynamicVideoList = mutableListOf(data)
            dynamicType = data.photoType
        }
    }

    private fun setVipLevel(imageView: ImageView, item: DynamicBean) {
        if (item.bigV || item.vipLevel > 0) {
            imageView.visibility = View.VISIBLE
            imageView.setImageResource(
                if (item.bigV) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    item.vipLevel
                )
            )
        } else {
            imageView.visibility = View.GONE
        }
    }

    override fun onLoadFinish() {
        listener?.onLoadFinish(2, getDataList())
    }

    override fun onDataEmpty() {
        listener?.onDataEmpty(2)
    }

    override fun isEnableClickLoading(): Boolean {
        return GlobalValue.isLogin()
    }
}