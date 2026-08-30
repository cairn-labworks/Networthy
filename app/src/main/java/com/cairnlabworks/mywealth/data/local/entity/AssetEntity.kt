package com.cairnlabworks.mywealth.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.ValuationMode

@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(
            entity = PortfolioEntity::class,
            parentColumns = ["id"],
            childColumns = ["portfolioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("portfolioId")],
)
data class AssetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val portfolioId: Long,
    val type: AssetType,
    val name: String,
    val currency: String,
    /** Units held (shares, coins, grams). Used by MARKET and QUANTITY valuation. */
    val quantity: Double? = null,
    /** Manually entered price per unit (e.g. gold). Used by QUANTITY valuation. */
    val pricePerUnit: Double? = null,
    /** Ticker / market symbol (e.g. AAPL, BTC-USD). Used by MARKET valuation. */
    val symbol: String? = null,
    /** Last fetched market price per unit, in [currency]. */
    val lastPrice: Double? = null,
    /** Epoch millis when [lastPrice] was fetched. */
    val lastPriceTimestamp: Long? = null,
    /** Flat total value (e.g. cash, property). Used by FLAT valuation. */
    val manualValue: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    /** The asset's total value expressed in its own [currency]. */
    val value: Double
        get() = when (type.valuationMode) {
            ValuationMode.MARKET -> (quantity ?: 0.0) * (lastPrice ?: 0.0)
            ValuationMode.QUANTITY -> (quantity ?: 0.0) * (pricePerUnit ?: 0.0)
            ValuationMode.FLAT -> manualValue ?: 0.0
        }
}
