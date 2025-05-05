package com.cqcsy.lgsp.main.mine

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.*
import com.cqcsy.lgsp.R
import com.cqcsy.library.base.NormalActivity
import com.cqcsy.lgsp.bean.BonusListBean
import com.cqcsy.lgsp.base.RequestUrls
import com.cqcsy.library.network.callback.HttpCallBack
import com.cqcsy.library.network.HttpRequest
import com.cqcsy.library.utils.GlobalValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lzy.okgo.model.HttpParams
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import kotlinx.android.synthetic.main.activity_sign_get_gift.*
import kotlinx.android.synthetic.main.item_sign_coin.view.*
import kotlinx.android.synthetic.main.item_sign_exp.view.*
import org.json.JSONObject
import kotlin.collections.ArrayList

/**
 * 签到领好礼
 */
class SignGetGiftActivity : NormalActivity() {
    private var bonusData: MutableList<BonusListBean> = ArrayList()

    // 当前签到第几天,默认第一天1
    private var currentDay = 1

    // 我的金币数量
    private var coinCount = 0

    // 今天是否签到
    private var isSign = false

    // 下次签到倒计时，毫秒
    private var countDownTimeMilliSecond = 0
    private var mTimer: CountDownTimer? = null

    override fun getContainerView(): Int {
        return R.layout.activity_sign_get_gift
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHeaderTitle(R.string.signTaskTitle)
        initData()
        getBonus()
    }

    override fun onDestroy() {
        mTimer?.cancel()
        mTimer = null
        super.onDestroy()
    }

    private fun initData() {
        coinCount = GlobalValue.userInfoBean?.userExtension?.gold ?: 0
    }

