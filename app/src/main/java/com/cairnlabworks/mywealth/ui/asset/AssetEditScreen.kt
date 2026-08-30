package com.cairnlabworks.mywealth.ui.asset

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.domain.model.AssetType
import com.cairnlabworks.mywealth.domain.model.ValuationMode
import com.cairnlabworks.mywealth.ui.components.CurrencyPickerDialog
import com.cairnlabworks.mywealth.ui.components.DropdownSelector
import com.cairnlabworks.mywealth.ui.components.LabeledTextField
import com.cairnlabworks.mywealth.util.CurrencyUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetEditScreen(
    portfolioId: Long,
    assetId: Long,
    onClose: () -> Unit,
) {
    val container = rememberAppContainer()
    val viewModel: AssetEditViewModel =
        viewModel(factory = ViewModelFactories.assetEdit(container, portfolioId, assetId))
    val form by viewModel.form.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is AssetEditEvent.Saved, is AssetEditEvent.Deleted -> {
                viewModel.consumeEvent()
                onClose()
            }

            is AssetEditEvent.Error -> {
                snackbarHostState.showSnackbar(e.message)
                viewModel.consumeEvent()
            }

            null -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (form.isEditing) "Edit asset" else "New asset") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (form.isEditing) {
                        IconButton(onClick = { viewModel.delete() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    TextButton(onClick = { viewModel.save() }, enabled = !form.isSaving) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        if (form.loading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DropdownSelector(
                label = "Type",
                selected = form.type,
                options = AssetType.entries,
                optionLabel = { it.displayName },
                onSelected = viewModel::onTypeChange,
            )

            LabeledTextField(
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = "Name",
                placeholder = "e.g. Apple shares",
            )

            CurrencyField(
                currency = form.currency,
                onClick = { showCurrencyPicker = true },
            )

            when (form.valuationMode) {
                ValuationMode.MARKET -> MarketFields(form, viewModel)
                ValuationMode.QUANTITY -> QuantityFields(form, viewModel)
                ValuationMode.FLAT -> FlatField(form, viewModel)
            }

            LabeledTextField(
                value = form.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes (optional)",
                singleLine = false,
            )

            EstimatedValueCard(
                amount = CurrencyUtil.format(form.estimatedValue, form.currency),
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selected = form.currency,
            onSelected = viewModel::onCurrencyChange,
            onDismiss = { showCurrencyPicker = false },
        )
    }
}

@Composable
private fun MarketFields(form: AssetFormState, viewModel: AssetEditViewModel) {
    LabeledTextField(
        value = form.symbol,
        onValueChange = viewModel::onSymbolChange,
        label = "Ticker symbol",
        placeholder = "e.g. AAPL, BTC-USD",
    )
    Spacer(Modifier.height(16.dp))
    LabeledTextField(
        value = form.quantity,
        onValueChange = viewModel::onQuantityChange,
        label = "Quantity",
        keyboardType = KeyboardType.Decimal,
    )
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { viewModel.fetchPrice() },
            enabled = !form.isFetchingPrice,
        ) {
            if (form.isFetchingPrice) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp).padding(end = 8.dp), strokeWidth = 2.dp)
            }
            Text("Fetch latest price")
        }
    }
    val price = form.lastPrice
    if (price != null) {
        Text(
            text = "Last price: ${CurrencyUtil.format(price, form.currency)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    } else {
        Text(
            text = "Fetch the latest closing price, or it will show as 0 until refreshed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun QuantityFields(form: AssetFormState, viewModel: AssetEditViewModel) {
    LabeledTextField(
        value = form.quantity,
        onValueChange = viewModel::onQuantityChange,
        label = "Quantity",
        suffix = form.type.unitLabel,
        keyboardType = KeyboardType.Decimal,
    )
    Spacer(Modifier.height(16.dp))
    LabeledTextField(
        value = form.pricePerUnit,
        onValueChange = viewModel::onPricePerUnitChange,
        label = "Price per ${form.type.unitLabel ?: "unit"}",
        prefix = CurrencyUtil.symbol(form.currency),
        keyboardType = KeyboardType.Decimal,
    )
}

@Composable
private fun FlatField(form: AssetFormState, viewModel: AssetEditViewModel) {
    LabeledTextField(
        value = form.manualValue,
        onValueChange = viewModel::onManualValueChange,
        label = "Value",
        prefix = CurrencyUtil.symbol(form.currency),
        keyboardType = KeyboardType.Decimal,
    )
}

@Composable
private fun CurrencyField(currency: String, onClick: () -> Unit) {
    Box {
        OutlinedTextField(
            value = "$currency — ${CurrencyUtil.displayName(currency)}",
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick),
        )
    }
}

@Composable
private fun EstimatedValueCard(amount: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Estimated value", style = MaterialTheme.typography.titleSmall)
            Text(amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
