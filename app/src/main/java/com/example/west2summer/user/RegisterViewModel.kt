package com.example.west2summer.user

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.isValidPassword
import com.example.west2summer.source.Network
import com.example.west2summer.source.User
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
        if (it == 0) app.getString(R.string.man) else app.getString(R.string.woman)
    }

    val message = MutableLiveData<String?>()

    val registerSuccess = MutableLiveData<Boolean>()

    fun onSexPicked(choose: Int?) {
        sex.value = choose
    }

    fun onRegisterClicked() {
        if (name.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_name)
        } else if (id.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_id)
        } else if (password.value.isNullOrBlank() || !password.value!!.isValidPassword()) {
            message.value = app.getString(R.string.please_enter_correct_password)
        } else if (!password.value.equals(passwordConfirm.value)) {
            message.value = app.getString(R.string.password_diff)
        } else if (sex.value == null) {
            message.value = app.getString(R.string.please_choose_sex)
        } else if (address.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_address)
        } else {
            uiScope.launch {
                register()
            }
        }
    }

    private suspend fun register() {
        withContext(Dispatchers.IO) {
            val newUser =
                User(
                    id.value!!.toLong(),
                    password.value,
                    name.value,
                    sex.value,
                    address.value
                )
            try {
                val response = Network.service.registerAsync(newUser).await()
                if (response.msg == "same") {
                    message.postValue(app.getString(R.string.id_registered))
                } else if (response.msg == app.getString(R.string.user_response_ok)) {
                    prf.edit().apply() {
                        putString("id", id.value)
                        putString("password", password.value)
                    }.commit()
                    User.postCurrentUser(response.user!!)
                    message.postValue(app.getString(R.string.register_success))
                    registerSuccess.postValue(true)
                } else {
                    throw Exception(response.msg)
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

    fun onRegistered() {
        registerSuccess.value = false
    }

    fun onMessageShowed() {
        message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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