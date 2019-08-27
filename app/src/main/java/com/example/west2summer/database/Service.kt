package com.example.west2summer.database

import com.example.west2summer.BuildConfig
import com.example.west2summer.user.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Json
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface Service {

    @FormUrlEncoded
    @POST("api/ev_user/login")
    fun login(@Field("id") id: Long, @Field("password") password: String): Deferred<NetworkResponse>

    @GET("api/query/ev_user/id/{id}")
    fun getUserInfo(@Path("id") id: Long): Deferred<NetworkUser>

    @POST("api/ev_user/register")
    fun register(@Body user: User): Deferred<NetworkUser>

    @FormUrlEncoded
    @POST("api/ev_user/delete")
    fun deleteUser(@Field("id") id: Long): Deferred<NetworkResponse>

    @GET("api/query/ev_user/all")
    fun getAllUser(): Deferred<NetworkResponse>

    @GET("api")
    fun getBikeInfoAsnc(): Deferred<BikeInfo?>

    @GET("api")
    fun getBikeInfoListAsnc(): Deferred<List<BikeInfo>>

    @POST("api")
    fun uploadBikeInfoAsync(@Body newBikeInfo: BikeInfo): Deferred<Response<Void>>

    @DELETE("api/{infoId}")
    fun deleteBikeInfoAsync(@Path("infoID") infoId: Long): Deferred<Response<Void>>

    @PUT("api/{infoId}")
    fun updateBikeInfoAsync(@Path(value = "infoID") infoId: Long): Deferred<Response<Void>>

    @GET("api")
    fun getOrderRecordListAsnc(): Deferred<List<OrderRecord>>

    @POST("api")
    fun uploadOrderRecordAsync(@Body newOrderRecord: OrderRecord): Deferred<Response<Void>>
}


object Network {

    private val clientBuilder = OkHttpClient.Builder()
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(this)
        }
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://47.95.3.253:8080/shared_ev/")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(clientBuilder.build())
        .build()
    val service = retrofit.create(Service::class.java)

}

data class NetworkResponse(
    val status: Int?,
    val msg: String?,
    val data: String?,
    val ok: String?
)

data class NetworkUser(
    val status: Int?,
    val msg: String?,
    @field:Json(name = "data")
    val user: User?,
    val ok: String?
)

