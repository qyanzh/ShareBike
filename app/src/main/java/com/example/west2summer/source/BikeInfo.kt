package com.example.west2summer.source

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.west2summer.component.shortTimeFormatter
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "bike_info")
@Parcelize
data class BikeInfo(
    @field:Json(name = "latitude") val lat: Double,//经度，非空
    @field:Json(name = "longitude") val lng: Double,//纬度，非空
    @field:Json(name = "owner_id") var ownerId: Long? = null,//车主ID，非空
    @PrimaryKey
    @field:Json(name = "id") var id: Long? = null,//条目ID，非空
    @field:Json(name = "name") var title: String? = null,//标题
    @field:Json(name = "battery") var battery: String? = null,//电池剩余
    @field:Json(name = "available_time") var avaFrom: Long? = null,//可用时间
    @field:Json(name = "blocking_time") var avaTo: Long? = null,//截止时间
    @field:Json(name = "price") var price: Double? = null,//价格
    @field:Json(name = "note") var note: String? = null,//备注
    @field:Json(name = "lease_status") var leaseStatus: Int = 0//租借状态
) : Parcelable {
    private fun avaFromString(): String =
        shortTimeFormatter.format(avaFrom)

    private fun avaToString(): String =
        shortTimeFormatter.format(avaTo)

    fun timeString(): String? {
        if (avaFrom != null && avaTo != null) {
            return "${avaFromString()} ~ ${avaToString()}"
        } else if (avaFrom != null) {
            return "${avaFromString()} 起"
        } else if (avaTo != null) {
            return "截至 ${avaToString()}"
        } else return null
    }

    fun priceString() = "￥" + price.toString()

    fun shouldShowTime() = (avaFrom != null || avaTo != null)

}

@Dao
interface BikeInfoDao {
    @Query("select * from bike_info")
    fun getAll(): LiveData<List<BikeInfo>>

    @Query("select * from bike_info where id= :id")
    fun get(id: Long): BikeInfo?

    @Query("delete from bike_info")
    fun deleteAll()

    @Delete
    fun delete(bikeInfo: BikeInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bikeInfos: List<BikeInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bikeInfo: BikeInfo)
}