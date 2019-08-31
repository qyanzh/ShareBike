package com.example.west2summer.list

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.*
import java.net.ConnectException

class OrderListViewModel(val app: Application) : AndroidViewModel(app) {

    private var job = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + job)

    val records = Transformations.map(Repository.orderRecordList) {
        val currentUserId = User.currentUser.value!!.id
        (it?.filter { order ->
            (order.ownerId == currentUserId && order.isUsed == 1) ||
                    (order.userId == currentUserId)
        } ?: listOf()).sortedByDescending { it.startTime }
    }

    val isBlank = Transformations.map(records) {
        it?.isEmpty()
    }

    init {
        uiScope.launch {
            try {
                refreshList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val isRefreshing = MutableLiveData<Boolean?>()

    fun refreshList() {
        uiScope.launch {
            try {
                isRefreshing.value = true
                Repository.refreshOrderRecordList(User.currentUser.value?.id!!)
            } catch (e: Exception) {
                message.value = app.getString(R.string.network_error)
            } finally {
                delay(800)
                isRefreshing.value = false
            }
        }
    }

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }


    suspend fun getBikeInfo(id: Long): BikeInfo? {
        try {
            return Repository.getBikeInfo(id)
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> app.getString(R.string.bike_deleted)
            }
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderListViewModel::class.java)) {
                return OrderListViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}