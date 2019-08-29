package com.example.west2summer.source

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.west2summer.component.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object Repository {

    private lateinit var database: MyDatabase
    private lateinit var spf: SharedPreferences

    fun init(app: Application) {
        database = getDatabase(app)
        spf = app.getSharedPreferences(SPF_FILE_NAME_USER, Context.MODE_PRIVATE)
    }

    val activeBikeList by lazy {
        database.bikeInfoDao.getActiveBikes()
    }

    val orderRecordList by lazy {
        database.orderRecordDao.getAll()
    }

    @Throws(Exception::class)
    suspend fun refreshBikeList() {
        val response = Network.service.getAllBikesAsync().await()
        if (response.msg == RESPONSE_OK) {
            val bikes = response.bikes
            withContext(Dispatchers.IO) {
                database.bikeInfoDao.deleteAll()
                database.bikeInfoDao.insertAll(bikes)
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun register(newUser: User) {
        val response = Network.service.registerAsync(newUser).await()
        when {
            response.msg == RESPONSE_OK -> login(newUser.id, newUser.password!!)
            response.msg == RESPONSE_SAME -> throw RegisteredException()
            else -> throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun login(id: Long, password: String) {
        val response =
            Network.service.loginAsync(id, password).await()
        when (response.msg) {
            RESPONSE_SUCCESS -> {
                User.postCurrentUser(getUserInfo(id))
                saveAccount(id, password)
            }
            RESPONSE_DEFEAT -> throw IdentifyErrorException()
            else -> throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun autoLogin() {
        if (User.currentUser.value == null
            && spf.contains(SPF_USER_ID)
            && spf.contains(SPF_USER_PASSWORD)
        ) {
            val id = spf.getLong(SPF_USER_ID, -1)
            val password = spf.getString(SPF_USER_PASSWORD, "")
            login(id, password!!)
        }
    }

    fun logout() {
        User.logout()
        spf.edit().remove(SPF_USER_ID).remove(SPF_USER_PASSWORD).apply()
    }

    private fun saveAccount(id: Long, password: String) {
        spf.edit().putLong(SPF_USER_ID, id)
            .putString(SPF_USER_PASSWORD, password)
            .apply()
    }

    @Throws(Exception::class)
    suspend fun getUserInfo(id: Long): User {
        val response =
            Network.service.getUserInfoAsync(id).await()
        if (response.msg == RESPONSE_OK) {
            return response.user!!
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun updateUserInfo(user: User) {
        val response = Network.service.updateUserAsync(user).await()
        if (response.msg == RESPONSE_OK) {
            User.postCurrentUser(response.user!!)
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun insertBikeInfo(bikeInfo: BikeInfo) {
        val response = Network.service.insertBikeAsync(bikeInfo).await()
        if (response.msg == RESPONSE_OK) {
            bikeInfo.id = response.id!!
            withContext(Dispatchers.IO) {
                database.bikeInfoDao.insert(bikeInfo)
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun updateBikeInfo(bikeInfo: BikeInfo) {
        val response = Network.service.updateBikeAsync(bikeInfo).await()
        if (response.msg == RESPONSE_OK) {
            withContext(Dispatchers.IO) {
                database.bikeInfoDao.insert(requireNotNull(response.bike))
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun deleteBikeInfo(bikeInfo: BikeInfo) {
        val response =
            Network.service.deleteBikeAsync(bikeInfo.id).await()
        if (response.msg == RESPONSE_SUCCESS) {
            withContext(Dispatchers.IO) {
                database.bikeInfoDao.delete(bikeInfo)
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun getBikeInfo(id: Long): BikeInfo {
        val response = Network.service.getBikeAsync(id).await()
        if (response.msg == RESPONSE_OK) {
            requireNotNull(response.bike).let {
                withContext(Dispatchers.IO) {
                    database.bikeInfoDao.insert(it)
                }
                return it
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun refreshOrderRecordList(id: Long) {
        val response1 = Network.service.getRecordByOwnerIdAsync(id).await()
        val response2 = Network.service.getRecordByUserIdAsync(id).await()
        if (response1.msg == RESPONSE_OK && response2.msg == RESPONSE_OK) {
            val records = response1.records + response2.records
            withContext(Dispatchers.IO) {
                database.orderRecordDao.deleteAll()
                database.orderRecordDao.insertAll(records)
            }
        } else {
            throw Exception(response1.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun getOrderRecords(id: Long): List<OrderRecord> {
        val response = Network.service.getRecordByBikeIdAsync(id).await()
        if (response.msg == RESPONSE_OK) {
            val records = response.records
            withContext(Dispatchers.IO) {
                database.orderRecordDao.insertAll(records)
            }
            return records
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun sendLikeRequest(bikeId: Long, ownerId: Long, userId: Long) {
        val orderRecord = OrderRecord(bikeId, ownerId, userId)
        val response = Network.service.sendLikeRequestAsync(orderRecord).await()
        if (response.msg == RESPONSE_SUCCESS) {
            withContext(Dispatchers.IO) {
                //TODO: 返回ID 添加到数据库
//                database.orderRecordDao.insert(orderRecord)
            }
        } else {
            throw Exception(response.msg)
        }
    }

    @Throws(Exception::class)
    suspend fun sendUnlikeRequest(id: Long) {
        val response = Network.service.deleteRecordAsync(id).await()
        if (response.msg == RESPONSE_SUCCESS) {
            withContext(Dispatchers.IO) {
                database.orderRecordDao.deleteById(id)
            }
        } else {
            throw Exception(response.msg)
        }
    }


}
