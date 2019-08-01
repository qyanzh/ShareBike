package com.example.west2summer.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BikeInfoDao {
    @Query("select * from bikeinfo")
    fun getAll(): LiveData<List<BikeInfo>>

    @Query("select * from bikeinfo where infoId= :infoId")
    fun get(infoId: Long): LiveData<BikeInfo>

    @Query("delete from bikeinfo")
    fun deleteAll()

    @Delete
    fun delete(bikeInfo: BikeInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(bikeinfos: List<BikeInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bikeinfos: BikeInfo)
}

@Database(entities = [BikeInfo::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract val bikeInfoDao: BikeInfoDao
}

@Volatile
private lateinit var INSTANCE: MyDatabase

fun getDatabase(context: Context): MyDatabase {
    synchronized(Database::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                MyDatabase::class.java,
                "bikeinfos"
            ).build()
        }
    }
    return INSTANCE
}