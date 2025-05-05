package com.cqcsy.lgsp.vip

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.blankj.utilcode.util.ToastUtils
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.CommentBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_new_vote.*
import org.json.JSONObject

/**
 * 发起投票
 */
class NewVoteActivity : NormalActivity() {
    private val itemIds = arrayOf("A.", "B.", "C.", "D.", "E.")
    private val defaultItems = 2
    private var mediaKey = ""
    private var videoType = 0
    private var voteType = 1

    override fun getContainerView(): Int {
        return R.layout.activity_new_vote
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.sendVote)
        initData()
        voteTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                setButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        optionsContainer.removeAllViews()
        for (i in 0 until defaultItems) {
            addOptionItem()
        }
        singleSelect.setOnClickListener {
            singleSelect.isSelected = true
            fewSelect.isSelected = false
            voteType = 1
        }
        fewSelect.setOnClickListener {
            singleSelect.isSelected = false
            fewSelect.isSelected = true
            voteType = 2
        }
    }

    private fun initData() {
        mediaKey = intent.getStringExtra("mediaKey") ?: ""
        videoType = intent.getIntExtra("videoType", 0)
    }

    private fun addOptionItem() {
        val item = View.inflate(this, R.layout.layout_vote_option_item, null)
        val id = item.findViewById<TextView>(R.id.option_id)
        val input = item.findViewById<EditText>(R.id.option_input)
        val delete = item.findViewById<ImageView>(R.id.row_delete)
        val count = optionsContainer.childCount
        if (count < defaultItems) {
            delete.visibility = View.GONE
            input.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    setButtonState()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

            })
        } else {
            delete.visibility = View.VISIBLE
        }
        delete.setOnClickListener {
            optionsContainer.removeView(item)
            sortId()
            setAddState()
        }
        id.text = itemIds[count]
        optionsContainer.addView(item)
        setAddState()
    }

    private fun setAddState() {
        if (optionsContainer.childCount >= itemIds.size) {
            addOptionContent.visibility = View.GONE
        } else {
            addOptionContent.visibility = View.VISIBLE
        }
    }

    private fun sortId() {
        if (optionsContainer.childCount > 2) {
            for (i in 2 until optionsContainer.childCount) {
                val item = optionsContainer.getChildAt(i)
                val id = item.findViewById<TextView>(R.id.option_id)
                id.text = itemIds[i]
            }
        }
    }

    private fun setButtonState() {
        var flag = true
        val title = voteTitle.text
        if (title.isNullOrEmpty()) {
            flag = false
        } else {
            for (i in 0 until 2) {
                val item = optionsContainer.getChildAt(i)
                val input = item.findViewById<EditText>(R.id.option_input)
                if (input.text.isNullOrEmpty()) {
                    flag = false
                }
            }
        }
        submitVote.isEnabled = flag
    }

    /**
     * 拼接投票选项内容
     */
    private fun appendVoteItem(): MutableList<String> {
        val jsonArray = ArrayList<String>()
        for (i in 0 until optionsContainer.childCount) {
            val item = optionsContainer.getChildAt(i)
            val input = item.findViewById<EditText>(R.id.option_input)
            val value = input.text.toString()
            if(value.isNotEmpty()) {
                jsonArray.add(value)
            }
        }
        Log.i("投票：", jsonArray.toString())
        return jsonArray
    }

    fun addOptions(view: View) {
        addOptionItem()
    }

    /**
     * 发布投票接口
     */
    fun submitVote(view: View) {
        showProgressDialog()
        val voteItemList = appendVoteItem()
        if (hasSame(voteItemList)) {
            dismissProgressDialog()
            ToastUtils.showLong(R.string.vote_item_tip)
            return
        }
        val params = HttpParams()
        params.put("UID", GlobalValue.userInfoBean!!.token.uid)
        params.put("mediaKey", mediaKey)
        params.putUrlParams("VoteItem", voteItemList)
        params.put("VoteType", voteType)
        params.put("Contxt", voteTitle.text.toString())
        HttpRequest.post(RequestUrls.RELEASE_COMMENT + "?videoType=" + videoType, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                val commentBean = Gson().fromJson<CommentBean>(
                    response.toString(),
                    object : TypeToken<CommentBean>() {}.type
                )
                ToastUtils.showLong(R.string.voteSuccess)
                val intent = Intent()
                intent.putExtra("commentBean", commentBean)
                setResult(Activity.RESULT_OK, intent)
                dismissProgressDialog()
                finish()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }
        }, params, this)
    }

    /**
     * 判断集合是否有相同元素 true有相同  false没有相同
     */
    private fun hasSame(list: MutableList<String>): Boolean {
        return list.size != HashSet(list).size
    }
}