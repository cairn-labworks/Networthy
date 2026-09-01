package com.cairnlabworks.mywealth.ui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.ui.components.DonutChart
import com.cairnlabworks.mywealth.ui.components.EmptyState
import com.cairnlabworks.mywealth.ui.components.PieSlice
import com.cairnlabworks.mywealth.ui.theme.FinanceTheme
import com.cairnlabworks.mywealth.util.CurrencyUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    val container = rememberAppContainer()
    val viewModel: StatisticsViewModel = viewModel(factory = ViewModelFactories.statistics(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var portfolioMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
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
                                text = state.activePortfolio?.name ?: "Statistics",
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
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (!state.hasAnything) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyState(
                    icon = Icons.Filled.PieChart,
                    title = "Nothing to chart yet",
                    subtitle = "Add assets or liabilities to this portfolio to see its breakdown.",
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1) Overview: assets vs liabilities.
            val overviewSlices = buildList {
                if (state.totalAssets > 0) {
                    add(PieSlice("Assets", state.totalAssets, FinanceTheme.colors.positive))
                }
                if (state.totalLiabilities > 0) {
                    add(PieSlice("Liabilities", state.totalLiabilities, FinanceTheme.colors.negative))
                }
            }
            ChartCard(
                title = "Overview",
                subtitle = "Assets vs. liabilities",
                slices = overviewSlices,
                defaultCenterLabel = "Net worth",
                defaultCenterValue = state.netWorth,
                baseCurrency = state.baseCurrency,
                hideBalances = state.hideBalances,
            )

            // 2) Assets by type.
            val assetSlices = state.assetsByType.mapIndexed { i, t ->
                PieSlice(t.label, t.amount, ChartColors.at(i))
            }
            if (assetSlices.isNotEmpty()) {
                ChartCard(
                    title = "Assets",
                    subtitle = "By type",
                    slices = assetSlices,
                    defaultCenterLabel = "Assets",
                    defaultCenterValue = state.totalAssets,
                    baseCurrency = state.baseCurrency,
                    hideBalances = state.hideBalances,
                )
            }

            // 3) Liabilities by type.
            val liabilitySlices = state.liabilitiesByType.mapIndexed { i, t ->
                PieSlice(t.label, t.amount, ChartColors.at(i))
            }
            if (liabilitySlices.isNotEmpty()) {
                ChartCard(
                    title = "Liabilities",
                    subtitle = "By type",
                    slices = liabilitySlices,
                    defaultCenterLabel = "Liabilities",
                    defaultCenterValue = state.totalLiabilities,
                    baseCurrency = state.baseCurrency,
                    hideBalances = state.hideBalances,
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    subtitle: String,
    slices: List<PieSlice>,
    defaultCenterLabel: String,
    defaultCenterValue: Double,
    baseCurrency: String,
    hideBalances: Boolean,
) {
    var selected by remember(slices) { mutableStateOf<Int?>(null) }

    fun money(amount: Double): String =
        if (hideBalances) CurrencyUtil.masked(baseCurrency) else CurrencyUtil.format(amount, baseCurrency)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(
                    slices = slices,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                ) {
                    val sel = selected?.let { slices.getOrNull(it) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = sel?.label ?: defaultCenterLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = money(sel?.value ?: defaultCenterValue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                        if (sel != null) {
                            Text(
                                text = percentage(sel.value, slices.sumOf { it.value }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            slices.forEachIndexed { index, slice ->
                LegendRow(
                    color = slice.color,
                    label = slice.label,
                    valueText = money(slice.value),
                    highlighted = selected == index,
                    onClick = { selected = if (selected == index) null else index },
                )
            }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String,
    valueText: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = color, shape = CircleShape, modifier = Modifier.size(12.dp)) {}
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun percentage(value: Double, total: Double): String {
    if (total <= 0.0) return ""
    val pct = value / total * 100.0
    return if (pct >= 10.0) "${pct.toInt()}%" else String.format("%.1f%%", pct)
}
