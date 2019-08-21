package com.example.west2summer

import com.example.west2summer.database.BikeInfo
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
    fun md5test() {
        val a = "something just like this"
        println(toMD5(a))
    }


    @Test
    fun jsonToMapTest() {

        val moshi = Moshi.Builder().build()

        val adapter = moshi.adapter(BikeInfo::class.java)

        val bikeInfo = BikeInfo(123)

        bikeInfo.note=""
        bikeInfo.note = null

        val json = adapter.toJson(bikeInfo)

        println(json)

        println(adapter.fromJson(json))

    }

    @Test
    fun hash() {

    }


}

