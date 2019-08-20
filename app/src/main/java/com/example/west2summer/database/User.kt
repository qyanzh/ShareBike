package com.example.west2summer.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.random.Random

class User(var userId: Long) {
    companion object {
        private var me: User? = null
        val live = MutableLiveData<User>()

        fun setCurrentUser(user: User) {
            me = user
            me?.qq = Random.nextInt().toString()
            live.value = me
        }

        fun isLoginned(): Boolean {
            return me != null
        }

        fun getCurrentUserLive(): LiveData<User> {
            return live
        }

        fun getCurrentUser(): User {
            if (me == null) {
                me =
                    User(0)
            }
            return me!!
        }
    }

    var qq: String? = "10000"
    val wechat: String? = "z991204-"
    val phone: String? = "18150632336"
}