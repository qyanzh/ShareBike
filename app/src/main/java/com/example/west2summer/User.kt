package com.example.west2summer

class User(val userId: Long) {
    companion object {
        private var me: User? = null
        fun setCurrentUser(user: User) {
            me = user
        }

        fun getCurrentUser(): User {
            if (me == null) {
                me = User(123L)
            }
            return me!!
        }
    }

    var qq: String? = "10000"
    val wechat: String? = "z991204-"
    val phone: String? = "18150632336"
}