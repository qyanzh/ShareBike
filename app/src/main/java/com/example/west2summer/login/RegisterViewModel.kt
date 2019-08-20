package com.example.west2summer.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.database.fakeLogin
import com.example.west2summer.database.fakeRegister
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class RegisterViewModel(val app: Application) : AndroidViewModel(app) {
    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val username = MutableLiveData<String?>()
    val password = MutableLiveData<String?>()
    val passwordConfirm = MutableLiveData<String?>()

    val wechat = MutableLiveData<String?>()
    val qq = MutableLiveData<String?>()
    val phone = MutableLiveData<String?>()

    val message = MutableLiveData<String?>()

    val registerSuccess = MutableLiveData<Boolean>()

    fun onRegisterClicked() {
        if (username.value.isNullOrBlank()) {
            message.value = "请输入学号"
        } else if (password.value.isNullOrBlank() || !password.value!!.isValidPassword()) {
            message.value = "请输入密码，长度不少于6位"
        } else if (!password.value.equals(passwordConfirm.value)) {
            message.value = "两次输入密码不一致，请重试"
        } else if (qq.value.isNullOrBlank() && wechat.value.isNullOrBlank() && phone.value.isNullOrBlank()) {
            message.value = "请至少填写一种联系方式"
        } else {
            try {
                register()
            } catch (e: Exception) {
                message.value = e.toString()
            }
        }
    }

    private fun register() {
        //TODO: 向服务器发送注册请求
        if (fakeRegister()) {
            if (fakeLogin(username.value!!.toLong(), password.value!!)) {
                prf.edit().apply() {
                    putString("username", username.value)
                    putString("password", password.value)
                }.apply()
                registerSuccess.value = true
            }
        }
    }

    fun onRegistered() {
        registerSuccess.value = false
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun String.isValidPassword(): Boolean {
        if (length < 6) return false
        return true
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RegisterViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}