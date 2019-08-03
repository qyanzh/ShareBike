package com.example.west2summer.database

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.example.west2summer.convertLatLngToPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import java.util.*
import kotlin.random.Random

class BikeInfoRepository(private val database: MyDatabase) {

    val bikeInfos = database.bikeInfoDao.getAll()

    suspend fun refreshBikeInfos() {
        withContext(Dispatchers.IO) {
            Log.d(
                "BikeInfoRepository", "refreshBikeInfos: " +
                        "Called"
            )
            val list = BikeInfoNetwork.bikeInfoService.getBikeInfoListAsnc().await()
            database.bikeInfoDao.deleteAll()
            database.bikeInfoDao.insertAll(list)
        }
    }

    @TestOnly
    suspend fun fakeRefreshBikeInfos(context: Context) {
        withContext(Dispatchers.IO) {
            Log.d(
                "BikeInfoRepository", "fakeRefreshBikeInfos: " +
                        "Called"
            )
            val list = fakeApiRequest(context)
            database.bikeInfoDao.deleteAll()
            database.bikeInfoDao.insertAll(list)

        }
    }

    private suspend fun fakeApiRequest(context: Context):List<BikeInfo> {
        val list = mutableListOf<BikeInfo>()
        val c = Calendar.getInstance()
        val now = c.timeInMillis
        c.add(Calendar.MONTH,1)
        val end = c.timeInMillis
        for (i in 1..2) {
            val lat = Random.nextDouble(25.704998, 25.720367)
            val lng = Random.nextDouble(
                119.372895,
                119.38431
            )
            val battery = Random.nextDouble(1.toDouble(), 100.toDouble())
            val place = convertLatLngToPlace(context, lat, lng).formatAddress
            val from = Random.nextLong(now, end)
            val to = Random.nextLong(from, end)
            val price = Random.nextDouble(0.toDouble(), 30.toDouble())
            val bikeInfo = BikeInfo(123, now+i, lat, lng, place, battery, from, to, price)
            list.add(bikeInfo)
        }
        return list
    }

}