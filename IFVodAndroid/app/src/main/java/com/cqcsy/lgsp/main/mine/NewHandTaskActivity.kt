package com.cqcsy.lgsp.main.mine

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.TaskBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.lgsp.utils.TaskJumpUtils
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.layout_recyclerview.*
import org.json.JSONObject

/**
 * 新手任务
 */
class NewHandTaskActivity : NormalActivity() {
    private var dataList: MutableList<TaskBean> = ArrayList()

    override fun getContainerView(): Int {
        return R.layout.layout_recyclerview
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.newHandTaskTitle)
        findViewById<LinearLayout>(R.id.recyclerLayout).setBackgroundResource(R.color.new_hand_background)
        initView()
        showProgress()
    }

    override fun onResume() {
        super.onResume()
        getAllTask()
    }

    private fun initView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = object : BaseQuickAdapter<TaskBean, BaseViewHolder>(
            R.layout.item_new_hand_task,
            dataList
        ) {
            override fun convert(holder: BaseViewHolder, item: TaskBean) {
                holder.setText(R.id.taskName, item.name)
                if (item.defaultValue > 1) {
                    holder.setText(
                        R.id.taskCounts,
                        "(" + item.currentValue.toString() + "/" + item.defaultValue.toString() + ")"
                    )
                    holder.setVisible(R.id.taskCounts, true)
                } else {
                    holder.setGone(R.id.taskCounts, true)
                }
                holder.setText(
                    R.id.taskCoin,
                    StringUtils.getString(R.string.taskValue, item.gold)
                )
                holder.setText(
                    R.id.taskExperience,
                    StringUtils.getString(R.string.taskValue, item.experince)
                )
                holder.setEnabled(R.id.goToFinish, item.status != 1)
                if (item.status == 1) {
                    holder.setText(R.id.goToFinish, StringUtils.getString(R.string.finished))
                } else {
                    holder.setText(R.id.goToFinish, StringUtils.getString(R.string.goToFinish))
                }
                holder.getView<TextView>(R.id.goToFinish).setOnClickListener {
                    TaskJumpUtils.jumpFinishTask(this@NewHandTaskActivity, item.actionType)
                }
            }
        }
    }

    /**
     * 获取每日任务数据
     */
    private fun getAllTask() {
        val params = HttpParams()
        params.put("TaskType", "0")
        HttpRequest.post(RequestUrls.GET_ALL_TASK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    showEmpty()
                    return
                }
                val list: MutableList<TaskBean> =
                    Gson().fromJson(
                        jsonArray.toString(),
                        object : TypeToken<List<TaskBean>>() {}.type
                    )
                dataList.clear()
                dataList.addAll(list)
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed {
                    showProgress()
                    getAllTask()
                }
            }

        }, params, tag = this)
    }
}