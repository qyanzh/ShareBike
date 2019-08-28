package com.example.west2summer.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.R
import com.example.west2summer.component.isValidPassword
import com.example.west2summer.source.Network
import com.example.west2summer.source.User
import kotlinx.coroutines.*
import java.net.ConnectException

class LoginViewModel(val app: Application) : AndroidViewModel(app) {


    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val id = MutableLiveData<String?>()

    val password = MutableLiveData<String?>()

    val message = MutableLiveData<String?>()

    val loginSuccess = MutableLiveData<Boolean>()

    init {
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
            message.value = app.getString(R.string.please_enter_correct_id)
        } else if (password.value.isNullOrBlank() || !password.value!!.isValidPassword()) {
            message.value = app.getString(R.string.please_enter_correct_password)
        } else {
            uiScope.launch {
                login()
            }
        }
    }

    private suspend fun login() {
        withContext(Dispatchers.IO) {
            try {
                val uid = id.value!!.toLong()
                if (Network.service.loginAsync(uid, password.value!!).await().msg == app.getString(
                        R.string.login_response_success
                    )
                ) {
                    User.postCurrentUser(Network.service.getUserInfoAsync(uid).await().user!!)
                    prf.edit().apply() {
                        putString("id", id.value)
                        putString("password", password.value)
                    }.apply()
                    message.postValue(app.getString(R.string.login_success))
                    loginSuccess.postValue(true)
                } else {
                    throw Exception(app.getString(R.string.id_or_password_error))
                }
            } catch (e: Exception) {
                message.postValue(
                    if (e is ConnectException)
                        app.getString(R.string.exam_network)
                    else
                        e.toString()
                )
            }
        }
    }

    fun onMessageShowed() {
        message.value = null
    }

    fun onLoginSuccess() {
        loginSuccess.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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

