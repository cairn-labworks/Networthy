package com.cairnlabworks.mywealth.data.remote.stock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A resolved quote for a market-linked symbol. */
data class StockQuote(
    val symbol: String,
    val price: Double,
    val currency: String?,
)

/**
 * Fetches the latest/last-closing price for a ticker symbol. Runs on the IO
 * dispatcher and returns a [Result] so callers can surface friendly errors.
 */
class StockPriceService(private val api: StockApi) {

    suspend fun fetchQuote(symbol: String): Result<StockQuote> = withContext(Dispatchers.IO) {
        val cleaned = symbol.trim().uppercase()
        if (cleaned.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Symbol is empty"))
        }
        runCatching {
            val response = api.getChart(cleaned)
            val error = response.chart?.error
            if (error != null) {
                throw IllegalStateException(error.description ?: "Symbol not found")
            }
            val meta = response.chart?.result?.firstOrNull()?.meta
                ?: throw IllegalStateException("No data returned for $cleaned")
            val price = meta.regularMarketPrice
                ?: meta.previousClose
                ?: meta.chartPreviousClose
                ?: throw IllegalStateException("No price available for $cleaned")
            StockQuote(symbol = cleaned, price = price, currency = meta.currency)
        }
    }
}
