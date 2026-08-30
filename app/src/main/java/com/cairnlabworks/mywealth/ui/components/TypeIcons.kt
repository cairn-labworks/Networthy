package com.cairnlabworks.mywealth.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.ui.graphics.vector.ImageVector
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.LiabilityType

fun AssetType.icon(): ImageVector = when (this) {
    AssetType.STOCK -> Icons.Filled.ShowChart
    AssetType.MUTUAL_FUND -> Icons.Filled.PieChart
    AssetType.CRYPTO -> Icons.Filled.CurrencyBitcoin
    AssetType.GOLD -> Icons.Filled.Diamond
    AssetType.CASH -> Icons.Filled.Payments
    AssetType.BANK_DEPOSIT -> Icons.Filled.AccountBalance
    AssetType.REAL_ESTATE -> Icons.Filled.Home
    AssetType.VEHICLE -> Icons.Filled.DirectionsCar
    AssetType.BOND -> Icons.Filled.Description
    AssetType.OTHER -> Icons.Filled.Category
}

fun LiabilityType.icon(): ImageVector = when (this) {
    LiabilityType.LOAN -> Icons.Filled.AccountBalanceWallet
    LiabilityType.MORTGAGE -> Icons.Filled.HomeWork
    LiabilityType.CREDIT_CARD -> Icons.Filled.CreditCard
    LiabilityType.PENDING_PAYMENT -> Icons.Filled.Schedule
    LiabilityType.TAX -> Icons.Filled.ReceiptLong
    LiabilityType.OTHER -> Icons.Filled.RequestQuote
}
