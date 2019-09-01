package com.example.west2summer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.west2summer.source.Network
import com.example.west2summer.source.Repository
import com.nanchen.compresshelper.CompressHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

class ImageUploadWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope {
        val id = inputData.getLong("bikeId", -1)
        val imgPath = inputData.getString("path")!!
        val job = async {
            uploadFile(id, imgPath)
        }
        job.await()
        Result.success()
    }

    @Throws(Exception::class)
    private suspend fun uploadFile(bikeId: Long, filePath: String) {
        val file = File(filePath)
        val compressedFile = CompressHelper.getDefault(applicationContext).compressToFile(file)
        val type = compressedFile.absolutePath.split(".").last()
        val requestFile = compressedFile
            .asRequestBody("image/$type".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        Timber.d("uploading")
        Network.service.uploadFileAsync(bikeId, body).await()
        Repository.getBikeInfo(bikeId)
        Timber.d("success")
    }
}
