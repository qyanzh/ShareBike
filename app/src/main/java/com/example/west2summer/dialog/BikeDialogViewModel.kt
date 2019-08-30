package com.example.west2summer.dialog

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.LikeState
import com.example.west2summer.source.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class BikeDialogViewModel(
    val app: Application,
    val bikeInfo: BikeInfo
) : AndroidViewModel(app) {

    var choseRecordId = -1L

    fun setChoice(recordId: Long) {
        choseRecordId = recordId
    }

    fun clearChoice() {
        choseRecordId = -1
    }

    fun onConfirmRentClicked() {
        if (choseRecordId != -1L) {
            uiScope.launch {
                try {
                    Repository.startRent(choseRecordId, bikeInfo.id)
                    refreshRecords()
                    _fabState.value = LikeState.DONE
                    message.value = app.getString(R.string.rent_out_success)
                } catch (e: Exception) {
                    message.value = when (e) {
                        is ConnectException -> app.getString(R.string.exam_network)
                        is RentedException -> app.getString(R.string.bike_rented)
                        else -> e.toString()
                    }
                }
            }
        }
    }

    fun onEndRentClicked() {
        records.value?.let {
            for (record in it) {
                if (record.isUsed == 1 && record.isFinished == 0) {
                    uiScope.launch {
                        try {
                            Repository.endRent(record.id, record.bikeId)
                            refreshRecords()
                            _fabState.value = LikeState.EDIT
                            message.value = app.getString(R.string.order_complete)
                        } catch (e: Exception) {
                            message.value = when (e) {
                                is ConnectException -> app.getString(R.string.exam_network)
                                else -> e.toString()
                            }
                        }
                    }
                    break
                }
            }
        }
    }

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    val owner = MutableLiveData<User>()


    val records = MutableLiveData<List<OrderRecord>?>().apply {
        value = listOf()
    }

    val activeRecords = Transformations.map(records) {
        it?.filter { order ->
            order.isFinished == 0
        } ?: listOf()
    }

    val recordsSize = Transformations.map(activeRecords) {
        activeRecords.value?.size ?: 0
    }

    var likeRecordId: Long = -1

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    private val _fabState = MutableLiveData<LikeState?>()

    val fabState: LiveData<LikeState?>
        get() = _fabState

    val shouldShowContact = Transformations.map(fabState) {
        when (it) {
            LikeState.NULL -> false
            LikeState.UNLIKE -> false
            else -> true
        }
    }

    init {
        uiScope.launch {
            refreshRecords()
            if (User.isLoginned()) {
                if (User.currentUser.value!!.id == bikeInfo.ownerId) {
                    owner.value = User.currentUser.value!!
                    if (bikeInfo.leaseStatus == 0) {
                        _fabState.value = LikeState.EDIT
                    } else {
                        _fabState.value = LikeState.DONE
                    }
                } else {
                    checkLike()
                }
            } else {
                _fabState.value = LikeState.NULL
            }
        }
    }

    private suspend fun refreshRecords() {
        try {
            records.value = Repository.getOrderRecords(bikeInfo.id)
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> e.toString()
            }
        }
    }

    private suspend fun checkLike() {
        try {
            if (User.isLoginned() && bikeInfo.ownerId != User.currentUser.value!!.id) {
                _fabState.value = LikeState.UNLIKE
                likeRecordId = -1
                owner.value = null
                for (orderRecord in records.value!!) {
                    if (orderRecord.userId == User.currentUser.value?.id
                        && orderRecord.isFinished == 0
                    ) {
                        _fabState.value = LikeState.LIKED
                        likeRecordId = orderRecord.id
                        owner.value = Repository.getUserInfo(bikeInfo.ownerId)
                        break
                    }
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
                    refreshRecords()
                    checkLike()
                    if (likeRecordId != -1L) {
                        message.value = app.getString(R.string.liked)
                    }
                } catch (e: Exception) {
                    message.value = when (e) {
                        is ConnectException -> app.getString(R.string.exam_network)
                        else -> e.toString()
                    }
                }
            }
        }
    }

    fun sendUndoLikeRequest() {
        uiScope.launch {
            if (User.isLoginned()) {
                try {
                    Repository.sendUnlikeRequest(likeRecordId, bikeInfo.id)
                    refreshRecords()
                    checkLike()
                    if (likeRecordId == -1L) {
                        message.value = app.getString(R.string.canceled)
                    }
                } catch (e: Exception) {
                    message.value = when (e) {
                        is ConnectException -> app.getString(R.string.exam_network)
                        is UsingException -> app.getString(R.string.using_uncancelable)
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