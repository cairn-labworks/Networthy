package com.cairnlabworks.mywealth.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val STATISTICS = "statistics"
    const val PORTFOLIOS = "portfolios"
    const val SETTINGS = "settings"

    const val ASSET_EDIT = "asset_edit"
    const val LIABILITY_EDIT = "liability_edit"

    const val ARG_PORTFOLIO_ID = "portfolioId"
    const val ARG_ASSET_ID = "assetId"
    const val ARG_LIABILITY_ID = "liabilityId"

    fun assetEdit(portfolioId: Long, assetId: Long) = "$ASSET_EDIT/$portfolioId/$assetId"
    fun liabilityEdit(portfolioId: Long, liabilityId: Long) =
        "$LIABILITY_EDIT/$portfolioId/$liabilityId"
}

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    STATISTICS(Routes.STATISTICS, "Statistics", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    PORTFOLIOS(
        Routes.PORTFOLIOS,
        "Portfolios",
        Icons.Filled.AccountBalanceWallet,
        Icons.Outlined.AccountBalanceWallet,
    ),
}
