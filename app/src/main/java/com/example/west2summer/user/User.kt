package com.example.west2summer.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.moshi.Json

data class User(
    val id: Long,
    val password: String? = null,
    var name: String? = null,
    var sex: Int? = null,
    var address: String? = null,
    @field:Json(name = "contact_wechat")
    var wechat: String? = null,
    @field:Json(name = "contact_qq")
    var qq: String? = null,
    @field:Json(name = "contact_phone")
    var phone: String? = null
) {
    companion object {

        @Transient
        private val _currentUser = MutableLiveData<User>().apply {
            value = null
        }
        val currentUser: LiveData<User>
            get() = _currentUser

        fun setCurrentUser(user: User) {
            _currentUser.value = user
        }

        fun postCurrentUser(user: User) {
            _currentUser.postValue(user)
        }

        fun isLoginned(): Boolean {
            return _currentUser.value != null
        }

        fun logout() {
            _currentUser.value = null
        }
    }
}