package com.example.west2summer.edit

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.EditState
import com.example.west2summer.source.BikeInfo
import com.example.west2summer.source.Repository
import com.example.west2summer.source.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

class BikeEditViewModel(
    val app: Application,
    private val bikeInfo: BikeInfo,
    private val bikeIndex: Int
) : AndroidViewModel(app) {

    private val viewModelJob = Job()

    val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val message = MutableLiveData<String?>()

    fun onMessageShowed() {
        message.value = null
    }

    /* 标题 */
    val uiTitle = MutableLiveData<String?>()

    /* 电池 */
    val uiBattery = MutableLiveData<String?>()

    /* 价格 */
    val uiPrice = MutableLiveData<String?>()

    /* 备注 */
    val uiNote = MutableLiveData<String?>()

    /* 模式 */
    val mode: EditState = when (bikeIndex) {
        -1 -> EditState.ADD
        else -> EditState.EDIT
    }

    /* 图片 */
    var bikeImage = MutableLiveData<String?>()

    fun onImagePicked(image: String) {
        bikeImage.value = image
    }

    fun onImageCanceled() {
        bikeImage.value = null
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

    private val _shouldOpenPicker = MutableLiveData<Int>()
    val shouldOpenPicker: LiveData<Int>
        get() = _shouldOpenPicker

    fun onTimeClicked(mode: Int) {
        _shouldOpenPicker.value = mode
    }

    fun onTimePicked(c: Calendar?) {
        when (_shouldOpenPicker.value) {
            1 -> preUiFrom.value = c
            2 -> preUiTo.value = c
        }
        _shouldOpenPicker.value = 0
    }

    /* 初始化 */
    init {
        with(bikeInfo) {
            if (ownerId == null) {
                ownerId = User.currentUser.value?.id
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
        }
    }

    /* 完成 */
    suspend fun onDoneMenuClicked(): Boolean {
        if (uiTitle.value.isNullOrEmpty()) {
            Toast.makeText(
                getApplication(),
                R.string.please_enter_title,
                Toast.LENGTH_SHORT
            ).show()
            return false
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
        var result = false
        if (mode == EditState.ADD) {
            if (Repository.insertBike(bikeInfo)) {
                message.value = app.getString(R.string.create_success)
                result = true
            }
        } else {
            if (Repository.modifyBike(bikeIndex, bikeInfo)) {
                message.value = app.getString(R.string.modify_success)
                result = true
            }
        }
        return result
    }

    /* 删除 */
    suspend fun onDelete(): Boolean {
        var result = false
        if (Repository.deleteBike(bikeIndex)) {
            message.value = app.getString(R.string.delete_success)
            result = true
        } else {
            message.value = "未知错误"
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    class Factory(val app: Application, val bikeInfo: BikeInfo, val bikeIndex: Int) :
        ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BikeEditViewModel(app, bikeInfo.copy(), bikeIndex) as T
        }
    }

}
