package com.cairnlabworks.mywealth

import android.app.Application
import com.cairnlabworks.mywealth.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyWealthApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Warm up the encrypted database off the main thread and guarantee a
        // default portfolio exists before the UI needs it.
        appScope.launch {
            runCatching { container.portfolioRepository.ensureDefaultPortfolio() }
        }
    }
}
