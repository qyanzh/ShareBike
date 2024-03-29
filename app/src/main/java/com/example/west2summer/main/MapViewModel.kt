package com.example.west2summer.main

import android.app.Application
import androidx.lifecycle.*
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.west2summer.R
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Repository
import kotlinx.coroutines.*
import kotlin.collections.set

class MapViewModel(
    val app: Application
) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val infoList = Repository.activeBikeList

    var markerMapping = Transformations.map(infoList) {
        val map = HashMap<MarkerOptions, BikeInfo>()
        infoList.value?.forEach { info ->
            info.let {
                val latLng = LatLng(info.lat, info.lng)
                val markerOptions = MarkerOptions().position(latLng)
                map[markerOptions] = info
            }
        }
        map
    }

    val isRefreshing = MutableLiveData<Boolean?>()

    fun refreshList() {
        uiScope.launch {
            try {
                isRefreshing.value = true
                Repository.refreshBikeList()
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
