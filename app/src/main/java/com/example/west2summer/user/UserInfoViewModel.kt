package com.example.west2summer.user

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        when {
            name.value.isNullOrBlank() -> message.value = app.getString(R.string.please_enter_name)
            address.value.isNullOrBlank() -> message.value =
                app.getString(R.string.please_enter_address)
            else -> uiScope.launch {
                modifyInfo()
            }
        }
    }

    private suspend fun modifyInfo() = try {
        User.currentUser.value?.copy(
            name = name.value,
            address = address.value,
            sex = sex.value,
            wechat = wechat.value,
            qq = qq.value,
            phone = phone.value
        )?.also {
            Repository.updateUserInfo(it)
            message.value = app.getString(R.string.modify_success)
            modifySuccess.value = true
        }
    } catch (e: Exception) {
        message.value =
            if (e is ConnectException)
                app.getString(R.string.exam_network)
            else
                e.toString()
    }


    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    fun onModifySuccess() {
        modifySuccess.value = false
    }

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
