package com.example.west2summer.component

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavOptions
import com.example.west2summer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*


var shortTimeFormatter = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())
var longTimeFormatter = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())

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

fun <T> MutableLiveData<T>.notifyObserver() {
    this.postValue(this.value)
}

fun String?.isValidPassword(): Boolean {
    this?.let {
        if (it.length >= 6) return true
    }
    return false
}

fun convertImageUriToPath(context: Context, data: Intent): String? {
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

fun String?.toMD5(): String {
    return convertMD5(this)
}

private fun convertMD5(string: String?): String {
    if (string.isNullOrEmpty()) {
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

val defaultNavOptions = NavOptions.Builder()
    .setLaunchSingleTop(true)
    .setEnterAnim(R.anim.nav_default_enter_anim)
    .setExitAnim(R.anim.nav_default_exit_anim)
    .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
    .setPopExitAnim(R.anim.nav_default_pop_exit_anim).build()
