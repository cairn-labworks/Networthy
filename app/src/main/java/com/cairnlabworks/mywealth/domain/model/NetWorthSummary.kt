package com.cairnlabworks.mywealth.domain.model

/** Aggregated total for a single asset/liability category. */
data class CategoryTotal(
    val typeName: String,
    val label: String,
    val amount: Double,
    val count: Int,
)

/** A portfolio's net worth expressed in a single base currency. */
data class NetWorthSummary(
    val baseCurrency: String,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val assetBreakdown: List<CategoryTotal>,
    val liabilityBreakdown: List<CategoryTotal>,
    /** True if one or more items used a 1:1 fallback because no FX rate was cached. */
    val hasApproximateConversions: Boolean,
) {
    val netWorth: Double get() = totalAssets - totalLiabilities

    companion object {
        fun empty(baseCurrency: String) = NetWorthSummary(
            baseCurrency = baseCurrency,
            totalAssets = 0.0,
            totalLiabilities = 0.0,
            assetBreakdown = emptyList(),
            liabilityBreakdown = emptyList(),
            hasApproximateConversions = false,
        )
    }
}
