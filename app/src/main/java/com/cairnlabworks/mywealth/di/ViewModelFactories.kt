package com.cairnlabworks.mywealth.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cairnlabworks.mywealth.ui.asset.AssetEditViewModel
import com.cairnlabworks.mywealth.ui.home.HomeViewModel
import com.cairnlabworks.mywealth.ui.liability.LiabilityEditViewModel
import com.cairnlabworks.mywealth.ui.portfolio.PortfolioListViewModel
import com.cairnlabworks.mywealth.ui.settings.SettingsViewModel
import com.cairnlabworks.mywealth.ui.statistics.StatisticsViewModel

/**
 * Central place that builds every [androidx.lifecycle.ViewModel] from the
 * [AppContainer], keeping the ViewModels free of any DI wiring.
 */
object ViewModelFactories {

    fun home(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                settingsRepository = container.settingsRepository,
                portfolioRepository = container.portfolioRepository,
                assetRepository = container.assetRepository,
                liabilityRepository = container.liabilityRepository,
                fxRepository = container.fxRepository,
            )
        }
    }

    fun portfolioList(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            PortfolioListViewModel(
                portfolioRepository = container.portfolioRepository,
                assetRepository = container.assetRepository,
                liabilityRepository = container.liabilityRepository,
                fxRepository = container.fxRepository,
                settingsRepository = container.settingsRepository,
            )
        }
    }

    fun statistics(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            StatisticsViewModel(
                settingsRepository = container.settingsRepository,
                portfolioRepository = container.portfolioRepository,
                assetRepository = container.assetRepository,
                liabilityRepository = container.liabilityRepository,
                fxRepository = container.fxRepository,
            )
        }
    }

    fun assetEdit(
        container: AppContainer,
        portfolioId: Long,
        assetId: Long,
    ): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AssetEditViewModel(
                assetRepository = container.assetRepository,
                settingsRepository = container.settingsRepository,
                portfolioId = portfolioId,
                assetId = assetId,
            )
        }
    }

    fun liabilityEdit(
        container: AppContainer,
        portfolioId: Long,
        liabilityId: Long,
    ): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            LiabilityEditViewModel(
                liabilityRepository = container.liabilityRepository,
                settingsRepository = container.settingsRepository,
                portfolioId = portfolioId,
                liabilityId = liabilityId,
            )
        }
    }

    fun settings(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SettingsViewModel(
                settingsRepository = container.settingsRepository,
                portfolioRepository = container.portfolioRepository,
                fxRepository = container.fxRepository,
                backupRepository = container.backupRepository,
            )
        }
    }
}
