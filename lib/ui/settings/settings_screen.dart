import 'dart:typed_data';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';

import '../../data/backup/backup_models.dart';
import '../../data/local/entity/portfolio_entity.dart';
import '../../di/app_container.dart';
import '../../domain/model/app_theme.dart';
import '../../domain/model/theme_mode.dart';
import '../../util/biometric_authenticator.dart';
import '../../util/currency_util.dart';
import '../../util/relative_time.dart';
import '../components/form_components.dart';
import 'settings_view_model.dart';

/// Appearance, currency, security, backup and about.
class SettingsScreen extends StatefulWidget {
  const SettingsScreen({required this.onClose, super.key});

  final VoidCallback onClose;

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  SettingsViewModel? _viewModel;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_viewModel != null) return;
    final AppContainer container = AppScope.of(context);
    _viewModel = SettingsViewModel(
      settingsRepository: container.settingsRepository,
      portfolioRepository: container.portfolioRepository,
      fxRepository: container.fxRepository,
      backupRepository: container.backupRepository,
    )..addListener(_onChanged);
  }

  void _onChanged() {
    final String? message = _viewModel?.message;
    if (message != null && mounted) {
      _viewModel!.consumeMessage();
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(message)));
    }
  }

  @override
  void dispose() {
    _viewModel?.removeListener(_onChanged);
    _viewModel?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final SettingsViewModel viewModel = _viewModel!;
    final ThemeData theme = Theme.of(context);
    return ListenableBuilder(
      listenable: viewModel,
      builder: (BuildContext context, _) {
        final settings = viewModel.settings;
        final int? fxUpdatedAt = viewModel.fxUpdatedAt;
        return Scaffold(
          appBar: AppBar(
            title: const Text('Settings'),
            leading: IconButton(
              tooltip: 'Back',
              onPressed: widget.onClose,
              icon: const Icon(Icons.arrow_back),
            ),
          ),
          body: ListView(
            children: <Widget>[
              const _SectionTitle('Appearance'),
              _SettingRow(
                title: 'App theme',
                subtitle:
                    '${settings.appTheme.displayName} — '
                    '${settings.appTheme.description}',
                onTap: () => _showAppThemeDialog(settings.appTheme),
              ),
              _SettingRow(
                title: 'Light / dark',
                subtitle: settings.appTheme == AppTheme.midnight
                    ? 'Always dark on Midnight'
                    : settings.themeMode.displayName,
                onTap: settings.appTheme == AppTheme.midnight
                    ? null
                    : () => _showThemeDialog(settings.themeMode),
              ),
              _SwitchRow(
                title: 'Dynamic color',
                subtitle: settings.appTheme == AppTheme.midnight
                    ? 'Not used on Midnight'
                    : 'Use colors from your wallpaper (Android 12+)',
                value:
                    settings.dynamicColor &&
                    settings.appTheme == AppTheme.classic,
                onChanged: settings.appTheme == AppTheme.midnight
                    ? null
                    : viewModel.setDynamicColor,
              ),
              const Divider(height: 1),
              const _SectionTitle('Currency'),
              _SettingRow(
                title: 'Base currency',
                subtitle:
                    '${settings.baseCurrency} — '
                    '${CurrencyUtil.displayName(settings.baseCurrency)}',
                onTap: () => _showCurrencyPicker(settings.baseCurrency),
              ),
              const Divider(height: 1),
              const _SectionTitle('Security'),
              _SwitchRow(
                title: 'App lock',
                subtitle: 'Require fingerprint or device PIN to open Networthy',
                value: settings.appLockEnabled,
                onChanged: _onAppLockChanged,
              ),
              const Divider(height: 1),
              const _SectionTitle('Data'),
              _SettingRow(
                title: 'Exchange rates',
                subtitle: fxUpdatedAt == null
                    ? 'Not fetched yet'
                    : 'Updated ${relativeTimeSpan(fxUpdatedAt)}',
                trailing: IconButton(
                  tooltip: 'Refresh rates',
                  onPressed: viewModel.refreshRates,
                  icon: const Icon(Icons.refresh),
                ),
              ),
              _SettingRow(
                title: 'Export portfolios',
                subtitle: 'Save an encrypted, password-protected backup',
                onTap: () {
                  if (viewModel.portfolios.isNotEmpty) {
                    _showExportDialog(viewModel.portfolios);
                  }
                },
              ),
              _SettingRow(
                title: 'Import portfolios',
                subtitle: 'Restore from an encrypted backup file',
                onTap: _startImport,
              ),
              const Divider(height: 1),
              const _SectionTitle('About'),
              const _SettingRow(
                title: 'Networthy',
                subtitle: 'Version 1.2.0 · Open source (MIT)',
              ),
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 20,
                  vertical: 12,
                ),
                child: Text(
                  'Your data is stored encrypted on this device and never '
                  'leaves it, except when you export a backup. Stock and '
                  'exchange-rate lookups send only the ticker or currency code.',
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        );
      },
    );
  }

  Future<void> _onAppLockChanged(bool enabled) async {
    final SettingsViewModel viewModel = _viewModel!;
    if (!enabled) {
      viewModel.setAppLockEnabled(false);
      return;
    }
    switch (await BiometricAuthenticator.capability()) {
      case BiometricCapability.available:
        viewModel.setAppLockEnabled(true);
      case BiometricCapability.notEnrolled:
        viewModel.postMessage(
          'Set up a screen lock or fingerprint in system settings first',
        );
      case BiometricCapability.unavailable:
        viewModel.postMessage("This device can't authenticate you");
    }
  }

  Future<void> _showThemeDialog(AppThemeMode selected) async {
    final AppThemeMode? mode = await showDialog<AppThemeMode>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('Theme'),
        content: RadioGroup<AppThemeMode>(
          groupValue: selected,
          onChanged: (AppThemeMode? value) => Navigator.of(context).pop(value),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              for (final AppThemeMode mode in AppThemeMode.values)
                RadioListTile<AppThemeMode>(
                  value: mode,
                  title: Text(mode.displayName),
                ),
            ],
          ),
        ),
        actions: <Widget>[
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
    if (mode != null) _viewModel?.setThemeMode(mode);
  }

  Future<void> _showAppThemeDialog(AppTheme selected) async {
    final AppTheme? theme = await showDialog<AppTheme>(
      context: context,
      builder: (BuildContext context) => AlertDialog(
        title: const Text('App theme'),
        content: RadioGroup<AppTheme>(
          groupValue: selected,
          onChanged: (AppTheme? value) => Navigator.of(context).pop(value),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              for (final AppTheme option in AppTheme.values)
                RadioListTile<AppTheme>(
                  value: option,
                  title: Text(option.displayName),
                  subtitle: Text(option.description),
                ),
            ],
          ),
        ),
        actions: <Widget>[
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
    if (theme != null) _viewModel?.setAppTheme(theme);
  }

  Future<void> _showCurrencyPicker(String selected) async {
    final String? code = await showCurrencyPickerDialog(
      context,
      selected: selected,
    );
    if (code != null) _viewModel?.setBaseCurrency(code);
  }

  Future<void> _showExportDialog(List<PortfolioEntity> portfolios) async {
    final _ExportRequest? request = await showDialog<_ExportRequest>(
      context: context,
      builder: (BuildContext context) => _ExportDialog(portfolios: portfolios),
    );
    if (request == null) return;
    final SettingsViewModel viewModel = _viewModel!;
    try {
      final Uint8List bytes = await viewModel.buildExport(
        request.portfolioIds,
        request.password,
      );
      final Uri? saved = await FilePicker.saveFile(
        dialogTitle: 'Export portfolios',
        fileName: 'networthy-backup.networthy',
        bytes: bytes,
      );
      if (saved == null) return;
      viewModel.postMessage(
        'Exported ${request.portfolioIds.length} portfolio(s)',
      );
    } catch (error) {
      viewModel.postMessage('Export failed: $error');
    }
  }

  Future<void> _startImport() async {
    final PlatformFile? file = await FilePicker.pickFile(
      dialogTitle: 'Import backup',
    );
    if (file == null || !mounted) return;
    final String? password = await showDialog<String>(
      context: context,
      builder: (BuildContext context) => const _ImportPasswordDialog(),
    );
    if (password == null) return;
    final SettingsViewModel viewModel = _viewModel!;
    try {
      final Uint8List bytes = await file.readAsBytes();
      final ImportResult result = await viewModel.importBackup(bytes, password);
      viewModel.postMessage(
        'Imported ${result.portfolios} portfolio(s), '
        '${result.assets} assets, ${result.liabilities} liabilities',
      );
    } catch (error) {
      viewModel.postMessage('Import failed: $error');
    }
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.title);

  final String title;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(left: 20, right: 20, top: 16, bottom: 4),
      child: Text(
        title,
        style: theme.textTheme.titleSmall?.copyWith(
          color: theme.colorScheme.primary,
        ),
      ),
    );
  }
}

