package com.cairnlabworks.mywealth.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.domain.model.ValuationMode
import com.cairnlabworks.mywealth.ui.components.EmptyState
import com.cairnlabworks.mywealth.ui.components.HoldingRow
import com.cairnlabworks.mywealth.ui.components.SectionHeader
import com.cairnlabworks.mywealth.ui.components.icon
import com.cairnlabworks.mywealth.util.CurrencyUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAssetEditor: (portfolioId: Long, assetId: Long) -> Unit,
    onOpenLiabilityEditor: (portfolioId: Long, liabilityId: Long) -> Unit,
    onManagePortfolios: () -> Unit,
) {
    val container = rememberAppContainer()
    val viewModel: HomeViewModel = viewModel(factory = ViewModelFactories.home(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.messages.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var portfolioMenuOpen by remember { mutableStateOf(false) }
    var addSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (message != null) {
        val text = message!!
        androidx.compose.runtime.LaunchedEffect(text) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable(enabled = state.portfolios.isNotEmpty()) {
                                    portfolioMenuOpen = true
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = state.activePortfolio?.name ?: "MyWealth",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch portfolio")
                        }
                        DropdownMenu(
                            expanded = portfolioMenuOpen,
                            onDismissRequest = { portfolioMenuOpen = false },
                        ) {
                            state.portfolios.forEach { portfolio ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            portfolio.name +
                                                if (portfolio.isDefault) "  •  Default" else "",
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectPortfolio(portfolio.id)
                                        portfolioMenuOpen = false
                                    },
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Manage portfolios") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Tune, contentDescription = null)
                                },
                                onClick = {
                                    portfolioMenuOpen = false
                                    onManagePortfolios()
                                },
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }, enabled = !state.isRefreshing) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh prices")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            val active = state.activePortfolio
            if (active != null) {
                ExtendedFloatingActionButton(
                    onClick = { addSheetOpen = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add") },
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            else -> HomeContent(
                state = state,
                contentPadding = padding,
                onOpenAsset = { asset -> onOpenAssetEditor(asset.portfolioId, asset.id) },
                onOpenLiability = { liability ->
                    onOpenLiabilityEditor(liability.portfolioId, liability.id)
                },
            )
        }
    }

    if (addSheetOpen) {
        val active = state.activePortfolio
        ModalBottomSheet(
            onDismissRequest = { addSheetOpen = false },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Add to ${active?.name ?: "portfolio"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                DropdownMenuItemRow(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = "New asset",
                    onClick = {
                        val pid = active?.id ?: return@DropdownMenuItemRow
                        scope.launch { sheetState.hide() }
                        addSheetOpen = false
                        onOpenAssetEditor(pid, 0L)
                    },
                )
                DropdownMenuItemRow(
                    icon = Icons.Filled.Inbox,
                    label = "New liability",
                    onClick = {
                        val pid = active?.id ?: return@DropdownMenuItemRow
                        scope.launch { sheetState.hide() }
                        addSheetOpen = false
                        onOpenLiabilityEditor(pid, 0L)
                    },
                )
            }
        }
    }
}

@Composable
private fun DropdownMenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    contentPadding: PaddingValues,
    onOpenAsset: (AssetEntity) -> Unit,
    onOpenLiability: (com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
    ) {
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                NetWorthCard(summary = state.summary)
            }
        }

        item {
            SectionHeader(
                title = "Assets",
                trailing = CurrencyUtil.format(state.summary.totalAssets, state.baseCurrency),
            )
        }
        if (state.assets.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.AccountBalanceWallet,
                    title = "No assets yet",
                    subtitle = "Add stocks, cash, property and more to track their value.",
                )
            }
        } else {
            items(state.assets, key = { "asset-${it.id}" }) { asset ->
                HoldingRow(
                    icon = asset.type.icon(),
                    title = asset.name,
                    subtitle = assetSubtitle(asset, state.baseCurrency),
                    amount = CurrencyUtil.format(asset.value, asset.currency),
                    onClick = { onOpenAsset(asset) },
                )
            }
        }

        item {
            SectionHeader(
                title = "Liabilities",
                trailing = CurrencyUtil.format(state.summary.totalLiabilities, state.baseCurrency),
            )
        }
        if (state.liabilities.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Filled.Inbox,
                    title = "No liabilities",
                    subtitle = "Track loans, credit cards and pending payments here.",
                )
            }
        } else {
            items(state.liabilities, key = { "liability-${it.id}" }) { liability ->
                HoldingRow(
                    icon = liability.type.icon(),
                    title = liability.name,
                    subtitle = liability.type.displayName +
                        currencyBadge(liability.currency, state.baseCurrency),
                    amount = CurrencyUtil.format(liability.value, liability.currency),
                    onClick = { onOpenLiability(liability) },
                )
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun assetSubtitle(asset: AssetEntity, baseCurrency: String): String {
    val core = when (asset.type.valuationMode) {
        ValuationMode.MARKET -> {
            val qty = asset.quantity?.let { formatQuantity(it) } ?: "—"
            val sym = asset.symbol?.uppercase().orEmpty()
            if (sym.isNotEmpty()) "$qty × $sym" else "$qty units"
        }

        ValuationMode.QUANTITY -> {
            val qty = asset.quantity?.let { formatQuantity(it) } ?: "—"
            "$qty ${asset.type.unitLabel ?: ""}".trim()
        }

        ValuationMode.FLAT -> asset.type.displayName
    }
    return core + currencyBadge(asset.currency, baseCurrency)
}

private fun currencyBadge(currency: String, baseCurrency: String): String =
    if (currency != baseCurrency) "  ·  $currency" else ""

private fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
