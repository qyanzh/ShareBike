package com.example.west2summer.edit

import android.app.Application
import androidx.lifecycle.*
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.west2summer.R
import com.example.west2summer.component.EditState
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import com.example.west2summer.worker.ImageUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.text.SimpleDateFormat
import java.util.*

class BikeEditViewModel(
    val app: Application,
    private val bikeInfo: BikeInfo
) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val workManager = WorkManager.getInstance(app)

    val editSuccess = MutableLiveData<Boolean?>()

    fun onSuccess() {
        editSuccess.value = null
    }

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    /* 模式 */
    val mode: EditState = when (bikeInfo.id) {
        -1L -> EditState.ADD
        else -> EditState.EDIT
    }

    /* 标题 */
    val uiTitle = MutableLiveData<String?>()

    /* 电池 */
    val uiBattery = MutableLiveData<String?>()

    /* 价格 */
    val uiPrice = MutableLiveData<String?>()

    /* 备注 */
    val uiNote = MutableLiveData<String?>()

    /* 图片 */
    var uiImg = MutableLiveData<String?>()

    fun onImagePicked(image: String) {
        uiImg.value = image
    }

    fun onImageCanceled() {
        uiImg.value = null
    }

    /* 时间 */
    private val timeFormatter = SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault())

    val preUiFrom = MutableLiveData<Calendar?>()
    val uiFrom = Transformations.map(preUiFrom) {
        it?.let {
            timeFormatter.format(it.time)
        } ?: ""
    }

    val preUiTo = MutableLiveData<Calendar?>()
    val uiTo = Transformations.map(preUiTo) {
        it?.let {
            timeFormatter.format(it.time)
        } ?: ""
    }

    private val _shouldOpenPicker = MutableLiveData<Int?>()
    val shouldOpenPicker: LiveData<Int?>
        get() = _shouldOpenPicker

    fun onTimeClicked(mode: Int) {
        _shouldOpenPicker.value = mode
    }

    fun onTimePicked(c: Calendar?) {
        when (_shouldOpenPicker.value) {
            1 -> preUiFrom.value = c
            2 -> preUiTo.value = c
        }
        _shouldOpenPicker.value = null
    }

    /* 初始化 */
    init {
        with(bikeInfo) {
            if (ownerId == -1L) {
                User.currentUser.value?.id?.let { ownerId = it }
            }
            title?.let { uiTitle.value = it }
            battery?.let { uiBattery.value = it }
            avaFrom?.let {
                preUiFrom.value = Calendar.getInstance().apply { timeInMillis = it }
            }
            avaTo?.let {
                preUiTo.value = Calendar.getInstance().apply { timeInMillis = it }
            }
            price?.let { uiPrice.value = price.toString() }
            note?.let { uiNote.value = note }
            img?.let { uiImg.value = img }
        }
    }

    /* 完成 */
    fun onDoneClicked() {
        if (uiTitle.value.isNullOrEmpty()) {
            message.value = app.getString(R.string.please_enter_title)
            return
        } else {
            bikeInfo.title = uiTitle.value
        }

        if (uiBattery.value.isNullOrEmpty()) {
            bikeInfo.battery = null
        } else {
            bikeInfo.battery = uiBattery.value
        }

        if (uiPrice.value.isNullOrEmpty()) {
            bikeInfo.price = null
        } else {
            bikeInfo.price = uiPrice.value!!.toDouble()
        }

        if (uiNote.value.isNullOrEmpty()) {
            bikeInfo.note = null
        } else {
            bikeInfo.note = uiNote.value!!
        }

        bikeInfo.avaFrom = preUiFrom.value?.timeInMillis
        bikeInfo.avaTo = preUiTo.value?.timeInMillis

        uiScope.launch {
            submit()
        }
    }

    /*提交*/
    private suspend fun submit() {
        try {
            var id = bikeInfo.id
            if (mode == EditState.ADD) {
                id = Repository.insertBikeInfo(bikeInfo)
                message.value = app.getString(R.string.create_success)
            } else {
                if (uiImg.value == null) bikeInfo.img = null
                Repository.updateBikeInfo(bikeInfo)
                message.value = app.getString(R.string.modify_success)
            }
            uiImg.value?.let {
                if (bikeInfo.img != it) {
                    uploadImg(id, it)
                }
            }
            editSuccess.value = true
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> e.toString()
            }
        }
    }

    private fun uploadImg(id: Long, path: String) {
        val data = Data.Builder()
            .putLong("bikeId", id)
            .putString("path", path)
            .build()
        val uploadRequest = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(data)
            .build()
        workManager.enqueue(uploadRequest)
    }

    /* 删除 */
    fun onDeleteClicked() {
        uiScope.launch {
            delete()
        }
    }

    private suspend fun delete() {
        try {
            Repository.deleteBikeInfo(bikeInfo)
            message.value = app.getString(R.string.delete_success)
            editSuccess.value = true
        } catch (e: Exception) {
            message.value = when (e) {
                is ConnectException -> app.getString(R.string.exam_network)
                else -> e.toString()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeEditViewModel(app, bikeInfo) as T
        }
    }

}
