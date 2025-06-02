package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.SeekBar
import com.blankj.utilcode.util.SPUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import kotlinx.android.synthetic.main.layout_danama_setting.*

/**
 * 弹幕设置
 */
class DanamaSettingDialog(context: Context) : VideoMenuDialog(context), View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    var listener: IDanamaController? = null

    fun setDanamaController(l: IDanamaController) {
        listener = l
    }

    init {
        setMenuColumn(2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_danama_setting)

        setListener()

        setSelectLayout()

        init()
    }

    private fun setListener() {
        speedProgress.setOnSeekBarChangeListener(this)
        fontProgress.setOnSeekBarChangeListener(this)
        backgroundProgress.setOnSeekBarChangeListener(this)
        rollCheck.setOnClickListener(this)
        topCheck.setOnClickListener(this)
        bottomCheck.setOnClickListener(this)
        keywordForbidden.setOnClickListener(this)
        userForbidden.setOnClickListener(this)
        danamaList.setOnClickListener(this)
    }

    private fun setSelectLayout() {
        val textParams = forbidden_text.layoutParams as RelativeLayout.LayoutParams
        val userParams = forbidden_user.layoutParams as RelativeLayout.LayoutParams
        val listParams = danmaku_list.layoutParams as RelativeLayout.LayoutParams
        if (isVertical) {
            bottom_container.orientation = LinearLayout.HORIZONTAL
            textParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            userParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
            listParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT
        } else {
            bottom_container.orientation = LinearLayout.VERTICAL
            textParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
            userParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
            listParams.width = RelativeLayout.LayoutParams.MATCH_PARENT
        }
        forbidden_text.layoutParams = textParams
        forbidden_user.layoutParams = userParams
        danmaku_list.layoutParams = listParams
    }

    fun init() {
        val speed = SPUtils.getInstance().getInt(Constant.KEY_WATCH_DANAMA_SPEED + GlobalValue.userInfoBean?.id, 2)
        val font = SPUtils.getInstance().getInt(Constant.KEY_WATCH_DANAMA_FONT + GlobalValue.userInfoBean?.id, 2)
        val alpha = SPUtils.getInstance().getFloat(Constant.KEY_WATCH_DANAMA_ALPHA + GlobalValue.userInfoBean?.id, 1f)
        val forbidden = SPUtils.getInstance().getString(Constant.KEY_WATCH_DANAMA_FORBIDDEN + GlobalValue.userInfoBean?.id, "")
        when (speed) {
            1 -> {
                speedProgress.progress = 0
                speedName.setText(R.string.slow)
            }

            2 -> {
                speedProgress.progress = 50
                speedName.setText(R.string.normal)
            }

            3 -> {
                speedProgress.progress = 100
                speedName.setText(R.string.fast)
            }
        }
        when (font) {
            1 -> {
                fontProgress.progress = 0
                fontName.setText(R.string.small)
            }

            2 -> {
                fontProgress.progress = 50
                fontName.setText(R.string.normal)
            }

            3 -> {
                fontProgress.progress = 100
                fontName.setText(R.string.large)
            }
        }
        backgroundProgress.progress = (alpha * 100).toInt()
        if (forbidden.isNotEmpty()) {
            val array = forbidden.split(",")
            if (array.isNotEmpty()) {
                for (value in array) {
                    when (value) {
                        "1" -> rollCheck.isChecked = true
                        "5" -> topCheck.isChecked = true
                        "4" -> bottomCheck.isChecked = true
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rollCheck -> listener?.onDanamaForbidden(0, rollCheck.isChecked)
            R.id.topCheck -> listener?.onDanamaForbidden(1, topCheck.isChecked)
            R.id.bottomCheck -> listener?.onDanamaForbidden(2, bottomCheck.isChecked)
            R.id.keywordForbidden -> {
                dismiss()
                listener?.onForbiddenWordClick()
            }

            R.id.userForbidden -> {
                dismiss()
                listener?.onForbiddenUserClick()
            }

            R.id.danamaList -> {
                dismiss()
                listener?.onDanamaListClick()
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (seekBar == backgroundProgress) {
            backgroundName.text = "$progress%";
            SPUtils.getInstance().put(
                Constant.KEY_WATCH_DANAMA_ALPHA + GlobalValue.userInfoBean?.id,
                progress / 100f
            )
            listener?.onFontBackgroundChange(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (seekBar != backgroundProgress) {
            val progress = seekBar?.progress
            when (progress) {
                in 0..25 -> {
                    seekBar?.progress = 0
                }

                in 26..75 -> {
                    seekBar?.progress = 50
                }

                else -> {
                    seekBar?.progress = 100
                }
            }
            if (seekBar == speedProgress) {
                when (seekBar?.progress) {
                    0 -> {
                        speedName.setText(R.string.slow)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_SPEED + GlobalValue.userInfoBean?.id,
                            1
                        )
                        listener?.onSpeedChange(0f)
                    }

                    50 -> {
                        speedName.setText(R.string.normal)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_SPEED + GlobalValue.userInfoBean?.id,
                            2
                        )
                        listener?.onSpeedChange(1f)
                    }

                    100 -> {
                        speedName.setText(R.string.fast)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_SPEED + GlobalValue.userInfoBean?.id,
                            3
                        )
                        listener?.onSpeedChange(2f)
                    }
                }
            } else {
                when (seekBar?.progress) {
                    0 -> {
                        fontName.setText(R.string.small)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_FONT + GlobalValue.userInfoBean?.id,
                            1
                        )
                        listener?.onFontSizeChange(0)
                    }

                    50 -> {
                        fontName.setText(R.string.normal)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_FONT + GlobalValue.userInfoBean?.id,
                            2
                        )
                        listener?.onFontSizeChange(1)
                    }

                    100 -> {
                        fontName.setText(R.string.large)
                        SPUtils.getInstance().put(
                            Constant.KEY_WATCH_DANAMA_FONT + GlobalValue.userInfoBean?.id,
                            3
                        )
                        listener?.onFontSizeChange(2)
                    }
                }
            }
        }
    }
}