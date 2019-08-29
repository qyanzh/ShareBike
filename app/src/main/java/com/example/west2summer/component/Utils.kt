package com.example.west2summer.component

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.geocoder.*
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.bumptech.glide.Glide
import com.example.west2summer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


var shortTimeFormatter = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
var longTimeFormatter = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())

val defaultNavOptions = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .setEnterAnim(R.anim.nav_default_enter_anim)
    .setExitAnim(R.anim.nav_default_exit_anim)
    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
    .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build()

const val SPF_FILE_NAME_USER = "user"
const val SPF_USER_ID = "id"
const val SPF_USER_PASSWORD = "password"

const val RESPONSE_OK = "OK"
const val RESPONSE_SAME = "same"
const val RESPONSE_SUCCESS = "success"
const val RESPONSE_DEFEAT = "defeat"


fun <T> MutableLiveData<T>.notifyObserver() {
    this.postValue(this.value)
}

fun toast(context: Context, msg: String) {
    if (!msg.isBlank()) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}

suspend fun toastUiScope(context: Context, msg: String) {
    withContext(Dispatchers.Main) {
        toast(context, msg)
    }
}

fun String?.isValidPassword(): Boolean {
    this?.let {
        if (it.length >= 6) return true
    }
    return false
}

enum class LikeState {
    UNLIKE, LIKED, EDIT, NULL
}

enum class EditState {
    EDIT, ADD
}

const val REQUEST_NAV_HEADER = 0
const val REQUEST_BIKE_IMAGE = 1

fun toMD5(string: String): String {
    if (TextUtils.isEmpty(string)) {
        return ""
    }
    val md5: MessageDigest?
    try {
        md5 = MessageDigest.getInstance("MD5")
        val bytes = md5!!.digest(string.toByteArray())
        var result = ""
        for (b in bytes) {
            var temp = Integer.toHexString(b.toInt().and(0xff))
            if (temp.length == 1) {
                temp = "0$temp"
            }
            result += temp
        }
        return result
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return ""
}

fun handleImage(context: Context, data: Intent): String? {
    val uri = data.data
    var imagePath: String? = null
    if (DocumentsContract.isDocumentUri(context, uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        if ("com.android.providers.media.documents" == uri!!.authority) {
            val id = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val selection = MediaStore.Images.Media._ID + "=" + id
            imagePath =
                getImagePath(
                    context,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selection
                )
        } else if ("com.android.providers.downloads.documents" == uri.authority) {
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(docId)
            )
            imagePath = getImagePath(context, contentUri, null)
        }
    }
    return imagePath
}

private fun getImagePath(context: Context, uri: Uri, selection: String?): String? {
    var path: String? = null
    val cursor = context.contentResolver.query(uri, null, selection, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        }
        cursor.close()
    }
    return path
}

@BindingAdapter("glide")
fun ImageView.glide(url: String?) {
    url?.let {
        Glide.with(this).load(url).into(this)
    }
}

//经纬度转地点
suspend fun convertLatLngToPlaceAsync(
    context: Context,
    lat: Double,
    lng: Double
): RegeocodeAddress =
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
                    0f, GeocodeSearch.AMAP
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




