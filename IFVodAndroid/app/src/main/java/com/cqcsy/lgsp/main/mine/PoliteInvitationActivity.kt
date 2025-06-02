package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.bean.InviteResultBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.views.dialog.InviteCodeDialog
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.library.views.TipsDialog
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_invite_polite.*
import org.json.JSONObject

/**
 * 邀请有礼
 */
class PoliteInvitationActivity : NormalActivity() {
    private var adapter: BaseQuickAdapter<InviteResultBean, BaseViewHolder>? = null
    private var resultList: MutableList<InviteResultBean> = ArrayList()
    private val writeCode = 1001
    private val hideString = "******"

    override fun getContainerView(): Int {
        return R.layout.activity_invite_polite
    }

    override fun onViewCreate() {
        setHeaderTitle(R.string.invite_title)
        setRightText(R.string.write_invite)
        val spannableString = SpannableString(getString(R.string.noInviteResult))
        spannableString.setSpan(RelativeSizeSpan(2.0f), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        resultEmpty.text = spannableString
        initView()
    }

    override fun onResume() {
        super.onResume()
        getInviteCode()
    }

    private fun getInviteCode() {
        if (GlobalValue.userInfoBean?.phone.isNullOrEmpty()) {
            inviteCode.text = hideString
            copyText.visibility = View.GONE
            showPhoneBind()
            return
        } else {
            copyText.visibility = View.VISIBLE
        }
        if (!inviteCode.text.isNullOrEmpty() && hideString != inviteCode.text) {
            return
        }
        HttpRequest.post(RequestUrls.INVITE_CODE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                inviteCode.text = response?.optString("inviteCode") ?: ""
                inviteCode.tag = response?.optBoolean("isInvited", false) ?: false
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg ?: StringUtils.getString(R.string.get_invite_code_error))
            }
        }, tag = this)
    }

    private fun initView() {
        adapter = object :
            BaseQuickAdapter<InviteResultBean, BaseViewHolder>(
                R.layout.item_invite_result,
                resultList
            ) {
            override fun convert(holder: BaseViewHolder, item: InviteResultBean) {
                holder.setText(R.id.nickName, item.nickName)
                holder.setText(R.id.phoneNumb, formatAccount(item.accountName))
                holder.setText(R.id.reward, item.context)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
        getResult()
    }

    /**
     * 格式化账号
     */
    private fun formatAccount(account: String): String {
        var newAccount = ""
        newAccount = if (account.length < 8 || account.isEmpty()) {
            account
        } else {
            account.substring(0, 5) + "***" + account.substring(
                account.length - 3,
                account.length
            )
        }
        return newAccount
    }

    private fun getResult() {
        HttpRequest.post(RequestUrls.INVITE_LIST, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    showEmpty()
                    return
                }
                val list: List<InviteResultBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<InviteResultBean>>() {}.type
                )
                resultList.addAll(list)
                adapter?.notifyItemRangeInserted(0, resultList.size)
            }

            override fun onError(response: String?, errorMsg: String?) {
                showEmpty()
            }
        }, tag = this)
    }

    override fun showEmpty() {
        recyclerView.visibility = View.GONE
        resultEmpty.visibility = View.VISIBLE
    }

    fun goBack(view: View) {
        finish()
    }

    override fun onRightClick(view: View) {
        val intent = Intent(this, WriteInviteCodeActivity::class.java)
        if (inviteCode.tag is Boolean) {
            intent.putExtra("isInvited", inviteCode.tag as Boolean)
        }
        startActivityForResult(intent, writeCode)
    }

    fun btnInvite(view: View) {
        val dialog = InviteCodeDialog(this, inviteCode.text.toString(), GlobalValue.downloadH5Address)
        dialog.show()
    }

    private fun showPhoneBind() {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setCancelable(false)
        tipsDialog.setMsg(R.string.invite_bind_phone)
        tipsDialog.setLeftListener(R.string.cancel) {
            tipsDialog.dismiss()
            finish()
        }
        tipsDialog.setRightListener(R.string.go_to_bind) {
            tipsDialog.dismiss()
            val intent = Intent(this, AccountSettingActivity::class.java)
            intent.putExtra("formId", 0)
            startActivity(intent)
        }
        tipsDialog.show()
    }

    fun rulesClick(view: View) {
        showProgressDialog()
        HttpRequest.post(RequestUrls.INVITE_RULE, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgressDialog()
                if (response == null) {
                    return
                }
                showRules(response.optString("activityContent"))
            }

            override fun onError(response: String?, errorMsg: String?) {
                ToastUtils.showLong(errorMsg)
                dismissProgressDialog()
            }
        }, tag = this)
    }

    fun copyText(view: View) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cmData = ClipData.newPlainText("LABEL", inviteCode.text.toString())
        cm.setPrimaryClip(cmData)
        ToastUtils.showLong(R.string.copySuccess)
    }

    private fun showRules(rules: String) {
        val tipsDialog = TipsDialog(this)
        tipsDialog.setDialogTitle(R.string.inviteRules)
        tipsDialog.setGravity(Gravity.START)
        tipsDialog.setMsg(Html.fromHtml(rules).toString())
        tipsDialog.setRightListener(R.string.known) {
            tipsDialog.dismiss()
        }
        tipsDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == writeCode) {
                finish()
            }
        }
    }

}