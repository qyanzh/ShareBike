package com.example.west2summer.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.database.fakeLogin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class LoginViewModel(val app: Application) : AndroidViewModel(app) {


    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val username = MutableLiveData<String?>()
    val password = MutableLiveData<String?>()

    val message = MutableLiveData<String?>()

    val loginSuccess = MutableLiveData<Boolean>()

    init {
        if (prf.contains("username")) {
            username.value = prf.getString("username", "")
        }
        if (prf.contains("password")) {
            password.value = prf.getString("password", "")
        }
    }

    fun onFabClicked() {
        if (username.value.isNullOrBlank()) {
            message.value = "请输入正确账号"
        } else if (password.value.isNullOrBlank() || !password.value!!.isValidPassword()) {
            message.value = "请输入密码，长度不少于6位"
        } else {
            try {
                login()
            } catch (e: Exception) {
                message.value = e.toString()
            }
        }
    }

    private fun login() {
        //TODO: 向服务器发送登录请求
        if (fakeLogin(username.value!!.toLong(), password.value!!)) {
            prf.edit().apply() {
                putString("username", username.value)
                putString("password", password.value)
            }.apply()
            loginSuccess.value = true
        }
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
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}

