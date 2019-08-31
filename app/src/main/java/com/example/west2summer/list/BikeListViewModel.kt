package com.example.west2summer.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.R
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.*

class BikeListViewModel(val app: Application) : AndroidViewModel(app) {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

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

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    fun refreshList() {
        uiScope.launch {
            try {
                isRefreshing.value = true
                Repository.refreshMyBikes(User.currentUser.value?.id!!)
            } catch (e: Exception) {
                message.value = app.getString(R.string.network_error)
            } finally {
                delay(800)
                isRefreshing.value = false
            }
        }
    }

    val bikes = Repository.myBikeList(User.currentUser.value?.id!!)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BikeListViewModel::class.java)) {
                return BikeListViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}