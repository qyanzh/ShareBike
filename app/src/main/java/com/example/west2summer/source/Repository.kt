package com.example.west2summer.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.west2summer.component.notifyObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: MyDatabase) {


    val orderRecords by lazy {
        database.orderRecordDao.getAll(User.currentUser.value!!.id)
    }

    companion object {
        private val _bikeList = MutableLiveData<MutableList<BikeInfo?>>()
        val bikeList: LiveData<MutableList<BikeInfo?>>
            get() = _bikeList

        private fun addBikeToList(bikeInfo: BikeInfo) {
            _bikeList.value?.add(bikeInfo)
            _bikeList.notifyObserver()
        }

        private fun deleteBikeFromList(index: Int) {
            _bikeList.value?.indices?.let { range ->
                if (index in range) {
                    _bikeList.value!!.removeAt(index)
                    _bikeList.notifyObserver()
                }
            }
        }

        private fun modifyBikeFromList(index: Int, bikeInfo: BikeInfo) {
            _bikeList.value?.indices?.let { range ->
                if (index in range) {
                    _bikeList.value!![index] = bikeInfo
                    _bikeList.notifyObserver()
                }
            }
        }

        suspend fun refreshBikeList() {
            withContext(Dispatchers.IO) {
                try {
                    _bikeList.postValue(Network.service.getAllBikesAsync().await().bikes.toMutableList())
                } catch (e: Exception) {
                    Log.d(
                        "Repository", "refreshBikeList: " +
                                "$e"
                    )
                }
            }
        }

        suspend fun insertBike(bikeInfo: BikeInfo): Boolean {
            var result = false
            withContext(Dispatchers.IO) {
                val response =
                    Network.service.insertBikeAsync(bikeInfo).await()
                if (response.msg == "OK") {
                    bikeInfo.id = response.id
                    addBikeToList(bikeInfo)
                    result = true
                }
            }
            return result
        }

        suspend fun modifyBike(index: Int, bikeInfo: BikeInfo): Boolean {
            var result = false
            withContext(Dispatchers.IO) {
                val response =
                    Network.service.updateBikeAsync(bikeInfo).await()
                if (response.msg == "OK") {
                    modifyBikeFromList(index, bikeInfo)
                    result = true
                }
            }
            return result
        }

        suspend fun deleteBike(index: Int): Boolean {
            var result = false
            withContext(Dispatchers.IO) {
                _bikeList.value?.indices?.let {
                    if (index in it) {
                        val id = _bikeList.value!![index]!!.id
                        val response =
                            Network.service.deleteBikeAsync(id!!).await()
                        if (response.msg == "success") {
                            deleteBikeFromList(index)
                            result = true
                        }
                    }
                }
            }
            return result
        }

    }


}