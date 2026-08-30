package com.cairnlabworks.mywealth.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached foreign-exchange rate expressed as units of [currency] per 1 USD.
 * Used to convert asset/liability values into the user's base currency for
 * net-worth aggregation. USD itself is stored with unitsPerUsd = 1.0.
 */
@Entity(tableName = "fx_rates")
data class FxRateEntity(
    @PrimaryKey
    val currency: String,
    val unitsPerUsd: Double,
    val timestamp: Long = System.currentTimeMillis(),
)
