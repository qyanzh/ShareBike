package com.example.west2summer

import com.example.west2summer.component.toMD5
import com.example.west2summer.source.NetworkUser
import com.squareup.moshi.Moshi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

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


//    @Test
//    fun jsonToMapTest() {
//
//        val moshi = Moshi.Builder().build()
//
//        val bikeAdapter = moshi.adapter(BikeInfo::class.java)
//
//
//        val bikeInfo = BikeInfo(123.0, 123.0)
//
//        bikeInfo.note=""
//        bikeInfo.note = null
//
//        val json = bikeAdapter.toJson(bikeInfo)
//
//        println(json)
//
//        println(bikeAdapter.fromJson(json))
//
//    }

    @Test
    fun userJson() {

        val moshi = Moshi.Builder().build()

        val userAdapter = moshi.adapter(NetworkUser::class.java)

        val test = NetworkUser(0, "", null, "")


        val json = """
            {"msg":"","ok":"","status":0,"data":null}
            """.trimIndent()

        println(json)

        println(userAdapter.fromJson(json))
    }

    @Test
    fun hash() {
        print(toMD5("221701412"))
    }


    data class User(var id: Long)

    @Test
    fun live() {
        val data = User(123L)
        val copy = data.copy()
        print(data === copy)

        val data2 = User(123L)
        val copy2 = data.copy(456L)
        print(data2.hashCode() == copy2.hashCode())
    }

    @Test
    fun time() {
        val formatter = SimpleDateFormat("yy/MM/dd hh:mm", Locale.getDefault())

        val calendar = Calendar.getInstance()
        println("Calendar : ${calendar.timeInMillis}") // Java : calendar.getTimeInMillis()
        println(formatter.format(calendar.timeInMillis))

        val date = Date()
        println("date : ${date.time}")
        println(formatter.format(date))
    }

    fun throwfun() {
        try {
            throw Exception("try")
        } catch (e: Exception) {
            throw Exception("catch")
        }
    }

    @Test
    fun test() {
        println(String.format("%d", 123L))
    }

    @Test
    fun main() {
        GlobalScope.launch {
            // launch a new coroutine in background and continue
            test(3)
            test(1)
            test(2)
        }
        println("Hello,") // main thread continues while coroutine is delayed
        Thread.sleep(8000L) // block main thread for 2 seconds to keep JVM alive
    }

    suspend fun test(id: Int) {
        val involkTime = System.currentTimeMillis()
        delay(id * 1000L)
        println("test${id} : ${involkTime}")
    }

    @Test
    fun time2() {
        println(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(Int.MAX_VALUE))
        println(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(Int.MAX_VALUE))
        println(SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(Int.MAX_VALUE))
    }
}