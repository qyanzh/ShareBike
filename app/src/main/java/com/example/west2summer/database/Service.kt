package com.example.west2summer.database

import com.example.west2summer.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface BikeInfoService {

    @GET("api")
    fun getNewInfoId(): Deferred<Long?>

    @GET("api")
    fun getBikeInfoListAsnc(): Deferred<List<BikeInfo>>

    @POST("api")
    fun uploadBikeInfoAsync(@Body newBikeInfo: BikeInfo): Deferred<Response<Void>>

    @DELETE("api/{infoId}")
    fun deleteBikeInfoAsync(@Path("infoID") infoId: Long): Deferred<Response<Void>>

    @PUT("api/{infoId}")
    fun updateBikeInfoAsync(@Path(value = "infoID") infoId: Long): Deferred<Response<Void>>

}

fun fakeLogin(username: Long, password: String): Boolean {
    if (password.isBlank()) {
        throw Exception("账号密码错误")
    }
    User.setCurrentUser(User(username, password))
    return true
}

object BikeInfoNetwork {

    private val retrofit = Retrofit.Builder()
        .baseUrl("url")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val bikeInfoService = retrofit.create(BikeInfoService::class.java)

}