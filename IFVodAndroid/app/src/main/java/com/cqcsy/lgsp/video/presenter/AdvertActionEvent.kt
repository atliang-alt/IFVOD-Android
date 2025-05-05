package com.cqcsy.lgsp.video.presenter

enum class AdvertAction {
    ACTION_PLAY,ACTION_FULL
}

class AdvertActionEvent(var action: AdvertAction)