package com.example.west2summer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

class User(var userId: Long, var password: String) {
    companion object {
        private var me = User(0, "")
        val live = MutableLiveData<User>().apply {
            value = me
        }
        fun setCurrentUser(user: User) {
            me = user
            me.qq = Random.nextInt().toString()
            live.value = me
        }

        fun isLogined(): Boolean {
            return me != null
        }

        fun getCurrentUserLive(): LiveData<User> {
            return live
        }

        fun getCurrentUser(): User {
            if (me == null) {
                me = User(0L, "")
            }
            return me!!
        }
    }

    var qq: String? = "10000"
    val wechat: String? = "z991204-"
    val phone: String? = "18150632336"
}