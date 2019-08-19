package com.example.west2summer

import android.util.Log
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

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