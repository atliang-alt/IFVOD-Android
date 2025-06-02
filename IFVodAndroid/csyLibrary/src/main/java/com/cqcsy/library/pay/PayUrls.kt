package com.cqcsy.library.pay

import com.cqcsy.library.network.BaseUrl

object PayUrls {

    // 修改/创建信用卡支付地址
    val GET_PAY_ADDRESS = BaseUrl.BASE_URL + "api/payment/AddOrUpdateAddress"

    // 获取国家省份
    val PROVINCE_LIST = BaseUrl.BASE_URL + "api/login/CountryStateList"

    // 移除信用卡支付地址
    val DELETE_PAY_ADDRESS = BaseUrl.BASE_URL + "api/payment/RemoveAddress"

    // 信用卡支付创建订单
    val CREATE_CARD_ORDER = BaseUrl.BASE_URL + "api/payment/CreateOrder"

    // 获取信用卡支付订单
    val GET_CARD_ORDER = BaseUrl.BASE_URL + "api/payment/GetOrderInfo"

    // 无法付款原因
    val NO_PAY_REASON = BaseUrl.BASE_URL + "api/payment/GetFailureList"

    // 无法付款原因提交
    val NO_PAY_FEEDBACK = BaseUrl.BASE_URL + "api/payment/FeedbackReason"

    // 获取TGC余额
    val GET_TGC_BALANCE = BaseUrl.BASE_URL + "api/payment/TGCBalance"

    // 获取信用卡账单地址
    val GET_CREDIT_CARD_ADDRESS = BaseUrl.BASE_URL + "api/payment/GetAddressList"

    // 创建TGC订单
    val CREATE_TGC_ORDER = BaseUrl.BASE_URL + "api/payment/GenerateThirdPartyOrder"

    // 确认、取消支付订单
    val SURE_CANCEL_ORDER = BaseUrl.BASE_URL + "api/payment/ModifyOrderStatus"

    /**
     * 获取常用地区
     */
    val COMMON_USE_COUNTRY_LIST = BaseUrl.BASE_URL + "api/home/GetMyregion"

    // 获取城市区域和当前区域
    val COUNTRY_LIST = BaseUrl.BASE_URL + "api/Login/countrylist"
}