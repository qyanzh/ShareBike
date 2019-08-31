package com.example.west2summer.component

import android.app.Application
import com.example.west2summer.source.Repository
import timber.log.Timber

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.init(this)
        Timber.plant(Timber.DebugTree())
    }
}