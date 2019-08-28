package com.example.west2summer.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.west2summer.source.User

class MainNavHeaderViewModel : ViewModel() {
    var user: LiveData<User> = User.currentUser
    val name = Transformations.map(user) {
        user.value?.name
    }
    val id = Transformations.map(user) {
        user.value?.id?.toString()
    }
}