package com.cairnlabworks.mywealth.data.backup

import com.google.gson.annotations.SerializedName

/** Serializable snapshot of one or more portfolios for encrypted export/import. */
data class BackupFile(
    @SerializedName("schema") val schema: Int = CURRENT_SCHEMA,
    @SerializedName("app") val app: String = "MyWealth",
    @SerializedName("exportedAt") val exportedAt: Long,
    @SerializedName("portfolios") val portfolios: List<BackupPortfolio>,
) {
    companion object {
        const val CURRENT_SCHEMA = 1
    }
}

data class BackupPortfolio(
    @SerializedName("name") val name: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("assets") val assets: List<BackupAsset>,
    @SerializedName("liabilities") val liabilities: List<BackupLiability>,
)

data class BackupAsset(
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("quantity") val quantity: Double?,
    @SerializedName("pricePerUnit") val pricePerUnit: Double?,
    @SerializedName("symbol") val symbol: String?,
    @SerializedName("lastPrice") val lastPrice: Double?,
    @SerializedName("lastPriceTimestamp") val lastPriceTimestamp: Long?,
    @SerializedName("manualValue") val manualValue: Double?,
    @SerializedName("notes") val notes: String?,
)

data class BackupLiability(
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("notes") val notes: String?,
)

data class ImportResult(
    val portfolios: Int,
    val assets: Int,
    val liabilities: Int,
)
