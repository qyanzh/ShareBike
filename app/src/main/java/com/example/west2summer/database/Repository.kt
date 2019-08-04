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
        for (i in 0..1) {
            val lat = Random.nextDouble(25.704998, 25.720367)
            val lng = Random.nextDouble(
                119.372895,
                119.38431
            )
            val battery = 3
            val place = convertLatLngToPlace(context, lat, lng).formatAddress
            val from = Random.nextLong(now, end)
            val to = Random.nextLong(from, end)
            val price = 50
            val bikeInfo = BikeInfo(123L+i, now+i, lat, lng, place, battery.toDouble(), from, to, price.toDouble())
            list.add(bikeInfo)
        }
        return list
    }

}