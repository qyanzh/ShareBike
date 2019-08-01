package com.example.west2summer.dialog

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.west2summer.database.BikeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class BikeInfoDialogViewModel(
    val application: Application,
    private val bikeInfo: BikeInfo
) : ViewModel() {

    val detailText = "dummy"
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    init {

    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeInfoDialogViewModel(app, bikeInfo) as T
        }
    }


}