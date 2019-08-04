package com.example.west2summer.edit

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.convertLatLngToPlace
import com.example.west2summer.convertPlaceToAddress
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.MyDatabase
import com.example.west2summer.database.getDatabase
import com.example.west2summer.getSuggestionTipsList
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
        } ?: ""
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
            bikeInfo.infoId ?: initBikeInfoId()
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
                bikeInfo.infoId = System.currentTimeMillis()
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
        var result: String? = null
        if (bikeInfo.latitude != null && bikeInfo.latitude != null) {
            withContext(Dispatchers.IO) {
                try {
                    result = convertLatLngToPlace(
                        getApplication(),
                        bikeInfo.latitude!!,
                        bikeInfo.longitude!!
                    ).formatAddress
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "网络异常", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return result
    }

    fun refreshPlaceSuggestion() {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                val tipList =
                    uiPlace.value?.let { getSuggestionTipsList(getApplication(), it) }
                withContext(Dispatchers.Main) {
                    if (tipList.isNullOrEmpty()) {
                        placeSuggestionsList.value = listOf()
                    } else {
                        placeSuggestionsList.value = tipList.map {
                            it.district + it.name
                        }
                    }
                }
            }
        }
    }

    var shouldOpenPicker = MutableLiveData<Int>()

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
                    if (uiPlace.value.isNullOrEmpty()) {
                        throw Exception("empty input")
                    }
                    val placeToFormattedResult =
                        convertPlaceToAddress(getApplication(), uiPlace.value!!)
                    bikeInfo.place = uiPlace.value
                    bikeInfo.latitude = placeToFormattedResult.latLonPoint.latitude
                    bikeInfo.longitude = placeToFormattedResult.latLonPoint.longitude
                    Log.d(
                        "EditBikeInfoViewModel", "onDoneMenuClicked: " +
                                "place have been convert to :${placeToFormattedResult.formatAddress}"
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

                if (uiBattery.value.isNullOrEmpty()) {
                    bikeInfo.battery = null
                } else {
                    bikeInfo.battery = uiBattery.value!!.toDouble()
                }

                if (uiPrice.value.isNullOrEmpty()) {
                    bikeInfo.price = null
                } else {
                    bikeInfo.price = uiPrice.value!!.toDouble()
                }

                if (uiNote.value.isNullOrEmpty()) {
                    bikeInfo.note = null
                } else {
                    bikeInfo.note = uiNote.value!!
                }

                bikeInfo.availableFrom = preuiFrom.value?.timeInMillis
                bikeInfo.availableTo = preuiTo.value?.timeInMillis

                //TODO: 上传到服务器
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
