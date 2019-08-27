package com.example.west2summer.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.R
import com.example.west2summer.database.Network
import kotlinx.coroutines.*
import java.net.ConnectException

class LoginViewModel(val app: Application) : AndroidViewModel(app) {


    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val id = MutableLiveData<String?>()
    val password = MutableLiveData<String?>()

    val message = MutableLiveData<String?>()

    val loginSuccess = MutableLiveData<Boolean>()

    fun autoComplete() {
        if (prf.contains("id")) {
            id.value = prf.getString("id", "")
        }
        if (prf.contains("password")) {
            password.value = prf.getString("password", "")
        }
    }

    val shouldNavigateToRegister = MutableLiveData<Boolean>()

    fun onRegisterClicked() {
        shouldNavigateToRegister.value = true
    }

    fun onRegisterNavigated() {
        shouldNavigateToRegister.value = false
    }

    fun onLoginClicked() {
        if (id.value.isNullOrBlank()) {
            message.value = "请输入正确学号"
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
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val uid = id.value!!.toLong()
                    if (Network.service.login(uid, password.value!!).await().data == "success") {
                        User.postCurrentUser(Network.service.getUserInfo(uid).await().user!!)
                        prf.edit().apply() {
                            putString("id", id.value)
                            putString("password", password.value)
                        }.apply()
                        message.postValue(app.getString(R.string.login_success))
                        loginSuccess.postValue(true)
                    } else {
                        throw Exception(app.getString(R.string.login_failed))
                    }
                } catch (e: Exception) {
                    if (e is ConnectException) {
                        message.postValue(app.getString(R.string.exam_network))
                    } else {
                        message.postValue(e.toString())
                    }
                }
            }
        }
    }

    fun onMessageShowed() {
        message.value = null
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

