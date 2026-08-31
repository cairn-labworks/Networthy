package com.cairnlabworks.mywealth.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Helpers for currency codes and money formatting. All formatting happens
 * on-device; nothing is transmitted.
 */
object CurrencyUtil {

    /** A curated shortlist surfaced first in pickers. */
    val COMMON_CURRENCIES: List<String> = listOf(
        "USD", "EUR", "GBP", "INR", "JPY", "CNY", "AUD", "CAD", "CHF",
        "SGD", "HKD", "AED", "SAR", "ZAR", "BRL", "RUB", "KRW", "SEK",
        "NOK", "DKK", "NZD", "MXN", "THB", "IDR", "MYR", "PHP",
    )

    /** Every ISO-4217 currency code available on the device, sorted. */
    val ALL_CURRENCIES: List<String> by lazy {
        Currency.getAvailableCurrencies()
            .map { it.currencyCode }
            .distinct()
            .sorted()
    }

    fun deviceCurrencyCode(): String = runCatching {
        Currency.getInstance(Locale.getDefault()).currencyCode
    }.getOrDefault("USD")

    fun displayName(code: String): String = runCatching {
        Currency.getInstance(code).getDisplayName(Locale.getDefault())
    }.getOrDefault(code)

    /** Fraction digits appropriate for the currency (e.g. 0 for JPY, 2 for USD). */
    fun fractionDigits(code: String): Int = runCatching {
        Currency.getInstance(code).defaultFractionDigits.coerceAtLeast(0)
    }.getOrDefault(2)

    /**
     * The currency symbol, with a space inserted between letter prefixes and the
     * currency sign so codes like USD read as "US $" instead of "US$".
     */
    fun symbol(code: String): String = spaceSymbol(rawSymbol(code))

    private fun rawSymbol(code: String): String = runCatching {
        Currency.getInstance(code).getSymbol(Locale.getDefault())
    }.getOrDefault(code)

    private fun spaceSymbol(symbol: String): String =
        symbol.replace(Regex("(\\p{L})(\\p{Sc})"), "$1 $2")

    /**
     * Formats [amount] as a currency string with grouping and no decimals,
     * e.g. "US $1,234" or "₹1,234". Decimals are intentionally truncated.
     */
    fun format(amount: Double, code: String): String {
        val symbol = symbol(code)
        val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = 0
        val rounded = Math.round(amount)
        val sign = if (rounded < 0) "-" else ""
        return "$sign$symbol${numberFormat.format(kotlin.math.abs(rounded))}"
    }

    /** A masked placeholder for a hidden balance, e.g. "₹ ••••" or "US $ ••••". */
    fun masked(code: String): String = "${symbol(code)} ••••"

    /** Short masked placeholder for compact spots (rows, tiles): "••••". */
    const val MASK_SHORT: String = "••••"

    /** Compact form for large headline figures, e.g. "US $1.2M". */
    fun formatCompact(amount: Double, code: String): String {
        val symbol = symbol(code)
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 1_000_000_000 -> "$sign$symbol${round(abs / 1_000_000_000)}B"
            abs >= 1_000_000 -> "$sign$symbol${round(abs / 1_000_000)}M"
            abs >= 10_000 -> "$sign$symbol${round(abs / 1_000)}K"
            else -> format(amount, code)
        }
    }

    private fun round(value: Double): String {
        val rounded = (value * 10).toLong() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
}
