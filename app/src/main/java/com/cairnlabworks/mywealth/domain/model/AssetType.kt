package com.cairnlabworks.mywealth.domain.model

/**
 * How an asset's total value is derived from its stored fields.
 */
enum class ValuationMode {
    /** value = quantity * last fetched market price (e.g. stocks, crypto). */
    MARKET,

    /** value = quantity * a manually entered price per unit (e.g. gold grams). */
    QUANTITY,

    /** value = a single, manually entered total amount (e.g. cash, property). */
    FLAT,
}

/**
 * The category of an asset. Each type maps to a [ValuationMode] which drives the
 * input fields shown in the editor and how the total value is computed.
 */
enum class AssetType(
    val displayName: String,
    val valuationMode: ValuationMode,
    /** Optional hint for the unit used in [ValuationMode.QUANTITY] types. */
    val unitLabel: String? = null,
) {
    STOCK("Stock", ValuationMode.MARKET),
    MUTUAL_FUND("Mutual fund / ETF", ValuationMode.MARKET),
    CRYPTO("Cryptocurrency", ValuationMode.MARKET),
    GOLD("Gold", ValuationMode.QUANTITY, unitLabel = "g"),
    CASH("Cash", ValuationMode.FLAT),
    BANK_DEPOSIT("Bank / deposit", ValuationMode.FLAT),
    REAL_ESTATE("Real estate", ValuationMode.FLAT),
    VEHICLE("Vehicle", ValuationMode.FLAT),
    BOND("Bond", ValuationMode.FLAT),
    OTHER("Other", ValuationMode.FLAT);

    val isMarketLinked: Boolean get() = valuationMode == ValuationMode.MARKET

    companion object {
        fun fromName(name: String): AssetType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