class _SettingRow extends StatelessWidget {
  const _SettingRow({
    required this.title,
    required this.subtitle,
    this.onTap,
    this.trailing,
  });

  final String title;
  final String subtitle;
  final VoidCallback? onTap;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final Widget row = Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
      child: Row(
        children: <Widget>[
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                Text(title, style: theme.textTheme.bodyLarge),
                Text(
                  subtitle,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          ?trailing,
        ],
      ),
    );
    return onTap == null ? row : InkWell(onTap: onTap, child: row);
  }
}

class _SwitchRow extends StatelessWidget {
  const _SwitchRow({
    required this.title,
    required this.subtitle,
    required this.value,
    required this.onChanged,
  });

  final String title;
  final String subtitle;
  final bool value;
  final ValueChanged<bool>? onChanged;

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final ValueChanged<bool>? onChanged = this.onChanged;
    return InkWell(
      onTap: onChanged == null ? null : () => onChanged(!value),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
        child: Row(
          children: <Widget>[
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(title, style: theme.textTheme.bodyLarge),
                  Text(
                    subtitle,
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
            Switch(value: value, onChanged: onChanged),
          ],
        ),
      ),
    );
  }
}

class _ExportRequest {
  const _ExportRequest(this.portfolioIds, this.password);

  final List<int> portfolioIds;
  final String password;
}

