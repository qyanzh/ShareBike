package com.example.west2summer.user

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.isValidPassword
import com.example.west2summer.source.RegisteredException
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class RegisterViewModel(val app: Application) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

    val registerSuccess = MutableLiveData<Boolean?>()

    fun onSexPicked(choose: Int?) {
        sex.value = choose
    }

    fun onRegisterClicked() {
        if (name.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_name)
        } else if (id.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_id)
        } else if (!password.value.isValidPassword()) {
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
        try {
            val newUser =
                User(
                    id.value!!.toLong(),
                    password.value,
                    name.value,
                    sex.value,
                    address.value
                )
            Repository.register(newUser)
            message.value = app.getString(R.string.register_success)
            registerSuccess.value = true
        } catch (e: Exception) {
            when (e) {
                is ConnectException -> message.postValue(app.getString(R.string.exam_network))
                is RegisteredException -> message.postValue(app.getString(R.string.id_registered))
                else -> message.postValue(e.toString())
            }
        }
    }

    fun onRegistered() {
        registerSuccess.value = null
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