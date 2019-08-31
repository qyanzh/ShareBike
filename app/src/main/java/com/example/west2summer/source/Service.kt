package com.example.west2summer.source

import com.example.west2summer.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Json
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
    fun loginAsync(@Field("id") id: Long, @Field("password") password: String): Deferred<NetworkResponse>

    /* 个人信息*/
    @GET("api/query/ev_user/id/{id}")
    fun getUserInfoAsync(@Path("id") id: Long): Deferred<NetworkUser>

    /* 更新信息 */
    @POST("api/ev_user/update")
    fun updateUserAsync(@Body user: User): Deferred<NetworkUser>

    /* 修改密码*/
    @FormUrlEncoded
    @POST("api/ev_user/password/update")
    fun updatePasswordAsync(
        @Field("id") id: Long,
        @Field("password") password: String
    ): Deferred<NetworkResponse>

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
    fun deleteBikeAsync(@Field("id") id: Long): Deferred<NetworkResponse>

    /* 全部车辆*/
    @GET("api/query/ev_bike/all")
    fun getAllBikesAsync(): Deferred<NetworkBikeList>

    /* 我上传的车辆*/
    @GET("api/query/ev_bike/owner_id/{owner_id}")
    fun getMyBikesAsync(@Path("owner_id") ownerId: Long): Deferred<NetworkBikeList>

    /* 更新租借状态*/
    @FormUrlEncoded
    @POST("api/ev_bike/lease_status/update")
    fun updateLeaseStatusAsync(
        @Field("id") id: Long,
        @Field("lease_status") leaseState: Int
    ): Deferred<ResponseBody>

    /* ------流动信息------*/

    /* 根据id查询流动记录*/
    @GET("api/query/ev_record/id/{id}")
    fun getRecordByIdAsync(@Path("id") id: Long): Deferred<NetworkRecord>

    /* 根据车主获取流动记录*/
    @GET("api/query/ev_record/owner_id/{owner_id}")
    fun getRecordByOwnerIdAsync(@Path("owner_id") ownerId: Long): Deferred<NetworkRecordList>

    /* 根据使用者获取流动记录*/
    @GET("api/query/ev_record/user_id/{user_id}")
    fun getRecordByUserIdAsync(@Path("user_id") userId: Long): Deferred<NetworkRecordList>

    /* 根据车辆获取流动记录*/
    @GET("api/query/ev_record/bike_id/{bike_id}")
    fun getRecordByBikeIdAsync(@Path("bike_id") bikeId: Long): Deferred<NetworkRecordList>

    /* 添加流动记录（想要）*/
    @POST("api/ev_record/insert")
    fun sendLikeRequestAsync(@Body record: OrderRecord): Deferred<NetworkResponse>

    /* 删除流动记录 （取消想要）*/
    @FormUrlEncoded
    @POST("api/ev_record/delete")
    fun deleteRecordAsync(@Field("id") id: Long): Deferred<NetworkResponse>

    /* 确认租出*/
    @FormUrlEncoded
    @POST("api/ev_record/start_use/update")
    fun startRentAsync(@Field("id") recordId: Long, @Field("bike_id") bikeId: Long): Deferred<NetworkResponse>

    /* 结束租借*/
    @FormUrlEncoded
    @POST("api/ev_record/finish_use/update")
    fun endRentAsync(@Field("id") recordId: Long, @Field("bike_id") bikeId: Long): Deferred<NetworkResponse>


    /* ------图片上传------*/

    @Multipart
    @POST("upload/setFileUpload")
    fun uploadFileAsync(
        @Part("id") id: Long,
        @Part file: MultipartBody.Part
    ): Deferred<ResponseBody>


    /* ------开发------*/

    @FormUrlEncoded
    @POST("api/ev_user/delete")
    fun deleteUserAsync(@Field("id") id: Long): Deferred<NetworkResponse>

    @GET("api/query/ev_record/all")
    fun getAllOrderRecordAsync(): Deferred<NetworkRecordList>

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
    val bikes: List<BikeInfo>,
    val ok: String?
)

data class NetworkUser(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val user: User?,
    val ok: String?
)

data class NetworkRecordList(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val records: List<OrderRecord>,
    val ok: String?
)

data class NetworkRecord(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val record: OrderRecord,
    val ok: String?
)

data class NetworkBikeId(
    val status: Int?,
    val msg: String?,//OK
    @field:Json(name = "data")
    val id: Long?,
    val ok: String?
)

class IdentifyErrorException : Exception()
class RegisteredException : Exception()
class RentedException : Exception()
class UsingException : Exception()
class UseCompletedException : Exception()
class WrongPasswordException : Exception()