class _ExportDialog extends StatefulWidget {
  const _ExportDialog({required this.portfolios});

  final List<PortfolioEntity> portfolios;

  @override
  State<_ExportDialog> createState() => _ExportDialogState();
}

class _ExportDialogState extends State<_ExportDialog> {
  late final Map<int, bool> _selected = <int, bool>{
    for (final PortfolioEntity portfolio in widget.portfolios)
      portfolio.id: true,
  };
  final TextEditingController _password = TextEditingController();
  final TextEditingController _confirm = TextEditingController();
  bool _visible = false;

  @override
  void dispose() {
    _password.dispose();
    _confirm.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    final List<int> chosen = <int>[
      for (final PortfolioEntity portfolio in widget.portfolios)
        if (_selected[portfolio.id] == true) portfolio.id,
    ];
    final String password = _password.text;
    final String confirm = _confirm.text;
    final bool mismatch = confirm.isNotEmpty && password != confirm;
    final bool canExport =
        chosen.isNotEmpty && password.length >= 4 && password == confirm;

    return AlertDialog(
      title: const Text('Export portfolios'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              "Choose portfolios and set a password. You'll need this exact "
              'password to import the file again.',
              style: theme.textTheme.bodyMedium,
            ),
            const SizedBox(height: 12),
            for (final PortfolioEntity portfolio in widget.portfolios)
              CheckboxListTile(
                contentPadding: EdgeInsets.zero,
                controlAffinity: ListTileControlAffinity.leading,
                dense: true,
                value: _selected[portfolio.id] ?? false,
                onChanged: (bool? value) =>
                    setState(() => _selected[portfolio.id] = value ?? false),
                title: Text(portfolio.name),
              ),
            const SizedBox(height: 8),
            _PasswordField(
              controller: _password,
              label: 'Password',
              visible: _visible,
              onToggleVisibility: () => setState(() => _visible = !_visible),
              onChanged: (_) => setState(() {}),
              isError: password.isNotEmpty && password.length < 4,
              supportingText: password.isNotEmpty && password.length < 4
                  ? 'Use at least 4 characters'
                  : null,
            ),
            const SizedBox(height: 8),
            _PasswordField(
              controller: _confirm,
              label: 'Confirm password',
              visible: _visible,
              onToggleVisibility: () => setState(() => _visible = !_visible),
              onChanged: (_) => setState(() {}),
              isError: mismatch,
              supportingText: mismatch ? "Passwords don't match" : null,
            ),
          ],
        ),
      ),
      actions: <Widget>[
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        TextButton(
          onPressed: canExport
              ? () =>
                    Navigator.of(context).pop(_ExportRequest(chosen, password))
              : null,
          child: const Text('Export'),
        ),
      ],
    );
  }
}

