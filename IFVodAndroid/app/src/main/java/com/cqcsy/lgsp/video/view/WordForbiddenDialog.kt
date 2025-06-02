package com.cqcsy.lgsp.video.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.littlejerk.rvdivider.builder.XGridBuilder
import com.littlejerk.rvdivider.builder.XLinearBuilder
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_word_forbidden.*
import org.json.JSONObject

/**
 * 屏蔽关键词列表
 */
class WordForbiddenDialog(context: Context) : VideoMenuDialog(context) {
    var listData: MutableList<CharSequence>? = null
    var listener: OnWordForbidden? = null

    interface OnWordForbidden {
        fun onAdd(word: String)
        fun onRemove(word: CharSequence)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_word_forbidden)
        if (listData == null) {
            listData = ArrayList()
        }
        if (isVertical) {
            addWordTop.visibility = View.VISIBLE
            addWord.visibility = View.GONE
        } else {
            addWordTop.visibility = View.GONE
            addWord.visibility = View.VISIBLE
        }
        wordForbiddenNum.text = StringUtils.getString(R.string.word_forbidden_num, listData?.size ?: 0)
        emptyText.text = StringUtils.getString(R.string.word_empty)
        addWord.setOnClickListener {
            showInputDialog()
        }
        addWordTop.setOnClickListener {
            showInputDialog()
        }
        initList()
    }

    private fun initList() {
        if (listData.isNullOrEmpty()) {
            wordList.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            wordList.visibility = View.VISIBLE
            emptyText.visibility = View.GONE
        }
        if (isVertical) {
            wordList.layoutManager = GridLayoutManager(context, 2)
            wordList.addItemDecoration(XGridBuilder(context).setHLineSpacing(5f).setVLineSpacing(10f).build())
            itemLayout.setPadding(
                SizeUtils.dp2px(7f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(7f),
                0
            )
        } else {
            wordList.layoutManager = LinearLayoutManager(context)
            wordList.addItemDecoration(XLinearBuilder(context).setSpacing(10f).build())
            itemLayout.setPadding(
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                SizeUtils.dp2px(20f),
                0
            )
        }
        wordList.adapter = object : BaseQuickAdapter<CharSequence, BaseViewHolder>(R.layout.layout_word_forbidden_item, listData) {
            override fun convert(holder: BaseViewHolder, item: CharSequence) {
                holder.setText(R.id.item_name, item)
                holder.getView<ImageView>(R.id.delete_item).setOnClickListener {
                    removeWordForbidden(holder.absoluteAdapterPosition, item)
                }
            }
        }
    }

    private fun showInputDialog() {
        val dialog = InputKeyWordBoard(context)
        dialog.setOnFinishListener(object : InputKeyWordBoard.OnFinishInputListener {
            override fun onFinishInput(input: String) {
                dialog.dismiss()
                if (input.isNotEmpty()) {
                    addWordForbidden(input)
                }
            }

        })
        dialog.show()
    }

    private fun addWordForbidden(word: String) {
        val params = HttpParams()
        params.put("keyWord", word)
        HttpRequest.post(RequestUrls.ADD_FORBIDDEN_WORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listener?.onAdd(word)
//                (wordList.adapter as BaseQuickAdapter<CharSequence, BaseViewHolder>).setList(listData)
//                wordForbiddenNum.text = StringUtils.getString(R.string.word_forbidden_num, listData?.size)
//                if (!listData.isNullOrEmpty() && emptyText.isVisible) {
//                    wordList.visibility = View.VISIBLE
//                    emptyText.visibility = View.GONE
//                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }

    private fun removeWordForbidden(position: Int, word: CharSequence) {
        val params = HttpParams()
        params.put("keyWord", word.toString())
        HttpRequest.post(RequestUrls.REMOVE_FORBIDDEN_WORD, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                listener?.onRemove(word)
//                (wordList.adapter as BaseQuickAdapter<CharSequence, BaseViewHolder>).setList(listData)
                wordList.adapter?.notifyItemRemoved(position)
                wordForbiddenNum.text = StringUtils.getString(R.string.word_forbidden_num, listData?.size)
                if (listData.isNullOrEmpty()) {
                    wordList.visibility = View.GONE
                    emptyText.visibility = View.VISIBLE
                }
            }

            override fun onError(response: String?, errorMsg: String?) {

            }
        }, params, this)
    }
}