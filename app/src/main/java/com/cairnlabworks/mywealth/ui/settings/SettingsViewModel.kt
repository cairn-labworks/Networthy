package com.cairnlabworks.mywealth.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.backup.BackupRepository
import com.cairnlabworks.mywealth.data.backup.ImportResult
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.data.repository.AppSettings
import com.cairnlabworks.mywealth.data.repository.FxRepository
import com.cairnlabworks.mywealth.data.repository.PortfolioRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import com.cairnlabworks.mywealth.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val portfolioRepository: PortfolioRepository,
    private val fxRepository: FxRepository,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val portfolios: StateFlow<List<PortfolioEntity>> = portfolioRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _fxUpdatedAt = MutableStateFlow<Long?>(null)
    val fxUpdatedAt: StateFlow<Long?> = _fxUpdatedAt.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch { _fxUpdatedAt.value = fxRepository.latestTimestamp() }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setDynamicColor(enabled) }

    fun setBaseCurrency(code: String) =
        viewModelScope.launch { settingsRepository.setBaseCurrency(code) }

    fun setAppLockEnabled(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setAppLockEnabled(enabled) }

    fun refreshRates() {
        viewModelScope.launch {
            val result = fxRepository.refreshRates()
            _fxUpdatedAt.value = fxRepository.latestTimestamp()
            _message.value = if (result.isSuccess) "Exchange rates updated" else "Couldn't update rates"
        }
    }

    suspend fun buildExport(portfolioIds: List<Long>, password: CharArray): ByteArray =
        backupRepository.export(portfolioIds, password)

    suspend fun importBackup(data: ByteArray, password: CharArray): ImportResult =
        backupRepository.import(data, password)

    fun postMessage(text: String) {
        _message.value = text
    }

    fun consumeMessage() {
        _message.value = null
    }
}
