package com.example.west2summer.edit

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
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
import com.example.west2summer.database.MyDatabase
import com.example.west2summer.database.getDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class EditBikeInfoViewModel(
    application: Application,
    private val bikeInfo: BikeInfo
) : AndroidViewModel(application) {

    private lateinit var database: MyDatabase

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val timeFormatter by lazy { SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault()) }

    var mode: String
    val MODE_ADD = application.resources.getString(R.string.toolbar_add)
    val MODE_EDIT = application.resources.getString(R.string.toolbar_edit)

    val uiPlace = MutableLiveData<String?>()
    val uiBattery = MutableLiveData<String?>()
    val uiPrice = MutableLiveData<String?>()
    val uiNote = MutableLiveData<String?>()
    val placeSuggestionsList = MutableLiveData<List<String>>()

    val preuiFrom = MutableLiveData<Calendar?>()
    val uiFrom = Transformations.map(preuiFrom) {
        it?.let {
            timeFormatter.format(it.time)
        }
    }
    val preuiTo = MutableLiveData<Calendar?>()
    val uiTo = Transformations.map(preuiTo) {
        it?.let {
            timeFormatter.format(it.time)
        } ?: ""
    }


    init {
        mode = when (bikeInfo.infoId) {
            null -> MODE_ADD
            else -> MODE_EDIT
        }
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database = getDatabase(application)
            }

//            TODO:从服务器获取infoId
//            bikeInfo.infoId ?: initBikeInfoId()
            uiPlace.value = bikeInfo.place ?: getInitUiPlace()
            with(bikeInfo) {
                battery?.let { uiBattery.value = it.toString() }
                availableFrom?.let {
                    preuiFrom.value = Calendar.getInstance().apply { timeInMillis = it }
                }
                availableTo?.let {
                    preuiTo.value = Calendar.getInstance().apply { timeInMillis = it }
                }
                price?.let { uiPrice.value = price.toString() }
                note?.let { uiNote.value = note }
            }
        }
    }


    private suspend fun initBikeInfoId() {
        withContext(Dispatchers.Default) {
            try {
//                bikeInfo.infoId = BikeInfoNetwork.bikeInfoService.getNewInfoId().await()
                bikeInfo.infoId ?: throw Exception("null infoId")
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "获取infoId失败", Toast.LENGTH_SHORT).show()
                }
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
                withContext(Dispatchers.Main) {
                    if (tipList.isNotEmpty()) {
                        placeSuggestionsList.value = tipList.map {
                            it.district + it.name
                        }
                    } else {
                        placeSuggestionsList.value = listOf()
                    }
                }
            }
        }
    }

    var shouldOpenPicker = MutableLiveData<Int>()//0 for nothing.

    fun onTimeClicked(mode: Int) {
        shouldOpenPicker.value = mode
    }

    fun onPickerShowed(c: Calendar?) {
        when (shouldOpenPicker.value) {
            1 -> preuiFrom.value = c
            2 -> preuiTo.value = c
        }
    }


    fun onDoneMenuClicked() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val placeToFormattedResult =
                        GeocodeQuery(
                            uiPlace.value,
                            "fujian"
                        ).await(getApplication()).geocodeAddressList[0]
                    bikeInfo.place = uiPlace.value
                    bikeInfo.latitude = placeToFormattedResult.latLonPoint.latitude
                    bikeInfo.longitude = placeToFormattedResult.latLonPoint.longitude
                    Log.d(
                        "EditBikeInfoViewModel", "onDoneMenuClicked: " +
                                "place have been convert to ${placeToFormattedResult.formatAddress}"
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            (R.string.address_not_available_retry),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (!uiBattery.value.isNullOrEmpty()) {
                    bikeInfo.battery = uiBattery.value!!.toDouble()
                }
                if (!uiPrice.value.isNullOrEmpty()) {
                    bikeInfo.battery = uiPrice.value!!.toDouble()
                }
                if (!uiNote.value.isNullOrEmpty()) {
                    bikeInfo.note = uiNote.value!!
                }
                preuiFrom.value?.let { bikeInfo.availableFrom = it.timeInMillis }
                preuiTo.value?.let { bikeInfo.availableTo = it.timeInMillis }
                //TODO: 上传到服务器
                bikeInfo.infoId = System.currentTimeMillis()
                database.bikeInfoDao.insert(bikeInfo)

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditBikeInfoViewModel(app, bikeInfo) as T
        }
    }

}
