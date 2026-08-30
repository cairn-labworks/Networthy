package com.cairnlabworks.mywealth.ui.asset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.local.entity.AssetEntity
import com.cairnlabworks.mywealth.data.repository.AssetRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.ValuationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssetFormState(
    val id: Long = 0,
    val portfolioId: Long = 0,
    val type: AssetType = AssetType.STOCK,
    val name: String = "",
    val currency: String = "USD",
    val quantity: String = "",
    val pricePerUnit: String = "",
    val symbol: String = "",
    val manualValue: String = "",
    val lastPrice: Double? = null,
    val lastPriceTimestamp: Long? = null,
    val notes: String = "",
    val loading: Boolean = true,
    val isFetchingPrice: Boolean = false,
    val isSaving: Boolean = false,
) {
    val isEditing: Boolean get() = id != 0L
    val valuationMode: ValuationMode get() = type.valuationMode

    /** Live estimate of the asset's value in its own currency. */
    val estimatedValue: Double
        get() = when (type.valuationMode) {
            ValuationMode.MARKET ->
                (quantity.toDoubleOrNull() ?: 0.0) * (lastPrice ?: 0.0)

            ValuationMode.QUANTITY ->
                (quantity.toDoubleOrNull() ?: 0.0) * (pricePerUnit.toDoubleOrNull() ?: 0.0)

            ValuationMode.FLAT -> manualValue.toDoubleOrNull() ?: 0.0
        }
}

class AssetEditViewModel(
    private val assetRepository: AssetRepository,
    private val settingsRepository: SettingsRepository,
    private val portfolioId: Long,
    private val assetId: Long,
) : ViewModel() {

    private val _form = MutableStateFlow(AssetFormState(portfolioId = portfolioId, loading = true))
    val form: StateFlow<AssetFormState> = _form.asStateFlow()

    private val _events = MutableStateFlow<AssetEditEvent?>(null)
    val events: StateFlow<AssetEditEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            if (assetId != 0L) {
                val asset = assetRepository.getById(assetId)
                if (asset != null) {
                    _form.value = asset.toForm()
                    return@launch
                }
            }
            val base = settingsRepository.settings.first().baseCurrency
            _form.update { it.copy(currency = base, loading = false) }
        }
    }

    fun onTypeChange(type: AssetType) = _form.update { it.copy(type = type) }
    fun onNameChange(value: String) = _form.update { it.copy(name = value) }
    fun onCurrencyChange(value: String) = _form.update { it.copy(currency = value) }
    fun onQuantityChange(value: String) = _form.update { it.copy(quantity = value.filterDecimal()) }
    fun onPricePerUnitChange(value: String) =
        _form.update { it.copy(pricePerUnit = value.filterDecimal()) }

    fun onSymbolChange(value: String) =
        _form.update { it.copy(symbol = value, lastPrice = null, lastPriceTimestamp = null) }

    fun onManualValueChange(value: String) =
        _form.update { it.copy(manualValue = value.filterDecimal()) }

    fun onNotesChange(value: String) = _form.update { it.copy(notes = value) }

    fun fetchPrice() {
        val symbol = _form.value.symbol.trim()
        if (symbol.isEmpty()) {
            _events.value = AssetEditEvent.Error("Enter a ticker symbol first")
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(isFetchingPrice = true) }
            val result = assetRepository.fetchQuote(symbol)
            _form.update { current ->
                result.fold(
                    onSuccess = { quote ->
                        current.copy(
                            isFetchingPrice = false,
                            lastPrice = quote.price,
                            lastPriceTimestamp = System.currentTimeMillis(),
                            currency = quote.currency ?: current.currency,
                        )
                    },
                    onFailure = { current.copy(isFetchingPrice = false) },
                )
            }
            result.exceptionOrNull()?.let {
                _events.value = AssetEditEvent.Error("Couldn't fetch price: ${it.message}")
            }
        }
    }

    fun save() {
        val state = _form.value
        val error = validate(state)
        if (error != null) {
            _events.value = AssetEditEvent.Error(error)
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            assetRepository.save(state.toEntity())
            _events.value = AssetEditEvent.Saved
        }
    }

    fun delete() {
        if (assetId == 0L) return
        viewModelScope.launch {
            assetRepository.delete(assetId)
            _events.value = AssetEditEvent.Deleted
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private fun validate(state: AssetFormState): String? {
        if (state.name.isBlank()) return "Give this asset a name"
        if (state.currency.isBlank()) return "Choose a currency"
        return when (state.type.valuationMode) {
            ValuationMode.MARKET -> when {
                state.symbol.isBlank() -> "Enter a ticker symbol"
                (state.quantity.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Enter a quantity"
                else -> null
            }

            ValuationMode.QUANTITY -> when {
                (state.quantity.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Enter a quantity"
                (state.pricePerUnit.toDoubleOrNull() ?: 0.0) <= 0.0 -> "Enter a price per unit"
                else -> null
            }

            ValuationMode.FLAT ->
                if (state.manualValue.toDoubleOrNull() == null) "Enter a value" else null
        }
    }

    private fun AssetFormState.toEntity(): AssetEntity = AssetEntity(
        id = id,
        portfolioId = portfolioId,
        type = type,
        name = name.trim(),
        currency = currency.trim().uppercase(),
        quantity = quantity.toDoubleOrNull(),
        pricePerUnit = pricePerUnit.toDoubleOrNull(),
        symbol = symbol.trim().uppercase().ifBlank { null },
        lastPrice = lastPrice,
        lastPriceTimestamp = lastPriceTimestamp,
        manualValue = manualValue.toDoubleOrNull(),
        notes = notes.trim().ifBlank { null },
    )

    private fun AssetEntity.toForm(): AssetFormState = AssetFormState(
        id = id,
        portfolioId = portfolioId,
        type = type,
        name = name,
        currency = currency,
        quantity = quantity?.let { formatInput(it) } ?: "",
        pricePerUnit = pricePerUnit?.let { formatInput(it) } ?: "",
        symbol = symbol.orEmpty(),
        manualValue = manualValue?.let { formatInput(it) } ?: "",
        lastPrice = lastPrice,
        lastPriceTimestamp = lastPriceTimestamp,
        notes = notes.orEmpty(),
        loading = false,
    )
}

sealed interface AssetEditEvent {
    data object Saved : AssetEditEvent
    data object Deleted : AssetEditEvent
    data class Error(val message: String) : AssetEditEvent
}

private fun String.filterDecimal(): String {
    val filtered = filterIndexed { index, c ->
        c.isDigit() || (c == '.' && !substring(0, index).contains('.'))
    }
    return filtered
}

private fun formatInput(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()
