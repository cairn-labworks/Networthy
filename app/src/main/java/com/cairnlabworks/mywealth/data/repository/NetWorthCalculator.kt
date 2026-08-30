package com.cairnlabworks.mywealth.data.repository

import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.domain.model.CategoryTotal
import com.cairnlabworks.mywealth.domain.model.NetWorthSummary

/**
 * Pure computation of a [NetWorthSummary] from a portfolio's holdings, converting
 * every item into [baseCurrency] using the supplied [CurrencyConverter].
 */
object NetWorthCalculator {

    fun compute(
        assets: List<AssetEntity>,
        liabilities: List<LiabilityEntity>,
        converter: CurrencyConverter,
        baseCurrency: String,
    ): NetWorthSummary {
        var approximate = false

        fun convert(amount: Double, currency: String): Double {
            if (currency != baseCurrency && !converter.canConvert(currency)) {
                approximate = true
            }
            return converter.convert(amount, currency, baseCurrency)
        }

        val assetByType = assets.groupBy { it.type }
        val assetBreakdown = assetByType.map { (type, items) ->
            CategoryTotal(
                typeName = type.name,
                label = type.displayName,
                amount = items.sumOf { convert(it.value, it.currency) },
                count = items.size,
            )
        }.sortedByDescending { it.amount }

        val liabilityByType = liabilities.groupBy { it.type }
        val liabilityBreakdown = liabilityByType.map { (type, items) ->
            CategoryTotal(
                typeName = type.name,
                label = type.displayName,
                amount = items.sumOf { convert(it.value, it.currency) },
                count = items.size,
            )
        }.sortedByDescending { it.amount }

        return NetWorthSummary(
            baseCurrency = baseCurrency,
            totalAssets = assetBreakdown.sumOf { it.amount },
            totalLiabilities = liabilityBreakdown.sumOf { it.amount },
            assetBreakdown = assetBreakdown,
            liabilityBreakdown = liabilityBreakdown,
            hasApproximateConversions = approximate,
        )
    }
}
