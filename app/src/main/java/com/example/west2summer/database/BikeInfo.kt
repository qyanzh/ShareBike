package com.example.west2summer.database

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.west2summer.component.shortTimeFormatter
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "bike_info")
@Parcelize
data class BikeInfo(
    val lat: Double,//经度，非空
    val lng: Double,//纬度，非空
    var ownerId: Long? = null,//车主ID，非空
    @PrimaryKey
    var bikeId: Long? = null,//条目ID，非空
    var title: String? = null,//标题
    var battery: Double? = null,//电池剩余km数
    var avaFrom: Long? = null,//可用时间
    var avaTo: Long? = null,//截止时间
    var price: Double? = null,//价格
    var note: String? = null,//备注
    var leaseStatus: Boolean = false//租借状态
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

    fun batteryString() = battery.toString() + "km"

    fun priceString() = "￥" + price.toString()

    fun shouldShowTime() = (avaFrom != null || avaTo != null)

}

@Dao
interface BikeInfoDao {
    @Query("select * from bike_info")
    fun getAll(): LiveData<List<BikeInfo>>

    @Query("select * from bike_info where bikeId= :bikeId")
    fun get(bikeId: Long): BikeInfo?

    @Query("delete from bike_info")
    fun deleteAll()

    @Delete
    fun delete(bikeInfo: BikeInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bikeInfos: List<BikeInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bikeInfo: BikeInfo)
}