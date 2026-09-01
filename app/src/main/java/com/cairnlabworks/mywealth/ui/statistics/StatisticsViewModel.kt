package com.cairnlabworks.mywealth.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.data.repository.AssetRepository
import com.cairnlabworks.mywealth.data.repository.CurrencyConverter
import com.cairnlabworks.mywealth.data.repository.FxRepository
import com.cairnlabworks.mywealth.data.repository.LiabilityRepository
import com.cairnlabworks.mywealth.data.repository.PortfolioRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A single named amount used to build a chart wedge. */
data class TypeAmount(
    val typeName: String,
    val label: String,
    val amount: Double,
)

data class StatisticsUiState(
    val loading: Boolean = true,
    val portfolios: List<PortfolioEntity> = emptyList(),
    val activePortfolio: PortfolioEntity? = null,
    val baseCurrency: String = "USD",
    val hideBalances: Boolean = false,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val assetsByType: List<TypeAmount> = emptyList(),
    val liabilitiesByType: List<TypeAmount> = emptyList(),
) {
    val netWorth: Double get() = totalAssets - totalLiabilities
    val hasAssets: Boolean get() = assetsByType.isNotEmpty()
    val hasLiabilities: Boolean get() = liabilitiesByType.isNotEmpty()
    val hasAnything: Boolean get() = hasAssets || hasLiabilities
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
    private val fxRepository: FxRepository,
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        settingsRepository.settings,
        portfolioRepository.observeAll(),
        fxRepository.converter,
    ) { settings, portfolios, converter ->
        Triple(settings, portfolios, converter)
    }.flatMapLatest { (settings, portfolios, converter) ->
        val active = resolveActive(portfolios, settings.selectedPortfolioId)
        if (active == null) {
            flowOf(
                StatisticsUiState(
                    loading = false,
                    portfolios = portfolios,
                    activePortfolio = null,
                    baseCurrency = settings.baseCurrency,
                    hideBalances = settings.hideBalances,
                ),
            )
        } else {
            combine(
                assetRepository.observeForPortfolio(active.id),
                liabilityRepository.observeForPortfolio(active.id),
            ) { assets, liabilities ->
                val base = settings.baseCurrency
                val assetsByType = assets
                    .groupBy { it.type }
                    .map { (type, items) ->
                        TypeAmount(
                            typeName = type.name,
                            label = type.displayName,
                            amount = items.sumOf { convert(converter, it.value, it.currency, base) },
                        )
                    }
                    .filter { it.amount > 0.0 }
                    .sortedByDescending { it.amount }
                val liabilitiesByType = liabilities
                    .groupBy { it.type }
                    .map { (type, items) ->
                        TypeAmount(
                            typeName = type.name,
                            label = type.displayName,
                            amount = items.sumOf { convert(converter, it.value, it.currency, base) },
                        )
                    }
                    .filter { it.amount > 0.0 }
                    .sortedByDescending { it.amount }

                StatisticsUiState(
                    loading = false,
                    portfolios = portfolios,
                    activePortfolio = active,
                    baseCurrency = base,
                    hideBalances = settings.hideBalances,
                    totalAssets = assetsByType.sumOf { it.amount },
                    totalLiabilities = liabilitiesByType.sumOf { it.amount },
                    assetsByType = assetsByType,
                    liabilitiesByType = liabilitiesByType,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState(),
    )

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            fxRepository.ensureFreshRates()
        }
    }

    fun selectPortfolio(id: Long) {
        viewModelScope.launch { settingsRepository.setSelectedPortfolioId(id) }
    }

    private fun convert(converter: CurrencyConverter, amount: Double, from: String, to: String) =
        converter.convert(amount, from, to)

    private fun resolveActive(portfolios: List<PortfolioEntity>, selectedId: Long?): PortfolioEntity? {
        if (portfolios.isEmpty()) return null
        selectedId?.let { id -> portfolios.firstOrNull { it.id == id }?.let { return it } }
        return portfolios.firstOrNull { it.isDefault } ?: portfolios.first()
    }
}
