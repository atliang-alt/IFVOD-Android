package com.cqcsy.lgsp.bean.net

import com.cqcsy.library.base.BaseBean

class VerificationCodeNetBean : BaseBean() {
    var code: Int = 0
    var msg: String = ""
    var info: MutableList<Boolean> = ArrayList()
}