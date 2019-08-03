package com.example.west2summer.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.model.CameraPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

//    private val bikeInfoRepository = BikeInfoRepository(getDatabase(application))

//    private val infoList = bikeInfoRepository.bikeinfos


    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        refreshDataFromRepository()
    }

    private fun refreshDataFromRepository() {
        uiScope.launch {
            try {
//                bikeInfoRepository.refreshBikeInfos()
            } catch (networkError: IOException) {
                Log.d(
                    "MapViewModel", "refreshDataFromRepository: " +
                            "network error"
                )
            }
        }
    }

    private val _navigateToAdd = MutableLiveData<Boolean>()
    val navigateToAdd: LiveData<Boolean>
        get() = _navigateToAdd

    fun onFabClicked() {
        _navigateToAdd.value = true
    }

    fun doneNavigating() {
        _navigateToAdd.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MapViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
