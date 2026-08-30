package com.cairnlabworks.mywealth.data.repository

import com.cairnlabworks.mywealth.data.local.dao.FxRateDao
import com.cairnlabworks.mywealth.data.local.entity.FxRateEntity
import com.cairnlabworks.mywealth.data.remote.fx.FxApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FxRepository(
    private val dao: FxRateDao,
    private val api: FxApi,
) {

    val converter: Flow<CurrencyConverter> = dao.observeAll().map { rates ->
        CurrencyConverter(rates.associate { it.currency to it.unitsPerUsd })
    }

    suspend fun currentConverter(): CurrencyConverter =
        CurrencyConverter(dao.getAll().associate { it.currency to it.unitsPerUsd })

    suspend fun latestTimestamp(): Long? = dao.latestTimestamp()

    /** Fetches and caches the latest USD-based exchange rates. */
    suspend fun refreshRates(): Result<Unit> = runCatching {
        val response = api.getLatest("USD")
        val rates = response.rates
        if (response.result != "success" || rates.isNullOrEmpty()) {
            throw IllegalStateException("Exchange rates unavailable")
        }
        val now = System.currentTimeMillis()
        dao.upsertAll(rates.map { (code, value) -> FxRateEntity(code, value, now) })
    }

    /** Refreshes only if the cache is empty or older than [maxAgeMillis]. */
    suspend fun ensureFreshRates(maxAgeMillis: Long = DEFAULT_MAX_AGE): Result<Unit> {
        val latest = dao.latestTimestamp()
        val fresh = latest != null && System.currentTimeMillis() - latest < maxAgeMillis
        return if (fresh) Result.success(Unit) else refreshRates()
    }

    private companion object {
        const val DEFAULT_MAX_AGE = 12 * 60 * 60 * 1000L // 12 hours
    }
}
