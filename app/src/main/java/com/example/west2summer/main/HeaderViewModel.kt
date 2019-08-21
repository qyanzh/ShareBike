package com.example.west2summer.main

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.west2summer.user.User

class HeaderViewModel : ViewModel() {
    var user = User.getCurrentUserLive()
    val username = Transformations.map(user) {
        user.value?.let {
            it.wechat
        } ?: "未登录"
    }
    val phone = Transformations.map(user) {
        user.value?.let {
            it.phone
        } ?: "点击登录"
    }


}