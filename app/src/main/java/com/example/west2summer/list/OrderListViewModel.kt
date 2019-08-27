package com.example.west2summer.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.Repository
import com.example.west2summer.database.getDatabase
import com.example.west2summer.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class OrderListViewModel(app: Application) : AndroidViewModel(app) {

    //TODO: move to repository
    private val repository = Repository(getDatabase(app))
    val records = getDatabase(app).orderRecordDao.getAll(User.currentUser.value!!.id)

    private var viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    suspend fun getBikeInfo(id: Long): BikeInfo? {
        return repository.getBikeInfo(id)
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