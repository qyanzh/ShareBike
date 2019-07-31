package com.example.west2summer

import androidx.lifecycle.MutableLiveData
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.database.getDatabase
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun jsonToMapTest() {

    }

    class TestBike {
        var a = MutableLiveData<String>().apply {
            value="!23"
        }

    }


}

