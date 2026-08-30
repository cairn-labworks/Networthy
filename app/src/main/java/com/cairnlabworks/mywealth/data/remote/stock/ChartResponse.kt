package com.cairnlabworks.mywealth.data.remote.stock

import com.google.gson.annotations.SerializedName

/** Subset of the Yahoo Finance chart response used to read the latest price. */
data class ChartResponse(
    @SerializedName("chart") val chart: Chart?,
)

data class Chart(
    @SerializedName("result") val result: List<ChartResult>?,
    @SerializedName("error") val error: ChartError?,
)

data class ChartError(
    @SerializedName("code") val code: String?,
    @SerializedName("description") val description: String?,
)

data class ChartResult(
    @SerializedName("meta") val meta: ChartMeta?,
)

data class ChartMeta(
    @SerializedName("currency") val currency: String?,
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("previousClose") val previousClose: Double?,
    @SerializedName("chartPreviousClose") val chartPreviousClose: Double?,
)
