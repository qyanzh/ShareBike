package com.example.west2summer.database

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BikeInfoRepository(private val database: MyDatabase) {
    suspend fun refreshBikeInfos() {
        withContext(Dispatchers.IO) {
            Log.d(
                "BikeInfoRepository", "refreshBikeInfos: " +
                        "Called"
            )
            val list = BikeInfoNetwork.bikeInfoService.getBikeInfoListAsnc().await()
            database.bikeInfoDao.insertAll(list)
        }
    }

    val bikeinfos = database.bikeInfoDao.getAll()
}