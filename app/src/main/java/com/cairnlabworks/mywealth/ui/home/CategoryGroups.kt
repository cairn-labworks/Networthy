package com.cairnlabworks.mywealth.ui.home

import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.LiabilityType

/** One collapsible asset category (all assets sharing a type) shown on Home. */
data class AssetCategoryGroup(
    val type: AssetType,
    /** Total value of the group, converted to the base currency. */
    val total: Double,
    val baseCurrency: String,
    val items: List<AssetEntity>,
)

/** One collapsible liability category (all liabilities sharing a type). */
data class LiabilityCategoryGroup(
    val type: LiabilityType,
    val total: Double,
    val baseCurrency: String,
    val items: List<LiabilityEntity>,
)
