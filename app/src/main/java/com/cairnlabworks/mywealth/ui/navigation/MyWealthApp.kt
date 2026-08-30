package com.cairnlabworks.mywealth.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cairnlabworks.mywealth.ui.asset.AssetEditScreen
import com.cairnlabworks.mywealth.ui.home.HomeScreen
import com.cairnlabworks.mywealth.ui.liability.LiabilityEditScreen
import com.cairnlabworks.mywealth.ui.portfolio.PortfolioScreen
import com.cairnlabworks.mywealth.ui.settings.SettingsScreen

@Composable
fun MyWealthApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon
                                    else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenAssetEditor = { portfolioId, assetId ->
                        navController.navigate(Routes.assetEdit(portfolioId, assetId))
                    },
                    onOpenLiabilityEditor = { portfolioId, liabilityId ->
                        navController.navigate(Routes.liabilityEdit(portfolioId, liabilityId))
                    },
                    onManagePortfolios = {
                        navController.navigate(Routes.PORTFOLIOS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(Routes.PORTFOLIOS) {
                PortfolioScreen(onClose = { navController.popBackStack() })
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onClose = { navController.popBackStack() })
            }

            composable(
                route = "${Routes.ASSET_EDIT}/{${Routes.ARG_PORTFOLIO_ID}}/{${Routes.ARG_ASSET_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_PORTFOLIO_ID) { type = NavType.LongType },
                    navArgument(Routes.ARG_ASSET_ID) { type = NavType.LongType },
                ),
            ) { entry ->
                AssetEditScreen(
                    portfolioId = entry.arguments?.getLong(Routes.ARG_PORTFOLIO_ID) ?: 0L,
                    assetId = entry.arguments?.getLong(Routes.ARG_ASSET_ID) ?: 0L,
                    onClose = { navController.popBackStack() },
                )
            }

            composable(
                route = "${Routes.LIABILITY_EDIT}/{${Routes.ARG_PORTFOLIO_ID}}/{${Routes.ARG_LIABILITY_ID}}",
                arguments = listOf(
                    navArgument(Routes.ARG_PORTFOLIO_ID) { type = NavType.LongType },
                    navArgument(Routes.ARG_LIABILITY_ID) { type = NavType.LongType },
                ),
            ) { entry ->
                LiabilityEditScreen(
                    portfolioId = entry.arguments?.getLong(Routes.ARG_PORTFOLIO_ID) ?: 0L,
                    liabilityId = entry.arguments?.getLong(Routes.ARG_LIABILITY_ID) ?: 0L,
                    onClose = { navController.popBackStack() },
                )
            }
        }
    }
}
