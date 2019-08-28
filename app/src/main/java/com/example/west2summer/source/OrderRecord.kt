package com.example.west2summer.source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.west2summer.component.longTimeFormatter

@Entity(tableName = "order_record")
data class OrderRecord(
    val bikeId: Long,
    val ownerId: Long,
    val userId: Long
) {
    var isUsed: Boolean = false
    var isFinished: Boolean = false
    var startTime: Long? = null
    var endTime: Long? = null
    @PrimaryKey
    var id: Long = -1

    fun isUsing() = isUsed && !isFinished

    fun formattedStartTime() = longTimeFormatter.format(startTime)
    fun formattedEndTime() = longTimeFormatter.format(endTime)

}

@Dao
interface OrderRecordDao {
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

    @Delete
    fun delete(orderRecords: OrderRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(orderRecords: List<OrderRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(orderRecord: OrderRecord)
}