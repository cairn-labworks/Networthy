package com.cairnlabworks.mywealth.ui.settings

import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cairnlabworks.mywealth.BuildConfig
import com.cairnlabworks.mywealth.di.ViewModelFactories
import com.cairnlabworks.mywealth.di.rememberAppContainer
import com.cairnlabworks.mywealth.domain.model.ThemeMode
import com.cairnlabworks.mywealth.ui.components.CurrencyPickerDialog
import com.cairnlabworks.mywealth.util.BiometricAuthenticator
import com.cairnlabworks.mywealth.util.BiometricCapability
import com.cairnlabworks.mywealth.util.CurrencyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onClose: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: SettingsViewModel = viewModel(factory = ViewModelFactories.settings(container))
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val portfolios by viewModel.portfolios.collectAsStateWithLifecycle()
    val fxUpdatedAt by viewModel.fxUpdatedAt.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingExport by remember { mutableStateOf<Pair<List<Long>, String>?>(null) }

    if (message != null) {
        val text = message!!
        LaunchedEffect(text) {
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
    ) { uri ->
        val pending = pendingExport
        pendingExport = null
        if (uri != null && pending != null) {
            scope.launch {
                runCatching {
                    val bytes = viewModel.buildExport(pending.first, pending.second.toCharArray())
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    }
                }.onSuccess {
                    viewModel.postMessage("Exported ${pending.first.size} portfolio(s)")
                }.onFailure {
                    viewModel.postMessage("Export failed: ${it.message}")
                }
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> importUri = uri }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle("Appearance")
            SettingRow(
                title = "Theme",
                subtitle = settings.themeMode.displayName,
                onClick = { showThemeDialog = true },
            )
            SwitchRow(
                title = "Dynamic color",
                subtitle = "Use colors from your wallpaper (Android 12+)",
                checked = settings.dynamicColor,
                onCheckedChange = { viewModel.setDynamicColor(it) },
            )

            HorizontalDivider()
            SettingsSectionTitle("Currency")
            SettingRow(
                title = "Base currency",
                subtitle = "${settings.baseCurrency} — ${CurrencyUtil.displayName(settings.baseCurrency)}",
                onClick = { showCurrencyPicker = true },
            )

            HorizontalDivider()
            SettingsSectionTitle("Security")
            SwitchRow(
                title = "App lock",
                subtitle = "Require fingerprint or device PIN to open MyWealth",
                checked = settings.appLockEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        viewModel.setAppLockEnabled(false)
                    } else {
                        when (BiometricAuthenticator.capability(context)) {
                            BiometricCapability.AVAILABLE -> viewModel.setAppLockEnabled(true)
                            BiometricCapability.NOT_ENROLLED ->
                                viewModel.postMessage("Set up a screen lock or fingerprint in system settings first")
                            BiometricCapability.UNAVAILABLE ->
                                viewModel.postMessage("This device can't authenticate you")
                        }
                    }
                },
            )

            HorizontalDivider()
            SettingsSectionTitle("Data")
            SettingRow(
                title = "Exchange rates",
                subtitle = fxUpdatedAt?.let {
                    "Updated ${DateUtils.getRelativeTimeSpanString(it)}"
                } ?: "Not fetched yet",
                trailing = {
                    IconButton(onClick = { viewModel.refreshRates() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh rates")
                    }
                },
            )
            SettingRow(
                title = "Export portfolios",
                subtitle = "Save an encrypted, password-protected backup",
                onClick = { if (portfolios.isNotEmpty()) showExportDialog = true },
            )
            SettingRow(
                title = "Import portfolios",
                subtitle = "Restore from an encrypted backup file",
                onClick = { openDocLauncher.launch(arrayOf("*/*")) },
            )

            HorizontalDivider()
            SettingsSectionTitle("About")
            SettingRow(
                title = "MyWealth",
                subtitle = "Version ${BuildConfig.VERSION_NAME} · Open source (MIT)",
                onClick = null,
            )
            Text(
                text = "Your data is stored encrypted on this device and never leaves it, " +
                    "except when you export a backup. Stock and exchange-rate lookups send " +
                    "only the ticker or currency code.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showThemeDialog) {
        ThemeDialog(
            selected = settings.themeMode,
            onSelect = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            selected = settings.baseCurrency,
            onSelected = { viewModel.setBaseCurrency(it) },
            onDismiss = { showCurrencyPicker = false },
        )
    }

    if (showExportDialog) {
        ExportDialog(
            portfolios = portfolios,
            onConfirm = { ids, password ->
                pendingExport = ids to password
                showExportDialog = false
                createDocLauncher.launch("mywealth-backup.mywealth")
            },
            onDismiss = { showExportDialog = false },
        )
    }

    importUri?.let { uri ->
        ImportPasswordDialog(
            onConfirm = { password ->
                val target = uri
                importUri = null
                scope.launch {
                    runCatching {
                        val bytes = withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(target)?.use { it.readBytes() }
                                ?: ByteArray(0)
                        }
                        viewModel.importBackup(bytes, password.toCharArray())
                    }.onSuccess { result ->
                        viewModel.postMessage(
                            "Imported ${result.portfolios} portfolio(s), " +
                                "${result.assets} assets, ${result.liabilities} liabilities",
                        )
                    }.onFailure {
                        viewModel.postMessage("Import failed: ${it.message}")
                    }
                }
            },
            onDismiss = { importUri = null },
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (trailing != null) {
            Spacer(Modifier.height(0.dp))
            trailing()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ThemeDialog(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Theme") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(selected = mode == selected, onClick = { onSelect(mode) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == selected, onClick = { onSelect(mode) })
                        Spacer(Modifier.height(0.dp))
                        Text(mode.displayName, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
    )
}

@Composable
private fun ExportDialog(
    portfolios: List<com.cairnlabworks.mywealth.data.local.entity.PortfolioEntity>,
    onConfirm: (ids: List<Long>, password: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val selected = remember {
        androidx.compose.runtime.mutableStateMapOf<Long, Boolean>().apply {
            portfolios.forEach { put(it.id, true) }
        }
    }
    var password by remember { mutableStateOf("") }
    val chosen = portfolios.filter { selected[it.id] == true }.map { it.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export portfolios") },
        text = {
            Column {
                Text(
                    "Choose portfolios and set a password. You'll need this password to import the file again.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                portfolios.forEach { portfolio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = selected[portfolio.id] == true,
                                onValueChange = { selected[portfolio.id] = it },
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = selected[portfolio.id] == true, onCheckedChange = null)
                        Text(portfolio.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(chosen, password) },
                enabled = chosen.isNotEmpty() && password.length >= 4,
            ) { Text("Export") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ImportPasswordDialog(
    onConfirm: (password: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import backup") },
        text = {
            Column {
                Text(
                    "Enter the password used when this backup was exported.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password) }, enabled = password.isNotEmpty()) {
                Text("Import")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
