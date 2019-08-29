package com.example.west2summer.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.R
import com.example.west2summer.component.isValidPassword
import com.example.west2summer.source.IdentifyErrorException
import com.example.west2summer.source.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class LoginViewModel(val app: Application) : AndroidViewModel(app) {


    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val spf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val id = MutableLiveData<String?>()

    val password = MutableLiveData<String?>()

    val message = MutableLiveData<String?>()

    val loginSuccess = MutableLiveData<Boolean?>()

    init {
        if (spf.contains("id")) {
            id.value = spf.getLong("id", 0L).toString()
        }
        if (spf.contains("password")) {
            password.value = spf.getString("password", "")
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
        try {
            val uid = id.value!!.toLong()
            val pas = password.value!!.toString()
            Repository.login(uid, pas)
            message.value = app.getString(R.string.login_success)
            loginSuccess.value = true
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                is IdentifyErrorException -> app.getString(R.string.id_or_password_error)
                else -> e.toString()
            }
        }
    }

    fun onMessageShowed() {
        message.value = null
    }

    fun onLoginSuccess() {
        loginSuccess.value = null
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

