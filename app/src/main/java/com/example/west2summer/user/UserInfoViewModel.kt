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

//    val id = MutableLiveData<String?>()


    val name = MutableLiveData<String?>()
    val wechat = MutableLiveData<String?>()
    val qq = MutableLiveData<String?>()
    val phone = MutableLiveData<String?>()

    init {
        Log.d(
            "UserInfoViewModel", "${User.currentUser.value?.wechat}: " +
                    ""
        )
        name.value = User.currentUser.value?.name
        wechat.value = User.currentUser.value?.wechat
        qq.value = User.currentUser.value?.qq
        phone.value = User.currentUser.value?.phone
    }

    fun onDoneClicked() {
        if (name.value.isNullOrBlank()) {
            message.value = "请输入昵称"
        } else {
            try {
                //TODO:更新个人信息
            } catch (e: Exception) {
                message.value = e.toString()
            }
        }
    }


    val message = MutableLiveData<String?>()

    val registerSuccess = MutableLiveData<Boolean>()
}
