package com.cqcsy.lgsp.main.mine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.TaskBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.utils.ImageUtil
import com.cqcsy.lgsp.utils.TaskJumpUtils
import com.cqcsy.library.network.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import kotlinx.android.synthetic.main.activity_task_center.*
import org.json.JSONObject

/**
 * 我的 - 任务中心
 */
class TaskCenterActivity : NormalActivity() {
    private var taskData: MutableList<TaskBean> = ArrayList()

    override fun getContainerView(): Int {
        return R.layout.activity_task_center
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.taskCenterTitle)
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
            R.layout.item_day_task,
            taskData
        ) {
            override fun convert(holder: BaseViewHolder, item: TaskBean) {
                ImageUtil.loadCircleImage(context, item.icon, holder.getView(R.id.taskIcon))
                holder.setText(R.id.taskName, item.name)
                holder.setText(R.id.taskCoin, StringUtils.getString(R.string.taskValue, item.gold))
                holder.setText(
                    R.id.taskExp,
                    StringUtils.getString(R.string.taskValue, item.experince)
                )
                if (item.defaultValue > 1) {
                    holder.setText(
                        R.id.taskSignCounts,
                        "(" + item.currentValue.toString() + "/" + item.defaultValue.toString() + ")"
                    )
                    holder.setVisible(R.id.taskSignCounts, true)
                } else {
                    holder.setGone(R.id.taskSignCounts, true)
                }
                val finish = holder.getView<TextView>(R.id.finishBtn)
                if (item.status == 1) {
                    finish.isEnabled = false
                    finish.setText(R.string.finished)
                } else {
                    finish.setText(R.string.goToFinish)
                    finish.isEnabled = true
                }
                holder.getView<TextView>(R.id.finishBtn).setOnClickListener {
                    TaskJumpUtils.jumpFinishTask(this@TaskCenterActivity, item.actionType)
                }
            }
        }
    }

    /**
     * 获取每日任务数据
     */
    private fun getAllTask() {
        val params = HttpParams()
        params.put("TaskType", "1")
        HttpRequest.post(RequestUrls.GET_ALL_TASK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    return
                }
                val list: MutableList<TaskBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<TaskBean>>() {}.type
                )
                taskData.clear()
                taskData.addAll(list)
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgress()
                ToastUtils.showLong(errorMsg)
            }

        }, params, tag = this)
    }

    /**
     * 新手任务
     */
    fun goToNewHand(view: View) {
        startActivityForResult(Intent(this, NewHandTaskActivity::class.java), 1001)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1001) {
                val index = data?.getIntExtra("index", 0)
                val intent = Intent()
                intent.putExtra("index", index)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}