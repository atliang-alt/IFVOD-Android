package com.cqcsy.lgsp.views.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.utils.QRCodeUtil
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_invitation_dialog.*
import java.io.File

/**
 * 立即邀请dialog
 */
class InviteCodeDialog(context: Context, private val code: String, private val urlStr: String) :
    Dialog(context, R.style.dialog_style) {
    private val imageFilePath = GlobalValue.IMAGE_SCREEN_SHORT + "/" + "invite_image.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_invitation_dialog)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        inviteCode.text = code
        val activityAreas = SPUtils.getInstance().getString(Constant.ACTIVITY_AREA)
        if (!activityAreas.isNullOrEmpty()) {
            val areasList = GsonUtils.fromJson<MutableList<String>>(activityAreas, object : TypeToken<MutableList<String>>() {}.type)
            areaText.text = StringUtils.getString(R.string.inviteArea, areasList.joinToString(separator = "，") { it })
        }
        closeImg.setOnClickListener {
            dismiss()
        }
        val logoBitMap = ImageUtils.getBitmap(R.mipmap.ic_launcher)
        val codeBitmap = QRCodeUtil.createQRCodeBitmap(
            urlStr,
            SizeUtils.dp2px(110f),
            SizeUtils.dp2px(110f),
            Color.BLACK,
            Color.WHITE,
            logoBitMap,
            0.2f
        )
        downloadImage.setImageBitmap(codeBitmap)
        saveImage.setOnClickListener {
            val file = File(imageFilePath)
            ImageUtils.save(ImageUtils.view2Bitmap(frameLayout), file, Bitmap.CompressFormat.PNG)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = Uri.fromFile(file)
            context.sendBroadcast(intent)
            ToastUtils.showLong(StringUtils.getString(R.string.imageSaveSuccess))
            dismiss()
        }
    }
}