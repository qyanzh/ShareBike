package com.example.west2summer.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.west2summer.source.Repository
import com.example.west2summer.source.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.collections.set

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = Repository(getDatabase(application))

    val infoList = Repository.bikeList

    var markerMapping = Transformations.map(infoList) {
        val map = HashMap<MarkerOptions, Int>()
        infoList.value?.let {
            for ((index, info) in infoList.value!!.withIndex()) {
                info?.let {
                    val latLng = LatLng(info.lat, info.lng)
                    val markerOptions = MarkerOptions().position(latLng)
                    map[markerOptions] = index
                }
            }
        }
        map
    }

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    init {
        refreshDataFromRepository()
    }

    private fun refreshDataFromRepository() {
        uiScope.launch {
            try {
//                repository.fakeRefreshBikeInfos(getApplication())
            } catch (networkError: IOException) {
                Log.d(
                    "MapViewModel", "refreshDataFromRepository: " +
                            "network error"
                )
            }
        }
    }

    private val _fabStatus = MutableLiveData<Boolean>().apply {
        value = false
    }

    val centerMarkerIsVisible: LiveData<Boolean>
        get() = _fabStatus

    fun onFabClicked() {
        _fabStatus.value = !(_fabStatus.value!!)
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
