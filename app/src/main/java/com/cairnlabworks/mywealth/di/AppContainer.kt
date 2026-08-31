package com.cairnlabworks.mywealth.di

import android.content.Context
import com.cairnlabworks.mywealth.data.backup.BackupRepository
import com.cairnlabworks.mywealth.data.local.MyWealthDatabase
import com.cairnlabworks.mywealth.data.remote.fx.FxApi
import com.cairnlabworks.mywealth.data.remote.stock.StockApi
import com.cairnlabworks.mywealth.data.remote.stock.StockPriceService
import com.cairnlabworks.mywealth.data.repository.AssetRepository
import com.cairnlabworks.mywealth.data.repository.FxRepository
import com.cairnlabworks.mywealth.data.repository.LiabilityRepository
import com.cairnlabworks.mywealth.data.repository.PortfolioRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import com.cairnlabworks.mywealth.data.security.DatabaseKeyProvider
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Lightweight manual dependency container. A single instance lives on the
 * [com.cairnlabworks.mywealth.MyWealthApplication] and provides repositories to
 * the UI layer. Everything is created lazily on first use.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val keyProvider by lazy { DatabaseKeyProvider(appContext) }

    private val database: MyWealthDatabase by lazy {
        MyWealthDatabase.create(appContext, keyProvider.getOrCreatePassphrase())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Networthy/1.0 (Android)")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val stockApi: StockApi by lazy {
        Retrofit.Builder()
            .baseUrl(StockApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StockApi::class.java)
    }

    private val fxApi: FxApi by lazy {
        Retrofit.Builder()
            .baseUrl(FxApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FxApi::class.java)
    }

    private val stockPriceService by lazy { StockPriceService(stockApi) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val portfolioRepository: PortfolioRepository by lazy {
        PortfolioRepository(database.portfolioDao())
    }

    val assetRepository: AssetRepository by lazy {
        AssetRepository(database.assetDao(), stockPriceService)
    }

    val liabilityRepository: LiabilityRepository by lazy {
        LiabilityRepository(database.liabilityDao())
    }

    val fxRepository: FxRepository by lazy {
        FxRepository(database.fxRateDao(), fxApi)
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(database.portfolioDao(), database.assetDao(), database.liabilityDao())
    }
}
