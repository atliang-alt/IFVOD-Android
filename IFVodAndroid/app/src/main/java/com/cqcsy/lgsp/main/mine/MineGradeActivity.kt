package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.GradeItemBean
import com.cqcsy.library.utils.GlobalValue
import com.littlejerk.rvdivider.builder.XGridBuilder
import kotlinx.android.synthetic.main.activity_mine_grade.*

/**
 * 我的等级
 */
class MineGradeActivity : NormalActivity() {
    private val itemIconArray = arrayOf(
        R.mipmap.icon_exp_480,
        R.mipmap.icon_exp_comment,
        R.mipmap.icon_exp_upload,
        R.drawable.grade_dan_selector,
        R.drawable.grade_emoji_selector,
        R.drawable.grade_vote_selector,
        R.drawable.grade_play_record_selector,
        R.drawable.grade_speed_selector,
        R.drawable.grade_video_selector,
        R.drawable.grade_720_selector
    )

    private val levelIconArray = arrayOf(
        R.mipmap.icon_level_lv1,
        R.mipmap.icon_level_lv2,
        R.mipmap.icon_level_lv3,
        R.mipmap.icon_level_lv4,
        R.mipmap.icon_level_lv5,
        R.mipmap.icon_level_lv6,
        R.mipmap.icon_level_lv7,
        R.mipmap.icon_level_lv8
    )
    private val itemList = ArrayList<GradeItemBean>()

    /**
     * 当前等级
     */
    private var currentLevel = 1

    /**
     * 距离下一级经验值
     */
    private var nextLevel = 0

    /**
     * 经验值
     */
    private var experience = 0

    override fun getContainerView(): Int {
        return R.layout.activity_mine_grade
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.mineGrade)
        initData()
        initView()
    }

    private fun initData() {
        currentLevel = GlobalValue.userInfoBean!!.userExtension?.currentLevel ?: 1
        nextLevel = (GlobalValue.userInfoBean!!.userExtension?.nextLevel ?: 0) -
                (GlobalValue.userInfoBean!!.userExtension?.experience ?: 0)
        experience = GlobalValue.userInfoBean!!.userExtension?.experience ?: 0
        val arrayName = resources.getStringArray(R.array.grade_names)
        for (i in itemIconArray.indices) {
            val bean = GradeItemBean()
            bean.itemName = arrayName[i]
            bean.itemImage = itemIconArray[i]
            when (i) {
                3 -> bean.itemGrade = 2
                4 -> bean.itemGrade = 3
                5 -> bean.itemGrade = 4
                6 -> bean.itemGrade = 5
                7 -> bean.itemGrade = 6
                8 -> bean.itemGrade = 7
                9 -> bean.itemGrade = 8
                else -> bean.itemGrade = 1
            }
            itemList.add(bean)
        }
    }

    private fun initView() {
        if (currentLevel <= 0) {
            currentLevel = 1
        }
        levelImage.setImageResource(levelIconArray[currentLevel - 1])
        val size = itemList.filter { it.itemGrade <= currentLevel }.size
        gradeExpValue.text = experience.toString()
        if (GlobalValue.isVipUser()) {
            gradePrivilege.text = StringUtils.getString(R.string.level_is_vip)
            gradeExp.visibility = View.GONE
            exp.visibility = View.GONE
        } else {
            gradeExp.text = nextLevel.toString()
            gradePrivilege.text = StringUtils.getString(R.string.gradeTips, size)
        }
        gradeRecycler.layoutManager = GridLayoutManager(this, 3)
        gradeRecycler.addItemDecoration(XGridBuilder(this).setHLineSpacing(40f).build())
        val adapter = object : BaseQuickAdapter<GradeItemBean, BaseViewHolder>(
            R.layout.item_grade_info,
            itemList
        ) {
            override fun convert(holder: BaseViewHolder, item: GradeItemBean) {
                holder.setText(R.id.itemName, item.itemName)
                holder.setImageResource(R.id.itemImage, item.itemImage)
                if (item.itemGrade <= currentLevel || GlobalValue.isVipUser()) {
                    holder.setGone(R.id.gradeLock, true)
                    holder.getView<ImageView>(R.id.itemImage).isSelected = true
                } else {
                    holder.setVisible(R.id.gradeLock, true)
                    holder.setText(
                        R.id.gradeLock,
                        StringUtils.getString(R.string.gradeLock, item.itemGrade)
                    )
                    holder.getView<ImageView>(R.id.itemImage).isSelected = false
                }
            }
        }
        gradeRecycler.adapter = adapter
    }
}