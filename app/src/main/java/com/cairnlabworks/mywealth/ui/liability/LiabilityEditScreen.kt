package com.cairnlabworks.mywealth.ui.liability

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.domain.model.LiabilityType
import com.cairnlabworks.mywealth.ui.components.CurrencyPickerDialog
import com.cairnlabworks.mywealth.ui.components.DropdownSelector
import com.cairnlabworks.mywealth.ui.components.LabeledTextField
import com.cairnlabworks.mywealth.util.CurrencyUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiabilityEditScreen(
    portfolioId: Long,
    liabilityId: Long,
    onClose: () -> Unit,
) {
    val container = rememberAppContainer()
    val viewModel: LiabilityEditViewModel =
        viewModel(factory = ViewModelFactories.liabilityEdit(container, portfolioId, liabilityId))
    val form by viewModel.form.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCurrencyPicker by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        when (val e = event) {
            is LiabilityEditEvent.Saved, is LiabilityEditEvent.Deleted -> {
                viewModel.consumeEvent()
                onClose()
            }

            is LiabilityEditEvent.Error -> {
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
                title = { Text(if (form.isEditing) "Edit liability" else "New liability") },
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
                options = LiabilityType.entries,
                optionLabel = { it.displayName },
                onSelected = viewModel::onTypeChange,
            )
            LabeledTextField(
                value = form.name,
                onValueChange = viewModel::onNameChange,
                label = "Name",
                placeholder = "e.g. Home loan",
            )
            Box {
                OutlinedTextField(
                    value = "${form.currency} — ${CurrencyUtil.displayName(form.currency)}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Currency") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showCurrencyPicker = true },
                )
            }
            LabeledTextField(
                value = form.amount,
                onValueChange = viewModel::onAmountChange,
                label = "Outstanding amount",
                prefix = CurrencyUtil.symbol(form.currency),
                keyboardType = KeyboardType.Decimal,
            )
            LabeledTextField(
                value = form.notes,
                onValueChange = viewModel::onNotesChange,
                label = "Notes (optional)",
                singleLine = false,
            )
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
