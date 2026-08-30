package com.cairnlabworks.mywealth.data.repository

/**
 * Converts monetary amounts between currencies using cached rates expressed as
 * units-per-USD. When a rate is unavailable the amount is passed through 1:1 and
 * [canConvert] returns false so the UI can warn that a total may be approximate.
 */
class CurrencyConverter(private val unitsPerUsd: Map<String, Double>) {

    fun canConvert(code: String): Boolean =
        code == USD || unitsPerUsd.containsKey(code)

    fun convert(amount: Double, from: String, to: String): Double {
        if (from == to) return amount
        val fromRate = rateFor(from) ?: return amount
        val toRate = rateFor(to) ?: return amount
        if (fromRate == 0.0) return amount
        val inUsd = amount / fromRate
        return inUsd * toRate
    }

    private fun rateFor(code: String): Double? =
        if (code == USD) 1.0 else unitsPerUsd[code]

    val isEmpty: Boolean get() = unitsPerUsd.isEmpty()

    private companion object {
        const val USD = "USD"
    }
}
