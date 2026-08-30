package com.cairnlabworks.mywealth.ui.portfolio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.util.CurrencyUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(onClose: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: PortfolioListViewModel =
        viewModel(factory = ViewModelFactories.portfolioList(container))
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var createOpen by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PortfolioEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<PortfolioEntity?>(null) }

    if (message != null) {
        val text = message!!
        androidx.compose.runtime.LaunchedEffect(text) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Portfolios") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { createOpen = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(rows, key = { it.portfolio.id }) { row ->
                PortfolioCard(
                    row = row,
                    onSetDefault = { viewModel.setDefault(row.portfolio.id) },
                    onRename = { renameTarget = row.portfolio },
                    onDelete = { deleteTarget = row.portfolio },
                )
            }
        }
    }

    if (createOpen) {
        NameDialog(
            title = "New portfolio",
            initial = "",
            confirmLabel = "Create",
            onConfirm = {
                viewModel.create(it)
                createOpen = false
            },
            onDismiss = { createOpen = false },
        )
    }

    renameTarget?.let { target ->
        NameDialog(
            title = "Rename portfolio",
            initial = target.name,
            confirmLabel = "Save",
            onConfirm = {
                viewModel.rename(target.id, it)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete portfolio?") },
            text = {
                Text("\"${target.name}\" and all of its assets and liabilities will be permanently removed.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(target.id)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PortfolioCard(
    row: PortfolioRow,
    onSetDefault: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.portfolio.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (row.portfolio.isDefault) {
                    AssistChip(onClick = {}, enabled = false, label = { Text("Default") })
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (!row.portfolio.isDefault) {
                            DropdownMenuItem(
                                text = { Text("Set as default") },
                                onClick = { menuOpen = false; onSetDefault() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = { menuOpen = false; onRename() },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { menuOpen = false; onDelete() },
                        )
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = CurrencyUtil.format(row.netWorth, row.baseCurrency),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "${row.assetCount} assets · ${row.liabilityCount} liabilities",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Name") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }, enabled = text.isNotBlank()) {
                Text(confirmLabel)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
