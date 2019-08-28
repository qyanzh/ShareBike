package com.example.west2summer.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Network
import com.example.west2summer.source.User
import com.example.west2summer.source.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class OrderListViewModel(app: Application) : AndroidViewModel(app) {

    val records = getDatabase(app).orderRecordDao.getAll(User.currentUser.value!!.id)

    private var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    suspend fun getBikeInfo(id: Long): BikeInfo? {
        return Network.service.getBikeAsync(id).await().bike
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