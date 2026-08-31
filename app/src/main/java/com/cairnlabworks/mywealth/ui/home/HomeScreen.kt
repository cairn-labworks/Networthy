package com.cairnlabworks.mywealth.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.domain.model.ValuationMode
import com.cairnlabworks.mywealth.ui.components.DragReorderColumn
import com.cairnlabworks.mywealth.ui.components.EmptyState
import com.cairnlabworks.mywealth.ui.components.SectionHeader
import com.cairnlabworks.mywealth.ui.components.TypeAvatar
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
        LaunchedEffect(text) {
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
                                text = state.activePortfolio?.name ?: "Networthy",
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
                    IconButton(onClick = { viewModel.toggleHideBalances() }) {
                        Icon(
                            imageVector = if (state.hideBalances) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (state.hideBalances) {
                                "Show balances"
                            } else {
                                "Hide balances"
                            },
                        )
                    }
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
            if (state.activePortfolio != null) {
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
                onAssetCategoriesReordered = { groups ->
                    viewModel.onAssetCategoriesReordered(groups.map { it.type })
                },
                onLiabilityCategoriesReordered = { groups ->
                    viewModel.onLiabilityCategoriesReordered(groups.map { it.type })
                },
                onAssetItemsReordered = { items ->
                    viewModel.onAssetItemsReordered(items.map { it.id })
                },
                onLiabilityItemsReordered = { items ->
                    viewModel.onLiabilityItemsReordered(items.map { it.id })
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
                AddSheetRow(
                    icon = Icons.Filled.AccountBalanceWallet,
                    label = "New asset",
                    onClick = {
                        val pid = active?.id ?: return@AddSheetRow
                        scope.launch { sheetState.hide() }
                        addSheetOpen = false
                        onOpenAssetEditor(pid, 0L)
                    },
                )
                AddSheetRow(
                    icon = Icons.Filled.Inbox,
                    label = "New liability",
                    onClick = {
                        val pid = active?.id ?: return@AddSheetRow
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
private fun AddSheetRow(
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
    onOpenLiability: (LiabilityEntity) -> Unit,
    onAssetCategoriesReordered: (List<AssetCategoryGroup>) -> Unit,
    onLiabilityCategoriesReordered: (List<LiabilityCategoryGroup>) -> Unit,
    onAssetItemsReordered: (List<AssetEntity>) -> Unit,
    onLiabilityItemsReordered: (List<LiabilityEntity>) -> Unit,
) {
    // Remembers which category cards are expanded, keyed by section + type.
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 96.dp,
            ),
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            NetWorthCard(summary = state.summary, hideBalances = state.hideBalances)
        }

        SectionHeader(
            title = "Assets",
            trailing = if (state.hideBalances) {
                CurrencyUtil.MASK_SHORT
            } else {
                CurrencyUtil.format(state.summary.totalAssets, state.baseCurrency)
            },
        )
        if (!state.hasAnyAssets) {
            EmptyState(
                icon = Icons.Filled.AccountBalanceWallet,
                title = "No assets yet",
                subtitle = "Add stocks, cash, property and more to track their value.",
            )
        } else {
            DragReorderColumn(
                items = state.assetGroups,
                keyOf = { it.type },
                onReordered = onAssetCategoriesReordered,
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalSpacing = 12.dp,
            ) { group, isDragging, handle ->
                AssetCategoryCard(
                    group = group,
                    isDragging = isDragging,
                    dragHandle = handle,
                    expandedKey = "asset-${group.type.name}",
                    expandedMap = expanded,
                    hideBalances = state.hideBalances,
                    onOpenAsset = onOpenAsset,
                    onItemsReordered = onAssetItemsReordered,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        SectionHeader(
            title = "Liabilities",
            trailing = if (state.hideBalances) {
                CurrencyUtil.MASK_SHORT
            } else {
                CurrencyUtil.format(state.summary.totalLiabilities, state.baseCurrency)
            },
        )
        if (!state.hasAnyLiabilities) {
            EmptyState(
                icon = Icons.Filled.Inbox,
                title = "No liabilities",
                subtitle = "Track loans, credit cards and pending payments here.",
            )
        } else {
            DragReorderColumn(
                items = state.liabilityGroups,
                keyOf = { it.type },
                onReordered = onLiabilityCategoriesReordered,
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalSpacing = 12.dp,
            ) { group, isDragging, handle ->
                LiabilityCategoryCard(
                    group = group,
                    isDragging = isDragging,
                    dragHandle = handle,
                    expandedKey = "liability-${group.type.name}",
                    expandedMap = expanded,
                    hideBalances = state.hideBalances,
                    onOpenLiability = onOpenLiability,
                    onItemsReordered = onLiabilityItemsReordered,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AssetCategoryCard(
    group: AssetCategoryGroup,
    isDragging: Boolean,
    dragHandle: Modifier,
    expandedKey: String,
    expandedMap: MutableMap<String, Boolean>,
    hideBalances: Boolean,
    onOpenAsset: (AssetEntity) -> Unit,
    onItemsReordered: (List<AssetEntity>) -> Unit,
) {
    val isExpanded = expandedMap[expandedKey] == true
    CategoryCardShell(
        icon = group.type.icon(),
        title = group.type.displayName,
        count = group.items.size,
        totalText = if (hideBalances) {
            CurrencyUtil.MASK_SHORT
        } else {
            CurrencyUtil.format(group.total, group.baseCurrency)
        },
        isExpanded = isExpanded,
        isDragging = isDragging,
        dragHandle = dragHandle,
        onToggle = { expandedMap[expandedKey] = !(expandedMap[expandedKey] == true) },
    ) {
        DragReorderColumn(
            items = group.items,
            keyOf = { it.id },
            onReordered = onItemsReordered,
        ) { asset, itemDragging, itemHandle ->
            HoldingItemRow(
                title = asset.name,
                subtitle = assetSubtitle(asset, group.baseCurrency),
                amount = if (hideBalances) {
                    CurrencyUtil.MASK_SHORT
                } else {
                    CurrencyUtil.format(asset.value, asset.currency)
                },
                isDragging = itemDragging,
                dragHandle = itemHandle,
                onOpen = { onOpenAsset(asset) },
            )
        }
    }
}

@Composable
private fun LiabilityCategoryCard(
    group: LiabilityCategoryGroup,
    isDragging: Boolean,
    dragHandle: Modifier,
    expandedKey: String,
    expandedMap: MutableMap<String, Boolean>,
    hideBalances: Boolean,
    onOpenLiability: (LiabilityEntity) -> Unit,
    onItemsReordered: (List<LiabilityEntity>) -> Unit,
) {
    val isExpanded = expandedMap[expandedKey] == true
    CategoryCardShell(
        icon = group.type.icon(),
        title = group.type.displayName,
        count = group.items.size,
        totalText = if (hideBalances) {
            CurrencyUtil.MASK_SHORT
        } else {
            CurrencyUtil.format(group.total, group.baseCurrency)
        },
        isExpanded = isExpanded,
        isDragging = isDragging,
        dragHandle = dragHandle,
        onToggle = { expandedMap[expandedKey] = !(expandedMap[expandedKey] == true) },
    ) {
        DragReorderColumn(
            items = group.items,
            keyOf = { it.id },
            onReordered = onItemsReordered,
        ) { liability, itemDragging, itemHandle ->
            HoldingItemRow(
                title = liability.name,
                subtitle = currencyNote(liability.currency, group.baseCurrency),
                amount = if (hideBalances) {
                    CurrencyUtil.MASK_SHORT
                } else {
                    CurrencyUtil.format(liability.value, liability.currency)
                },
                isDragging = itemDragging,
                dragHandle = itemHandle,
                onOpen = { onOpenLiability(liability) },
            )
        }
    }
}

@Composable
private fun CategoryCardShell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    totalText: String,
    isExpanded: Boolean,
    isDragging: Boolean,
    dragHandle: Modifier,
    onToggle: () -> Unit,
    expandedContent: @Composable () -> Unit,
) {
    val elevation by animateDpAsState(if (isDragging) 8.dp else 1.dp, label = "cardElevation")
    val latestToggle by rememberUpdatedState(onToggle)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Long-press anywhere on the header to reorder the category.
                    .then(dragHandle)
                    // A short tap expands / collapses the category.
                    .pointerInput(Unit) { detectTapGestures { latestToggle() } }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TypeAvatar(icon)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (count == 1) "1 item" else "$count items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = totalText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(4.dp))
                expandedContent()
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun HoldingItemRow(
    title: String,
    subtitle: String,
    amount: String,
    isDragging: Boolean,
    dragHandle: Modifier,
    onOpen: () -> Unit,
) {
    val background =
        if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Long-press to reorder within this category; tap to edit.
            .then(dragHandle)
            .pointerInput(title, amount) { detectTapGestures { onOpen() } }
            .background(background)
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(text = amount, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Filled.DragIndicator,
            contentDescription = "Drag to reorder",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp),
        )
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

        ValuationMode.FLAT -> ""
    }
    return (core + currencyNote(asset.currency, baseCurrency)).trim()
}

private fun currencyNote(currency: String, baseCurrency: String): String =
    if (currency != baseCurrency) "  ·  $currency" else ""

private fun formatQuantity(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
