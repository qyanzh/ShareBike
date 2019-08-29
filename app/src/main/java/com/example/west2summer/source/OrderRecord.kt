package com.example.west2summer.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.squareup.moshi.Json

@Entity(tableName = "order_record")
data class OrderRecord(
    @field:Json(name = "bike_id") val bikeId: Long,
    @field:Json(name = "owner_id") val ownerId: Long,
    @field:Json(name = "user_id") val userId: Long,
    @field:Json(name = "is_used")
    var isUsed: Int = 0,
    @field:Json(name = "is_finished")
    var isFinished: Int = 0,
    @field:Json(name = "start_time")
    var startTime: String? = null,
    @field:Json(name = "end_time")
    var endTime: String? = null,
    @PrimaryKey
    @field:Json(name = "id")
    var id: Long = -1
) {

    fun isUsing() = isUsed == 1 && isFinished == 0

//    fun formattedStartTime() = longTimeFormatter.format(startTime)
//    fun formattedEndTime() = longTimeFormatter.format(endTime)

}

@Dao
interface OrderRecordDao {

    @Query("select * from order_record")
    fun getAll(): LiveData<List<OrderRecord>?>

    @Query("select * from order_record where ownerId=:userId or userId=:userId")
    fun getAll(userId: Long): LiveData<List<OrderRecord>?>

    @Query("select * from order_record where userId=:userId and isUsed = 0 and isFinished = 0")
    fun getLiked(userId: Long): LiveData<List<OrderRecord>>

    @Query("select * from order_record where userId=:userId and isUsed = 1 or isFinished = 1")
    fun getOrder(userId: Long): LiveData<List<OrderRecord>>

    @Query("select * from order_record where userId=:userId and isUsed = 1 and isFinished = 0")
    fun getUsing(userId: Long): LiveData<List<OrderRecord>>

    @Query("select * from order_record where userId=:userId  and isFinished = 1")
    fun getFinished(userId: Long): LiveData<List<OrderRecord>>

    @Query("select * from order_record where id= :id")
    fun get(id: Long): OrderRecord

    @Query("delete from order_record")
    fun deleteAll()

    @Query("delete from order_record where id=:id")
    fun deleteById(id: Long)

    @Delete
    fun delete(record: OrderRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(orderRecords: List<OrderRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(orderRecord: OrderRecord)
}