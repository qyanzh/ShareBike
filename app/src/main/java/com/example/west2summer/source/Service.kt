package com.example.west2summer.source

import com.example.west2summer.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Json
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface Service {

    /* ------用户------*/

    /* 注册 */
    @POST("api/ev_user/register")
    fun registerAsync(@Body user: User): Deferred<NetworkUser>

    /* 登录 */
    @FormUrlEncoded
    @POST("api/ev_user/login")
    fun loginAsync(@Field("id") id: Long, @Field("password") password: String): Deferred<BasicResponse>

    /* 个人信息*/
    @GET("api/query/ev_user/id/{id}")
    fun getUserInfoAsync(@Path("id") id: Long): Deferred<NetworkUser>

    /* 更新信息 */
    @POST("api/ev_user/update")
    fun updateUserAsync(@Body user: User): Deferred<NetworkUser>

    /* ------车辆------*/

    /* 插入车辆*/
    @POST("api/ev_bike/insert")
    fun insertBikeAsync(@Body bike: BikeInfo): Deferred<NetworkBikeId>

    /* 修改车辆*/
    @POST("api/ev_bike/update")
    fun updateBikeAsync(@Body bike: BikeInfo): Deferred<NetworkBike>

    /* 车辆信息*/
    @GET("api/query/ev_bike/id/{id}")
    fun getBikeAsync(@Path("id") id: Long): Deferred<NetworkBike>

    /* 删除车辆 */
    @FormUrlEncoded
    @POST("api/ev_bike/delete")
    fun deleteBikeAsync(@Field("id") id: Long): Deferred<BasicResponse>

    /* 全部车辆*/
    @GET("api/query/ev_bike/all")
    fun getAllBikesAsync(): Deferred<NetworkBikeList>


    /* ------开发------*/

    @FormUrlEncoded
    @POST("api/ev_user/delete")
    fun deleteUserAsync(@Field("id") id: Long): Deferred<BasicResponse>

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

data class BasicResponse(
    val status: Int?,
    val msg: String?,//Success
    val data: String?,
    val ok: String?
)

data class NetworkBike(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val bike: BikeInfo?,
    val ok: String?
)

data class NetworkBikeList(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val bikes: List<BikeInfo?>,
    val ok: String?
)

data class NetworkUser(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val user: User?,
    val ok: String?
)

data class NetworkBikeId(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val id: Long?,
    val ok: String?
)

