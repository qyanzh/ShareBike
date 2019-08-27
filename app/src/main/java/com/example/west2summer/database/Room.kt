package com.example.west2summer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BikeInfo::class, OrderRecord::class], version = 1)
abstract class MyDatabase : RoomDatabase() {
    abstract val bikeInfoDao: BikeInfoDao
    abstract val orderRecordDao: OrderRecordDao
}

@Volatile
private lateinit var INSTANCE: MyDatabase

fun getDatabase(context: Context): MyDatabase {
    synchronized(Database::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                MyDatabase::class.java,
                "bike-db"
            ).build()
        }
    }
    return INSTANCE
}