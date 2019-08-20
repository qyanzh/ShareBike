package com.example.west2summer.dialog

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.random.Random

class BikeInfoDialogViewModel(
    val application: Application,
    val bikeInfo: BikeInfo
) : ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val fabState = MutableLiveData<Int>().apply {
        value = 0
    }

    init {
        if (bikeInfo.ownerId == User.getCurrentUser().userId) {
            fabState.value = 2
        } else {
            //TODO: 询问服务器该用户是否想要这辆车
            fabState.value = Random.nextInt(0, 2)
        }
    }

    fun sendLikeRequest(): User {
        //TODO:向服务器发送想租请求，返回车主联系方式
        fabState.value = 1
        return User.getCurrentUser()
    }

    fun sendUndoLikeRequest() {
        fabState.value = 0
    }

    fun getUserContact(): User {
        //TODO:从数据库中拿出车主联系方式
        return User.getCurrentUser()
    }


    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeInfoDialogViewModel(app, bikeInfo) as T
        }
    }


}