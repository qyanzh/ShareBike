package com.example.west2summer.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.BikeInfoRepository
import com.example.west2summer.database.getDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val bikeInfoRepository = BikeInfoRepository(getDatabase(application))

    val infoList = bikeInfoRepository.bikeInfos

    var markerMapping = Transformations.map(infoList) {
        val map = HashMap<MarkerOptions,BikeInfo>()
        infoList.value?.let {
            for(info in infoList.value!!) {
                val latLng = LatLng(info.latitude!!,info.longitude!!)
                val markerOptions = MarkerOptions().position(latLng)
                map[markerOptions] = info
                Log.d(
                    "MapViewModel", "refreshMarkersAndBikeInfos: " +
                            "added marker$markerOptions"
                )
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
                bikeInfoRepository.fakeRefreshBikeInfos(getApplication())
            } catch (networkError: IOException) {
                Log.d(
                    "MapViewModel", "refreshDataFromRepository: " +
                            "network error"
                )
            }
        }
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
