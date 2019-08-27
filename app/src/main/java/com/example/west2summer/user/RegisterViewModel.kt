package com.example.west2summer.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.example.west2summer.database.Network
import kotlinx.coroutines.*
import java.net.ConnectException

class RegisterViewModel(val app: Application) : AndroidViewModel(app) {
    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val prf = app.getSharedPreferences("user", Context.MODE_PRIVATE)

    val name = MutableLiveData<String?>()
    val id = MutableLiveData<String?>()
    val password = MutableLiveData<String?>()
    val passwordConfirm = MutableLiveData<String?>()
    val address = MutableLiveData<String?>()
    val sex = MutableLiveData<Int>()
    val uiSex: LiveData<String> = Transformations.map(sex) {
        if (it == 0) "男" else "女"
    }

    fun onSexPicked(choose: Int?) {
        sex.value = choose
    }

    val message = MutableLiveData<String?>()

    val registerSuccess = MutableLiveData<Boolean>()

    fun onRegisterClicked() {
        if (name.value.isNullOrBlank()) {
            message.value = "请输入昵称"
        } else if (id.value.isNullOrBlank()) {
            message.value = "请输入学号"
        } else if (password.value.isNullOrBlank() || !password.value!!.isValidPassword()) {
            message.value = "请输入密码，长度不少于6位"
        } else if (!password.value.equals(passwordConfirm.value)) {
            message.value = "两次输入密码不一致，请重试"
        } else if (sex.value == null) {
            message.value = "请选择性别"
        } else if (address.value.isNullOrBlank()) {
            message.value = "请输入住址"
        } else {
            try {
                register()
            } catch (e: Exception) {
                message.value = e.toString()
            }
        }
    }

    private fun register() {
        val newUser =
            User(id.value!!.toLong(), password.value, name.value, sex.value, address.value)
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    Network.service.register(newUser).await()
                    prf.edit().apply() {
                        putString("id", id.value)
                        putString("password", password.value)
                    }.apply()
                    registerSuccess.postValue(true)
                } catch (e: Exception) {
                    if (e is ConnectException) {
                        message.postValue("请检查网络连接")
                    } else {
                        message.postValue(e.toString())
                    }
                }
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