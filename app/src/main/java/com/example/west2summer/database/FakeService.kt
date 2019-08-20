package com.example.west2summer.database

fun fakeLogin(username: Long, password: String): Boolean {
    if (password.isBlank()) {
        throw Exception("账号密码错误")
    }
    User.setCurrentUser(
        User(
            username
        )
    )
    return true
}

fun fakeRegister(): Boolean {
    return true
}

