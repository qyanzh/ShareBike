package com.example.west2summer.edit

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.example.west2summer.R
import com.example.west2summer.component.EditState
import com.example.west2summer.source.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class BikeEditViewModel(
    application: Application,
    private val bikeInfo: BikeInfo
) : AndroidViewModel(application) {

    //TODO: move to repository
    private val repository = Repository(getDatabase(application))

    private lateinit var database: MyDatabase

    private val viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /* 标题 */
    val uiTitle = MutableLiveData<String?>()

    /* 电池 */
    val uiBattery = MutableLiveData<String?>()

    /* 价格 */
    val uiPrice = MutableLiveData<String?>()

    /* 备注 */
    val uiNote = MutableLiveData<String?>()

    /* 模式 */
    val mode: EditState = when (bikeInfo.bikeId) {
        null -> EditState.ADD
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
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database = getDatabase(application)
            }
            with(bikeInfo) {
                ownerId = User.currentUser.value?.id
                title?.let { uiTitle.value = it }
                battery?.let { uiBattery.value = it.toString() }
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
    }

    /* 完成 */
    fun onDoneMenuClicked(): Boolean {
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
            bikeInfo.battery = uiBattery.value!!.toDouble()
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
        //TODO: 上传到服务器
        CoroutineScope(Dispatchers.IO).launch {
            database.bikeInfoDao.insert(bikeInfo)
            withContext(Dispatchers.Main) {
                if (mode == EditState.ADD) {
                    Toast.makeText(getApplication(), "创建成功", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(getApplication(), "修改成功", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    /* 删除 */
    fun onDelete(): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            database.bikeInfoDao.delete(bikeInfo)
            withContext(Dispatchers.Main) {
                Toast.makeText(getApplication(), "已删除", Toast.LENGTH_SHORT).show()
            }
        }
        //TODO: 删除本项
        return true
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
