package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.lgsp.medialoader.MediaType
import com.cqcsy.lgsp.upload.SelectLocalImageActivity
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.bean.UserInfoBean
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.Constant
import com.cqcsy.library.utils.GlobalValue
import com.cqcsy.library.utils.ImageUtil
import com.google.gson.Gson
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_edit_userinfo.*
import org.json.JSONObject
import java.io.File

/**
 * 编辑个人信息页
 */
class EditUserInfoActivity : NormalActivity() {
    private val selectLocalImageCode = 1001
    private val setNickNameCode = 1002
    private val setIntroduceCode = 1003
    private val setSexCode = 1004

    private val userImageKey = "Img"
    private val nickNameKey = "NickName"
    private val introduceKey = "Introduce"
    private val sexKey = "Sex"

    override fun getContainerView(): Int {
        return R.layout.activity_edit_userinfo
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.mineInfoEdit)
        initView()
    }

    private fun initView() {
        if (!GlobalValue.userInfoBean?.nickName.isNullOrEmpty()) {
            nickName.text = GlobalValue.userInfoBean?.nickName
        }
        if (!GlobalValue.userInfoBean?.introduce.isNullOrEmpty()) {
            userSign.text = GlobalValue.userInfoBean?.introduce
        }
        userSex.text = when (GlobalValue.userInfoBean?.sex) {
            1 -> {
                getString(R.string.sexMan)
            }
            0 -> {
                getString(R.string.sexWoman)
            }
            else -> {
                getString(R.string.unknown)
            }
        }
        GlobalValue.userInfoBean?.avatar?.let { ImageUtil.loadCircleImage(this, it, userLogo) }
        changePhoto.setOnClickListener {
            startSelectPhoto()
        }
        nickLayout.setOnClickListener {
            val intent = Intent(this, SetUserInfoActivity::class.java)
            intent.putExtra("formType", 0)
            startActivityForResult(intent, setNickNameCode)
        }
        signLayout.setOnClickListener {
            val intent = Intent(this, SetUserInfoActivity::class.java)
            intent.putExtra("formType", 1)
            startActivityForResult(intent, setIntroduceCode)
        }
        sexLayout.setOnClickListener {
            val intent = Intent(this, SetUserSexActivity::class.java)
            intent.putExtra("sex", GlobalValue.userInfoBean?.sex)
            startActivityForResult(intent, setSexCode)
        }
    }

    private fun startSelectPhoto() {
        val intent = Intent(this, SelectLocalImageActivity::class.java)
        intent.putExtra(SelectLocalImageActivity.widthKey, 1f)
        intent.putExtra(SelectLocalImageActivity.heightKey, 1f)
        intent.putExtra(SelectLocalImageActivity.isCutPhotoKey, true)
        intent.putExtra(SelectLocalImageActivity.mediaTypeKey, MediaType.PHOTO)
        startActivityForResult(intent, selectLocalImageCode)
    }

    private fun uploadUserPhoto(path: String) {
        val params = HttpParams()
        params.put("file", File(path))
        HttpRequest.post(
            RequestUrls.UPLOAD_FILE,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    val uploadUrl = response?.optString("filepath")
                    updateUserInfo(userImageKey, uploadUrl)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    private fun updateUserInfo(paramKey: String, value: String?) {
        if (value == null) {
            ToastUtils.showLong(R.string.uploadFailed)
            return
        }
        val params = HttpParams()
        params.put("NickName", GlobalValue.userInfoBean?.nickName)
        params.put("Introduce", GlobalValue.userInfoBean?.introduce)
        params.put("Sex", GlobalValue.userInfoBean?.sex?:-1)
        params.put("Img", GlobalValue.userInfoBean?.avatar)
        params.put(paramKey, value)
        HttpRequest.post(
            RequestUrls.UPDATE_USER_INFO,
            object : HttpCallBack<JSONObject>() {
                override fun onSuccess(response: JSONObject?) {
                    when (paramKey) {
                        userImageKey -> ImageUtil.loadCircleImage(
                            this@EditUserInfoActivity, response?.optString("avatar"), userLogo
                        )
                        nickNameKey -> nickName.text = value
                        introduceKey -> {
                            userSign.text = value
                        }
                        sexKey -> {
                            userSex.text = when (value) {
                                "1" -> {
                                    getString(R.string.sexMan)
                                }
                                "0" -> {
                                    getString(R.string.sexWoman)
                                }
                                else -> {
                                    getString(R.string.unknown)
                                }
                            }
                        }
                    }
                    val bean = Gson().fromJson(response.toString(), UserInfoBean::class.java)
                    dismissProgressDialog()
                    saveLocalData(bean)
                }

                override fun onError(response: String?, errorMsg: String?) {
                    dismissProgressDialog()
                    ToastUtils.showLong(errorMsg)
                }
            }, params, this
        )
    }

    private fun saveLocalData(bean: UserInfoBean) {
        if (!bean.avatar.isNullOrEmpty()) {
            GlobalValue.userInfoBean?.avatar = bean.avatar
        }
        if (!bean.nickName.isNullOrEmpty()) {
            GlobalValue.userInfoBean?.nickName = bean.nickName
        }
        if (!bean.introduce.isNullOrEmpty()) {
            GlobalValue.userInfoBean?.introduce = bean.introduce
        }
        GlobalValue.userInfoBean?.sex = bean.sex
        val jsonString = Gson().toJson(GlobalValue.userInfoBean)
        SPUtils.getInstance().put(
            Constant.KEY_USER_INFO,
            EncodeUtils.base64Encode2String(jsonString.toByteArray())
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == selectLocalImageCode) {
                val clipPath = data?.getStringExtra("clipPath") ?: ""
                uploadUserPhoto(clipPath)
                showProgressDialog()
            }
            if (requestCode == setNickNameCode) {
                val nickname = data?.getStringExtra("nickname") ?: ""
                updateUserInfo(nickNameKey, nickname)
                showProgressDialog()
            }
            if (requestCode == setIntroduceCode) {
                val introduce = data?.getStringExtra("introduce") ?: ""
                updateUserInfo(introduceKey, introduce)
                showProgressDialog()
            }
            if (requestCode == setSexCode) {
                val sex = data?.getIntExtra("sex", -1)
                updateUserInfo(sexKey, sex.toString())
                showProgressDialog()
            }
        }
    }
}