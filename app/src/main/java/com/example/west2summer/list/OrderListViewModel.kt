package com.example.west2summer.list

import android.app.Application
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException

class OrderListViewModel(val app: Application) : AndroidViewModel(app) {


    val records = Transformations.map(Repository.orderRecordList) {
        it?.filter { order ->
            order.ownerId != User.currentUser.value!!.id || order.isUsed == 1
        }
    }

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    private var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        uiScope.launch {
            try {
                Repository.refreshOrderRecordList(User.currentUser.value?.id!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getBikeInfo(id: Long): BikeInfo? {
        try {
            return Repository.getBikeInfo(id)
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> e.toString()
            }
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
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