package com.example.west2summer.main

import android.util.Log
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.west2summer.database.User

class HeaderViewModel : ViewModel() {
    var user = User.getCurrentUserLive()
    val username = Transformations.map(user) {
        Log.d(
            "HeaderViewModel", ": " +
                    ""
        )
        user.value?.wechat
    }
    val phone = Transformations.map(user) {
        Log.d(
            "HeaderViewModel", ": " +
                    ""
        )
        user.value?.phone
    }
}