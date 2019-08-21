package com.example.west2summer.user

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class UserInfoViewModel : ViewModel() {
    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

//    val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

//    val username = MutableLiveData<String?>()


    val nickname = MutableLiveData<String?>()
    val wechat = MutableLiveData<String?>()
    val qq = MutableLiveData<String?>()
    val phone = MutableLiveData<String?>()

    init {
        Log.d(
            "UserInfoViewModel", "${User.getCurrentUser()?.wechat}: " +
                    ""
        )
        nickname.value = User.getCurrentUser()?.nickname
        wechat.value = User.getCurrentUser()?.wechat
        qq.value = User.getCurrentUser()?.qq
        phone.value = User.getCurrentUser()?.phone
    }


    val message = MutableLiveData<String?>()

    val registerSuccess = MutableLiveData<Boolean>()
}
