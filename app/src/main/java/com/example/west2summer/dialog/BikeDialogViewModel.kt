package com.example.west2summer.dialog

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.LikeState
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.OrderRecord
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class BikeDialogViewModel(
    val app: Application,
    val bikeInfo: BikeInfo
) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val owner = MutableLiveData<User>()
    private lateinit var records: List<OrderRecord>
    var likeRecordId: Long = -1

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    private val _fabState = MutableLiveData<LikeState?>()

    val fabState: LiveData<LikeState?>
        get() = _fabState

    init {
        _fabState.value = LikeState.NULL
        if (User.isLoginned()) {
            if (User.currentUser.value!!.id == bikeInfo.ownerId) {
                owner.value = User.currentUser.value!!
                _fabState.value = LikeState.EDIT
            } else {
                uiScope.launch {
                    checkLike()
                }
            }
        }
    }

    private suspend fun checkLike() {
        try {
            records = Repository.getOrderRecords(bikeInfo.id)
            _fabState.value = LikeState.UNLIKE
            for (orderRecord in records) {
                if (orderRecord.userId == User.currentUser.value?.id) {
                    _fabState.value = LikeState.LIKED
                    likeRecordId = orderRecord.id
                    owner.value = Repository.getUserInfo(bikeInfo.ownerId)
                    break
                }
            }
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> e.toString()
            }
        }
    }


    fun sendLikeRequest() {
        uiScope.launch {
            if (User.isLoginned()) {
                try {
                    Repository.sendLikeRequest(
                        bikeInfo.id,
                        bikeInfo.ownerId,
                        User.currentUser.value!!.id
                    )
                    //TODO：返回ID初始化id
                    owner.value = Repository.getUserInfo(bikeInfo.ownerId)
                    message.value = app.getString(R.string.liked)
                    _fabState.value = LikeState.LIKED
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sendUndoLikeRequest() {
        uiScope.launch {
            if (User.isLoginned()) {
                try {
                    Repository.sendUnlikeRequest(likeRecordId)
                    owner.value = null
                    message.value = app.getString(R.string.canceled)
                    _fabState.value = LikeState.UNLIKE
                } catch (e: Exception) {
                    message.value = when (e) {
                        is ConnectException -> app.getString(R.string.exam_network)
                        else -> e.toString()
                    }
                }
            }
        }
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeDialogViewModel(app, bikeInfo) as T
        }
    }

}