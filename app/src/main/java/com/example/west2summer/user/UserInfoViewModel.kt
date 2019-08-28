package com.example.west2summer.user

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.source.Network
import com.example.west2summer.source.User
import kotlinx.coroutines.*
import java.net.ConnectException

class UserInfoViewModel(val app: Application) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val name = MutableLiveData<String?>()
    val sex = MutableLiveData<Int?>()
    val uiSex: LiveData<String> = Transformations.map(sex) {
        if (it == 0) app.getString(R.string.man) else app.getString(R.string.woman)
    }
    val address = MutableLiveData<String?>()
    val wechat = MutableLiveData<String?>()
    val qq = MutableLiveData<String?>()
    val phone = MutableLiveData<String?>()

    val modifySuccess = MutableLiveData<Boolean?>()

    init {
        name.value = User.currentUser.value?.name
        sex.value = User.currentUser.value?.sex
        address.value = User.currentUser.value?.address
        wechat.value = User.currentUser.value?.wechat
        qq.value = User.currentUser.value?.qq
        phone.value = User.currentUser.value?.phone
    }

    fun onSexPicked(choose: Int?) {
        sex.value = choose
    }

    fun onDoneClicked() {
        if (name.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_name)
        } else if (address.value.isNullOrBlank()) {
            message.value = app.getString(R.string.please_enter_address)
        } else {
            uiScope.launch {
                modifyInfo()
            }
        }
    }

    private suspend fun modifyInfo() {
        withContext(Dispatchers.IO) {
            try {
                val user = User.currentUser.value?.copy(
                    name = name.value,
                    address = address.value,
                    sex = sex.value,
                    wechat = wechat.value,
                    qq = qq.value,
                    phone = phone.value
                )?.also {
                    val response =
                        Network.service.updateUserAsync(it).await()
                    if (response.msg == app.getString(R.string.user_response_ok)) {
                        User.postCurrentUser(response.user!!)
                        message.postValue(app.getString(R.string.modify_success))
                        modifySuccess.postValue(true)
                    } else {
                        throw Exception(response.msg)
                    }
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


    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    fun onModifySuccess() {
        modifySuccess.value = false
    }

    val registerSuccess = MutableLiveData<Boolean>()

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserInfoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserInfoViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
