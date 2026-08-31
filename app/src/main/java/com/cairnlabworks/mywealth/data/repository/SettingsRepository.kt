package com.cairnlabworks.mywealth.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cairnlabworks.mywealth.domain.model.ThemeMode
import com.cairnlabworks.mywealth.util.CurrencyUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** All user-facing app preferences. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val baseCurrency: String = CurrencyUtil.deviceCurrencyCode(),
    val appLockEnabled: Boolean = false,
    val hideBalances: Boolean = false,
    val selectedPortfolioId: Long? = null,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mywealth_settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.applicationContext.dataStore

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.fromName(prefs[KEY_THEME_MODE]),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            baseCurrency = prefs[KEY_BASE_CURRENCY] ?: CurrencyUtil.deviceCurrencyCode(),
            appLockEnabled = prefs[KEY_APP_LOCK] ?: false,
            hideBalances = prefs[KEY_HIDE_BALANCES] ?: false,
            selectedPortfolioId = prefs[KEY_SELECTED_PORTFOLIO]?.takeIf { it > 0 },
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setBaseCurrency(code: String) {
        dataStore.edit { it[KEY_BASE_CURRENCY] = code }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_APP_LOCK] = enabled }
    }

    suspend fun setHideBalances(hidden: Boolean) {
        dataStore.edit { it[KEY_HIDE_BALANCES] = hidden }
    }

    suspend fun setSelectedPortfolioId(id: Long?) {
        dataStore.edit {
            if (id == null) it.remove(KEY_SELECTED_PORTFOLIO) else it[KEY_SELECTED_PORTFOLIO] = id
        }
    }

    /** Observes the custom category order for a portfolio section (asset/liability). */
    fun categoryOrder(portfolioId: Long, section: String): Flow<List<String>> =
        dataStore.data.map { prefs ->
            prefs[categoryOrderKey(portfolioId, section)]
                ?.split(',')
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        }

    suspend fun setCategoryOrder(portfolioId: Long, section: String, order: List<String>) {
        dataStore.edit { it[categoryOrderKey(portfolioId, section)] = order.joinToString(",") }
    }

    private fun categoryOrderKey(portfolioId: Long, section: String) =
        stringPreferencesKey("cat_order_${section}_$portfolioId")

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_BASE_CURRENCY = stringPreferencesKey("base_currency")
        val KEY_APP_LOCK = booleanPreferencesKey("app_lock_enabled")
        val KEY_HIDE_BALANCES = booleanPreferencesKey("hide_balances")
        val KEY_SELECTED_PORTFOLIO = longPreferencesKey("selected_portfolio_id")
    }
}

object CategorySection {
    const val ASSET = "asset"
    const val LIABILITY = "liability"
}
