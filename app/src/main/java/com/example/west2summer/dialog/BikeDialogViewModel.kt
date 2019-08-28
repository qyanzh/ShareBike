package com.example.west2summer.dialog

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.component.LikeFabState
import com.example.west2summer.source.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly

class BikeDialogViewModel(
    val application: Application,
    val bikeInfo: BikeInfo
) : ViewModel() {

    @TestOnly
    fun addRecord() {
        uiScope.launch {
            Log.d(
                "BikeDialogViewModel", "addRecord: " +
                        "${bikeInfo.bikeId}"
            )
            repository.insertOrderRecord(
                OrderRecord(
                    bikeInfo.bikeId!!,
                    bikeInfo.ownerId!!,
                    User.currentUser.value?.id!!
                ).apply {
                    id = System.currentTimeMillis()
                    isUsed = true
                    startTime = System.currentTimeMillis()
                    endTime = System.currentTimeMillis() + 12312414L
                })
        }
    }

    //TODO: move to repository
    private val repository = Repository(getDatabase(application))

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val owner = User.currentUser

    private val _fabState = MutableLiveData<LikeFabState?>()
    val fabState: LiveData<LikeFabState?>
        get() = _fabState

    init {
        if (User.isLoginned() && User.currentUser.value!!.id == bikeInfo.ownerId) {
            _fabState.value = LikeFabState.EDIT
        } else {
            //TODO: 询问服务器该用户是否想要这辆车
            _fabState.value = LikeFabState.LIKED
            _fabState.value = LikeFabState.UNLIKE
        }
    }

    fun sendLikeRequest() {
        //TODO:向服务器发送想租请求，返回车主联系方式,初始化ownner
        _fabState.value = LikeFabState.LIKED
        addRecord()
    }

    fun sendUndoLikeRequest() {
        _fabState.value = LikeFabState.UNLIKE
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeDialogViewModel(app, bikeInfo) as T
        }
    }

}