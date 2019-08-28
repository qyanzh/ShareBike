package com.example.west2summer.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val database: MyDatabase) {

    val bikeInfos = database.bikeInfoDao.getAll()
    val orderRecords by lazy {
        database.orderRecordDao.getAll(User.currentUser.value!!.id)
    }

    suspend fun insertOrderRecord(order: OrderRecord) {
        withContext(Dispatchers.IO) {
            database.orderRecordDao.insert(order)
        }
    }

    suspend fun getBikeInfo(id: Long): BikeInfo? {
        var bikeInfo: BikeInfo? = null
//        withContext(Dispatchers.IO) {
//            bikeInfo = try {
//                Network.service.getBikeInfoAsnc().await()?.apply {
//                    database.bikeInfoDao.insert(this)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
        database.bikeInfoDao.get(id)
        return bikeInfo
    }
//
//    suspend fun refreshBikeInfos() {
//        withContext(Dispatchers.IO) {
//            val list = Network.service.getBikeInfoListAsnc().await()
//            database.bikeInfoDao.deleteAll()
//            database.bikeInfoDao.insertAll(list)
//        }
//    }

}