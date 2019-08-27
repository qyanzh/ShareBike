package com.example.west2summer

import com.example.west2summer.component.toMD5
import com.example.west2summer.database.BikeInfo
import com.example.west2summer.user.User
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

        val bikeAdapter = moshi.adapter(BikeInfo::class.java)


        val bikeInfo = BikeInfo(123.0, 123.0)

        bikeInfo.note=""
        bikeInfo.note = null

        val json = bikeAdapter.toJson(bikeInfo)

        println(json)

        println(bikeAdapter.fromJson(json))

    }

    @Test
    fun userJson() {

        val moshi = Moshi.Builder().build()

        val userAdapter = moshi.adapter(User::class.java)


        val user = User(1L, "pass", "zhang", 1, "add", wechat = "123")

        val json = userAdapter.toJson(user)

        println(json)

        println(userAdapter.fromJson(json))
    }

    @Test
    fun hash() {
        print(toMD5("221701412"))
    }


}