    private fun startCountDown() {
        if (countDownTimeMilliSecond == 0) return
        if (mTimer != null) {
            mTimer?.cancel()
            mTimer = null
        }
        mTimer = object : CountDownTimer(countDownTimeMilliSecond.toLong(), 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                signBtn.text = getString(R.string.sign_in_next_time, CommonUtil.stringForTime(millisUntilFinished))
            }

            override fun onFinish() {
                isSign = false
                initSignView()
            }

        }
        mTimer?.start()
    }

    private fun initSignView() {
        signBtn.isEnabled = !isSign
        signBtn.setOnClickListener {
            if (isSign) {
                return@setOnClickListener
            }
            reqBonus()
        }
        if (isSign) {
            signBtn.setTextColor(ColorUtils.getColor(R.color.white_80))
            signBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            signedDays.visibility = View.VISIBLE
            signedDays.text = Html.fromHtml(
                StringUtils.getString(R.string.signDays, currentDay)
            )
            valueLayout.visibility = View.GONE
            signTopTips.visibility = View.GONE
            mineCoinCounts.visibility = View.VISIBLE
            mineCoinCounts.text = StringUtils.getString(R.string.mineCoinCounts, coinCount)
            startCountDown()
        } else {
            signBtn.setTextColor(ColorUtils.getColor(R.color.white))
            signBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            signBtn.setText(R.string.sign_in)
            signedDays.visibility = View.GONE
            valueLayout.visibility = View.VISIBLE
            signTopTips.visibility = View.VISIBLE
            mineCoinCounts.visibility = View.GONE
            if (currentDay >= bonusData.size && bonusData.last().type == 1) {
                coinValue.visibility = View.GONE
                expValue.visibility = View.GONE
            } else if (currentDay > 0) {
                coinValue.visibility = View.VISIBLE
                expValue.visibility = View.VISIBLE
                coinValue.text = StringUtils.getString(R.string.coinAdd, bonusData[currentDay - 1].coin)
                expValue.text = StringUtils.getString(R.string.expAdd, bonusData[currentDay - 1].reward)
            }
        }
        mineCoinCounts.setOnClickListener {
            startActivity(Intent(this, MineCoinActivity::class.java))
        }
    }

    private fun addCoinView() {
        signCoinLayout.removeAllViews()
        var sonLayout: LinearLayout? = null
        var itemWidth = (ScreenUtils.getScreenWidth()
                - SizeUtils.dp2px(44f)
                - SizeUtils.dp2px(4f) * 3) / 4
        val unitDay = 7
        val otherDay = currentDay % unitDay
        val intDay = currentDay / unitDay
        for (i in bonusData.indices) {
            if (i >= unitDay) {
                break
            }
            val bean = bonusData[i]
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_sign_coin, signCoinLayout, false)
            if (i == 6) {
                itemWidth = itemWidth * 2 + 4
            }
            if (bean.type == 1) {
                // 送会员
                if (i == 6) {
                    view.itemLayout.setBackgroundResource(R.drawable.sign_vip_big_selector)
                } else {
                    view.itemLayout.setBackgroundResource(R.drawable.sign_vip_min_selector)
                }
                view.itemCoin.text = StringUtils.getString(R.string.signVip, bean.vipDay)
            } else {
                // 送金币
                view.itemCoin.text = StringUtils.getString(R.string.whatCoin, bean.coin)
            }
            val lp = LinearLayout.LayoutParams(itemWidth, SizeUtils.dp2px(102f))
            if (otherDay == 0) {
                view.itemDay.text =
                    StringUtils.getString(R.string.whatDays, ((intDay - 1) * unitDay + i + 1))
                view.itemLayout.isSelected =
                    (i < unitDay && (i + 1) != unitDay) || ((i + 1) == unitDay && isSign)
            } else {
                view.itemDay.text =
                    StringUtils.getString(R.string.whatDays, (intDay * unitDay + i + 1))
                view.itemLayout.isSelected =
                    (i < otherDay && (i + 1) != otherDay) || ((i + 1) == otherDay && isSign)
            }
            lp.topMargin = SizeUtils.dp2px(10f)
            if (i % 4 == 0) {
                sonLayout = LinearLayout(this)
                view.layoutParams = lp
                sonLayout.addView(view)
            } else {
                lp.leftMargin = SizeUtils.dp2px(4f)
                view.layoutParams = lp
                sonLayout?.addView(view)
            }
            if (i == 3 || i == 6) {
                signCoinLayout.addView(sonLayout)
            }
        }
    }

    private fun addExpView() {
        signExpLayout.removeAllViews()
        val itemWidth = (ScreenUtils.getScreenWidth()
                - SizeUtils.dp2px(44f)
                - SizeUtils.dp2px(13f) * 6) / 7
        val lp = LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
        val sonLp = LinearLayout.LayoutParams(itemWidth, itemWidth)
        val unitDay = 7
        val otherDay = currentDay % unitDay
        val intDay = currentDay / unitDay
        for (i in bonusData.indices) {
            if (i >= unitDay) {
                break
            }
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_sign_exp, signExpLayout, false)
            if (i != 6) {
                lp.rightMargin = SizeUtils.dp2px(13f)
            }
            view.layoutParams = lp
            if (otherDay == 0) {
                view.itemExpDay.text =
                    StringUtils.getString(R.string.days, ((intDay - 1) * unitDay + i + 1))
                view.itemExpLayout.isSelected =
                    (i < unitDay && (i + 1) != unitDay) || ((i + 1) == unitDay && isSign)
            } else {
                view.itemExpDay.text =
                    StringUtils.getString(R.string.days, (intDay * unitDay + i + 1))
                view.itemExpLayout.isSelected =
                    (i < otherDay && (i + 1) != otherDay) || ((i + 1) == otherDay && isSign)
            }
            view.itemExpValue.text = StringUtils.getString(R.string.taskValue, bonusData[i].reward)
            view.itemExpLayout.layoutParams = sonLp
            signExpLayout.addView(view)
        }
    }

    private fun setGiftTip(day: Int, coin: Int, exp: Int) {
        if (coin == 0 || exp == 0) {
            sign_gif_tip.visibility = View.INVISIBLE
        } else {
            sign_gif_tip.visibility = View.VISIBLE
            sign_day_tip.text = StringUtils.getString(R.string.signTips, day)
            if (coin > 0) {
                task_coin.isVisible = true
                task_coin.text = coin.toString()
            } else {
                task_coin.isVisible = false
            }
            if (exp > 0) {
                task_exp.isVisible = true
                task_exp.text = exp.toString()
            } else {
                task_exp.isVisible = false
            }
        }
    }

    /**
     * 获取签到情况
     */
    private fun getBonus() {
        showProgress()
        HttpRequest.post(RequestUrls.GET_BONUS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                dismissProgress()
                val jsonArray = response?.optJSONArray("bonus_list")
                if (jsonArray == null || jsonArray.length() == 0) {
                    showEmpty()
                    return
                }
                setGiftTip(response.optInt("cumulativeDays"), response.optInt("gold"), response.optInt("empirical"))
                currentDay = response.optInt("bonus_day", 1)
                isSign = response.optInt("bonus_status") == 1
                val list: MutableList<BonusListBean> = Gson().fromJson(
                    jsonArray.toString(),
                    object : TypeToken<List<BonusListBean>>() {}.type
                )
                countDownTimeMilliSecond = response.optInt("millisecondsCount")
                bonusData.addAll(list)
                addCoinView()
                addExpView()
                initSignView()
            }

            override fun onError(response: String?, errorMsg: String?) {
                showFailed { getBonus() }
            }

        }, tag = this)
    }

    /**
     * 提交签到
     */
    private fun reqBonus() {
        showProgressDialog()
        HttpRequest.post(RequestUrls.REQ_BONUS, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                updateTask()
                if (response != null) {
                    coinCount += response.optInt("thisGold")
                    GlobalValue.userInfoBean?.userExtension?.gold = coinCount
                    countDownTimeMilliSecond = response.optInt("millisecondsCount")
                }
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }

        }, tag = this)
    }

    /**
     * 签到成功后更新任务
     * UserActionType=21(分享),12(签到),18(上传视频)
     */
    private fun updateTask() {
        val params = HttpParams()
        params.put("UserActionType", "12")
        HttpRequest.post(RequestUrls.UPD_TASK, object : HttpCallBack<JSONObject>() {
            override fun onSuccess(response: JSONObject?) {
                if (response == null) {
                    return
                }
                isSign = response.optBoolean("isSuccess")
                if (isSign) {
                    addCoinView()
                    addExpView()
                    initSignView()
                }
                dismissProgressDialog()
            }

            override fun onError(response: String?, errorMsg: String?) {
                dismissProgressDialog()
                ToastUtils.showLong(errorMsg)
            }

        }, params, tag = this)
    }
}