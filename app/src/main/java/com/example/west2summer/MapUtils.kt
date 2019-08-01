package com.example.west2summer

import android.content.Context
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.*
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun PoiSearch.await(): PoiResult = suspendCoroutine { cont ->
    setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
        override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}
        override fun onPoiSearched(result: PoiResult?, p1: Int) {
            result?.let {
                cont.resume(it)
            } ?: cont.resumeWithException(Exception("null PoiResult"))
        }

    })
    searchPOIAsyn()
}

suspend fun RegeocodeQuery.await(context: Context): RegeocodeResult = suspendCoroutine { cont ->
    GeocodeSearch(context).apply {
        setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {}
            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                result?.let {
                    cont.resume(it)
                } ?: cont.resumeWithException(Exception("null RegeocodeResult"))
            }
        })
        getFromLocationAsyn(this@await)
    }
}

suspend fun GeocodeQuery.await(context: Context): GeocodeResult = suspendCoroutine { cont ->
    GeocodeSearch(context).apply {
        setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
            override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {}
            override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {
                if (result != null && result.geocodeAddressList.isNotEmpty()) {
                    cont.resume(result)
                } else {
                    cont.resumeWithException(Exception("null GeocodeResult"))
                }
            }
        })
        getFromLocationNameAsyn(this@await)
    }
}

suspend fun Inputtips.await(): List<Tip> = suspendCoroutine { cont ->
    setInputtipsListener { result, code ->
        //        result?.let {
//            for (tip in it) {
//                Log.d(
//                    "TipQuery", " address:${tip.name}\n district:${tip.district}"
//                )
//            }
//        }
        result?.let {
            cont.resume(it)
        } ?: cont.resume(mutableListOf())
    }
    requestInputtipsAsyn()
}