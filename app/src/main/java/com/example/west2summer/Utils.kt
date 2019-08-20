package com.example.west2summer

import android.content.Context
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.*
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val southwestLatLng = LatLng(26.041229, 119.175761)
val northeastLatLng = LatLng(26.07757, 119.210339)

//经纬度转地点
suspend fun convertLatLngToPlace(context: Context, lat: Double, lng: Double): RegeocodeAddress =
    suspendCoroutine { cont ->
        GeocodeSearch(context).apply {
            setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
                override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {}
                override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {
                    result?.let {
                        cont.resume(it.regeocodeAddress)
                    } ?: cont.resumeWithException(Exception("null RegeocodeResult"))
                }
            })
            getFromLocationAsyn(
                RegeocodeQuery(
                    LatLonPoint(lat, lng),
                    200f, GeocodeSearch.AMAP
                )
            )
        }
    }


//地点转经纬度
suspend fun convertPlaceToAddress(context: Context, place: String): GeocodeAddress =
    suspendCoroutine { cont ->
        GeocodeSearch(context).apply {
            setOnGeocodeSearchListener(object : GeocodeSearch.OnGeocodeSearchListener {
                override fun onRegeocodeSearched(result: RegeocodeResult?, code: Int) {}
                override fun onGeocodeSearched(result: GeocodeResult?, code: Int) {
                    if (result != null && result.geocodeAddressList.isNotEmpty()) {

                        cont.resume(result.geocodeAddressList[0])
                    } else {
                        cont.resumeWithException(Exception("null GeocodeResult"))
                    }
                }
            })
            getFromLocationNameAsyn(
                GeocodeQuery(place, "fujian")
            )
        }
    }

//输入转输入建议
suspend fun getSuggestionTipsList(context: Context, input: String): List<Tip> =
    suspendCoroutine { cont ->
        with(Inputtips(context, InputtipsQuery(input, "fujian"))) {
            setInputtipsListener { result, _ ->
                result?.let {
                    cont.resume(it)
                } ?: cont.resume(mutableListOf())
            }
            requestInputtipsAsyn()
        }
    }

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