class _ImportPasswordDialog extends StatefulWidget {
  const _ImportPasswordDialog();

  @override
  State<_ImportPasswordDialog> createState() => _ImportPasswordDialogState();
}

class _ImportPasswordDialogState extends State<_ImportPasswordDialog> {
  final TextEditingController _password = TextEditingController();
  bool _visible = false;

  @override
  void dispose() {
    _password.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final ThemeData theme = Theme.of(context);
    return AlertDialog(
      title: const Text('Import backup'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(
            'Enter the password used when this backup was exported.',
            style: theme.textTheme.bodyMedium,
          ),
          const SizedBox(height: 12),
          _PasswordField(
            controller: _password,
            label: 'Password',
            visible: _visible,
            onToggleVisibility: () => setState(() => _visible = !_visible),
            onChanged: (_) => setState(() {}),
          ),
        ],
      ),
      actions: <Widget>[
        TextButton(
          onPressed: () => Navigator.of(context).pop(),
          child: const Text('Cancel'),
        ),
        TextButton(
          onPressed: _password.text.isEmpty
              ? null
              : () => Navigator.of(context).pop(_password.text),
          child: const Text('Import'),
        ),
      ],
    );
  }
}

/// A password entry field with a show/hide toggle. Uses the password keyboard
/// so the IME does not auto-capitalize or auto-correct the input, which would
/// otherwise silently change the password between export and import.
class _PasswordField extends StatelessWidget {
  const _PasswordField({
    required this.controller,
    required this.label,
    required this.visible,
    required this.onToggleVisibility,
    this.onChanged,
    this.isError = false,
    this.supportingText,
  });

  final TextEditingController controller;
  final String label;
  final bool visible;
  final VoidCallback onToggleVisibility;
  final ValueChanged<String>? onChanged;
  final bool isError;
  final String? supportingText;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      onChanged: onChanged,
      obscureText: !visible,
      autocorrect: false,
      enableSuggestions: false,
      textCapitalization: TextCapitalization.none,
      keyboardType: visible ? TextInputType.visiblePassword : null,
      decoration: InputDecoration(
        border: const OutlineInputBorder(),
        labelText: label,
        helperText: isError ? null : supportingText,
        errorText: isError ? supportingText : null,
        suffixIcon: IconButton(
          tooltip: visible ? 'Hide password' : 'Show password',
          onPressed: onToggleVisibility,
          icon: Icon(visible ? Icons.visibility_off : Icons.visibility),
        ),
      ),
    );
  }
}
