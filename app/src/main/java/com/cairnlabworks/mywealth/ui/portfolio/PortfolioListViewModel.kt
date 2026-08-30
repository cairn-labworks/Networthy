package com.cairnlabworks.mywealth.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.data.repository.AssetRepository
import com.cairnlabworks.mywealth.data.repository.FxRepository
import com.cairnlabworks.mywealth.data.repository.LiabilityRepository
import com.cairnlabworks.mywealth.data.repository.NetWorthCalculator
import com.cairnlabworks.mywealth.data.repository.PortfolioRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PortfolioRow(
    val portfolio: PortfolioEntity,
    val netWorth: Double,
    val baseCurrency: String,
    val assetCount: Int,
    val liabilityCount: Int,
)

class PortfolioListViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val assetRepository: AssetRepository,
    private val liabilityRepository: LiabilityRepository,
    private val fxRepository: FxRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val rows: StateFlow<List<PortfolioRow>> = combine(
        portfolioRepository.observeAll(),
        settingsRepository.settings,
        fxRepository.converter,
    ) { portfolios, settings, converter ->
        portfolios.map { portfolio ->
            val assets = assetRepository.getForPortfolio(portfolio.id)
            val liabilities = liabilityRepository.getForPortfolio(portfolio.id)
            val summary = NetWorthCalculator.compute(
                assets = assets,
                liabilities = liabilities,
                converter = converter,
                baseCurrency = settings.baseCurrency,
            )
            PortfolioRow(
                portfolio = portfolio,
                netWorth = summary.netWorth,
                baseCurrency = settings.baseCurrency,
                assetCount = assets.size,
                liabilityCount = liabilities.size,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun create(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { portfolioRepository.create(name) }
    }

    fun rename(id: Long, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { portfolioRepository.rename(id, name) }
    }

    fun setDefault(id: Long) {
        viewModelScope.launch { portfolioRepository.setDefault(id) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            val removed = portfolioRepository.delete(id)
            if (!removed) _message.value = "You must keep at least one portfolio"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
