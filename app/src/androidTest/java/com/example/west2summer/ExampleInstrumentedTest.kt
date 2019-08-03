package com.example.west2summer

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.base.MainThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.BikeInfoDao
import com.example.west2summer.database.MyDatabase
import com.example.west2summer.database.getDatabase
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito.mock
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var dao: BikeInfoDao
    private lateinit var db: MyDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, MyDatabase::class.java).build()
        dao = db.bikeInfoDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() {
        val bikeInfo = BikeInfo(1,2)
        val byName = dao.getAll()
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))

        for(i in 1..10) {
            dao.insert(BikeInfo(i.toLong(),i.toLong()))
        }
    }

}
