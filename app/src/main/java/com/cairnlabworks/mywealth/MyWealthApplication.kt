package com.cairnlabworks.mywealth

import android.app.Application
import com.cairnlabworks.mywealth.di.AppContainer

class MyWealthApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
