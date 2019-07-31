package com.example.west2summer.edit

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeQuery
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.example.west2summer.R
import com.example.west2summer.await
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.BikeInfoNetwork
import com.example.west2summer.database.MyDatabase
import com.example.west2summer.database.getDatabase
import kotlinx.coroutines.*

class EditBikeInfoViewModel(
    application: Application,
    private val bikeInfo: BikeInfo
) : AndroidViewModel(application) {

    private lateinit var database: MyDatabase

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var mode: String
    val MODE_ADD = application.resources.getString(R.string.toolbar_add)
    val MODE_EDIT = application.resources.getString(R.string.toolbar_edit)

    val uiPlace = MutableLiveData<String?>()
    val uiBattery = MutableLiveData<String?>()
    val uiFrom = MutableLiveData<String?>()
    val uiTo = MutableLiveData<String?>()
    val uiPrice = MutableLiveData<String?>()
    val uiNote = MutableLiveData<String?>()
    val placeSuggestionsList = MutableLiveData<List<String>>()


    init {
        mode = when (bikeInfo.infoId) {
            null -> MODE_ADD
            else -> MODE_EDIT
        }
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database = getDatabase(application)
            }

//            bikeInfo.infoId ?: initBikeInfoId()
            uiPlace.value = bikeInfo.place ?: getInitUiPlace()
        }
    }


    private suspend fun initBikeInfoId() {
        withContext(Dispatchers.Default) {
            try {
                bikeInfo.infoId = BikeInfoNetwork.bikeInfoService.getNewInfoId().await()
                bikeInfo.infoId ?: throw Exception("null infoId")
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "获取infoId失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getInitUiPlace(): String? {
        if (bikeInfo.latitude != null && bikeInfo.latitude != null) {
            var regeocodeResult: RegeocodeResult? = null
            withContext(Dispatchers.IO) {
                try {
                    regeocodeResult = RegeocodeQuery(
                        LatLonPoint(bikeInfo.latitude!!, bikeInfo.longitude!!),
                        200f,
                        GeocodeSearch.AMAP
                    ).await(getApplication())
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "网络异常", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            return regeocodeResult?.regeocodeAddress?.formatAddress
        }
        return null
    }

    fun refreshPlaceSuggestion() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val tipList =
                    Inputtips(getApplication(), InputtipsQuery(uiPlace.value, "fujian")).await()
                if (tipList.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        placeSuggestionsList.value = tipList.map {
                            it.district + it.name
                        }
                    }
                }
            }
        }
    }

    fun onDoneMenuClicked() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val placeToFormattedResult =
                    GeocodeQuery(uiPlace.value, "fujian").await(getApplication()).geocodeAddressList
                if (placeToFormattedResult.isNotEmpty()) {
                    bikeInfo.place = uiPlace.value
                    bikeInfo.latitude = placeToFormattedResult[0].latLonPoint.latitude
                    bikeInfo.longitude = placeToFormattedResult[0].latLonPoint.longitude
                    Log.d(
                        "EditBikeInfoViewModel", "onDoneMenuClicked: " +
                                "place have been convert to ${placeToFormattedResult[0].formatAddress}"
                    )
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            (R.string.address_not_available_retry),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditBikeInfoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EditBikeInfoViewModel(app, bikeInfo) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}
