package com.example.west2summer

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.west2summer.source.Service
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://uri.amap.com/marker")
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val bikeInfoService = retrofit.create(Service::class.java)





}
