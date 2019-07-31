package com.example.west2summer.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class BikeInfo(
    val ownerId: Long,//车主ID，非空
    @PrimaryKey
    var infoId: Long? = null,//条目ID，非空
    var latitude: Double? = null,//经度，非空
    var longitude: Double? = null,//纬度，非空
    var place:String? = null,//名称
    var battery: Double? = null,//电池剩余km数
    var availableFrom: Long? = null,//可用时间
    var availableTo: Long? = null,//截止时间
    var price: Double? = null,//价格
    var note: String? = null//备注
) : Parcelable {
}