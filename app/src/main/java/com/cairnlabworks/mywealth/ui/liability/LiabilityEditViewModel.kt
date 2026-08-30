package com.cairnlabworks.mywealth.ui.liability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cairnlabworks.mywealth.data.local.entity.LiabilityEntity
import com.cairnlabworks.mywealth.data.repository.LiabilityRepository
import com.cairnlabworks.mywealth.data.repository.SettingsRepository
import com.cairnlabworks.mywealth.domain.model.LiabilityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiabilityFormState(
    val id: Long = 0,
    val portfolioId: Long = 0,
    val type: LiabilityType = LiabilityType.LOAN,
    val name: String = "",
    val currency: String = "USD",
    val amount: String = "",
    val notes: String = "",
    val loading: Boolean = true,
    val isSaving: Boolean = false,
) {
    val isEditing: Boolean get() = id != 0L
}

class LiabilityEditViewModel(
    private val liabilityRepository: LiabilityRepository,
    private val settingsRepository: SettingsRepository,
    private val portfolioId: Long,
    private val liabilityId: Long,
) : ViewModel() {

    private val _form = MutableStateFlow(LiabilityFormState(portfolioId = portfolioId))
    val form: StateFlow<LiabilityFormState> = _form.asStateFlow()

    private val _events = MutableStateFlow<LiabilityEditEvent?>(null)
    val events: StateFlow<LiabilityEditEvent?> = _events.asStateFlow()

    init {
        viewModelScope.launch {
            if (liabilityId != 0L) {
                liabilityRepository.getById(liabilityId)?.let {
                    _form.value = it.toForm()
                    return@launch
                }
            }
            val base = settingsRepository.settings.first().baseCurrency
            _form.update { it.copy(currency = base, loading = false) }
        }
    }

    fun onTypeChange(type: LiabilityType) = _form.update { it.copy(type = type) }
    fun onNameChange(value: String) = _form.update { it.copy(name = value) }
    fun onCurrencyChange(value: String) = _form.update { it.copy(currency = value) }
    fun onAmountChange(value: String) = _form.update { it.copy(amount = value.filterDecimal()) }
    fun onNotesChange(value: String) = _form.update { it.copy(notes = value) }

    fun save() {
        val state = _form.value
        val error = when {
            state.name.isBlank() -> "Give this liability a name"
            state.currency.isBlank() -> "Choose a currency"
            state.amount.toDoubleOrNull() == null -> "Enter an amount"
            else -> null
        }
        if (error != null) {
            _events.value = LiabilityEditEvent.Error(error)
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true) }
            liabilityRepository.save(state.toEntity())
            _events.value = LiabilityEditEvent.Saved
        }
    }

    fun delete() {
        if (liabilityId == 0L) return
        viewModelScope.launch {
            liabilityRepository.delete(liabilityId)
            _events.value = LiabilityEditEvent.Deleted
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private fun LiabilityFormState.toEntity() = LiabilityEntity(
        id = id,
        portfolioId = portfolioId,
        type = type,
        name = name.trim(),
        currency = currency.trim().uppercase(),
        amount = amount.toDoubleOrNull() ?: 0.0,
        notes = notes.trim().ifBlank { null },
    )

    private fun LiabilityEntity.toForm() = LiabilityFormState(
        id = id,
        portfolioId = portfolioId,
        type = type,
        name = name,
        currency = currency,
        amount = if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString(),
        notes = notes.orEmpty(),
        loading = false,
    )
}

sealed interface LiabilityEditEvent {
    data object Saved : LiabilityEditEvent
    data object Deleted : LiabilityEditEvent
    data class Error(val message: String) : LiabilityEditEvent
}

private fun String.filterDecimal(): String = filterIndexed { index, c ->
    c.isDigit() || (c == '.' && !substring(0, index).contains('.'))
}
