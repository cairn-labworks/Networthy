package com.cairnlabworks.mywealth.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.data.repository.AssetRepository
import com.cairnlabworks.mywealth.data.repository.FxRepository
import com.cairnlabworks.mywealth.data.repository.LiabilityRepository
import com.cairnlabworks.mywealth.data.repository.NetWorthCalculator
import com.cairnlabworks.mywealth.data.repository.PortfolioRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import com.cairnlabworks.mywealth.domain.model.NetWorthSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val portfolios: List<PortfolioEntity> = emptyList(),
    val activePortfolio: PortfolioEntity? = null,
    val baseCurrency: String = "USD",
    val summary: NetWorthSummary = NetWorthSummary.empty("USD"),
    val assets: List<AssetEntity> = emptyList(),
    val liabilities: List<LiabilityEntity> = emptyList(),
    val isRefreshing: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
    private val fxRepository: FxRepository,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)

    private val _messages = MutableStateFlow<String?>(null)
    val messages: StateFlow<String?> = _messages.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        portfolioRepository.observeAll(),
        fxRepository.converter,
        refreshing,
    ) { settings, portfolios, converter, isRefreshing ->
        CombinedBase(settings.baseCurrency, settings.selectedPortfolioId, portfolios, converter, isRefreshing)
    }.flatMapLatest { base ->
        val active = resolveActive(base.portfolios, base.selectedId)
        if (active == null) {
            flowOf(
                HomeUiState(
                    loading = false,
                    portfolios = base.portfolios,
                    activePortfolio = null,
                    baseCurrency = base.baseCurrency,
                    summary = NetWorthSummary.empty(base.baseCurrency),
                    isRefreshing = base.isRefreshing,
                ),
            )
        } else {
            combine(
                assetRepository.observeForPortfolio(active.id),
                liabilityRepository.observeForPortfolio(active.id),
            ) { assets, liabilities ->
                val summary = NetWorthCalculator.compute(
                    assets = assets,
                    liabilities = liabilities,
                    converter = base.converter,
                    baseCurrency = base.baseCurrency,
                )
                HomeUiState(
                    loading = false,
                    portfolios = base.portfolios,
                    activePortfolio = active,
                    baseCurrency = base.baseCurrency,
                    summary = summary,
                    assets = assets,
                    liabilities = liabilities,
                    isRefreshing = base.isRefreshing,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            portfolioRepository.ensureDefaultPortfolio()
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            fxRepository.ensureFreshRates()
        }
    }

    fun selectPortfolio(id: Long) {
        viewModelScope.launch { settingsRepository.setSelectedPortfolioId(id) }
    }

    fun refresh() {
        val active = uiState.value.activePortfolio ?: return
        if (refreshing.value) return
        viewModelScope.launch {
            refreshing.value = true
            fxRepository.refreshRates()
            val result = assetRepository.refreshAllPrices(active.id)
            refreshing.value = false
            _messages.value = when {
                result.total == 0 -> "Prices up to date"
                result.failed == 0 -> "Updated ${result.updated} price(s)"
                else -> "Updated ${result.updated}, ${result.failed} failed"
            }
        }
    }

    fun consumeMessage() {
        _messages.value = null
    }

    private fun resolveActive(portfolios: List<PortfolioEntity>, selectedId: Long?): PortfolioEntity? {
        if (portfolios.isEmpty()) return null
        selectedId?.let { id -> portfolios.firstOrNull { it.id == id }?.let { return it } }
        return portfolios.firstOrNull { it.isDefault } ?: portfolios.first()
    }

    private data class CombinedBase(
        val baseCurrency: String,
        val selectedId: Long?,
        val portfolios: List<PortfolioEntity>,
        val converter: com.cairnlabworks.mywealth.data.repository.CurrencyConverter,
        val isRefreshing: Boolean,
    )
}
