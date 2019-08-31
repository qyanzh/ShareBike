package com.example.west2summer.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.R
import com.example.west2summer.component.isValidPassword
import com.example.west2summer.component.toMD5
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import com.example.west2summer.source.WrongPasswordException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class UpdatePasswordDialogViewModel(val app: Application) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val originPassword = MutableLiveData<String>()
    val newPassword = MutableLiveData<String>()
    val confirmPassword = MutableLiveData<String>()

    val modifySuccess = MutableLiveData<Boolean?>()

    init {
        originPassword.value = ""
        newPassword.value = ""
        confirmPassword.value = ""
    }

    fun onConfirmClicked() {
        when {
            originPassword.value.isNullOrBlank() -> message.value =
                app.getString(R.string.please_enter_original_password)
            originPassword.value.toMD5() != User.currentUser.value?.password!! -> message.value =
                app.getString(R.string.wrong_password)
            newPassword.value.isNullOrBlank() -> message.value =
                app.getString(R.string.please_enter_new_password)
            confirmPassword.value.isNullOrBlank() -> message.value =
                app.getString(R.string.please_enter_confirm_password)
            confirmPassword.value != newPassword.value -> message.value =
                app.getString(R.string.new_not_equal_confirm)
            !newPassword.value.isValidPassword() -> message.value =
                app.getString(R.string.password_length_longer_then_six)
            else -> {
                uiScope.launch {
                    modifyPassword()
                }
            }
        }
    }

    private suspend fun modifyPassword() {
        try {
            Repository.updatePassword(User.currentUser.value?.id!!, newPassword.value!!)
            message.value = app.getString(R.string.modify_success)
            modifySuccess.value = true
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                is WrongPasswordException -> app.getString(R.string.wrong_password_try_again)
                else -> e.toString()
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

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UpdatePasswordDialogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UpdatePasswordDialogViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
