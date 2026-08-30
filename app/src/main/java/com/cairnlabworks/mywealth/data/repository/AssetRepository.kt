package com.cairnlabworks.mywealth.data.repository

import com.cairnlabworks.mywealth.data.local.dao.AssetDao
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.remote.stock.StockPriceService
import kotlinx.coroutines.flow.Flow

class AssetRepository(
    private val dao: AssetDao,
    private val stockPriceService: StockPriceService,
) {

    fun observeForPortfolio(portfolioId: Long): Flow<List<AssetEntity>> =
        dao.observeForPortfolio(portfolioId)

    fun observeById(id: Long): Flow<AssetEntity?> = dao.observeById(id)

    suspend fun getById(id: Long): AssetEntity? = dao.getById(id)

    suspend fun save(asset: AssetEntity): Long {
        val stamped = asset.copy(updatedAt = System.currentTimeMillis())
        return if (asset.id == 0L) dao.insert(stamped) else {
            dao.update(stamped)
            asset.id
        }
    }

    suspend fun delete(id: Long) = dao.deleteById(id)

    /**
     * Refreshes the market price for a single asset (by symbol) and persists it.
     * Returns the updated price on success.
     */
    suspend fun refreshPrice(asset: AssetEntity): Result<Double> {
        val symbol = asset.symbol?.trim().orEmpty()
        if (!asset.type.isMarketLinked || symbol.isEmpty()) {
            return Result.failure(IllegalStateException("Asset has no market symbol"))
        }
        return stockPriceService.fetchQuote(symbol).map { quote ->
            val now = System.currentTimeMillis()
            dao.updatePrice(asset.id, quote.price, now)
            quote.price
        }
    }

    /** Refreshes prices for all market-linked assets in a portfolio. */
    suspend fun refreshAllPrices(portfolioId: Long): PriceRefreshResult {
        val assets = dao.getForPortfolio(portfolioId).filter {
            it.type.isMarketLinked && !it.symbol.isNullOrBlank()
        }
        var updated = 0
        var failed = 0
        for (asset in assets) {
            refreshPrice(asset).fold(onSuccess = { updated++ }, onFailure = { failed++ })
        }
        return PriceRefreshResult(updated = updated, failed = failed, total = assets.size)
    }
}

data class PriceRefreshResult(
    val updated: Int,
    val failed: Int,
    val total: Int,
)
