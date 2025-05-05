package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import kotlinx.android.synthetic.main.activity_set_user_sex.*

/**
 * 设置性别
 */
class SetUserSexActivity : NormalActivity() {
    // 未知 -1，男 1，女 0
    var sex = -1
    override fun getContainerView(): Int {
        return R.layout.activity_set_user_sex
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.sex)
        setRightText(R.string.preservation)
        initData()
        initView()
    }

    private fun initData() {
        sex = intent.getIntExtra("sex", -1)
        when (sex) {
            0 -> {
                manImage.isSelected = false
                womanImage.isSelected = true
            }
            1 -> {
                manImage.isSelected = true
                womanImage.isSelected = false
            }
            else -> {
                manImage.isSelected = false
                womanImage.isSelected = false
            }
        }
    }

    private fun initView() {
        manImage.setOnClickListener {
            sex = 1
            manImage.isSelected = true
            womanImage.isSelected = false
        }
        womanImage.setOnClickListener {
            sex = 0
            manImage.isSelected = false
            womanImage.isSelected = true
        }
    }

    override fun onRightClick(view: View) {
        val intent = Intent()
        intent.putExtra("sex", sex)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}