package com.cqcsy.lgsp.upper.pictures

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.bean.ImageBean
import com.cqcsy.lgsp.event.VideoActionResultEvent
import com.cqcsy.lgsp.login.LoginActivity
import com.cqcsy.lgsp.main.PictureViewerActivity
import com.cqcsy.lgsp.upper.UpperActivity
import com.cqcsy.lgsp.vip.util.VipGradeImageUtil
import com.cqcsy.library.base.refresh.RefreshDataFragment
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.reflect.TypeToken
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_album_header.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

/**
 * 相册图片列表
 */
class PicturesListFragment : RefreshDataFragment<ImageBean>() {
    var picturesBean: PicturesBean? = null
    var header: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        picturesBean = arguments?.getSerializable(UpperPicturesFragment.PICTURES_ITEM) as PicturesBean
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyLargeTip.text = StringUtils.getString(R.string.noPhotoData)
        addScrollHeaderLayout(addHeader())
    }

    private fun addHeader(): View {
        header = View.inflate(requireContext(), R.layout.layout_album_header, null)
        if (picturesBean?.description.isNullOrEmpty()) {
            header!!.albumInfo.visibility = View.GONE
        } else {
            header!!.albumInfo.visibility = View.VISIBLE
            header!!.albumInfo.text = picturesBean?.description ?: ""
        }
        initFocusView(picturesBean?.focus ?: false)
        ImageUtil.loadCircleImage(this, picturesBean?.headImg, header!!.userPhoto)
        if (picturesBean?.bigV == true || (picturesBean?.vipLevel ?: 0) > 0) {
            header!!.userVip.visibility = View.VISIBLE
            header!!.userVip.setImageResource(
                if (picturesBean?.bigV == true) R.mipmap.icon_big_v else VipGradeImageUtil.getVipImage(
                    picturesBean?.vipLevel ?: 0
                )
            )
        } else {
            header!!.userVip.visibility = View.GONE
        }
        header!!.nickName.text = picturesBean?.upperName
        header!!.fansCount.text =
            StringUtils.getString(R.string.fansCounts, picturesBean?.fansCount ?: 0)
        if (GlobalValue.userInfoBean?.id == picturesBean?.uid) {
            header!!.followText.visibility = View.GONE
        } else {
            header!!.followText.visibility = View.VISIBLE
        }
        header!!.followText.setOnClickListener {
            if (GlobalValue.isLogin()) {
                followClick()
            } else {
                startLogin()
            }
        }
        header!!.userPhoto.setOnClickListener {
            val intent = Intent(context, UpperActivity::class.java)
            intent.putExtra(UpperActivity.UPPER_ID, picturesBean?.uid)
            startActivity(intent)
        }
        addTags(picturesBean?.label)
        return header!!
    }

    private fun addTags(label: String?) {
        if (!label.isNullOrEmpty()) {
            val tags = label.split(",")
            if (tags.isNotEmpty()) {
                header!!.tagContent.visibility = View.VISIBLE
                val color = ColorUtils.getColor(R.color.grey)
                val padding = SizeUtils.dp2px(5f)
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.rightMargin = SizeUtils.dp2px(10f)
                params.topMargin = SizeUtils.dp2px(10f)
                tags.forEach {
                    val tagText = TextView(context)
                    tagText.setBackgroundResource(R.drawable.tag_bg)
                    tagText.text = it
                    tagText.setTextColor(color)
                    tagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    tagText.setPadding(padding, 4, padding, 4)
                    header!!.tagContent.addView(tagText, params)
                }
            }
        }
    }

    /**
     * 关注、取消关注接口
     */
    private fun followClick() {
        val params = HttpParams()
        params.put("userId", picturesBean?.uid ?: 0)
        HttpRequest.post(RequestUrls.VIDEO_FOLLOW, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val selected = response.optBoolean("selected")

                val event = VideoActionResultEvent()
                var fansCount = picturesBean?.fansCount ?: 0
                if (selected) {
                    event.action = VideoActionResultEvent.ACTION_ADD
                    picturesBean?.fansCount = ++fansCount
                } else {
                    event.action = VideoActionResultEvent.ACTION_REMOVE
                    if (fansCount > 0) {
                        picturesBean?.fansCount = --fansCount
                    }
                }
                header!!.fansCount.text = StringUtils.getString(R.string.fansCounts, fansCount)
                picturesBean?.uid.also { event.id = it.toString() }
                event.type = 1
                event.userLogo = picturesBean?.headImg ?: ""
                event.userName = picturesBean?.upperName ?: ""
                EventBus.getDefault().post(event)
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 初始化关注布局
     */
    private fun initFocusView(isSelected: Boolean) {
        header?.followText?.isSelected = isSelected
        if (isSelected) {
            header?.followText?.text = resources.getString(R.string.followed)
        } else {
            header?.followText?.text = resources.getString(R.string.attention)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAttentionRefreshEvent(event: VideoActionResultEvent) {
        if (event.type != 1) {
            return
        }
        when (event.action) {
            VideoActionResultEvent.ACTION_ADD -> {
                initFocusView(true)
            }
            VideoActionResultEvent.ACTION_REMOVE -> {
                initFocusView(false)
            }
        }
    }

    override fun getLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 3)
    }

    override fun addDecoration(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
            XGridBuilder(context).setVLineSpacing(2.5f).setHLineSpacing(5f).setIncludeEdge(true).build()
        )
    }

    override fun getParamsSize(): Int {
        return 30
    }

    override fun getParams(): HttpParams {
        val params = HttpParams()
        params.put("mediaKey", picturesBean?.mediaKey)
        params.put("sort", picturesBean?.sort ?: 1)
        return params
    }

    override fun getUrl(): String {
        return RequestUrls.ALBUM_DETAILS
    }

    override fun getItemLayout(): Int {
        return R.layout.item_album_details
    }

    override fun parsingData(jsonArray: JSONArray): MutableList<ImageBean> {
        return GsonUtils.fromJson(
            jsonArray.toString(),
            object : TypeToken<MutableList<ImageBean>>() {}.type
        )
    }

    override fun setItemView(holder: BaseViewHolder, item: ImageBean, position: Int) {
        ImageUtil.loadImage(
            this,
            item.imgPath,
            holder.getView(R.id.image),
            scaleType = ImageView.ScaleType.CENTER,
            defaultImage = R.mipmap.pictures_cover_default
        )
    }

    override fun isEnableClickLoading(): Boolean {
        return false
    }

    override fun onItemClick(position: Int, dataBean: ImageBean, holder: BaseViewHolder) {
        val intent = Intent(context, ViewAllActivity::class.java)
        intent.putExtra(UpperPicturesFragment.PICTURES_ITEM, picturesBean)
        intent.putExtra(ViewAllActivity.SHOW_DATA, getDataList() as Serializable)
        intent.putExtra(PictureViewerActivity.SHOW_INDEX, position - 1)
        intent.putExtra(PictureViewerActivity.SHOW_COUNTS, picturesBean?.photoCount)
        intent.putExtra(PictureViewerActivity.SHOW_TITLE, picturesBean?.title)
        intent.putExtra(PictureViewerActivity.SHOW_BOTTOM, true)
        startActivity(intent)
    }

    /**
     * 跳转登录页
     */
    private fun startLogin() {
        startActivity(Intent(context, LoginActivity::class.java))
    }

}