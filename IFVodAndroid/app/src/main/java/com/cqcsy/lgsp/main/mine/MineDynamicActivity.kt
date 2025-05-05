package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.LocalMediaBean
import com.cqcsy.lgsp.database.manger.DynamicCacheManger
import com.cqcsy.lgsp.medialoader.ChooseMode
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.views.TipsDialog
import java.io.Serializable

/**
 * 我的动态
 */
class MineDynamicActivity : NormalActivity() {
    var fragment: MineDynamicFragment? = null
    private val selectCode = 1000

    override fun getContainerView(): Int {
        return R.layout.activity_mine_dynamic
    }

    override fun onViewCreate() {
        super.onViewCreate()
        setHeaderTitle(R.string.mineDynamic)
        setRightImage(R.mipmap.icon_add_dynamic)
        fragment = MineDynamicFragment()
        val bundle = Bundle()
        bundle.putInt("userId", GlobalValue.userInfoBean?.id ?: 0)
        fragment?.arguments = bundle
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment!!)
        transaction.commitAllowingStateLoss()
    }

    private fun isReleasing(): Boolean {
        val selectData = DynamicCacheManger.instance.selectByStatus(DynamicReleaseStatus.RELEASING)
        return selectData.isNotEmpty()
    }

    private fun releasingDialog(context: Context) {
        val dialog = TipsDialog(context)
        dialog.setDialogTitle(R.string.tips)
        dialog.setMsg(R.string.releasing_dynamic_tip)
        dialog.setRightListener(R.string.sure) {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onRightClick(view: View) {
        if (isReleasing()) {
            releasingDialog(this)
            return
        }
        val intent = Intent(this, SelectLocalImageActivity::class.java)
        intent.putExtra(SelectLocalImageActivity.maxCountKey, 18)
        intent.putExtra(SelectLocalImageActivity.isBackGifKey, true)
        intent.putExtra(SelectLocalImageActivity.chooseModeKey, ChooseMode.ONLY)
        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.ALL)
        startActivityForResult(intent, selectCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == selectCode) {
                val list = data?.getSerializableExtra(SelectLocalImageActivity.imagePathList) as? MutableList<LocalMediaBean>
                if (!list.isNullOrEmpty()) {
                    if (list.size == 1 && list[0].isVideo) {
                        ReleaseDynamicVideoActivity.launch(this, list[0])
                    } else {
                        val intent = Intent(this, ReleaseDynamicActivity::class.java)
                        intent.putExtra("selectImg", list as Serializable)
                        startActivity(intent)
                    }
                }
            }
        }
    }
}